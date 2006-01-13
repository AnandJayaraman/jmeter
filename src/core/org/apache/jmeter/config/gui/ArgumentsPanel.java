/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.jmeter.config.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

/**
 * A GUI panel allowing the user to enter name-value argument pairs. These
 * arguments (or parameters) are usually used to provide configuration values
 * for some other component.
 * 
 * @version $Revision$ on $Date$
 */
public class ArgumentsPanel extends AbstractConfigGui implements ActionListener {

	/** The title label for this component. */
	private JLabel tableLabel;

	/** The table containing the list of arguments. */
	private transient JTable table;

	/** The model for the arguments table. */
	protected transient ObjectTableModel tableModel;

	/** A button for adding new arguments to the table. */
	private JButton add;

	/** A button for removing arguments from the table. */
	private JButton delete;

    /**
     * Added background support for reporting tool
     */
    private Color background;
    
	/**
	 * Boolean indicating whether this component is a standalong component or it
	 * is intended to be used as a subpanel for another component.
	 */
	private boolean standalone = true;

	/** Command for adding a row to the table. */
	private static final String ADD = "add";

	/** Command for removing a row from the table. */
	private static final String DELETE = "delete";

	public static final String COLUMN_NAMES_0 = JMeterUtils.getResString("name");

	public static final String COLUMN_NAMES_1 = JMeterUtils.getResString("value");

	// NOTUSED private static final String COLUMN_NAMES_2 =
	// JMeterUtils.getResString("metadata");

	/**
	 * Create a new ArgumentsPanel as a standalone component.
	 */
	public ArgumentsPanel() {
		tableLabel = new JLabel(JMeterUtils.getResString("user_defined_variables"));
		standalone = true;
		init();
	}

	/**
	 * Create a new ArgumentsPanel as an embedded component, using the specified
	 * title.
	 * 
	 * @param label
	 *            the title for the component.
	 */
	public ArgumentsPanel(String label) {
		tableLabel = new JLabel(label);
		standalone = false;
		init();
	}

    /**
     * Create a new ArgumentsPanel with a border and color background
     * @param label
     * @param borderHeight
     * @param borderWidth
     */
    public ArgumentsPanel(String label, Color bkg) {
        tableLabel = new JLabel(label);
        this.background = bkg;
        standalone = false;
        init();
    }
    
	/**
	 * This is the list of menu categories this gui component will be available
	 * under.
	 * 
	 * @return a Collection of Strings, where each element is one of the
	 *         constants defined in MenuFactory
	 */
	public Collection getMenuCategories() {
		if (standalone) {
			return super.getMenuCategories();
		} else {
			return null;
		}
	}

	public String getLabelResource() {
		return "user_defined_variables";
	}

	/* Implements JMeterGUIComponent.createTestElement() */
	public TestElement createTestElement() {
		Arguments args = new Arguments();
		modifyTestElement(args);
		return args;
	}

	/* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
	public void modifyTestElement(TestElement args) {
		stopTableEditing();
		Iterator modelData = tableModel.iterator();
		Arguments arguments = null;
		if (args instanceof Arguments) {
			arguments = (Arguments) args;
			arguments.clear();
			while (modelData.hasNext()) {
				Argument arg = (Argument) modelData.next();
				arg.setMetaData("=");
				arguments.addArgument(arg);
			}
		}
		this.configureTestElement(args);
	}

	/**
	 * A newly created component can be initialized with the contents of a Test
	 * Element object by calling this method. The component is responsible for
	 * querying the Test Element object for the relevant information to display
	 * in its GUI.
	 * 
	 * @param el
	 *            the TestElement to configure
	 */
	public void configure(TestElement el) {
		super.configure(el);
		if (el instanceof Arguments) {
			tableModel.clearData();
			PropertyIterator iter = ((Arguments) el).iterator();
			while (iter.hasNext()) {
				Argument arg = (Argument) iter.next().getObjectValue();
				tableModel.addRow(arg);
			}
		}
		checkDeleteStatus();
	}

	/**
	 * Get the table used to enter arguments.
	 * 
	 * @return the table used to enter arguments
	 */
	protected JTable getTable() {
		return table;
	}

	/**
	 * Get the title label for this component.
	 * 
	 * @return the title label displayed with the table
	 */
	protected JLabel getTableLabel() {
		return tableLabel;
	}

	/**
	 * Get the button used to delete rows from the table.
	 * 
	 * @return the button used to delete rows from the table
	 */
	protected JButton getDeleteButton() {
		return delete;
	}

	/**
	 * Get the button used to add rows to the table.
	 * 
	 * @return the button used to add rows to the table
	 */
	protected JButton getAddButton() {
		return add;
	}

	/**
	 * Enable or disable the delete button depending on whether or not there is
	 * a row to be deleted.
	 */
	protected void checkDeleteStatus() {
		// Disable DELETE if there are no rows in the table to delete.
		if (tableModel.getRowCount() == 0) {
			delete.setEnabled(false);
		} else {
			delete.setEnabled(true);
		}
	}

	/**
	 * Clear all rows from the table. T.Elanjchezhiyan(chezhiyan@siptech.co.in)
	 */
	public void clear() {
		stopTableEditing();
		tableModel.clearData();
	}

