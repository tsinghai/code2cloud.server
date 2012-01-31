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
package com.tasktop.c2c.server.hudson.plugin.auth;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.security.GroupDetails;
import hudson.security.UserMayOrMayNotExistException;
import hudson.security.SecurityRealm;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;

public class AlmPreauthenticationSecurityRealm extends SecurityRealm implements UserDetailsService,
		AuthenticationManager {

	@DataBoundConstructor
	public AlmPreauthenticationSecurityRealm() {
	}

	@Override
	public SecurityComponents createSecurityComponents() {
		return new SecurityComponents(this, this);
	}

	public Authentication authenticate(Authentication auth) throws AuthenticationException {
		// TODO Auto-generated method stub
		return auth;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		// does not check the underlying user datastore.
		throw new UserMayOrMayNotExistException(username);
	}

	@Override
	public GroupDetails loadGroupByGroupname(final String groupname) throws UsernameNotFoundException,
			DataAccessException {
		return new GroupDetails() {

			@Override
			public String getName() {
				return groupname;
			}
		};
	}

	@Override
	public Filter createFilter(FilterConfig filterConfig) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "classpath:applicationContext-security.xml" }, false) {
			@Override
			protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
				super.initBeanDefinitionReader(reader);
				reader.setNamespaceHandlerResolver(new DefaultNamespaceHandlerResolver(new FilterClassLoader(
						AlmPreauthenticationSecurityRealm.class.getClassLoader())));
			}
		};
		context.setClassLoader(AlmPreauthenticationSecurityRealm.class.getClassLoader());

		context.refresh();
		Filter securityFilter = (Filter) context.getBean("springSecurityFilterChain");
		return securityFilter;
	}

	public static final class DescriptorImpl extends Descriptor<SecurityRealm> {

		@Override
		public String getDisplayName() {
			return "Code2Cloud Authentication";
		}
	}

	@Extension
	public static DescriptorImpl install() {
		return new DescriptorImpl();
	}

}
