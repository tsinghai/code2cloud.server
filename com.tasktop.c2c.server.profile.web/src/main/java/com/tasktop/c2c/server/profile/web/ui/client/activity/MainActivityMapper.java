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
package com.tasktop.c2c.server.profile.web.ui.client.activity;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.AsyncProxy;
import com.tasktop.c2c.server.common.web.client.presenter.ActivityProxy;
import com.tasktop.c2c.server.common.web.client.presenter.SplittableActivity;
import com.tasktop.c2c.server.profile.web.client.place.AgreementsPlace;
import com.tasktop.c2c.server.profile.web.client.place.ProjectAdminPlace;
import com.tasktop.c2c.server.profile.web.client.place.ProjectHomePlace;
import com.tasktop.c2c.server.profile.web.client.place.ProjectsDiscoverPlace;
import com.tasktop.c2c.server.profile.web.client.place.SignInPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.AdminProfilePlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.AppSectionPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.HelpPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.InvitationCreatorPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.NewProjectPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectDashboardPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectDeploymentPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectInvitationPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.ProjectTeamPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.RequestPasswordResetPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.ResetPasswordPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.SignUpPlace;
import com.tasktop.c2c.server.profile.web.ui.client.place.UserAccountPlace;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.ProjectAdminActivity;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.AdminProfilePresenter;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.AgreementsPresenter;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.AppSectionPresenter;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.CreateInvitationPresenter;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.DeploymentsPresenter;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.HelpPresenter;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.NewProjectPresenter;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.PasswordResetPresenter;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.PasswordResetRequestPresenter;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.ProjectDashboardPresenter;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.ProjectDiscoveryPresenter;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.ProjectInvitationPresenter;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.ProjectPresenter;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.ProjectTeamPresenter;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.SignInPresenter;
import com.tasktop.c2c.server.profile.web.ui.client.presenter.components.SignUpPresenterImpl;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.AgreementsView;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.AppSectionView;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.CreateInvitationView;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.NewProjectView;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.PasswordResetRequestView;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.PasswordResetView;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.ProjectInvitationView;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.SignInView;
import com.tasktop.c2c.server.profile.web.ui.client.view.components.account.presenter.AccountActivity;
import com.tasktop.c2c.server.tasks.client.place.ProjectEditTaskPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectNewTaskPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTaskHistoryPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTaskPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTasksPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTasksSummaryListPlace;
import com.tasktop.c2c.server.tasks.client.place.ProjectTasksSummaryPlace;
import com.tasktop.c2c.server.tasks.client.presenters.EditTaskPresenter;
import com.tasktop.c2c.server.tasks.client.presenters.NewTaskPresenter;
import com.tasktop.c2c.server.tasks.client.presenters.TaskHistoryPresenter;
import com.tasktop.c2c.server.tasks.client.presenters.TaskPresenterImpl;
import com.tasktop.c2c.server.tasks.client.presenters.TasksPresenter;
import com.tasktop.c2c.server.tasks.client.presenters.TasksSummaryListPresenter;
import com.tasktop.c2c.server.tasks.client.presenters.TasksSummaryPresenter;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiEditPagePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiHomePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.place.ProjectWikiViewPagePlace;
import com.tasktop.c2c.server.wiki.web.ui.client.presenter.EditWikiPagePresenter;
import com.tasktop.c2c.server.wiki.web.ui.client.presenter.WikiIndexPresenter;
import com.tasktop.c2c.server.wiki.web.ui.client.presenter.WikiPageContentPresenter;

/**
 * @author straxus (Tasktop Technologies Inc.)
 * 
 */
public class MainActivityMapper implements ActivityMapper {

