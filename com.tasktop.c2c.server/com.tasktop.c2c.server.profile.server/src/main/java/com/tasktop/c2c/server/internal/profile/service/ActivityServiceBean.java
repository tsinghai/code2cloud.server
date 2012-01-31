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
package com.tasktop.c2c.server.internal.profile.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.tenancy.context.DefaultTenancyContext;
import org.springframework.tenancy.context.TenancyContext;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.tenancy.core.Tenant;
import org.springframework.tenancy.provider.DefaultTenant;

import com.tasktop.c2c.server.common.service.domain.Region;
import com.tasktop.c2c.server.common.service.query.QueryUtil;
import com.tasktop.c2c.server.profile.domain.activity.BuildActivity;
import com.tasktop.c2c.server.profile.domain.activity.ProjectActivity;
import com.tasktop.c2c.server.profile.domain.activity.ScmActivity;
import com.tasktop.c2c.server.profile.domain.activity.TaskActivity;
import com.tasktop.c2c.server.profile.domain.build.BuildDetails;
import com.tasktop.c2c.server.profile.domain.build.JobSummary;
import com.tasktop.c2c.server.profile.domain.scm.Commit;
import com.tasktop.c2c.server.profile.service.ActivityService;
import com.tasktop.c2c.server.profile.service.HudsonService;
import com.tasktop.c2c.server.profile.service.provider.HudsonServiceProvider;
import com.tasktop.c2c.server.profile.service.provider.TaskServiceProvider;
import com.tasktop.c2c.server.profile.service.provider.WikiServiceProvider;
import com.tasktop.c2c.server.scm.service.ScmService;
import com.tasktop.c2c.server.tasks.service.TaskService;
import com.tasktop.c2c.server.wiki.domain.WikiActivity;
import com.tasktop.c2c.server.wiki.service.WikiService;

@Service
@Qualifier("main")
public class ActivityServiceBean implements ActivityService {

	@Resource(name = "hudsonServiceProvider")
	private HudsonServiceProvider hudsonServiceProvider;

	@Resource(name = "taskServiceProvider")
	private TaskServiceProvider taskServiceProvider;

	@Resource(name = "wikiServiceProvider")
	private WikiServiceProvider wikiServiceProvider;

	@Resource(name = "scmService")
	private ScmService scmService;

	private ExecutorService activityThreadPool = Executors.newCachedThreadPool();

	private static final int RECENT_ACTIVITY_NUMROWS = 100;
	private static final int SHORT_ACTIVITY_LIST_NUMROWS = 10;

	@Override
	public List<ProjectActivity> getShortActivityList(String projectIdentifier) {
		return this.getNumActivitiesForProject(SHORT_ACTIVITY_LIST_NUMROWS, projectIdentifier);
	}

	@Override
	public List<ProjectActivity> getRecentActivity(String projectIdentifier) {
		return this.getNumActivitiesForProject(RECENT_ACTIVITY_NUMROWS, projectIdentifier);
	}

