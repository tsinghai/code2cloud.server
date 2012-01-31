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
package com.tasktop.c2c.server.util.docs;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Conventions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.common.service.doc.DocumentElementContainer;
import com.tasktop.c2c.server.common.service.doc.Documentation;
import com.tasktop.c2c.server.common.service.doc.Exclude;
import com.tasktop.c2c.server.common.service.doc.Section;
import com.tasktop.c2c.server.common.service.doc.Title;
import com.tasktop.c2c.server.common.service.domain.QueryRequest;
import com.tasktop.c2c.server.common.service.domain.QueryResult;
import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.domain.SortInfo;
import com.tasktop.c2c.server.common.service.domain.SortInfo.Order;
import com.tasktop.c2c.server.common.service.domain.criteria.ColumnCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria;
import com.tasktop.c2c.server.common.service.domain.criteria.NaryCriteria;
import com.tasktop.c2c.server.common.service.domain.criteria.Criteria.Operator;
import com.tasktop.c2c.server.internal.wiki.server.WikiServiceController;
import com.tasktop.c2c.server.profile.domain.project.Agreement;
import com.tasktop.c2c.server.profile.domain.project.AgreementProfile;
import com.tasktop.c2c.server.profile.domain.project.Profile;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectService;
import com.tasktop.c2c.server.profile.domain.project.SignUpToken;
import com.tasktop.c2c.server.profile.web.ui.server.ProfileWebServiceController;
import com.tasktop.c2c.server.tasks.domain.Comment;
import com.tasktop.c2c.server.tasks.domain.CommentType;
import com.tasktop.c2c.server.tasks.domain.Component;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.FieldType;
import com.tasktop.c2c.server.tasks.domain.Iteration;
import com.tasktop.c2c.server.tasks.domain.Keyword;
import com.tasktop.c2c.server.tasks.domain.Milestone;
import com.tasktop.c2c.server.tasks.domain.PredefinedTaskQuery;
import com.tasktop.c2c.server.tasks.domain.Priority;
import com.tasktop.c2c.server.tasks.domain.Product;
import com.tasktop.c2c.server.tasks.domain.QuerySpec;
import com.tasktop.c2c.server.tasks.domain.RepositoryConfiguration;
import com.tasktop.c2c.server.tasks.domain.StateTransition;
import com.tasktop.c2c.server.tasks.domain.Task;
import com.tasktop.c2c.server.tasks.domain.TaskActivity;
import com.tasktop.c2c.server.tasks.domain.TaskFieldConstants;
import com.tasktop.c2c.server.tasks.domain.TaskHandle;
import com.tasktop.c2c.server.tasks.domain.TaskResolution;
import com.tasktop.c2c.server.tasks.domain.TaskSeverity;
import com.tasktop.c2c.server.tasks.domain.TaskStatus;
import com.tasktop.c2c.server.tasks.domain.TaskSummary;
import com.tasktop.c2c.server.tasks.domain.TaskSummaryItem;
import com.tasktop.c2c.server.tasks.domain.TaskUserProfile;
import com.tasktop.c2c.server.tasks.domain.TaskActivity.FieldUpdate;
import com.tasktop.c2c.server.tasks.service.CriteriaQueryArguments;
import com.tasktop.c2c.server.tasks.service.PredefinedQueryArguments;
import com.tasktop.c2c.server.tasks.service.SaveAttachmentArguments;
import com.tasktop.c2c.server.tasks.web.service.TaskServiceController;
import com.tasktop.c2c.server.wiki.domain.Attachment;
import com.tasktop.c2c.server.wiki.domain.AttachmentHandle;
import com.tasktop.c2c.server.wiki.domain.Page;
import com.tasktop.c2c.server.wiki.domain.PageHandle;
import com.tasktop.c2c.server.wiki.domain.Person;
import com.tasktop.c2c.server.wiki.domain.WikiActivity;
import com.tasktop.c2c.server.wiki.service.SearchTermQueryArguments;

public class APIDocGenerator {
	private static final String[] TASK_SEVERITIES = new String[] { "blocker", "critical", "major", "normal", "minor",
			"trivial", "enhancement" };

	private Logger log = LoggerFactory.getLogger(APIDocGenerator.class.getName());

