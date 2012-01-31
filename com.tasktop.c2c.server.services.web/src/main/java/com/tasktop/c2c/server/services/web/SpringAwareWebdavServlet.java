/*******************************************************************************
 * Copyright (c) 2010, 2012 Tasktop Technologies
 * Copyright (c) 2010, 2011 SpringSource, a division of VMware
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 ******************************************************************************/
package com.tasktop.c2c.server.services.web;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.webdav.IMethodExecutor;
import net.sf.webdav.IMimeTyper;
import net.sf.webdav.ITransaction;
import net.sf.webdav.IWebdavStore;
import net.sf.webdav.WebdavStatus;
import net.sf.webdav.exceptions.UnauthenticatedException;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.methods.DoCopy;
import net.sf.webdav.methods.DoDelete;
import net.sf.webdav.methods.DoGet;
import net.sf.webdav.methods.DoHead;
import net.sf.webdav.methods.DoLock;
import net.sf.webdav.methods.DoMkcol;
import net.sf.webdav.methods.DoMove;
import net.sf.webdav.methods.DoNotImplemented;
import net.sf.webdav.methods.DoOptions;
import net.sf.webdav.methods.DoPropfind;
import net.sf.webdav.methods.DoProppatch;
import net.sf.webdav.methods.DoPut;
import net.sf.webdav.methods.DoUnlock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.web.context.ServletContextAware;


import com.tasktop.c2c.server.auth.service.AuthenticationServiceUser;
import com.tasktop.c2c.server.auth.service.AuthenticationToken;
import com.tasktop.c2c.server.common.service.domain.Role;

// This class is based upon net.sf.webdav.WebDavServletBean and net.sf.webdav.WebdavServlet, however they
// were written in such a way that they did not work correctly with Spring, and also suppressed security
// exceptions. As a result, it was necessary to rewrite them in this manner to correspond with the system's
// architecture. At the same time, code paths which were irrelevant to Code2Cloud were deleted (since they
// are unused, having them around simply adds clutter to no good purpose).
@SuppressWarnings("serial")
public class SpringAwareWebdavServlet extends HttpServlet implements ServletContextAware {

	private static final Log LOG = LogFactory.getLog(SpringAwareWebdavServlet.class);

	private IWebdavStore webdavStore;

	// We need to reimplement parts of WebDavServletBean since it is currently written to suppress all exceptions - this
	// prevents us from correctly handling Security exceptions since they never escape the internal service() method.
	private static final boolean READ_ONLY = false;
	private ResourceLocks resLocks = new ResourceLocks();
	private HashMap<String, IMethodExecutor> methodMap = new HashMap<String, IMethodExecutor>();
	private boolean lazyFolderCreationOnPut = false;
	private String defaultIndexFile = null;
	private String insteadOf404 = null;
	private int noContentLengthHeaders = -1;
	private ServletContext servletContext = null;

	@Override
	@Required
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public void init() throws ServletException {
		// Call our internal initialization method now.
		init(defaultIndexFile, insteadOf404, noContentLengthHeaders, lazyFolderCreationOnPut);
	}