	private List<ProjectActivity> getNumActivitiesForProject(int numActivities, final String projectIdentifier) {
		final Region region = new Region(0, numActivities);

		List<Callable<List<ProjectActivity>>> jobs = new ArrayList<Callable<List<ProjectActivity>>>();
		jobs.add(new TenancyCallable<List<ProjectActivity>>(projectIdentifier) {

			@Override
			protected List<ProjectActivity> callAsTenant() throws Exception {
				final TaskService taskService = taskServiceProvider.getTaskService(projectIdentifier);
				List<com.tasktop.c2c.server.tasks.domain.TaskActivity> tasksActivities = taskService
						.getRecentActivity(region);
				List<ProjectActivity> results = new ArrayList<ProjectActivity>(tasksActivities.size());
				for (com.tasktop.c2c.server.tasks.domain.TaskActivity taskActivity : tasksActivities) {
					results.add(new TaskActivity(taskActivity));
				}
				return results;
			}
		});

		jobs.add(new TenancyCallable<List<ProjectActivity>>(projectIdentifier) {

			@Override
			protected List<ProjectActivity> callAsTenant() throws Exception {
				final HudsonService hudsonService = hudsonServiceProvider.getService(projectIdentifier);
				Map<JobSummary, List<BuildDetails>> builds = hudsonService.getBuildHistory();
				List<ProjectActivity> results = new ArrayList<ProjectActivity>(builds.size());

				for (Entry<JobSummary, List<BuildDetails>> entry : builds.entrySet()) {
					for (BuildDetails buildDetails : entry.getValue()) {
						results.add(new BuildActivity(buildDetails, entry.getKey()));
					}
				}
				return results;
			}
		});

		jobs.add(new TenancyCallable<List<ProjectActivity>>(projectIdentifier) {

			@Override
			protected List<ProjectActivity> callAsTenant() throws Exception {
				final WikiService wikiService = wikiServiceProvider.getService(projectIdentifier);
				List<WikiActivity> activity = wikiService.getRecentActivity(region);
				List<ProjectActivity> results = new ArrayList<ProjectActivity>(activity.size());

				for (WikiActivity entry : activity) {
					results.add(new com.tasktop.c2c.server.profile.domain.activity.WikiActivity(entry));
				}
				return results;
			}
		});
		jobs.add(new TenancyCallable<List<ProjectActivity>>(projectIdentifier) {

			@Override
			protected List<ProjectActivity> callAsTenant() throws Exception {
				List<Commit> activity = scmService.getLog(region);
				List<ProjectActivity> results = new ArrayList<ProjectActivity>(activity.size());

				for (Commit c : activity) {
					if (c.getAuthor() != null) {
						c.getAuthor().setEmail(null);
					}
					results.add(new ScmActivity(c));
				}
				return results;
			}
		});

		List<ProjectActivity> results = new ArrayList<ProjectActivity>();

		try {
			List<Future<List<ProjectActivity>>> futures = activityThreadPool.invokeAll(jobs);

			for (Future<List<ProjectActivity>> future : futures) {
				// Dump our results into our return list.
				try {
					results.addAll(future.get());
				} catch (Exception e) {
					LoggerFactory.getLogger(ActivityServiceBean.class).warn("Activity service failure", e.getCause());
				}
			}
		} catch (InterruptedException e) {
			// interrupted, ignore
		}

		Collections.sort(results, new Comparator<ProjectActivity>() {

			@Override
			public int compare(ProjectActivity o1, ProjectActivity o2) {
				return o2.getActivityDate().compareTo(o1.getActivityDate()); // reversed
			}
		});
		for (ProjectActivity activity : results) {
			activity.setProjectIdentifier(projectIdentifier);
		}

		QueryUtil.applyRegionToList(results, region);

		return results;
	}

	private static abstract class TenancyCallable<T> implements Callable<T> {

		private Tenant tenant;
		private SecurityContext secContect;

		private TenancyCallable(String projectIdentifier) {
			tenant = new DefaultTenant(projectIdentifier, projectIdentifier);
			secContect = SecurityContextHolder.getContext();
		}

		private TenancyCallable() {
			tenant = TenancyContextHolder.getContext().getTenant();
			secContect = SecurityContextHolder.getContext();
		}

		@Override
		public T call() throws Exception {
			DefaultTenancyContext context = new DefaultTenancyContext();
			context.setTenant(tenant);
			final TenancyContext previousTenancyContext = TenancyContextHolder.getContext();
			final SecurityContext previousSecContext = SecurityContextHolder.getContext();
			TenancyContextHolder.setContext(context);
			SecurityContextHolder.setContext(secContect);
			try {
				return callAsTenant();
			} finally {
				TenancyContextHolder.setContext(previousTenancyContext);
				SecurityContextHolder.setContext(previousSecContext);
			}
		}

		protected abstract T callAsTenant() throws Exception;
	}
}
