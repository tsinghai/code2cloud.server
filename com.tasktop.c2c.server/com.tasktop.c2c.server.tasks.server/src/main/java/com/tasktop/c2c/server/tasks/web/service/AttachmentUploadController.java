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
package com.tasktop.c2c.server.tasks.web.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.tasktop.c2c.server.common.service.ConcurrentUpdateException;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.internal.tasks.service.TaskServiceConfiguration;
import com.tasktop.c2c.server.tasks.domain.Attachment;
import com.tasktop.c2c.server.tasks.domain.AttachmentHandle;
import com.tasktop.c2c.server.tasks.domain.AttachmentUploadUtil;
import com.tasktop.c2c.server.tasks.domain.TaskHandle;
import com.tasktop.c2c.server.tasks.service.TaskService;

@Controller
public class AttachmentUploadController {

	private static final int BUF_SIZE = 8192;

	@Autowired
	private TaskService taskService;

	@Autowired
	private TaskServiceConfiguration configuration;

	@Autowired
	private ObjectMapper jsonMapper;

	public static class UploadResult {
		private TaskHandle taskHandle;
		private List<Attachment> attachments;
		private String errorMessage;

		public UploadResult(String errorMessage) {
			this.errorMessage = errorMessage;
		}

		public UploadResult(TaskHandle taskHandle, List<Attachment> attachments) {
			this.taskHandle = taskHandle;
			this.attachments = attachments;
		}

		public TaskHandle getTaskHandle() {
			return taskHandle;
		}

		public void setTaskHandle(TaskHandle taskHandle) {
			this.taskHandle = taskHandle;
		}

		public List<Attachment> getAttachments() {
			return attachments;
		}

		public void setAttachments(List<Attachment> attachments) {
			this.attachments = attachments;
		}

		public String getErrorMessage() {
			return errorMessage;
		}

		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}
	}

	@RequestMapping(value = "", method = RequestMethod.POST)
	public void upload(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		List<Attachment> attachments = new ArrayList<Attachment>();
		Map<String, String> formValues = new HashMap<String, String>();

		try {
			List<FileItem> items = upload.parseRequest(request);

			for (FileItem item : items) {

				if (item.isFormField()) {
					formValues.put(item.getFieldName(), item.getString());
				} else {
					Attachment attachment = new Attachment();
					attachment.setAttachmentData(readInputStream(item.getInputStream()));
					attachment.setFilename(item.getName());
					attachment.setMimeType(item.getContentType());
					attachments.add(attachment);
				}

			}
		} catch (FileUploadException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE); // FIXME better code
		}

		for (int i = 0; i < attachments.size(); i++) {
			String description = formValues.get(AttachmentUploadUtil.ATTACHMENT_DESCRIPTION_FORM_NAME_PREFIX + i);
			if (description == null) {
				throw new IllegalArgumentException("Missing description " + i + 1 + " of " + attachments.size());
			}
			attachments.get(0).setDescription(description);
		}

		TaskHandle taskHandle = getTaskHandle(formValues);

		UploadResult result = doUpload(response, attachments, taskHandle);

		response.setContentType("text/html");
		response.getWriter().write(jsonMapper.writeValueAsString(Collections.singletonMap("uploadResult", result)));
	}

	private UploadResult doUpload(HttpServletResponse response, List<Attachment> attachments, TaskHandle taskHandle) {
		try {
			for (Attachment attachment : attachments) {
				AttachmentHandle attachmentHandle = taskService.saveAttachment(taskHandle, attachment);
				attachment.setId(attachmentHandle.getId());
				attachment.setUrl(configuration.getWebUrlForAttachment(attachmentHandle.getId()));
				taskHandle = attachmentHandle.getTaskHandle();
			}
		} catch (ConcurrentUpdateException e) {
			response.setStatus(HttpServletResponse.SC_CONFLICT);
			return new UploadResult("ConcurrentUpdate: " + e.getMessage());
		} catch (ValidationException e) {
			response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
			return new UploadResult("ValidationError: " + e.getMessage());
		} catch (EntityNotFoundException e) {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return new UploadResult("EntityNotFound: " + e.getMessage());
		}

		for (Attachment attachment : attachments) {
			attachment.setByteSize(attachment.getAttachmentData().length);
			attachment.setAttachmentData(null);
		}
		return new UploadResult(taskHandle, attachments);
	}

	private TaskHandle getTaskHandle(Map<String, String> formValues) {
		String taskHandleValue = formValues.get(AttachmentUploadUtil.TASK_HANDLE_FORM_NAME);
		if (taskHandleValue == null) {
			throw new IllegalStateException("Attachment upload form is missing task handle");
		}
		return AttachmentUploadUtil.parseTaskHandleValue(taskHandleValue);
	}

	private byte[] readInputStream(InputStream stream) throws IOException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int len;
			byte[] buffer = new byte[BUF_SIZE];
			while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
				out.write(buffer, 0, len);
			}
			return out.toByteArray();
		} finally {
			stream.close();
		}
	}
}