	private void init(String dftIndexFile, String insteadOf404, int nocontentLenghHeaders,
			boolean lazyFolderCreationOnPut) throws ServletException {

		IMimeTyper mimeTyper = new IMimeTyper() {
			@Override
			public String getMimeType(String path) {
				return servletContext.getMimeType(path);
			}
		};

		register("GET", new DoGet(webdavStore, dftIndexFile, insteadOf404, resLocks, mimeTyper, nocontentLenghHeaders));
		register("HEAD",
				new DoHead(webdavStore, dftIndexFile, insteadOf404, resLocks, mimeTyper, nocontentLenghHeaders));
		DoDelete doDelete = (DoDelete) register("DELETE", new DoDelete(webdavStore, resLocks, READ_ONLY));
		DoCopy doCopy = (DoCopy) register("COPY", new DoCopy(webdavStore, resLocks, doDelete, READ_ONLY));
		register("LOCK", new DoLock(webdavStore, resLocks, READ_ONLY));
		register("UNLOCK", new DoUnlock(webdavStore, resLocks, READ_ONLY));
		register("MOVE", new DoMove(resLocks, doDelete, doCopy, READ_ONLY));
		register("MKCOL", new DoMkcol(webdavStore, resLocks, READ_ONLY));
		register("OPTIONS", new DoOptions(webdavStore, resLocks));
		register("PUT", new DoPut(webdavStore, resLocks, READ_ONLY, lazyFolderCreationOnPut));
		register("PROPFIND", new DoPropfind(webdavStore, resLocks, mimeTyper));
		register("PROPPATCH", new DoProppatch(webdavStore, resLocks, READ_ONLY));
		register("*NO*IMPL*", new DoNotImplemented(READ_ONLY));
	}

	private IMethodExecutor register(String methodName, IMethodExecutor method) {
		methodMap.put(methodName, method);
		return method;
	}

	// This method is overridden to provide nice return-code handling in the event of an "Access Denied" error.
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		ITransaction transaction = null;
		boolean needRollback = false;

		try {
			Principal userPrincipal = req.getUserPrincipal();
			transaction = webdavStore.begin(userPrincipal);
			needRollback = true;
			webdavStore.checkAuthentication(transaction);

			IMethodExecutor methodExecutor = methodMap.get(req.getMethod());
			if (methodExecutor == null) {
				methodExecutor = methodMap.get("*NO*IMPL*");
			}

			methodExecutor.execute(transaction, req, resp);

			webdavStore.commit(transaction);
			needRollback = false;

		} catch (UnauthenticatedException e) {
			resp.sendError(WebdavStatus.SC_UNAUTHORIZED);
		} catch (AccessDeniedException ade) {

			// If we got a security exception, determine the correct type of error code to return.
			AuthenticationToken token = AuthenticationServiceUser.getCurrent().getToken();
			List<String> roles = token.getAuthorities();

			// Our request was rejected - time to send back an appropriate error.
			if (roles.contains(Role.Anonymous)) {
				// This was an anonymous request, so prompt the user for credentials - perhaps they can still do this.
				resp.addHeader("WWW-Authenticate", String.format("Basic realm=\"%s\"", TenancyContextHolder
						.getContext().getTenant().getIdentity()));
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please login to continue");
			} else {
				// This user was authenticated, but this request is not allowed for permissions reasons - reject it.
				resp.sendError(HttpServletResponse.SC_FORBIDDEN,
						"Insufficient permissions to perform this WebDav request");
			}
		} catch (Exception e) {
			LOG.error(e);
			resp.sendError(WebdavStatus.SC_INTERNAL_SERVER_ERROR);
			throw new ServletException(e);
		} finally {
			if (needRollback) {
				webdavStore.rollback(transaction);
			}
		}
	}

	@Required
	public void setWebdavStore(IWebdavStore webdavStore) {
		this.webdavStore = webdavStore;
	}

	public boolean isLazyFolderCreationOnPut() {
		return lazyFolderCreationOnPut;
	}

	public void setLazyFolderCreationOnPut(boolean lazyFolderCreationOnPut) {
		this.lazyFolderCreationOnPut = lazyFolderCreationOnPut;
	}

	public String getDefaultIndexFile() {
		return defaultIndexFile;
	}

	public void setDefaultIndexFile(String defaultIndexFile) {
		this.defaultIndexFile = defaultIndexFile;
	}

	public String getInsteadOf404() {
		return insteadOf404;
	}

	public void setInsteadOf404(String insteadOf404) {
		this.insteadOf404 = insteadOf404;
	}

	public int getNoContentLengthHeaders() {
		return noContentLengthHeaders;
	}

	public void setNoContentLengthHeaders(int noContentLengthHeaders) {
		this.noContentLengthHeaders = noContentLengthHeaders;
	}
}