	public static class TaskMappedActivity extends ActivityProxy<ProjectTaskPlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(TaskPresenterImpl.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public TaskMappedActivity() {
			super(ProjectTaskPlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}

	}

	public static class EditTaskMappedActivity extends ActivityProxy<ProjectEditTaskPlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(EditTaskPresenter.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public EditTaskMappedActivity() {
			super(ProjectEditTaskPlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}

	}

	public static class NewTaskMappedActivity extends ActivityProxy<ProjectNewTaskPlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(NewTaskPresenter.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public NewTaskMappedActivity() {
			super(ProjectNewTaskPlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}
	}

	public static class TasksMappedActivity extends ActivityProxy<ProjectTasksPlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(TasksPresenter.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public TasksMappedActivity() {
			super(ProjectTasksPlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}
	}

	public static class TasksSummaryActivity extends ActivityProxy<ProjectTasksSummaryPlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(TasksSummaryPresenter.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public TasksSummaryActivity() {
			super(ProjectTasksSummaryPlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}
	}

	public static class TasksSummaryListActivity extends ActivityProxy<ProjectTasksSummaryListPlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(TasksSummaryListPresenter.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public TasksSummaryListActivity() {
			super(ProjectTasksSummaryListPlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}
	}

	public static class SignupMappedActivity extends ActivityProxy<SignUpPlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(SignUpPresenterImpl.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public SignupMappedActivity() {
			super(SignUpPlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}
	}

	public static class DeploymentMappedActivity extends ActivityProxy<ProjectDeploymentPlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(DeploymentsPresenter.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public DeploymentMappedActivity() {
			super(ProjectDeploymentPlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}
	}

	public static class WikiHomeMappedActivity extends ActivityProxy<ProjectWikiHomePlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(WikiIndexPresenter.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public WikiHomeMappedActivity() {
			super(ProjectWikiHomePlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}
	}

	public static class WikiViewMappedActivity extends ActivityProxy<ProjectWikiViewPagePlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(WikiPageContentPresenter.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public WikiViewMappedActivity() {
			super(ProjectWikiViewPagePlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}
	}

	public static class WikiEditMappedActivity extends ActivityProxy<ProjectWikiEditPagePlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(EditWikiPagePresenter.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public WikiEditMappedActivity() {
			super(ProjectWikiEditPagePlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}
	}

	public static class DashboardActivity extends ActivityProxy<ProjectDashboardPlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(ProjectDashboardPresenter.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public DashboardActivity() {
			super(ProjectDashboardPlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}
	}

	public static class ProjectDiscoveryActivity extends ActivityProxy<ProjectsDiscoverPlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(ProjectDiscoveryPresenter.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public ProjectDiscoveryActivity() {
			super(ProjectsDiscoverPlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}
	}

	public static class ProjectActivity extends ActivityProxy<ProjectHomePlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(ProjectPresenter.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public ProjectActivity() {
			super(ProjectHomePlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}
	}

	// public static class ProjectAdminActivity extends ActivityProxy<ProjectAdminPlace> {
	//
	// @com.google.gwt.user.client.AsyncProxy.ConcreteType(com.tasktop.c2c.server.profile.web.ui.client.presenter.ProjectAdminActivity.class)
	// public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
	// };
	//
	// public ProjectAdminActivity() {
	// super(ProjectAdminPlace.class);
	// }
	//
	// @Override
	// protected ProxyReturn instantiate() {
	// return GWT.create(Proxy.class);
	// }
	// }

	// public static class ProjectAdminSrcActivity extends ActivityProxy<ProjectAdminSourcePlace> {
	//
	// @com.google.gwt.user.client.AsyncProxy.ConcreteType(ProjectAdminSourceActivity.class)
	// public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
	// };
	//
	// public ProjectAdminSrcActivity() {
	// super(ProjectAdminSourcePlace.class);
	// }
	//
	// @Override
	// protected ProxyReturn instantiate() {
	// return GWT.create(Proxy.class);
	// }
	// }
	//
	// public static class ProjectAdminTemActivity extends ActivityProxy<ProjectAdminTeamPlace> {
	//
	// @com.google.gwt.user.client.AsyncProxy.ConcreteType(ProjectAdminTeamActivity.class)
	// public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
	// };
	//
	// public ProjectAdminTemActivity() {
	// super(ProjectAdminTeamPlace.class);
	// }
	//
	// @Override
	// protected ProxyReturn instantiate() {
	// return GWT.create(Proxy.class);
	// }
	// }

	// public static class TasksAdminActivity extends ActivityProxy<ProjectAdminTasksPlace> {
	//
	// @com.google.gwt.user.client.AsyncProxy.ConcreteType(ProjectAdminTaskProductsActivity.class)
	// public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
	// };
	//
	// public TasksAdminActivity() {
	// super(ProjectAdminTasksPlace.class);
	// }
	//
	// @Override
	// protected ProxyReturn instantiate() {
	// return GWT.create(Proxy.class);
	// }
	// }

	public static class HelpActivity extends ActivityProxy<HelpPlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(HelpPresenter.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public HelpActivity() {
			super(HelpPlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}
	}

	public static class UserAccountActivity extends ActivityProxy<UserAccountPlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(AccountActivity.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public UserAccountActivity() {
			super(UserAccountPlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}
	}

	public static class ProjectTeamActivity extends ActivityProxy<ProjectTeamPlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(ProjectTeamPresenter.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public ProjectTeamActivity() {
			super(ProjectTeamPlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}
	}

	public static class ProjectTaskHistoryActivity extends ActivityProxy<ProjectTaskHistoryPlace> {

		@com.google.gwt.user.client.AsyncProxy.ConcreteType(TaskHistoryPresenter.class)
		public interface Proxy extends AsyncProxy<SplittableActivity>, SplittableActivity, ProxyReturn {
		};

		public ProjectTaskHistoryActivity() {
			super(ProjectTaskHistoryPlace.class);
		}

		@Override
		protected ProxyReturn instantiate() {
			return GWT.create(Proxy.class);
		}
	}

	public static final ActivityProxy<?>[] proxies = new ActivityProxy<?>[] { new TaskMappedActivity(),
			new EditTaskMappedActivity(), new NewTaskMappedActivity(), new TasksMappedActivity(),
			new TasksSummaryActivity(), new TasksSummaryListActivity(), new SignupMappedActivity(),
			new DeploymentMappedActivity(), new WikiHomeMappedActivity(), new WikiViewMappedActivity(),
			new WikiEditMappedActivity(), new DashboardActivity(), new ProjectDiscoveryActivity(),
			new ProjectActivity(), new HelpActivity(), new UserAccountActivity(), new ProjectTeamActivity(),
			new ProjectTaskHistoryActivity() };

	public void registerActivity(ActivityProxy<?> activity) {
		mappedActivities.put(activity.getPlaceClass().getName(), activity);
	}

	private Map<String, ActivityProxy<?>> mappedActivities = new HashMap<String, ActivityProxy<?>>();

	public MainActivityMapper() {
		for (ActivityProxy<?> p : proxies) {
			registerActivity(p);
		}
	}

	@Override
	public Activity getActivity(Place place) {
		if (mappedActivities.containsKey(place.getClass().getName())) {
			return mappedActivities.get(place.getClass().getName()).getActivity(place);
		}

		// Not split out
		if (place instanceof SignInPlace) {
			return new SignInPresenter(SignInView.getInstance(), (SignInPlace) place);
		} else if (place instanceof ProjectInvitationPlace) {
			return new ProjectInvitationPresenter(new ProjectInvitationView(),
					((ProjectInvitationPlace) place).getInvitationToken(),
					((ProjectInvitationPlace) place).getProject());
		} else if (place instanceof NewProjectPlace) {
			return new NewProjectPresenter(NewProjectView.getInstance(), (NewProjectPlace) place);
		} else if (place instanceof RequestPasswordResetPlace) {
			return new PasswordResetRequestPresenter(PasswordResetRequestView.getInstance(),
					(RequestPasswordResetPlace) place);
		} else if (place instanceof ResetPasswordPlace) {
			return new PasswordResetPresenter(new PasswordResetView(), ((ResetPasswordPlace) place).getResetToken());
		} else if (place instanceof AgreementsPlace) {
			return new AgreementsPresenter(AgreementsView.getInstance(), (AgreementsPlace) place);
		} else if (place instanceof AppSectionPlace) {
			return new AppSectionPresenter(AppSectionView.getInstance(), (AppSectionPlace) place);
		} else if (place instanceof InvitationCreatorPlace) {
			return new CreateInvitationPresenter(new CreateInvitationView());
		} else if (place instanceof AdminProfilePlace) {
			AdminProfilePresenter activity = new AdminProfilePresenter();
			activity.setPlace(place);
			return activity;
		} else if (place instanceof ProjectAdminPlace) {
			ProjectAdminActivity activity = new ProjectAdminActivity();
			activity.setPlace(place);
			return activity;
		}
		return null;
	}
}