	private File outputFolder;
	private List<Class<?>> apiClasses = new ArrayList<Class<?>>();
	{
		apiClasses.add(WikiServiceController.class);
		apiClasses.add(TaskServiceController.class);
		apiClasses.add(ProfileWebServiceController.class);
	}

	private Dictionary dictionary = new Dictionary();

	public File getOutputFolder() {
		return outputFolder;
	}

	@Option(name = "-output", required = true)
	public void setOutputFolder(File outputFolder) {
		this.outputFolder = outputFolder;
	}

	public static void main(String[] args) throws IOException {

		APIDocGenerator generateDocs = new APIDocGenerator();
		CmdLineParser cmdLineParser = new CmdLineParser(generateDocs);
		try {
			cmdLineParser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println("error: " + e.getMessage());
			OutputStreamWriter out = new OutputStreamWriter(System.err);
			cmdLineParser.printUsage(out, null);
			out.flush();
			System.exit(-1);
			return;
		}
		generateDocs.generateDocs();
	}

	public void generateDocs() {
		for (Class<?> apiClass : apiClasses) {
			generateDocs(apiClass);
		}
	}

	public void generateDocs(Class<?> apiClass) {
		if (apiClass.getAnnotation(Controller.class) == null) {
			throw new IllegalArgumentException(apiClass.getName() + " is not a @Controller");
		}
		try {
			File file = new File(outputFolder, apiClass.getName() + ".textile");

			log.info("Creating " + file);

			PrintWriter out = new PrintWriter(file);
			try {
				out.println("h1. " + computeTitle(apiClass) + " API Documentation");
				out.println();

				emitDocumentation(out, apiClass);

				out.println("{toc}");
				out.println();

				DocumentElementContainer<Method> container = new DocumentElementContainer<Method>();
				SortedMap<String, Method> titleToMethod = new TreeMap<String, Method>();
				for (Class<?> c = apiClass; c != Object.class; c = c.getSuperclass()) {
					for (Method m : c.getDeclaredMethods()) {
						if (m.getAnnotation(RequestMapping.class) != null && m.getAnnotation(Exclude.class) == null) {
							container.put(m);
						}
					}
				}
				for (Section section : container) {
					int titleLevel = 2;
					if (section.value() != null && section.value().length() > 0) {
						out.println();
						out.println("h" + titleLevel + ". " + section.value());
						out.println();
						++titleLevel;
					}
					for (Map.Entry<String, Method> entry : container.sectionElementByTitle(section).entrySet()) {

						String title = entry.getKey();
						Method m = entry.getValue();

						out.println();
						out.println("h" + titleLevel + ". " + title);
						out.println();

						emitDocumentation(out, m);

						// TODO: method description
						RequestMapping requestMapping = m.getAnnotation(RequestMapping.class);
						for (String uri : requestMapping.value()) {
							out.println("URI: @" + uri + "@");
						}

						for (RequestMethod requestMethod : requestMapping.method()) {
							out.println("HTTP Method: " + requestMethod.name());
						}

						// TODO: parameter doc
						int parameterIndex = -1;
						for (Annotation[] parameterAnnotations : m.getParameterAnnotations()) {
							++parameterIndex;
							for (Annotation parameterAnnotation : parameterAnnotations) {
								if (parameterAnnotation instanceof RequestBody) {
									out.println();
									out.println("Sample request body:");
									out.println();

									printSampleJson(out, m.getGenericParameterTypes()[parameterIndex], m, false);
								} else if (parameterAnnotation instanceof PathVariable) {
									PathVariable pathVariable = (PathVariable) parameterAnnotation;
									out.println("Path variable: @" + pathVariable.value() + "@ type: "
											+ m.getParameterTypes()[parameterIndex].getSimpleName());
								}
							}
						}

						if (m.getReturnType() != void.class) {
							out.println();

							out.println("Sample return value:");
							out.println();

							printSampleJson(out, m.getGenericReturnType(), m, true);
						}
					}
				}

			} finally {
				out.close();
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private void emitDocumentation(PrintWriter out, AnnotatedElement element) {
		Documentation doc = element.getAnnotation(Documentation.class);
		if (doc != null) {
			out.println(doc.value());
			out.println();
		}
	}

	protected String computeTitle(Class<?> apiClass) {
		Title title = apiClass.getAnnotation(Title.class);
		return title == null ? apiClass.getSimpleName() : title.value();
	}

	private void printSampleJson(PrintWriter out, Type type, Method m, boolean asReturnValue) {
		try {
			Object value = instantiateModel(type, m, asReturnValue, false);
			String variableName = Conventions.getVariableName(value);
			Map<String, Object> model = new HashMap<String, Object>();
			model.put(variableName, value);

			out.println("pre. ");
			ObjectMapper objectMapper = new ObjectMapper();
			JsonGenerator generator = objectMapper.getJsonFactory().createJsonGenerator(out);
			generator.useDefaultPrettyPrinter();
			objectMapper.writeValue(generator, asReturnValue ? model : value);

			out.println();
			out.println();
		} catch (Throwable t) {
			log.error("cannot print sample JSON for type " + type + ": " + t.getMessage(), t);
		}
	}

	private Object instantiateModel(Type type, Method m, boolean asReturnValue, boolean thin)
			throws InstantiationException, IllegalAccessException {
		ParameterizedType parameterizedType = (ParameterizedType) (type instanceof ParameterizedType ? type : null);
		Class<?> classType = (Class<?>) (parameterizedType == null ? (Class<?>) type : parameterizedType.getRawType());

		if (classType == List.class) {
			ArrayList<Object> list = new ArrayList<Object>();
			Type[] typeParameters = parameterizedType.getActualTypeArguments();
			if (typeParameters != null && typeParameters.length == 1) {
				list.add(instantiateModel(typeParameters[0], m, asReturnValue, thin));
			}
			return list;
		} else if (classType == Boolean.class) {
			return Boolean.TRUE;
		} else if (classType.isArray()) {
			Object array = Array.newInstance(classType.getComponentType(), 1);
			Array.set(array, 0, instantiateModel(classType.getComponentType(), m, asReturnValue, thin));
			return array;
		} else if (classType == Profile.class) {
			return createProfile();
		} else if (classType == SignUpToken.class) {
			return createSignUpToken(asReturnValue);
		} else if (classType == Agreement.class) {
			return createAgreement();
		} else if (classType == AgreementProfile.class) {
			return createAgreementProfile();
		} else if (classType == Project.class) {
			return createProject(asReturnValue, thin);
		} else if (classType == ProjectService.class) {
			return createProjectService();
		} else if (classType == QueryResult.class) {
			QueryResult queryResult = new QueryResult();
			queryResult.setOffset(100);
			queryResult.setPageSize(25);
			queryResult.setTotalResultSize(10234);
			queryResult.setResultPage(new ArrayList<Object>());
			Type[] typeParameters = parameterizedType.getActualTypeArguments();
			if (typeParameters != null && typeParameters.length == 1) {
				queryResult.getResultPage().add(instantiateModel(typeParameters[0], m, asReturnValue, true));
			}
			return queryResult;
		} else if (classType == Page.class) {
			return createPage(m, asReturnValue, thin);
		} else if (classType == Person.class) {
			return createWikiPerson();
		} else if (classType == SearchTermQueryArguments.class) {
			return createWikiSearchTermQueryArguments();
		} else if (classType == Attachment.class) {
			return createAttachment(m, asReturnValue);
		} else if (classType == WikiActivity.class) {
			return createWikiActivity(m);
		} else if (classType == AttachmentHandle.class) {
			return createAttachmentHandle();
		} else if (classType == Task.class) {
			return createTask(m, asReturnValue, thin);
		} else if (classType == com.tasktop.c2c.server.tasks.domain.Attachment.class) {
			return createTaskAttachment(m, asReturnValue, thin);
		} else if (classType == com.tasktop.c2c.server.tasks.domain.AttachmentHandle.class) {
			return createTaskAttachmentHandle(asReturnValue, thin);
		} else if (classType == TaskHandle.class) {
			return createTaskHandle(asReturnValue, thin);
		} else if (classType == SaveAttachmentArguments.class) {
			return createTaskSaveAttachmentArguments();
		} else if (classType == CriteriaQueryArguments.class) {
			return createTaskCriteriaQueryArguments();
		} else if (classType == PredefinedQueryArguments.class) {
			return createTaskPredefinedQueryArguments();
		} else if (classType == com.tasktop.c2c.server.tasks.service.SearchTermQueryArguments.class) {
			return createTaskSearchTermQueryArguments();
		} else if (classType == TaskActivity.class) {
			return createTaskActivity();
		} else if (classType == TaskSummary.class) {
			return createTaskSummary();
		} else if (classType == Product.class) {
			return createTaskProduct();
		} else if (classType == Milestone.class) {
			return createTaskMilestone();
		} else if (classType == Component.class) {
			return createTaskComponent();
		} else if (classType == RepositoryConfiguration.class) {
			return createTaskRepositoryConfiguration();
		} else if (classType == Keyword.class) {
			return createTaskKeyword();
		}
		return classType.newInstance();
	}

	private RepositoryConfiguration createTaskRepositoryConfiguration() {
		RepositoryConfiguration configuration = new RepositoryConfiguration();
		configuration.setComponents(createTaskComponents());
		configuration.setCustomFields(createCustomFields());
		configuration.setDefaultIteration(new Iteration("---"));
		configuration.setDefaultPriority(createTaskPriority());
		configuration.setDefaultProduct(createTaskProduct());
		configuration.setDefaultResolution(createTaskResolution());
		configuration.setDefaultSeverity(createTaskSeverity());
		configuration.setDefaultStatus(createTaskStatus(false));
		configuration.setDefaultType("Task");
		configuration.setIterations(Arrays.asList(new Iteration("---"), new Iteration("1"), new Iteration("2")));
		configuration.setMilestones(createTaskMilestones());
		configuration.setPriorities(createTaskPriorities());
		configuration.setProducts(createTaskProducts());
		configuration.setResolutions(createTaskResolutions());
		configuration.setSeverities(createTaskSeverities());
		configuration.setStateTransitions(createTaskStateTransitions());
		configuration.setStatuses(createTaskStatuses());
		configuration.setTaskTypes(Arrays.asList("Task", "Defect", "Feature"));
		configuration.setKeywords(createTaskKeywords());
		configuration.setUsers(createTaskUserProfiles());
		return configuration;
	}

	private List<TaskUserProfile> createTaskUserProfiles() {
		List<TaskUserProfile> list = new ArrayList<TaskUserProfile>();
		list.add(createTaskUserProfile());
		list.add(createTaskUserProfile());
		return list;
	}

	private List<TaskStatus> createTaskStatuses() {
		List<TaskStatus> list = new ArrayList<TaskStatus>();
		list.add(createTaskStatus(false));
		list.add(createTaskStatus(true));
		return list;
	}

	private List<StateTransition> createTaskStateTransitions() {
		List<StateTransition> list = new ArrayList<StateTransition>();
		list.add(new StateTransition(null, "NEW", false));
		list.add(new StateTransition(null, "ASSIGNED", false));
		list.add(new StateTransition("NEW", "ASSIGNED", false));
		list.add(new StateTransition("NEW", "OPEN", false));
		list.add(new StateTransition("ASSIGNED", "OPEN", false));
		list.add(new StateTransition("OPEN", "ASSIGNED", false));
		list.add(new StateTransition("OPEN", "RESOLVED", true));
		list.add(new StateTransition("ASSIGNED", "RESOLVED", true));
		return list;
	}

	private List<TaskSeverity> createTaskSeverities() {
		List<TaskSeverity> list = new ArrayList<TaskSeverity>();
		for (String severity : TASK_SEVERITIES) {
			list.add(createTaskSeverity(severity));
		}
		return list;
	}

	private List<TaskResolution> createTaskResolutions() {
		List<TaskResolution> list = new ArrayList<TaskResolution>();
		list.add(createTaskResolution("FIXED"));
		list.add(createTaskResolution("INVALID"));
		list.add(createTaskResolution("WORKSFORME"));
		list.add(createTaskResolution("WONTFIX"));
		return list;
	}

	private List<Product> createTaskProducts() {
		List<Product> list = new ArrayList<Product>();
		list.add(createTaskProduct());
		return list;
	}

	private List<Priority> createTaskPriorities() {
		List<Priority> list = new ArrayList<Priority>();
		list.add(createTaskPriority("Highest"));
		list.add(createTaskPriority("High"));
		list.add(createTaskPriority("Normal"));
		list.add(createTaskPriority("Low"));
		list.add(createTaskPriority("Lowest"));
		list.add(createTaskPriority("---"));
		return list;
	}

	private List<Milestone> createTaskMilestones() {
		List<Milestone> list = new ArrayList<Milestone>();
		list.add(createTaskMilestone());
		return list;
	}

	private List<Keyword> createTaskKeywords() {
		List<Keyword> list = new ArrayList<Keyword>();
		list.add(createTaskKeyword());
		return list;
	}

	private Keyword createTaskKeyword() {
		return new Keyword("Story", "User Story");
	}

	private List<FieldDescriptor> createCustomFields() {
		List<FieldDescriptor> list = new ArrayList<FieldDescriptor>();
		list.add(createFieldDescriptor(FieldType.TEXT));
		list.add(createFieldDescriptor(FieldType.LONG_TEXT));
		list.add(createFieldDescriptor(FieldType.SINGLE_SELECT));
		return list;
	}

	private FieldDescriptor createFieldDescriptor(FieldType fieldType) {
		FieldDescriptor fd = new FieldDescriptor();
		fd.setAvailableForNewTasks(true);
		fd.setDescription(dictionary.createText(2));
		fd.setFieldType(fieldType);
		fd.setName("custom" + fieldType);
		if (fieldType == FieldType.SINGLE_SELECT || fieldType == FieldType.MULTI_SELECT) {
			List<String> values = new ArrayList<String>();
			values.add(dictionary.randomWord());
			values.add(dictionary.randomWord());
			values.add(dictionary.randomWord());
			fd.setValueStrings(values);
		}
		return fd;
	}

	private List<Component> createTaskComponents() {
		List<Component> list = new ArrayList<Component>();
		list.add(createTaskComponent());
		list.add(createTaskComponent());
		return list;
	}

	private TaskSummary createTaskSummary() {
		TaskSummary taskSummary = new TaskSummary();
		taskSummary.setDate(new Date());
		List<TaskSummaryItem> items = new ArrayList<TaskSummaryItem>();
		for (String severity : TASK_SEVERITIES) {
			TaskSummaryItem item = new TaskSummaryItem();
			item.setSeverity(createTaskSeverity(severity));
			item.setOpenCount(103);
			item.setClosedCount(25);
			items.add(item);
		}
		taskSummary.setItems(items);
		return taskSummary;
	}

	private TaskSeverity createTaskSeverity(String severity) {
		TaskSeverity taskSeverity = new TaskSeverity();
		int id = Math.abs(severity.hashCode());
		taskSeverity.setId(id);
		taskSeverity.setSortkey((short) (id % 100));
		taskSeverity.setValue(severity);
		return taskSeverity;
	}

	private TaskActivity createTaskActivity() {
		TaskActivity taskActivity = new TaskActivity();
		taskActivity.setActivityType(com.tasktop.c2c.server.tasks.domain.TaskActivity.Type.UPDATED);
		taskActivity.setAuthor(createTaskUserProfile());
		List<FieldUpdate> fieldUpdates = new ArrayList<TaskActivity.FieldUpdate>();
		FieldUpdate fieldUpdate = new FieldUpdate();
		fieldUpdate.setFieldName("Summary");
		fieldUpdate.setNewValue(dictionary.createText(5));
		fieldUpdate.setOldValue(dictionary.createText(8));
		fieldUpdates.add(fieldUpdate);
		taskActivity.setFieldUpdates(fieldUpdates);
		return taskActivity;
	}

	private com.tasktop.c2c.server.tasks.service.SearchTermQueryArguments createTaskSearchTermQueryArguments() {
		com.tasktop.c2c.server.tasks.service.SearchTermQueryArguments model = new com.tasktop.c2c.server.tasks.service.SearchTermQueryArguments();
		model.setSearchTerm("some text");
		model.setQuerySpec(createQuerySpec());
		return model;
	}

	private PredefinedQueryArguments createTaskPredefinedQueryArguments() {
		PredefinedQueryArguments model = new PredefinedQueryArguments();
		model.setPredefinedTaskQuery(PredefinedTaskQuery.RECENT);
		model.setQuerySpec(createQuerySpec());
		return model;
	}

	private CriteriaQueryArguments createTaskCriteriaQueryArguments() {
		CriteriaQueryArguments model = new CriteriaQueryArguments();
		model.setCriteria(createTaskCriteria());
		model.setQuerySpec(createQuerySpec());
		return model;
	}

	private QuerySpec createQuerySpec() {
		QuerySpec spec = new QuerySpec();
		spec.setRegion(new Region(0, 50));
		spec.setSortInfo(createSortInfo());
		spec.setThin(true);
		return spec;
	}

	private SortInfo createSortInfo() {
		SortInfo info = new SortInfo();
		info.setSortField(TaskFieldConstants.CREATION_TIME_FIELD);
		info.setSortOrder(Order.DESCENDING);
		return info;
	}

	private Criteria createTaskCriteria() {
		ColumnCriteria summaryCriteria = new ColumnCriteria(TaskFieldConstants.SUMMARY_FIELD, "text");
		summaryCriteria.setOperator(Operator.STRING_CONTAINS);
		Criteria criteria = new NaryCriteria(Operator.AND, summaryCriteria, new NaryCriteria(Operator.OR,
				new ColumnCriteria(TaskFieldConstants.MILESTONE_FIELD, "1.1"), new ColumnCriteria(
						TaskFieldConstants.MILESTONE_FIELD, "1.2")));
		return criteria;
	}

	private SaveAttachmentArguments createTaskSaveAttachmentArguments() {
		SaveAttachmentArguments model = new SaveAttachmentArguments();
		model.setAttachment(createTaskAttachment(false, false));
		model.setComment(createTaskComment(false));
		model.setTaskHandle(createTaskHandle(true, true));
		return model;
	}

	private TaskHandle createTaskHandle(boolean asReturnValue, boolean thin) {
		TaskHandle handle = new TaskHandle();
		handle.setId(123);
		handle.setVersion("3");
		return handle;
	}

	private com.tasktop.c2c.server.tasks.domain.AttachmentHandle createTaskAttachmentHandle(
			boolean asReturnValue, boolean thin) {
		com.tasktop.c2c.server.tasks.domain.AttachmentHandle handle = new com.tasktop.c2c.server.tasks.domain.AttachmentHandle();
		handle.setId(123);
		handle.setTaskHandle(createTaskHandle());
		return handle;
	}

	private Task createTask(Method m, boolean asReturnValue, boolean thin) {
		Task task = new Task();
		boolean isUpdate = m.getName().startsWith("update");
		boolean isCreate = m.getName().startsWith("create");
		if (isUpdate) {
			task.setId(1234);
			task.setVersion("2");
		}
		if (asReturnValue) {
			task.setCreationDate(new Date());
			task.setId(1234);
			task.setVersion("3");
			task.setModificationDate(new Date());
			task.setUrl("http://...");
		}
		task.setShortDescription(dictionary.createText(12));
		task.setComponent(createTaskComponent());
		task.setProduct(createTaskProduct());
		task.setMilestone(createTaskMilestone());
		task.setPriority(createTaskPriority());
		task.setStatus(createTaskStatus(!isCreate));
		if (!isCreate) {
			task.setResolution(createTaskResolution());
		}
		task.setSeverity(createTaskSeverity());
		task.setAssignee(createTaskUserProfile());
		if (!thin) {
			Map<String, String> customFields = new HashMap<String, String>();
			customFields.put("ak_version", "123");
			task.setCustomFields(customFields);

			task.setDescription(dictionary.createText(150));
			task.setFoundInRelease("1.0.0.M1");
			task.setKeywords(createTaskKeywords());
		}
		if (asReturnValue && !thin) {
			List<Comment> comments = new ArrayList<Comment>();
			List<com.tasktop.c2c.server.tasks.domain.Attachment> attachments = new ArrayList<com.tasktop.c2c.server.tasks.domain.Attachment>();

			if (!isCreate) {
				for (int x = 0; x < 3; ++x) {
					comments.add(createTaskComment(asReturnValue));
				}
				attachments.add(createTaskAttachment(true, true));
			}
			task.setComments(comments);
			task.setAttachments(attachments);
		}
		return task;
	}

	private Object createTaskAttachment(Method m, boolean asReturnValue, boolean thin) {
		return createTaskAttachment(asReturnValue, thin);
	}

	private com.tasktop.c2c.server.tasks.domain.Attachment createTaskAttachment(boolean asReturnValue,
			boolean thin) {
		com.tasktop.c2c.server.tasks.domain.Attachment attachment = new com.tasktop.c2c.server.tasks.domain.Attachment();

		attachment.setByteSize(1024);
		attachment.setDescription(dictionary.createText(5));
		attachment.setFilename("filename.pdf");
		attachment.setMimeType("application/pdf");
		attachment.setTaskHandle(createTaskHandle());
		if (asReturnValue) {
			attachment.setId(1);
			attachment.setCreationDate(new Date());
			if (!thin) {
				attachment.setAttachmentData(dictionary.createText(20).getBytes());
			}
		}

		return attachment;
	}

	private TaskHandle createTaskHandle() {
		TaskHandle taskHandle = new TaskHandle();
		taskHandle.setId(123);
		taskHandle.setVersion("3");
		return taskHandle;
	}

	private TaskStatus createTaskStatus(boolean resolved) {
		TaskStatus taskStatus = new TaskStatus();
		taskStatus.setActive(true);
		taskStatus.setId(123);
		taskStatus.setOpen(!resolved);
		taskStatus.setSortkey((short) 1);
		taskStatus.setValue(resolved ? "RESOLVED" : "ASSIGNED");
		return taskStatus;
	}

	private TaskSeverity createTaskSeverity() {
		TaskSeverity taskSeverity = new TaskSeverity();
		taskSeverity.setId(123);
		taskSeverity.setSortkey((short) 2);
		taskSeverity.setValue("major");
		return taskSeverity;
	}

	private TaskResolution createTaskResolution() {
		return createTaskResolution("FIXED");
	}

	private TaskResolution createTaskResolution(String value) {
		TaskResolution taskResolution = new TaskResolution();
		taskResolution.setId(123);
		taskResolution.setSortkey((short) 5);
		taskResolution.setValue(value);
		return taskResolution;
	}

	private Priority createTaskPriority() {
		return createTaskPriority("High");
	}

	private Priority createTaskPriority(String value) {
		Priority priority = new Priority();
		priority.setActive(true);
		priority.setId(123);
		priority.setSortkey((short) 7);
		priority.setValue(value);
		return priority;
	}

	private Milestone createTaskMilestone() {
		Milestone milestone = new Milestone();
		milestone.setId(123);
		milestone.setSortkey((short) 0);
		milestone.setValue("1.0.1");
		return milestone;
	}

	private Product createTaskProduct() {
		Product product = new Product();
		product.setId(123);
		product.setIsActive(true);
		product.setName("Toaster");
		return product;
	}

	private Component createTaskComponent() {
		Component component = new Component();
		component.setId(123);
		component.setName("System");
		return component;
	}

	private Comment createTaskComment(boolean asReturnValue) {
		Comment comment = new Comment();
		if (asReturnValue) {
			comment.setAuthor(createTaskUserProfile());
			comment.setCreationDate(new Date());
			comment.setCommentType(CommentType.NORMAL);
		}
		comment.setCommentText(dictionary.createText(50));
		return comment;
	}

	private TaskUserProfile createTaskUserProfile() {
		// login name is the only relevant field when specifying a task user
		TaskUserProfile profile = new TaskUserProfile();
		profile.setLoginName("joe.bloe");
		return profile;
	}

	private AttachmentHandle createAttachmentHandle() {
		AttachmentHandle handle = new AttachmentHandle();
		handle.setId(1234);
		handle.setPage(createPageHandle());
		return handle;
	}

	private PageHandle createPageHandle() {
		PageHandle handle = new PageHandle();
		handle.setId(1234);
		return handle;
	}

	private WikiActivity createWikiActivity(Method m) {
		WikiActivity activity = new WikiActivity();
		activity.setActivityType(WikiActivity.Type.UPDATED);
		activity.setAuthor(createWikiPerson());
		activity.setPage(createPage(m, true, true));
		activity.setActivityDate(new Date());
		return activity;
	}

	private Attachment createAttachment(Method m, boolean asReturnValue) {
		Attachment attachment = new Attachment();
		attachment.setContent("012".getBytes());
		if (asReturnValue) {
			attachment.setCreationDate(new Date());
			attachment.setEtag(System.currentTimeMillis() + ":1234");
			attachment.setId(1234);
			attachment.setLastAuthor(createWikiPerson());
			attachment.setModificationDate(new Date());
			attachment.setOriginalAuthor(createWikiPerson());
			attachment.setSize(10223);
			attachment.setPage(createPage(m, true, true));
		}
		attachment.setName("image.png");
		attachment.setMimeType("image/png");
		return attachment;
	}

	protected Object createWikiSearchTermQueryArguments() {
		SearchTermQueryArguments queryArguments = new SearchTermQueryArguments();
		queryArguments.setSearchTerm("word");
		queryArguments.setQueryRequest(createWikiQueryRequest());
		return queryArguments;
	}

	private QueryRequest createWikiQueryRequest() {
		QueryRequest queryRequest = new QueryRequest();
		queryRequest.setPageInfo(new Region(20, 10));
		queryRequest.setSortInfo(new SortInfo("path", Order.ASCENDING));
		return queryRequest;
	}

	private Page createPage(Method m, boolean asReturnValue, boolean thin) {
		Page page = new Page();
		if (m.getName().startsWith("update")) {
			page.setId(1234);
		}
		if (asReturnValue) {
			page.setCreationDate(new Date());
			page.setId(1234);
			page.setModificationDate(new Date());
			page.setLastAuthor(createWikiPerson());
			page.setOriginalAuthor(createWikiPerson());
			page.setUrl("http://...");
			page.setAttachmentsUrl("https://...");
		}
		page.setPath("Page Title");
		if (m.getName().contains("Rendered")) {
			page.setContent("<h1>Heading</h1><p>Some content...</p>");
		} else {
			page.setContent("h1. Heading\n\nSome content...");
		}
		return page;
	}

	private Person createWikiPerson() {
		Person person = new Person();
		person.setId(103);
		person.setLoginName("joe.bloe");
		person.setName("Joe Bloe");
		return person;
	}

	private Project createProject(boolean asReturnValue, boolean thin) {
		Project project = new Project();
		project.setName("Pet Clinic");
		project.setDescription("project description");
		project.setPublic(true);
		if (asReturnValue) {
			project.setId(1234L);
			project.setIdentifier("pet-clinic");
			if (!thin) {
				project.setProjectServices(new ArrayList<ProjectService>());
				project.getProjectServices().add(createProjectService());
			}
		}
		return project;
	}

	private ProjectService createProjectService() {
		ProjectService service = new ProjectService();
		service.setAvailable(true);
		service.setId(1234L);
		service.setServiceType(ServiceType.SCM);
		service.setUrl("https://hostname/s/pet-clinic/scm/pet-clinic.git");
		return service;
	}

	private AgreementProfile createAgreementProfile() {
		AgreementProfile agreement = new AgreementProfile();
		agreement.setAgreementDate(new Date());
		agreement.setId(1234L);
		agreement.setAgreement(createAgreement());
		return agreement;
	}

	private Agreement createAgreement() {
		Agreement agreement = new Agreement();
		agreement.setCreationDate(new Date());
		agreement.setId(1234L);
		agreement.setTitle("Terms Of Use");
		agreement.setText("...terms of the agreement, typically legal jargon...");
		return agreement;
	}

	private SignUpToken createSignUpToken(boolean asReturnValue) {
		SignUpToken signUpToken = new SignUpToken();
		signUpToken.setEmail("joe.bloe@example.com");
		signUpToken.setFirstname("Joe");
		signUpToken.setLastname("Bloe");
		if (asReturnValue) {
			signUpToken.setToken(UUID.randomUUID().toString());
			signUpToken.setId(1234L);
		}
		return signUpToken;
	}

	private Profile createProfile() {
		Profile profile = new Profile();
		profile.setEmail("joe.bloe@example.com");
		profile.setFirstName("Joe");
		profile.setLastName("Bloe");
		profile.setId(1234L);
		profile.setUsername("joe.bloe");
		return profile;
	}

}
