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
package com.tasktop.c2c.server.tasks.client.widgets.admin.customfields;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import com.tasktop.c2c.server.common.web.client.view.CellTableResources;
import com.tasktop.c2c.server.profile.web.client.CustomActionCell;
import com.tasktop.c2c.server.tasks.domain.CustomFieldValue;

/**
 * @author cmorgan (Tasktop Technologies Inc.)
 * 
 */
public class CustomFieldValuesEditor extends Composite implements LeafValueEditor<List<CustomFieldValue>> {

	private CellTableResources resources = CellTableResources.get.resources;

	private CellTable<CustomFieldValue> cellTable = new CellTable<CustomFieldValue>(1000, resources);
	private ListDataProvider<CustomFieldValue> dataProvider = new ListDataProvider<CustomFieldValue>();

	// protected ListEditor<CustomFieldValue, CustomFieldValueEditor> customFieldsEditor;

	public CustomFieldValuesEditor() {
		initWidget(cellTable);
		initTableColumns();
		cellTable.setPageSize(1000);
		cellTable.setSelectionModel(new NoSelectionModel<CustomFieldValue>());
		dataProvider.addDataDisplay(cellTable);
	}

	private void initTableColumns() {

		CustomActionCell<String> moveUpCell = new CustomActionCell<String>(
				new CustomActionCell.TemplateDelegate<String>() {
					@Override
					public SafeHtml getHtml(Cell.Context context, String value, SafeHtmlBuilder sb) {
						return SafeHtmlUtils.fromSafeConstant("<span class=\"order-control\"><a class=\"up\"/></span>");
					}
				}, new CustomActionCell.ActionDelegate<String>() {
					@Override
					public void execute(Cell.Context object) {
						int index = object.getIndex();
						if (index > 0) {
							dataProvider.getList().get(index).setSortkey((short) (index - 1));
							dataProvider.getList().get(index - 1).setSortkey((short) index);
							Collections.sort(dataProvider.getList());
							refreshDisplays();
						}
					}
				});

		Column<CustomFieldValue, String> moveUpColumn = new Column<CustomFieldValue, String>(moveUpCell) {

			@Override
			public String getValue(CustomFieldValue object) {
				return null;
			}

			@Override
			public void render(Cell.Context context, CustomFieldValue object, SafeHtmlBuilder sb) {
				if (context.getIndex() != 0) {
					super.render(context, object, sb);
				}
			}
		};
		cellTable.addColumn(moveUpColumn);
		cellTable.setColumnWidth(moveUpColumn, 22, Unit.PX);

		CustomActionCell<String> moveDownCell = new CustomActionCell<String>(
				new CustomActionCell.TemplateDelegate<String>() {
					@Override
					public SafeHtml getHtml(Cell.Context context, String value, SafeHtmlBuilder sb) {
						return SafeHtmlUtils.fromSafeConstant("<span class=\"order-control\"><a class=\"down\"/></span>");
					}
				}, new CustomActionCell.ActionDelegate<String>() {
					@Override
					public void execute(Cell.Context object) {
						Short index = (short) object.getIndex();
						if (index < dataProvider.getList().size() - 1) {
							dataProvider.getList().get(index).setSortkey((short) (index + 1));
							dataProvider.getList().get(index + 1).setSortkey((short) index);
							Collections.sort(dataProvider.getList());
							refreshDisplays();
						}
					}
				});

		Column<CustomFieldValue, String> moveDownColumn = new Column<CustomFieldValue, String>(moveDownCell) {

			@Override
			public String getValue(CustomFieldValue object) {
				return null;
			}

			@Override
			public void render(Cell.Context context, CustomFieldValue object, SafeHtmlBuilder sb) {
				if (context.getIndex() != dataProvider.getList().size() - 1) {
					super.render(context, object, sb);
				}
			}
		};
		cellTable.addColumn(moveDownColumn);
		cellTable.setColumnWidth(moveDownColumn, 22, Unit.PX);

		Column<CustomFieldValue, String> nameColumn = new Column<CustomFieldValue, String>(new TextInputCell()) {

			@Override
			public String getValue(CustomFieldValue value) {
				return value.getValue();
			}
		};
		nameColumn.setFieldUpdater(new FieldUpdater<CustomFieldValue, String>() {

			@Override
			public void update(int index, CustomFieldValue customFieldValue, String value) {

				customFieldValue.setValue(value);
			}
		});
		cellTable.addColumn(nameColumn);

		// Column<CustomFieldValue, Boolean> activeColumn = new Column<CustomFieldValue, Boolean>(new CheckboxCell()) {
		//
		// @Override
		// public Boolean getValue(CustomFieldValue value) {
		// return value.getIsActive();
		// }
		// };
		// activeColumn.setFieldUpdater(new FieldUpdater<CustomFieldValue, Boolean>() {
		//
		// @Override
		// public void update(int index, CustomFieldValue customFieldValue, Boolean value) {
		// customFieldValue.setIsActive(value);
		// }
		// });
		//
		// cellTable.addColumn(activeColumn, "Active");

		CustomActionCell<String> removeCell = new CustomActionCell<String>(
				new CustomActionCell.TemplateDelegate<String>() {
					@Override
					public SafeHtml getHtml(Cell.Context context, String value, SafeHtmlBuilder sb) {
						final CustomFieldValue referenced = dataProvider.getList().get(context.getIndex());
						if (referenced != null && "---".equals(referenced.getValue())) {
							return SafeHtmlUtils.fromSafeConstant("<a class=\"delete-disabled\"><span/></a>");
						}
						return SafeHtmlUtils.fromSafeConstant("<a class=\"misc-icon cancel\"><span/></a>");
					}
				}, new CustomActionCell.ActionDelegate<String>() {
					@Override
					public void execute(final Cell.Context object) {
						final CustomFieldValue toRemove = dataProvider.getList().get(object.getIndex());
						if (toRemove == null) {
							return;
						}
						if ("---".equals(toRemove.getValue())) {
							return;
						}
						dataProvider.getList().remove(toRemove);

					}
				});
		Column<CustomFieldValue, String> removeColumn = new Column<CustomFieldValue, String>(removeCell) {

			@Override
			public String getValue(CustomFieldValue object) {
				return null;
			}

			@Override
			public void render(Cell.Context context, CustomFieldValue object, SafeHtmlBuilder sb) {
				if (dataProvider.getList().size() == 1) {
					sb.appendHtmlConstant("<a><span/></a>");
				} else {
					super.render(context, object, sb);
				}
			}
		};
		cellTable.addColumn(removeColumn);
		cellTable.setColumnWidth(removeColumn, 30, Unit.PX);

		CustomActionCell<String> addCell = new CustomActionCell<String>(
				new CustomActionCell.TemplateDelegate<String>() {
					@Override
					public SafeHtml getHtml(Cell.Context context, String value, SafeHtmlBuilder sb) {
						return SafeHtmlUtils.fromSafeConstant("<a class=\"misc-icon add right\"><span/></a>");
					}
				}, new CustomActionCell.ActionDelegate<String>() {
					@Override
					public void execute(Cell.Context object) {
						addNewValue();
					}
				});
		Column<CustomFieldValue, String> addColumn = new Column<CustomFieldValue, String>(addCell) {
			@Override
			public String getValue(CustomFieldValue object) {
				return null;
			}

			@Override
			public void render(Cell.Context context, CustomFieldValue object, SafeHtmlBuilder sb) {
				if (context.getIndex() == dataProvider.getList().size() - 1) {
					super.render(context, object, sb);
				} else {
					sb.appendHtmlConstant("<a><span/></a>");
				}
			}
		};
		cellTable.addColumn(addColumn);
		cellTable.setColumnWidth(addColumn, 30, Unit.PX);
	}

	private void refreshDisplays() {
		dataProvider.refresh();
	}

	@Override
	public void setValue(List<CustomFieldValue> values) {
		if (values == null) {
			values = new ArrayList<CustomFieldValue>();
		}
		dataProvider.setList(values);
		refreshDisplays();
	}

	@Override
	public List<CustomFieldValue> getValue() {
		List<CustomFieldValue> result = new ArrayList<CustomFieldValue>(dataProvider.getList());
		short i = 0;
		for (CustomFieldValue value : result) {
			value.setSortkey(i++);
		}
		return result;
	}

	protected void addNewValue() {
		dataProvider.getList().add(getNewValue((short) dataProvider.getList().size()));
		refreshDisplays();
	}

	protected CustomFieldValue getNewValue(short sortKey) {
		CustomFieldValue newValue = new CustomFieldValue();
		newValue.setValue("");
		newValue.setIsActive(true);
		newValue.setSortkey(sortKey);
		return newValue;
	}

}
