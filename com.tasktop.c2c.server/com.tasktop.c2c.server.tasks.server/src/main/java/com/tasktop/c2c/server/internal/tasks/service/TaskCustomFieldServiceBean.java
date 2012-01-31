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
package com.tasktop.c2c.server.internal.tasks.service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.validation.Errors;

import com.tasktop.c2c.server.common.service.AbstractJpaServiceBean;
import com.tasktop.c2c.server.common.service.EntityNotFoundException;
import com.tasktop.c2c.server.common.service.ValidationException;
import com.tasktop.c2c.server.internal.tasks.domain.Fielddef;
import com.tasktop.c2c.server.internal.tasks.domain.Task;
import com.tasktop.c2c.server.internal.tasks.service.sql.ColumnDescriptor;
import com.tasktop.c2c.server.internal.tasks.service.sql.IndexType;
import com.tasktop.c2c.server.internal.tasks.service.sql.SqlDialect;
import com.tasktop.c2c.server.tasks.domain.CustomFieldValue;
import com.tasktop.c2c.server.tasks.domain.FieldDescriptor;
import com.tasktop.c2c.server.tasks.domain.FieldType;

/**
 * implementation of custom fields as per Bugzilla implementation. See Field.pm in Bugzilla source.
 * 
 * @author David Green
 */
@Service
@Transactional
public class TaskCustomFieldServiceBean extends AbstractJpaServiceBean implements TaskCustomFieldService {

	private static final String MULTISELECT_FIELDVALUE_TABLE_PREFIX = "bug_";

	private static final String CUSTOM_FIELD_PREFIX = "cf_";

	private static final String SELECT_FIELD_DEFAULT_VALUE = "---";

	@Autowired
	protected SqlDialect sqlDialect;

	private void executeUpdate(List<String> statements) {
		for (String statement : statements) {
			executeUpdate(statement);
		}
	}