	/**
	 * Invoked when an action occurs. This implementation supports the add and
	 * delete buttons.
	 * 
	 * @param e
	 *            the event that has occurred
	 */
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equals(DELETE)) {
			deleteArgument();
		} else if (action.equals(ADD)) {
			addArgument();
		}
	}

	/**
	 * Remove the currently selected argument from the table.
	 */
	protected void deleteArgument() {
		// If a table cell is being edited, we must cancel the editing before
		// deleting the row
		if (table.isEditing()) {
			TableCellEditor cellEditor = table.getCellEditor(table.getEditingRow(), table.getEditingColumn());
			cellEditor.cancelCellEditing();
		}

		int rowSelected = table.getSelectedRow();
		if (rowSelected >= 0) {
			tableModel.removeRow(rowSelected);
			tableModel.fireTableDataChanged();

			// Disable DELETE if there are no rows in the table to delete.
			if (tableModel.getRowCount() == 0) {
				delete.setEnabled(false);
			}

			// Table still contains one or more rows, so highlight (select)
			// the appropriate one.
			else {
				int rowToSelect = rowSelected;

				if (rowSelected >= tableModel.getRowCount()) {
					rowToSelect = rowSelected - 1;
				}

				table.setRowSelectionInterval(rowToSelect, rowToSelect);
			}
		}
	}

	/**
	 * Add a new argument row to the table.
	 */
	protected void addArgument() {
		// If a table cell is being edited, we should accept the current value
		// and stop the editing before adding a new row.
		stopTableEditing();

		tableModel.addRow(makeNewArgument());

		// Enable DELETE (which may already be enabled, but it won't hurt)
		delete.setEnabled(true);

		// Highlight (select) the appropriate row.
		int rowToSelect = tableModel.getRowCount() - 1;
		table.setRowSelectionInterval(rowToSelect, rowToSelect);
	}

	/**
	 * Create a new Argument object.
	 * 
	 * @return a new Argument object
	 */
	protected Object makeNewArgument() {
		return new Argument("", "");
	}

	/**
	 * Stop any editing that is currently being done on the table. This will
	 * save any changes that have already been made.
	 */
	protected void stopTableEditing() {
		if (table.isEditing()) {
			TableCellEditor cellEditor = table.getCellEditor(table.getEditingRow(), table.getEditingColumn());
			cellEditor.stopCellEditing();
		}
	}

	/**
	 * Initialize the table model used for the arguments table.
	 */
	protected void initializeTableModel() {
		tableModel = new ObjectTableModel(new String[] { COLUMN_NAMES_0, COLUMN_NAMES_1 }, new Functor[] {
				new Functor("getName"), new Functor("getValue") }, new Functor[] { new Functor("setName"),
				new Functor("setValue") }, new Class[] { String.class, String.class });
	}

	/**
	 * Resize the table columns to appropriate widths.
	 * 
	 * @param table
	 *            the table to resize columns for
	 */
	protected void sizeColumns(JTable _table) {
	}

	/**
	 * Create the main GUI panel which contains the argument table.
	 * 
	 * @return the main GUI panel
	 */
	private Component makeMainPanel() {
		initializeTableModel();
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (this.background != null) {
            table.setBackground(this.background);
        }
		return makeScrollPane(table);
	}

	/**
	 * Create a panel containing the title label for the table.
	 * 
	 * @return a panel containing the title label
	 */
	protected Component makeLabelPanel() {
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		labelPanel.add(tableLabel);
        if (this.background != null) {
            labelPanel.setBackground(this.background);
        }
		return labelPanel;
	}

	/**
	 * Create a panel containing the add and delete buttons.
	 * 
	 * @return a GUI panel containing the buttons
	 */
	private JPanel makeButtonPanel() {
		add = new JButton(JMeterUtils.getResString("add"));
		add.setActionCommand(ADD);
		add.setEnabled(true);

		delete = new JButton(JMeterUtils.getResString("delete"));
		delete.setActionCommand(DELETE);

		checkDeleteStatus();

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        if (this.background != null) {
            buttonPanel.setBackground(this.background);
        }
		add.addActionListener(this);
		delete.addActionListener(this);
		buttonPanel.add(add);
		buttonPanel.add(delete);
		return buttonPanel;
	}

	/**
	 * Initialize the components and layout of this component.
	 */
	private void init() {
		JPanel p = this;

		if (standalone) {
			setLayout(new BorderLayout(0, 5));
			setBorder(makeBorder());
			add(makeTitlePanel(), BorderLayout.NORTH);
			p = new JPanel();
		}

		p.setLayout(new BorderLayout());

		p.add(makeLabelPanel(), BorderLayout.NORTH);
		p.add(makeMainPanel(), BorderLayout.CENTER);
		// Force a minimum table height of 70 pixels
		p.add(Box.createVerticalStrut(70), BorderLayout.WEST);
		p.add(makeButtonPanel(), BorderLayout.SOUTH);

		if (standalone) {
			add(p, BorderLayout.CENTER);
		}

		table.revalidate();
		sizeColumns(table);
	}
}
