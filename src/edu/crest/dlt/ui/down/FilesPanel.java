/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.crest.dlt.ui.down;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import edu.crest.dlt.ui.utils.TableHeaderRenderer;
import edu.crest.dlt.ui.utils.TableRowStatusRenderer;
import edu.crest.dlt.ui.utils.img.Icons;
import edu.crest.dlt.utils.Status;
import edu.crest.dlt.utils.Status.ui_status;

public class FilesPanel extends javax.swing.JPanel
{
	private static final Logger log = Logger.getLogger(FilesPanel.class.getClass().getName());

	/**
	 * Creates new form FilesPanel
	 */
	public FilesPanel()
	{
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents()
	{

		scrollpane_files = new javax.swing.JScrollPane();
		table_files = new javax.swing.JTable()
		{
			// Implement table cell tool tips.
			public String getToolTipText(MouseEvent e)
			{
				String tip = null;
				java.awt.Point p = e.getPoint();
				int row = rowAtPoint(p);
				int column = columnAtPoint(p);

				try {
					ui_status status_file = status_map.get(row);
					tip = Status.message(status_file);
				} catch (Exception e1) {
				}

				return tip;
			}
		};

		setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Files",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

		table_files.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

		}, new String[] { "[]", "File(s)", "Status" })
		{
			Class[] types = new Class[] { java.lang.Boolean.class, java.lang.String.class,
					java.lang.Object.class };
			boolean[] canEdit = new boolean[] { true, false, false };

			public Class getColumnClass(int columnIndex)
			{
				return types[columnIndex];
			}

			public boolean isCellEditable(int rowIndex, int columnIndex)
			{
				return canEdit[columnIndex];
			}
		});
		table_files.setColumnSelectionAllowed(true);
		table_files.setFillsViewportHeight(true);
		table_files.getTableHeader().setReorderingAllowed(false);
		scrollpane_files.setViewportView(table_files);
		table_files.getTableHeader().getColumnModel().getColumn(column_index_checkbox)
				.setHeaderRenderer(TableHeaderRenderer.header_checkbox);
		table_files.getTableHeader().getColumnModel().getColumn(column_index_file)
				.setHeaderRenderer(TableHeaderRenderer.header_file);
		table_files.getTableHeader().getColumnModel().getColumn(column_index_status)
				.setHeaderRenderer(TableHeaderRenderer.header_status);
		table_files.getColumnModel().getSelectionModel()
				.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		if (table_files.getColumnModel().getColumnCount() > 0) {
			table_files.getColumnModel().getColumn(0).setMinWidth(20);
			table_files.getColumnModel().getColumn(0).setPreferredWidth(20);
			table_files.getColumnModel().getColumn(0).setMaxWidth(20);
			table_files.getColumnModel().getColumn(1).setResizable(false);
			table_files.getColumnModel().getColumn(2).setMinWidth(50);
			table_files.getColumnModel().getColumn(2).setPreferredWidth(50);
			table_files.getColumnModel().getColumn(2).setMaxWidth(50);
			table_files.getColumnModel().getColumn(2).setCellRenderer(new TableRowStatusRenderer());
		}

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout
				.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addComponent(scrollpane_files, javax.swing.GroupLayout.DEFAULT_SIZE, 488,
								Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(scrollpane_files, javax.swing.GroupLayout.Alignment.TRAILING,
						javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE));
	}// </editor-fold>//GEN-END:initComponents

	public void add_file(String filename, ui_status status)
	{
		DefaultTableModel table_files_model = (DefaultTableModel) table_files.getModel();
		table_files_model.addRow(new Object[] { false, filename,
				status == ui_status.processing ? Icons.icon_processing(this) : Status.icon(status) });
		status_map.put(table_files_model.getRowCount() - 1, status);
	}

	public void add_files(List<String> files, ui_status status)
	{
		for (String file : files) {
			add_file(file, status);
		}
	}

	public int index_file(String filename)
	{
		TableModel table_files_model = table_files.getModel();
		if (filename != null) {
			for (int row = 0; row < table_files_model.getRowCount(); row++) {
				if (filename.equals(table_files_model.getValueAt(row, column_index_file))) {
					return row;
				}
			}
		}
		return -1;
	}

	public ui_status status_file(String filename)
	{
		int index_file = index_file(filename);
		if (index_file == -1) {
			return null;
		}
		// TableModel table_files_model = table_files.getModel();
		try {
			// return (ui_status) table_files_model.getValueAt(index_file,
			// column_index_status);
			return status_map.get(index_file);
		} catch (Exception e) {
			return null;
		}
	}

	public void status_file(String filename, ui_status status)
	{
		int row_file = index_file(filename);
		if (row_file != -1) {
			TableModel table_files_model = table_files.getModel();
			table_files_model.setValueAt(status == ui_status.processing ? Icons.icon_processing(this)
					: Status.icon(status), row_file, column_index_status);
			status_map.put(row_file, status);
		}
	}

	public List<String> files_selected()
	{
		List<String> files_selected = new ArrayList<String>();
		TableModel table_files_model = table_files.getModel();
		for (int row = 0; row < table_files_model.getRowCount(); row++) {
			if (Boolean.TRUE.equals(table_files_model.getValueAt(row, column_index_checkbox))) {
				files_selected.add((String) table_files_model.getValueAt(row, column_index_file));
				log.info((String) table_files_model.getValueAt(row, column_index_file) + " is selected.");
			}
		}
		return files_selected;
	}

	public void select_file(String filename)
	{
		if (filename == null) {
			return;
		}

		TableModel table_files_model = table_files.getModel();
		for (int row = 0; row < table_files_model.getRowCount(); row++) {
			if (!filename.equals("all")) {
				String filename_i = (String) table_files_model.getValueAt(row, column_index_file);
				if (!filename.equals(filename_i)) {
					continue;
				}
			}

			if (Boolean.FALSE.equals(table_files_model.getValueAt(row, column_index_checkbox))) {
				table_files_model.setValueAt(true, row, column_index_checkbox);
				log.info((String) table_files_model.getValueAt(row, column_index_file) + " selected.");
				break;
			}
		}
	}

	public void select_files(List<String> filenames)
	{
		if (filenames == null || filenames.size() == 0) {
			return;
		}

		TableModel table_files_model = table_files.getModel();
		for (int row = 0; row < table_files_model.getRowCount(); row++) {
			String filename_i = (String) table_files_model.getValueAt(row, column_index_file);
			if (!filenames.contains(filename_i)) {
				continue;
			}

			if (Boolean.FALSE.equals(table_files_model.getValueAt(row, column_index_checkbox))) {
				table_files_model.setValueAt(false, row, column_index_checkbox);
				log.info((String) table_files_model.getValueAt(row, column_index_file) + " deselected.");
			}
		}
	}

	public void select_files_all()
	{
		select_file("all");
	}

	public void select_files_selected()
	{
		select_files(files_selected());
	}

	public void deselect_file(String filename)
	{
		if (filename == null) {
			return;
		}

		TableModel table_files_model = table_files.getModel();
		for (int row = 0; row < table_files_model.getRowCount(); row++) {
			if (!filename.equals("all")) {
				String filename_i = (String) table_files_model.getValueAt(row, column_index_file);
				if (!filename.equals(filename_i)) {
					continue;
				}
			}

			if (Boolean.TRUE.equals(table_files_model.getValueAt(row, column_index_checkbox))) {
				table_files_model.setValueAt(false, row, column_index_checkbox);
				log.info((String) table_files_model.getValueAt(row, column_index_file) + " deselected.");
				break;
			}
		}
	}

	public void deselect_files(List<String> filenames)
	{
		if (filenames == null || filenames.size() == 0) {
			return;
		}

		TableModel table_files_model = table_files.getModel();
		for (int row = 0; row < table_files_model.getRowCount(); row++) {
			String filename_i = (String) table_files_model.getValueAt(row, column_index_file);
			if (!filenames.contains(filename_i)) {
				continue;
			}

			if (Boolean.TRUE.equals(table_files_model.getValueAt(row, column_index_checkbox))) {
				table_files_model.setValueAt(false, row, column_index_checkbox);
				log.info((String) table_files_model.getValueAt(row, column_index_file) + " deselected.");
			}
		}
	}

	public void deselect_files_all()
	{
		deselect_file("all");
	}

	public void deselect_files_selected()
	{
		deselect_files(files_selected());
	}

	public void remove_file(String filename)
	{
		if (filename == null) {
			return;
		}

		TableModel table_files_model = table_files.getModel();
		DefaultTableModel table_files_model_default = (DefaultTableModel) table_files_model;

		if (filename.equals("all")) {
			table_files_model_default.setRowCount(0);
		}

		for (int row = 0; row < table_files_model.getRowCount(); row++) {
			if (!filename.equals("all")) {
				String filename_i = (String) table_files_model.getValueAt(row, column_index_file);
				if (!filename.equals(filename_i)) {
					continue;
				}
			}

			if (filename.equals(table_files_model.getValueAt(row, column_index_file))) {
				log.info((String) table_files_model.getValueAt(row, column_index_file) + " removed.");
				table_files_model_default.removeRow(row);
				break;
			}
		}
	}

	public void remove_files_all()
	{
		remove_file("all");
	}

	public void remove_files_selected()
	{
		for (String file_selected : files_selected()) {
			remove_file(file_selected);
		}
	}

	public List<String> files()
	{
		List<String> files_published = new ArrayList<String>();
		TableModel table_files_model = table_files.getModel();
		for (int row = 0; row < table_files_model.getRowCount(); row++) {
			files_published.add((String) table_files_model.getValueAt(row, column_index_file));
			log.info((String) table_files_model.getValueAt(row, column_index_file) + " is published.");
		}
		return files_published;
	}

	private static final int column_index_checkbox = 0;
	private static final int column_index_file = 1;
	private static final int column_index_status = 2;

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JScrollPane scrollpane_files;
	public javax.swing.JTable table_files;
	// End of variables declaration//GEN-END:variables

	private Map<Integer, ui_status> status_map = new HashMap<Integer, Status.ui_status>();
}