	private void executeUpdate(String statement) {
		entityManager.createNativeQuery(statement).executeUpdate();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CustomFieldValue> getFieldValues(FieldDescriptor descriptor) throws EntityNotFoundException {
		// verify that the field exists
		String columnName = computeColumnName(descriptor);
		Query query = entityManager.createQuery("select e from " + Fielddef.class.getSimpleName() + " e where "
				+ "e.custom = true and e.name = :n");
		query.setParameter("n", columnName);
		Fielddef fielddef;
		try {
			fielddef = (Fielddef) query.getSingleResult();
		} catch (NoResultException e) {
			throw new EntityNotFoundException();
		}
		FieldType computedFieldType = computeFieldType(fielddef);
		if (computedFieldType == FieldType.SINGLE_SELECT || computedFieldType == FieldType.MULTI_SELECT) {
			String tableName = computeTableName(descriptor);

			String sqlString = String.format("select %s, %s, %s, %s from %s order by %s",//
					sqlDialect.quoteIdentifier("id"), //
					sqlDialect.quoteIdentifier("value"),//
					sqlDialect.quoteIdentifier("isactive"),//
					sqlDialect.quoteIdentifier("sortkey"),//
					sqlDialect.quoteIdentifier(tableName),//
					sqlDialect.quoteIdentifier("sortkey"));
			List<Object[]> resultList = entityManager.createNativeQuery(sqlString).getResultList();
			List<CustomFieldValue> values = new ArrayList<CustomFieldValue>(resultList.size());
			for (Object[] result : resultList) {
				CustomFieldValue value = new CustomFieldValue();
				value.setId((Integer) result[0]);
				value.setValue((String) result[1]);
				if (result[2] instanceof Integer) {
					value.setIsActive(((Integer) result[2]) == 1);
				} else if (result[2] instanceof Boolean) {
					value.setIsActive((Boolean) result[2]);
				}
				Integer sortKey = (Integer) result[3];
				value.setSortkey(sortKey.shortValue());
				values.add(value);
			}
			return values;
		}
		return Collections.emptyList();
	}

	@Override
	public void updateTaskCustomFields(Integer taskId, Map<String, Object> fields) throws EntityNotFoundException {
		if (fields.isEmpty()) {
			return;
		}
		Query query = entityManager.createQuery("select e from " + Fielddef.class.getSimpleName() + " e where "
				+ "e.custom = true and e.obsolete = false order by e.sortkey, e.name");
		String columnsSql = "";
		List<Fielddef> multiSelectFields = null;
		@SuppressWarnings("unchecked")
		List<Fielddef> allFieldDefs = (List<Fielddef>) query.getResultList();
		List<Object> values = new ArrayList<Object>(fields.size());
		for (Fielddef fielddef : allFieldDefs) {
			String computedFieldName = computeFieldName(fielddef);
			if (!fields.containsKey(computedFieldName)) {
				continue;
			}
			FieldType fieldType = computeFieldType(fielddef);
			if (fieldType == FieldType.MULTI_SELECT) {
				if (multiSelectFields == null) {
					multiSelectFields = new ArrayList<Fielddef>(allFieldDefs.size());
				}
				multiSelectFields.add(fielddef);
				continue;
			}
			Object fieldValue = fields.get(computedFieldName);
			if (columnsSql.length() > 0) {
				columnsSql += ", ";
			}
			columnsSql += sqlDialect.quoteIdentifier(fielddef.getName());

			if (fieldValue == null || (fieldValue instanceof String && ((String) fieldValue).isEmpty())) {
				columnsSql += " = NULL";
			} else {
				columnsSql += " = ?";
				values.add(fieldValue);
			}
		}
		if (columnsSql.length() > 0) {
			String sql = String.format("update %s set %s where %s = %s", //
					sqlDialect.quoteIdentifier(getTableName(Task.class)), //
					columnsSql, //
					sqlDialect.quoteIdentifier("bug_id"), //
					sqlDialect.literal(taskId));

			query = entityManager.createNativeQuery(sql);
			for (int x = 0; x < values.size(); ++x) {
				query.setParameter(x + 1, values.get(x));
			}
			query.executeUpdate();
		}
		if (multiSelectFields != null) {
			for (Fielddef fielddef : multiSelectFields) {
				String computedFieldName = computeFieldName(fielddef);
				String tableName = MULTISELECT_FIELDVALUE_TABLE_PREFIX + fielddef.getName();
				String valuesSql = String.format("select %s from %s where %s = %s",//
						sqlDialect.quoteIdentifier("value"), //
						sqlDialect.quoteIdentifier(tableName), //
						sqlDialect.quoteIdentifier("bug_id"), //
						sqlDialect.literal(taskId));
				Object fieldValue = fields.get(computedFieldName);
				Set<Object> newValues = fieldValue == null ? Collections.emptySet() : new HashSet<Object>(
						Arrays.asList((Object[]) fieldValue));
				@SuppressWarnings("unchecked")
				List<String> existingValues = entityManager.createNativeQuery(valuesSql).getResultList();
				for (String existingValue : existingValues) {
					if (!newValues.contains(existingValue)) {
						String deleteSql = String.format("delete from %s where %s = %s and %s = %s",//
								sqlDialect.quoteIdentifier(tableName), //
								sqlDialect.quoteIdentifier("bug_id"), //
								sqlDialect.literal(taskId), sqlDialect.quoteIdentifier("value"), //
								sqlDialect.literal(existingValue));
						entityManager.createNativeQuery(deleteSql).executeUpdate();
					}
				}
				for (Object newValue : newValues) {
					if (!existingValues.contains(newValue)) {
						String insertSql = String.format("insert into %s (%s, %s) values (%s, %s)",//
								sqlDialect.quoteIdentifier(tableName), //
								sqlDialect.quoteIdentifier("bug_id"), //
								sqlDialect.quoteIdentifier("value"), //
								sqlDialect.literal(taskId), sqlDialect.literal(newValue));
						entityManager.createNativeQuery(insertSql).executeUpdate();
					}
				}
			}
		}

	}

	private String computeFieldName(Fielddef fielddef) {
		return fielddef.getName().substring(CUSTOM_FIELD_PREFIX.length());
	}

	@Override
	public void removeCustomField(FieldDescriptor descriptor) throws EntityNotFoundException {
		// verify that the field exists
		String columnName = computeColumnName(descriptor);
		Query query = entityManager.createQuery("select e from " + Fielddef.class.getSimpleName() + " e where "
				+ "e.custom = true and e.name = :n");
		query.setParameter("n", columnName);
		Fielddef fielddef = null;
		try {
			fielddef = (Fielddef) query.getSingleResult();
		} catch (NoResultException e) {
			throw new EntityNotFoundException();
		}

		List<String> statements = new ArrayList<String>();
		if (columnExists(getTableName(Task.class), columnName)) {
			statements.addAll(sqlDialect.dropColumn(getTableName(Task.class), columnName));
		}
		String computedTableName = computeTableName(descriptor);
		if (tableExists(MULTISELECT_FIELDVALUE_TABLE_PREFIX + computedTableName)) {
			statements.addAll(sqlDialect.dropTable(MULTISELECT_FIELDVALUE_TABLE_PREFIX + computedTableName));
		}
		if (tableExists(computedTableName)) {
			statements.addAll(sqlDialect.dropTable(computedTableName));
		}

		// IMPORTANT: ORDER DEPENDENCY: entity manager must remove/flush before we execute these statements
		// due to how MySQL handles commit and alter table statements
		entityManager.remove(fielddef);
		System.out.println(TransactionAspectSupport.currentTransactionStatus().isRollbackOnly());
		entityManager.flush();
		System.out.println(TransactionAspectSupport.currentTransactionStatus().isRollbackOnly());

		executeUpdate(statements);
	}

	private boolean tableExists(String tableName) {

		for (String table : (List<String>) entityManager.createNativeQuery("show tables").getResultList()) {
			if (tableName.equals(table)) {
				return true;
			}
		}
		return false;
	}

	private boolean columnExists(String tableName, String columnName) {
		int size = entityManager
				.createNativeQuery(
						String.format("show columns from %s like '%s'", sqlDialect.quoteIdentifier(tableName),
								columnName)).getResultList().size();
		return size == 1;
	}

	@Override
	public FieldDescriptor createCustomField(FieldDescriptor descriptor) throws ValidationException {
		validate(descriptor);
		for (FieldDescriptor existingField : getCustomFields()) {
			if (existingField.getName().equals(descriptor.getName())) {
				Errors errors = super.createErrors(descriptor);
				errors.reject("customfield.name.unique");
				throw new ValidationException(errors);
			}
		}

		// # How various field types translate into SQL data definitions.
		// use constant SQL_DEFINITIONS => {
		// # Using commas because these are constants and they shouldn't
		// # be auto-quoted by the "=>" operator.
		// FIELD_TYPE_FREETEXT, { TYPE => 'varchar(255)' },
		// FIELD_TYPE_SINGLE_SELECT, { TYPE => 'varchar(64)', NOTNULL => 1,
		// DEFAULT => "'---'" },
		// FIELD_TYPE_TEXTAREA, { TYPE => 'MEDIUMTEXT' },
		// FIELD_TYPE_DATETIME, { TYPE => 'DATETIME' },
		// FIELD_TYPE_BUG_ID, { TYPE => 'INT3' },
		// };
		Integer sqlType = null;
		int precision = 0;
		int scale = 0;
		Object defaultValue = null;
		boolean nullable = true;
		switch (descriptor.getFieldType()) {
		case TEXT:
			sqlType = Types.VARCHAR;
			precision = 255;
			break;
		case LONG_TEXT:
			sqlType = Types.LONGVARCHAR;
			precision = 32535;
			break;
		case SINGLE_SELECT:
			sqlType = Types.VARCHAR;
			defaultValue = SELECT_FIELD_DEFAULT_VALUE;
			nullable = false;
			precision = 64;
			break;
		case MULTI_SELECT:
			defaultValue = SELECT_FIELD_DEFAULT_VALUE;
			break;
		case TIMESTAMP:
			sqlType = Types.TIMESTAMP;
			break;
		case TASK_REFERENCE:
			sqlType = Types.INTEGER;
			precision = 8;
			break;
		case CHECKBOX:
			sqlType = Types.BOOLEAN;
			break;
		default:
			throw new UnsupportedOperationException(descriptor.getFieldType() + " not implemented");
		}
		String columnName = computeColumnName(descriptor);
		List<String> statements = new ArrayList<String>();
		if (sqlType != null) {
			statements.addAll(sqlDialect.addColumn(getTableName(Task.class), new ColumnDescriptor(columnName, sqlType,
					precision, scale, nullable, defaultValue)));
		}
		if (descriptor.getFieldType() == FieldType.SINGLE_SELECT || descriptor.getFieldType() == FieldType.MULTI_SELECT) {
			String tableName = computeTableName(descriptor);
			maybeAddDefaultValue(descriptor.getValues());
			statements.addAll(sqlDialect.createTable(tableName,//
					new ColumnDescriptor("id", Types.SMALLINT, 4, 0, false, null).primaryKey(true).autoIncrement(true), //
					new ColumnDescriptor("value", Types.VARCHAR, 64, 0, false, null).unique(true), //
					new ColumnDescriptor("sortkey", Types.SMALLINT, 4, 0, false, 0).indexed(true), //
					new ColumnDescriptor("isactive", Types.BOOLEAN, 1, 0, false, true).indexed(true), //
					new ColumnDescriptor("visibility_value_id", Types.SMALLINT, 4, 0, true, null)));

			if (descriptor.getValues() != null) {
				for (CustomFieldValue value : descriptor.getValues()) {
					statements.add(insertCustomFieldValue(descriptor, value));
				}
			}
		}
		if (descriptor.getFieldType() == FieldType.MULTI_SELECT) {
			String fieldValuesTableName = computeTableName(descriptor);
			String tableName = MULTISELECT_FIELDVALUE_TABLE_PREFIX + fieldValuesTableName;
			statements.addAll(sqlDialect.createTable(tableName,//
					new ColumnDescriptor("bug_id", Types.SMALLINT, 9, 0, false, null), //
					new ColumnDescriptor("value", Types.VARCHAR, 64, 0, false, null)));
			statements.addAll(sqlDialect.index(IndexType.UNIQUE, tableName, "bug_id", "value"));
			statements.addAll(sqlDialect.index(IndexType.GENERIC, tableName, "value"));
			statements.addAll(sqlDialect.foreignKeyConstraint(tableName, "bug_id", "bugs", "bug_id"));
			statements.addAll(sqlDialect.foreignKeyConstraint(tableName, "value", fieldValuesTableName, "value"));
		}

		executeUpdate(statements);

		Fielddef fielddef = new Fielddef();
		fielddef.setCustom(true);
		fielddef.setName(computeColumnName(descriptor));
		fielddef.setType((short) toBugzillaType(descriptor.getFieldType()));
		fielddef.setDescription(descriptor.getDescription());
		fielddef.setSortkey((short) 101);
		fielddef.setBuglist(true);
		fielddef.setEnterBug(descriptor.isAvailableForNewTasks());
		fielddef.setObsolete(descriptor.isObsolete());
		entityManager.persist(fielddef);
		entityManager.flush();
		return createFieldDescriptor(fielddef);
	}

	private void maybeAddDefaultValue(List<CustomFieldValue> values) {
		if (values == null) {
			values = new ArrayList<CustomFieldValue>();
		}
		for (CustomFieldValue value : values) {
			if (value.getValue().equals(SELECT_FIELD_DEFAULT_VALUE)) {
				return;
			}
		}
		CustomFieldValue defaultValue = new CustomFieldValue();
		defaultValue.setIsActive(true);
		defaultValue.setSortkey((short) 0);
		defaultValue.setValue(SELECT_FIELD_DEFAULT_VALUE);
		values.add(defaultValue);
	}

	private String insertCustomFieldValue(FieldDescriptor descriptor, CustomFieldValue value) {
		String tableName = computeTableName(descriptor);

		return String.format("insert into %s (%s, %s) values (%s, %s)",//
				sqlDialect.quoteIdentifier(tableName),//
				sqlDialect.quoteIdentifier("value"),//
				sqlDialect.quoteIdentifier("sortkey"),//
				sqlDialect.literal(value.getValue()), sqlDialect.literal(value.getSortkey()));
	}

	@Override
	public void addNewValue(FieldDescriptor descriptor, CustomFieldValue newValue) {
		executeUpdate(insertCustomFieldValue(descriptor, newValue));
	}

	@Override
	public void udpateValue(FieldDescriptor descriptor, CustomFieldValue value) {
		String statment = updateCustomFieldValue(descriptor, value);
		executeUpdate(statment);
	}

	private String updateCustomFieldValue(FieldDescriptor descriptor, CustomFieldValue value) {
		String tableName = computeTableName(descriptor);
		String statment = String.format("update %s set %s = %s, %s = %s, %s = %s where %s = %s", //
				sqlDialect.quoteIdentifier(tableName), //
				sqlDialect.quoteIdentifier("value"), //
				sqlDialect.literal(value.getValue()), //
				sqlDialect.quoteIdentifier("sortkey"), //
				sqlDialect.literal(value.getSortkey()), //
				sqlDialect.quoteIdentifier("isactive"), //
				sqlDialect.literal(value.getIsActive()), //
				sqlDialect.quoteIdentifier("id"), //
				sqlDialect.literal(value.getId()));
		return statment;
	}

	private String removeCustomFieldValue(FieldDescriptor descriptor, CustomFieldValue value) {
		String tableName = computeTableName(descriptor);
		String statment = String.format("delete from %s  where %s = %s", //
				sqlDialect.quoteIdentifier(tableName), //
				sqlDialect.quoteIdentifier("id"), //
				sqlDialect.literal(value.getId()));
		return statment;
	}

	private int toBugzillaType(FieldType fieldType) {
		// use constant FIELD_TYPE_UNKNOWN => 0;
		// use constant FIELD_TYPE_FREETEXT => 1;
		// use constant FIELD_TYPE_SINGLE_SELECT => 2;
		// use constant FIELD_TYPE_MULTI_SELECT => 3;
		// use constant FIELD_TYPE_TEXTAREA => 4;
		// use constant FIELD_TYPE_DATETIME => 5;
		// use constant FIELD_TYPE_BUG_ID => 6;
		// use constant FIELD_TYPE_BUG_URLS => 7;
		// use constant FIELD_TYPE_KEYWORDS => 8;
		switch (fieldType) {
		case LONG_TEXT:
			return 4;
		case SINGLE_SELECT:
			return 2;
		case MULTI_SELECT:
			return 3;
		case TASK_REFERENCE:
			return 6;
		case TEXT:
			return 1;
		case TIMESTAMP:
			return 5;
		case CHECKBOX:
			return 9; // FIXME
			// case KEYWORDS:
			// return 8;
		}
		throw new IllegalStateException(fieldType.name());
	}

	protected String computeTableName(FieldDescriptor descriptor) {
		return CUSTOM_FIELD_PREFIX + descriptor.getName();
	}

	protected String computeColumnName(FieldDescriptor descriptor) {
		return CUSTOM_FIELD_PREFIX + descriptor.getName();
	}

	@Override
	public String getColumnName(FieldDescriptor descriptor) {
		return computeColumnName(descriptor);
	}

	private String getTableName(Class<?> entityType) {
		return entityType.getAnnotation(Table.class).name();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<FieldDescriptor> getCustomFields() {
		Query query = entityManager.createQuery("select e from " + Fielddef.class.getSimpleName() + " e where "
				+ "e.custom = true  order by e.sortkey, e.name");
		List<FieldDescriptor> results = new ArrayList<FieldDescriptor>(10);
		for (Fielddef fielddef : ((List<Fielddef>) query.getResultList())) {
			FieldDescriptor fieldDescriptor = createFieldDescriptor(fielddef);
			if (fieldDescriptor == null) {
				continue;
			}
			results.add(fieldDescriptor);
		}
		return results;
	}

	protected FieldDescriptor createFieldDescriptor(Fielddef fielddef) {
		FieldType fieldType = computeFieldType(fielddef);
		if (fieldType == null) {
			// ignore field types that are not supported
			return null;
		}
		FieldDescriptor fieldDescriptor = new FieldDescriptor();
		fieldDescriptor.setId(fielddef.getId());
		fieldDescriptor.setFieldType(fieldType);
		fieldDescriptor.setAvailableForNewTasks(fielddef.getEnterBug());
		fieldDescriptor.setObsolete(fielddef.getObsolete());
		fieldDescriptor.setDescription(fielddef.getDescription());
		fieldDescriptor.setName(fielddef.getName());
		if (fielddef.getCustom() && fielddef.getName().startsWith(CUSTOM_FIELD_PREFIX)) {
			fieldDescriptor.setName(computeFieldName(fielddef));
		}
		switch (fieldDescriptor.getFieldType()) {
		case SINGLE_SELECT:
		case MULTI_SELECT:
			try {
				fieldDescriptor.setValues(getFieldValues(fieldDescriptor));
			} catch (EntityNotFoundException e) {
				// should never happen
				throw new IllegalStateException(e);
			}
		}
		return fieldDescriptor;
	}

	private FieldType computeFieldType(Fielddef fielddef) {
		switch (fielddef.getType()) {
		case 1:
			return FieldType.TEXT;
		case 2:
			return FieldType.SINGLE_SELECT;
		case 3:
			return FieldType.MULTI_SELECT;
		case 4:
			return FieldType.LONG_TEXT;
		case 5:
			return FieldType.TIMESTAMP;
		case 6:
			return FieldType.TASK_REFERENCE;
		case 9:
			return FieldType.CHECKBOX;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> retrieveTaskCustomFields(Integer taskId) throws EntityNotFoundException {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		Query query = entityManager.createQuery("select e from " + Fielddef.class.getSimpleName() + " e where "
				+ "e.custom = true order by e.sortkey, e.name");

		String sql = "";
		List<String> fieldNames = new ArrayList<String>();
		List<Fielddef> multiSelectFields = null;
		List<Fielddef> allFieldDefs = (List<Fielddef>) query.getResultList();
		for (Fielddef fielddef : allFieldDefs) {

			FieldType fieldType = computeFieldType(fielddef);
			if (fieldType == FieldType.MULTI_SELECT) {
				if (multiSelectFields == null) {
					multiSelectFields = new ArrayList<Fielddef>(allFieldDefs.size());
				}
				multiSelectFields.add(fielddef);
				continue;
			}
			fieldNames.add(computeFieldName(fielddef));
			if (sql.length() > 0) {
				sql += ", ";
			}
			sql += sqlDialect.quoteIdentifier(fielddef.getName());
		}
		if (sql.length() > 0) {
			sql = String.format("select %s from %s where %s = %s",//
					sql,//
					sqlDialect.quoteIdentifier(getTableName(Task.class)), //
					sqlDialect.quoteIdentifier("bug_id"), //
					sqlDialect.literal(taskId));
			try {
				Object result = entityManager.createNativeQuery(sql).getSingleResult();
				if (fieldNames.size() == 1) {
					values.put(fieldNames.get(0), result);
				} else {
					Object[] fieldValues = (Object[]) result;
					for (int x = 0; x < fieldValues.length; ++x) {
						values.put(fieldNames.get(x), fieldValues[x]);
					}
				}
			} catch (NoResultException e) {
				throw new EntityNotFoundException();
			}
		}
		if (multiSelectFields != null) {
			for (Fielddef fielddef : multiSelectFields) {
				String tableName = MULTISELECT_FIELDVALUE_TABLE_PREFIX + fielddef.getName();
				sql = String.format("select %s from %s where %s = %s", //
						sqlDialect.quoteIdentifier("value"),//
						sqlDialect.quoteIdentifier(tableName),//
						sqlDialect.quoteIdentifier("bug_id"), //
						sqlDialect.literal(taskId));
				try {
					String customFieldName = computeFieldName(fielddef);
					fieldNames.add(customFieldName);

					List<String> fieldValues = entityManager.createNativeQuery(sql).getResultList();
					String csv = "";
					for (String fieldValue : fieldValues) {
						if (csv.length() > 0) {
							csv += ",";
						}
						csv += fieldValue;
					}
					values.put(customFieldName, csv);
				} catch (NoResultException e) {
					throw new EntityNotFoundException();

				}
			}
		}
		return values;
	}

	@Override
	public FieldDescriptor updateCustomField(FieldDescriptor customField) throws ValidationException,
			EntityNotFoundException {
		validate(customField);
		Fielddef fielddef = entityManager.find(Fielddef.class, customField.getId());
		if (fielddef == null) {
			throw new EntityNotFoundException();
		}
		fielddef.setDescription(customField.getDescription());
		fielddef.setEnterBug(customField.isAvailableForNewTasks());
		fielddef.setObsolete(customField.isObsolete());
		switch (computeFieldType(fielddef)) {
		case MULTI_SELECT:
		case SINGLE_SELECT:
			updateFieldValues(customField);
			break;
		}
		return createFieldDescriptor(fielddef);
	}

	/**
	 * @param customField
	 * @throws EntityNotFoundException
	 */
	private void updateFieldValues(FieldDescriptor customField) throws EntityNotFoundException {
		List<CustomFieldValue> oldValues = getFieldValues(customField);
		List<CustomFieldValue> valuesToRemove = new ArrayList<CustomFieldValue>(oldValues);
		List<CustomFieldValue> valuesToUpdate = new ArrayList<CustomFieldValue>();
		List<CustomFieldValue> valuesToCreate = new ArrayList<CustomFieldValue>();

		for (CustomFieldValue newValue : customField.getValues()) {
			if (newValue.getId() == null) {
				valuesToCreate.add(newValue);
			} else if (valuesToRemove.remove(newValue)) {
				valuesToUpdate.add(newValue);
			}
		}

		List<String> statements = new ArrayList<String>();

		for (CustomFieldValue toUpdate : valuesToUpdate) {
			statements.add(updateCustomFieldValue(customField, toUpdate));
		}
		for (CustomFieldValue toCreate : valuesToCreate) {
			statements.add(insertCustomFieldValue(customField, toCreate));
		}
		for (CustomFieldValue toRemove : valuesToRemove) {
			statements.add(removeCustomFieldValue(customField, toRemove));
		}

		executeUpdate(statements);
	}
}
