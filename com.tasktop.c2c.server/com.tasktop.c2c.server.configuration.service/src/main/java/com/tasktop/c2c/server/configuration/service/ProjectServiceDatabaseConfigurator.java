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
package com.tasktop.c2c.server.configuration.service;

import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.sql.DataSource;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.tenancy.context.TenancyContext;
import org.springframework.tenancy.context.TenancyContextHolder;
import org.springframework.tenancy.provider.DefaultTenant;

import com.tasktop.c2c.server.configuration.service.NodeConfigurationService.NodeConfiguration;
import com.tasktop.c2c.server.configuration.service.NodeConfigurationServiceBean.Configurator;

/**
 * Used to configure a new database for a service for a project. Will create a database named from the projectIdentifier
 * plus a suffix, and then run Liquibase for schema creation
 * 
 * @author Clint Morgan <clint.morgan@tasktop.com> (Tasktop Technologies Inc.)
 * @author Jennifer Hickey
 * 
 */
public class ProjectServiceDatabaseConfigurator implements Configurator, ResourceLoaderAware {

	private String suffix;

	private String changelog;

	private String changelogContexts;

	@Resource(name = "rawDataSource")
	private DataSource rawDatasource;

	private DataSource tenantAwareDataSource;

	private ResourceLoader resourceLoader;

	public void initializeNewProject(String projectIdentifier) throws SQLException {
		String dbType = createDatabase(projectIdentifier);
		installSchema(dbType, projectIdentifier);
	}

	private String createDatabase(String projectIdentifier) throws SQLException {
		java.sql.Connection dbConnection = rawDatasource.getConnection();
		try {
			Statement s = dbConnection.createStatement();
			String dbName = projectIdentifier + suffix;
			String dbType = dbConnection.getMetaData().getDatabaseProductName();
			String createStmt = "create database `" + dbName + "`";
			if (dbType.toUpperCase().startsWith("HSQL")) {
				createStmt = "create schema " + dbName;
			}
			s.execute(createStmt);

			if (!dbConnection.getAutoCommit()) {
				dbConnection.commit();
			}
			return dbType;
		} finally {
			dbConnection.close();
		}
	}

	private void installSchema(String dbType, String projectIdentifier) {
		final TenancyContext previousTenancyContext = TenancyContextHolder.getContext();
		setTenancyContext(projectIdentifier);
		try {
			SpringLiquibase schemaInstaller = new SpringLiquibase();
			schemaInstaller.setResourceLoader(resourceLoader);
			schemaInstaller.setDataSource(tenantAwareDataSource);
			schemaInstaller.setChangeLog(changelog);
			schemaInstaller.setContexts(changelogContexts);
			String dbName = projectIdentifier + suffix;
			if (dbType.toUpperCase().startsWith("HSQL")) {
				// HSQLDB will create the DB in upper case even if we specify lower case in the escaped create schema
				// statement
				schemaInstaller.setDefaultSchema(dbName.toUpperCase());
			} else {
				schemaInstaller.setDefaultSchema(dbName);
			}
			schemaInstaller.afterPropertiesSet();
		} catch (LiquibaseException e) {
			throw new RuntimeException(e);
		} finally {
			TenancyContextHolder.setContext(previousTenancyContext);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.tasktop.c2c.server.configuration.service.NodeConfigurationServiceBean.Configurator#configure(org.
	 * cloudfoundry.code.server.configuration.service.NodeConfigurationService.NodeConfiguration)
	 */
	@Override
	public void configure(NodeConfiguration configuration) {
		try {
			initializeNewProject(configuration.getApplicationId());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param suffix
	 *            the suffix to set
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	/**
	 * @param changelog
	 *            the changelog to use for schema creation
	 */
	public void setChangelog(String changelog) {
		this.changelog = changelog;
	}

	/**
	 * @param changelogContexts
	 *            the changelogContexts to set
	 */
	public void setChangelogContexts(String changelogContexts) {
		this.changelogContexts = changelogContexts;
	}

	/**
	 * @param rawDatasource
	 *            the rawDatasource to set
	 */
	public void setRawDatasource(DataSource rawDatasource) {
		this.rawDatasource = rawDatasource;
	}

	/**
	 * @param tenantAwareDataSource
	 *            the tenantAwareDataSource to set
	 */
	public void setTenantAwareDataSource(DataSource tenantAwareDataSource) {
		this.tenantAwareDataSource = tenantAwareDataSource;
	}

	private void setTenancyContext(String projectIdentifier) {
		TenancyContextHolder.createEmptyContext();
		DefaultTenant tenant = new DefaultTenant();
		tenant.setIdentity(projectIdentifier);
		TenancyContextHolder.getContext().setTenant(tenant);
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

}
