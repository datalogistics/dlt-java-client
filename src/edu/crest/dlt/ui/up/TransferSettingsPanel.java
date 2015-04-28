/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.crest.dlt.ui.up;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SpinnerListModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import edu.crest.dlt.ibp.Depot;
import edu.crest.dlt.ibp.DepotLocatorLbone;
import edu.crest.dlt.ui.utils.img.Icons;
import edu.crest.dlt.utils.Configuration;

public class TransferSettingsPanel extends javax.swing.JPanel
{
	private static final Logger log = Logger.getLogger(TransferSettingsPanel.class.getClass()
			.getName());

	/**
	 * Creates new form TransferSettingsPanel
	 */
	public TransferSettingsPanel()
	{
		initComponents();

		new Thread(
				() -> {
					for (DepotLocatorLbone lbone_server : Configuration.dlt_depot_locators_lbone) {
						/* fetch location-wise depots */
						Configuration.dlt_depot_locations
								.forEach((location) -> {
									for (int count_depots = 10; count_depots > 0; count_depots -= 2) {
										try {
											if (lbone_server.depots(10, 1, 0, 2 * 24 * 60 * 60, location).size() >= count_depots - 1) {
												break; // break when the maximum number of depots are
																// obtained
											}
										} catch (Exception e) {
										}
									}
								});

						/* fetch location irrelevant depots */
						for (int count_depots = 10; count_depots > 0; count_depots -= 2) {
							try {
								if (lbone_server.depots(10, 1, 0, 2 * 24 * 60 * 60, null).size() >= count_depots - 1) {
									break; // break when the maximum number of depots are obtained
								}
							} catch (Exception e) {
							}
						}
					}
				}).start();

		new Thread(() -> {
			int count_notifications = 0;
			while (true) {
				synchronized (Depot.depots) {
					try {
						/* wait for the first depot to notify */
						Depot.depots.wait();

						/* discourage user from attempting to reconnect */
						button_reconnect.setEnabled(false);

						/* try to gather as many notifications as possible ... */
						while (++count_notifications < Depot.depots().size()) {
							button_reconnect.setText("Reconnect ("
									+ (Depot.depots().size() - count_notifications) + ")");
							try {
								/* ... for a maximum of connect-timeout */
								Depot.depots.wait(Configuration.dlt_depot_connect_timeout);
								break;
							} catch (InterruptedException e) {
								continue;
							}
						}

						/* display depots and their status */
						sync_depots();
					} catch (InterruptedException e) {
					}
				}
				button_reconnect.setEnabled(true);
				button_reconnect.setText("Reconnect");
			}
		}).start();
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
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents()
	{

		label_connection_type = new javax.swing.JLabel();
		input_connection_type = new javax.swing.JSpinner();
		label_block_size = new javax.swing.JLabel();
		input_block_size = new javax.swing.JComboBox<Integer>();
		input_block_unit = new javax.swing.JSpinner();
		label_connection_count = new javax.swing.JLabel();
		input_connection_count = new javax.swing.JComboBox<Integer>();
		button_reconnect = new javax.swing.JButton();
		label_copies_count = new javax.swing.JLabel();
		input_copies_count = new javax.swing.JComboBox<Integer>();
		label_duration = new javax.swing.JLabel();
		input_time_days = new javax.swing.JComboBox<Integer>();
		label_duration_days = new javax.swing.JLabel();
		input_time_hours = new javax.swing.JComboBox<Integer>();
		label_duration_hours = new javax.swing.JLabel();
		input_depots_all = new javax.swing.JCheckBox();
		label_depots = new javax.swing.JLabel();
		input_location = new javax.swing.JSpinner(new SpinnerListModel(
				Configuration.dlt_depot_locations.toArray()));
		button_add_depot = new javax.swing.JLabel();
		scrollpane_depots = new javax.swing.JScrollPane();
		table_depots = new javax.swing.JTable()
		{
			// Implement table cell tool tips.
			public String getToolTipText(MouseEvent e)
			{
				String tip = null;
				java.awt.Point p = e.getPoint();
				int row = rowAtPoint(p);
				int column = columnAtPoint(p);

				try {
					Depot depot = (Depot) getValueAt(row, column_index_depot);
					tip = depot.connected() ? "connected" : "connection failed (try to refresh)";
				} catch (RuntimeException e1) {
					// catch null pointer exception if mouse is over an empty line
				}

				return tip;
			}
		};

		setBorder(javax.swing.BorderFactory.createTitledBorder(null, "File Transfer Settings",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
				javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

		label_connection_type.setText("Connection Type");

		input_connection_type.setModel(new javax.swing.SpinnerListModel(new String[] { "Dial-Up/ISDN",
				"DSL/Cable/T1", "< 100 Mbps", "> 100 Mbps" }));
		input_connection_type.setToolTipText("select the type of connection");
		input_connection_type.setEditor(new javax.swing.JSpinner.ListEditor(input_connection_type));
		input_connection_type.addChangeListener(new javax.swing.event.ChangeListener()
		{
			public void stateChanged(javax.swing.event.ChangeEvent evt)
			{
				input_connection_type_changed(evt);
			}
		});

		label_block_size.setText("Block Size");

		input_block_size.setEditable(true);
		input_block_size.setMaximumRowCount(15);
		input_block_size.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "4",
				"5", "8", "10", "16", "32", "64", "128", "256", "512", "1024" }));
		input_block_size.setSelectedIndex(11);
		input_block_size.setToolTipText("select block size in which to perform the transfer");
		input_block_size.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				input_block_size_changed(evt);
			}
		});

		input_block_unit.setModel(new javax.swing.SpinnerListModel(new String[] { "KB", "MB" }));
		input_block_unit.setToolTipText("specify block unit (KB=kilobytes; MB=megabytes)");
		input_block_unit.addChangeListener(new javax.swing.event.ChangeListener()
		{
			public void stateChanged(javax.swing.event.ChangeEvent evt)
			{
				input_block_unit_changed(evt);
			}
		});

		label_connection_count.setText("# of Connections");

		input_connection_count.setEditable(true);
		input_connection_count.setMaximumRowCount(10);
		input_connection_count.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2",
				"3", "4", "5", "6", "7", "8", "9", "10" }));
		input_connection_count.setToolTipText("select number of connections");

		button_reconnect.setIcon(Icons.icon_refresh);
		button_reconnect.setText("Reconnect");
		button_reconnect.addMouseListener(new java.awt.event.MouseAdapter()
		{
			public void mouseClicked(java.awt.event.MouseEvent evt)
			{
				button_reconnect_clicked(evt);
			}
		});

		label_copies_count.setText("# of Copies");

		input_copies_count.setEditable(true);
		input_copies_count.setMaximumRowCount(10);
		input_copies_count.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3",
				"4", "5" }));
		input_copies_count.setToolTipText("select number of copies/replicas to create");

		label_duration.setText("Duration");

		input_time_days.setEditable(true);
		input_time_days.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2",
				"3", "4", "5", "6", "7" }));
		input_time_days.setSelectedIndex(2);
		input_time_days.setToolTipText("specify file lifetime (in days)");

		label_duration_days.setText("day(s)");

		input_time_hours.setEditable(true);
		input_time_hours.setMaximumRowCount(15);
		input_time_hours.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2",
				"3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18",
				"19", "20", "21", "22", "23" }));
		input_time_hours.setToolTipText("specify file lifetime (in hours)");

		label_duration_hours.setText("hr");

		input_depots_all.setText("all");
		input_depots_all.setToolTipText("select all available depots");
		input_depots_all.addActionListener(new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				input_depots_all_clicked(evt);
			}
		});

		label_depots.setText("Depots");

		input_location.setModel(new javax.swing.SpinnerListModel(new String[] { "AL", "IN" }));
		input_location.setToolTipText("select target depots by location");
		input_location.addChangeListener(new javax.swing.event.ChangeListener()
		{
			public void stateChanged(javax.swing.event.ChangeEvent evt)
			{
				input_location_changed(evt);
			}
		});

		button_add_depot.setForeground(new java.awt.Color(255, 0, 0));
		button_add_depot.setText("+");
		button_add_depot.setToolTipText("add a depot (host:port)");
		button_add_depot.addMouseListener(new java.awt.event.MouseAdapter()
		{
			public void mouseClicked(java.awt.event.MouseEvent evt)
			{
				button_add_depot_clicked(evt);
			}
		});

		table_depots.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

		}, new String[] { "[]", "Depot(s)", "Status" })
		{
			Class[] types = new Class[] { java.lang.Boolean.class, java.lang.Object.class,
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
		table_depots.setColumnSelectionAllowed(true);
		table_depots.setFillsViewportHeight(true);
		table_depots.getTableHeader().setReorderingAllowed(false);
		scrollpane_depots.setViewportView(table_depots);
		table_depots.getColumnModel().getSelectionModel()
				.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		if (table_depots.getColumnModel().getColumnCount() > 0) {
			table_depots.getColumnModel().getColumn(0).setMinWidth(20);
			table_depots.getColumnModel().getColumn(0).setPreferredWidth(20);
			table_depots.getColumnModel().getColumn(0).setMaxWidth(20);
			table_depots.getColumnModel().getColumn(2).setMinWidth(50);
			table_depots.getColumnModel().getColumn(2).setPreferredWidth(50);
			table_depots.getColumnModel().getColumn(2).setMaxWidth(50);
			table_depots.getColumnModel().getColumn(2).setCellRenderer(new ConnectionStatusRenderer());
		}

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout
				.setHorizontalGroup(layout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								layout
										.createSequentialGroup()
										.addGroup(
												layout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																layout
																		.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																		.addGroup(
																				layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(label_connection_type)
																						.addComponent(label_block_size,
																								javax.swing.GroupLayout.Alignment.TRAILING))
																		.addComponent(input_location,
																				javax.swing.GroupLayout.Alignment.TRAILING,
																				javax.swing.GroupLayout.PREFERRED_SIZE, 53,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addGroup(
																javax.swing.GroupLayout.Alignment.TRAILING,
																layout
																		.createSequentialGroup()
																		.addComponent(label_depots)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(button_add_depot))
														.addComponent(input_depots_all,
																javax.swing.GroupLayout.Alignment.TRAILING))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												layout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																layout
																		.createSequentialGroup()
																		.addComponent(input_connection_type,
																				javax.swing.GroupLayout.PREFERRED_SIZE, 200,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(label_duration)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addGroup(
																				layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addComponent(input_time_days,
																								javax.swing.GroupLayout.PREFERRED_SIZE, 45,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(input_connection_count,
																								javax.swing.GroupLayout.PREFERRED_SIZE, 57,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addGap(6, 6, 6)
																		.addGroup(
																				layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addGroup(
																								javax.swing.GroupLayout.Alignment.TRAILING,
																								layout
																										.createSequentialGroup()
																										.addComponent(label_copies_count)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED))
																						.addGroup(
																								layout.createSequentialGroup()
																										.addComponent(label_duration_days)
																										.addGap(29, 29, 29)))
																		.addGroup(
																				layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING, false)
																						.addComponent(input_copies_count,
																								javax.swing.GroupLayout.PREFERRED_SIZE, 45,
																								javax.swing.GroupLayout.PREFERRED_SIZE)
																						.addComponent(input_time_hours, 0, 1, Short.MAX_VALUE))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																				javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																		.addComponent(label_duration_hours))
														.addGroup(
																layout
																		.createSequentialGroup()
																		.addGroup(
																				layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.LEADING)
																						.addGroup(
																								layout
																										.createSequentialGroup()
																										.addComponent(input_block_size,
																												javax.swing.GroupLayout.PREFERRED_SIZE, 58,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																										.addComponent(input_block_unit,
																												javax.swing.GroupLayout.PREFERRED_SIZE, 53,
																												javax.swing.GroupLayout.PREFERRED_SIZE)
																										.addPreferredGap(
																												javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																												javax.swing.GroupLayout.DEFAULT_SIZE,
																												Short.MAX_VALUE)
																										.addComponent(label_connection_count)
																										.addGap(107, 107, 107))
																						.addComponent(scrollpane_depots,
																								javax.swing.GroupLayout.PREFERRED_SIZE, 0,
																								Short.MAX_VALUE))
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(button_reconnect))).addGap(2, 2, 2)));
		layout
				.setVerticalGroup(layout
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								layout
										.createSequentialGroup()
										.addGroup(
												layout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																layout
																		.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																		.addComponent(label_connection_type)
																		.addComponent(input_connection_type,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addComponent(label_duration))
														.addGroup(
																layout
																		.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																		.addComponent(input_time_days,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addComponent(label_duration_days,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																		.addComponent(input_time_hours,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addComponent(label_duration_hours,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												layout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																layout
																		.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																		.addComponent(label_block_size,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																		.addComponent(input_block_unit,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addComponent(input_block_size,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE))
														.addGroup(
																layout
																		.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																		.addComponent(label_copies_count)
																		.addComponent(input_copies_count,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addComponent(input_connection_count,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addComponent(label_connection_count,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												layout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
														.addGroup(
																layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
																		.addComponent(button_reconnect))
														.addGroup(
																javax.swing.GroupLayout.Alignment.LEADING,
																layout
																		.createSequentialGroup()
																		.addGroup(
																				layout
																						.createParallelGroup(
																								javax.swing.GroupLayout.Alignment.BASELINE)
																						.addComponent(label_depots)
																						.addComponent(button_add_depot))
																		.addGap(18, 18, 18)
																		.addComponent(input_location,
																				javax.swing.GroupLayout.PREFERRED_SIZE,
																				javax.swing.GroupLayout.DEFAULT_SIZE,
																				javax.swing.GroupLayout.PREFERRED_SIZE)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(input_depots_all))
														.addComponent(scrollpane_depots,
																javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))));
	}// </editor-fold>//GEN-END:initComponents

	private void button_add_depot_clicked(java.awt.event.MouseEvent evt)
	{// GEN-FIRST:event_button_add_depot_clicked
		String host_port_entered = (String) JOptionPane.showInputDialog(new JFrame(),
				"Depot (host:port)", null);

		if ((host_port_entered != null) && (host_port_entered.length() > 0)
				&& host_port_entered.contains(":")) {
			List<String> host_port_verified = Configuration.host_port_verified(host_port_entered);
			try {
				Depot depot = Depot.depot(host_port_verified.get(0), host_port_verified.get(1));
				log.info("new user-entered depot " + depot);
				return;
			} catch (Exception e) {
			}
		}
		log.warning("failed to create new user-entered depot [" + host_port_entered + "]");
	}// GEN-LAST:event_button_add_depot_clicked

	public List<Depot> depots()
	{
		return Depot.depots();
	}

	public List<Depot> depots_selected()
	{
		List<Depot> depots_selected = new ArrayList<Depot>();
		TableModel table_files_model = table_depots.getModel();
		for (int row = 0; row < table_files_model.getRowCount(); row++) {
			if (Boolean.TRUE.equals(table_files_model.getValueAt(row, column_index_checkbox))) {
				depots_selected.add((Depot) table_files_model.getValueAt(row, column_index_depot));
				log.info((Depot) table_files_model.getValueAt(row, column_index_depot) + " is selected.");
			}
		}
		return depots_selected;
	}

	private void input_depots_all_clicked(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_input_depots_all_clicked
		TableModel table_depots_model = table_depots.getModel();
		for (int row = 0; row < table_depots_model.getRowCount(); row++) {
			table_depots_model.setValueAt(input_depots_all.isSelected(), row, column_index_checkbox);
			log.info((Depot) table_depots_model.getValueAt(row, column_index_depot)
					+ (input_depots_all.isSelected() ? " selected." : " deselected."));
		}
	}// GEN-LAST:event_input_depots_all_clicked

	private void input_location_changed(javax.swing.event.ChangeEvent evt)
	{// GEN-FIRST:event_input_location_changed
		TableModel table_depots_model = table_depots.getModel();
		if (!input_depots_all.isSelected()) {
			for (int row = 0; row < table_depots_model.getRowCount(); row++) {
				Depot depot = (Depot) table_depots_model.getValueAt(row, column_index_depot);
				if (depot.locations.contains(input_location.getValue())) {
					table_depots_model.setValueAt(true, row, column_index_checkbox);
					log.info(depot + " selected.");
				}
			}
		}
	}// GEN-LAST:event_input_location_changed

	private void button_reconnect_clicked(java.awt.event.MouseEvent evt)
	{// GEN-FIRST:event_button_reconnect_clicked
		button_reconnect.setEnabled(false);
		Depot.depots.entrySet().forEach((depot_entry) -> depot_entry.getValue().setup());
	}// GEN-LAST:event_button_reconnect_clicked

	public void sync_depots()
	{
		/* remove all depots from table */
		TableModel table_depots_model = table_depots.getModel();
		DefaultTableModel table_depots_model_default = (DefaultTableModel) table_depots_model;
		for (int row = table_depots_model_default.getRowCount(); row >= 0; row--) {
			try {
				table_depots_model_default.removeRow(row);
				log.info((Depot) table_depots_model.getValueAt(row, column_index_depot) + " removed.");
			} catch (Exception e) {
			}
		}

		/* repopulate depot-list */
		for (Depot depot : Depot.depots()) {
			table_depots_model_default.addRow(new Object[] { false, depot,
					(depot.connected() ? Icons.icon_depot_connected : Icons.icon_depot_not_connected) });
		}
	}

	private void input_connection_type_changed(javax.swing.event.ChangeEvent evt)
	{// GEN-FIRST:event_input_connection_type_changed
		String connection_type_selected = (String) input_connection_type.getValue();
		if (connection_type_selected.equals("Dial-Up/ISDN")) {
			input_block_size.setSelectedItem(512);
			input_block_unit.setValue("KB");
			input_connection_count.setSelectedItem(1);
		} else if (connection_type_selected.equals("DSL/Cable/T1")) {
			input_block_size.setSelectedItem(1024);
			input_block_unit.setValue("KB");
			input_connection_count.setSelectedItem(3);
		} else if (connection_type_selected.equals("< 100 Mbps")) {
			input_block_size.setSelectedItem(2);
			input_block_unit.setValue("MB");
			input_connection_count.setSelectedItem(6);
		} else if (connection_type_selected.equals("> 100 Mbps")) {
			input_block_size.setSelectedItem(10);
			input_block_unit.setValue("MB");
			input_connection_count.setSelectedItem(10);
		}
	}// GEN-LAST:event_input_connection_type_changed

	private void input_block_size_changed(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_input_block_size_changed
		transfer_size_validate();
	}// GEN-LAST:event_input_block_size_changed

	private void input_block_unit_changed(javax.swing.event.ChangeEvent evt)
	{// GEN-FIRST:event_input_block_unit_changed
		transfer_size_validate();
	}// GEN-LAST:event_input_block_unit_changed

	private void transfer_size_validate()
	{
		long size;
		try {
			String block_size_units = (String) input_block_unit.getValue();
			long block_size = Long.parseLong(input_block_size.getSelectedItem().toString());
			long multiplier = block_size_units.equals("KB") ? 1024
					: (block_size_units.equals("MB") ? (1024 * 1024) : 1);
			size = block_size * multiplier;
		} catch (NumberFormatException e) {
			log.warning("failed to read input block size; defaulting to "
					+ Configuration.dlt_exnode_transfer_size_default);
			size = Configuration.dlt_exnode_transfer_size_default;
		}

		if (size > 10 * 1024 * 1024 || size <= 0) {
			log.warning("unsupported transfer size of " + size + " bytes; defaulting to "
					+ Configuration.dlt_exnode_transfer_size_default);
			size = Configuration.dlt_exnode_transfer_size_default;
		}

		if (size / 1024 > 1) {
			if (size / (1024 * 1024) > 1) {
				input_block_size.setSelectedItem(size / (1024 * 1024));
				input_block_unit.setValue("MB");
			} else {
				input_block_size.setSelectedItem(size / 1024);
				input_block_unit.setValue("KB");
			}
		}
	}

	public long transfer_size()
	{
		transfer_size_validate();
		String block_size_units = (String) input_block_unit.getValue();
		Long block_size = Long.parseLong(input_block_size.getSelectedItem().toString());
		long multiplier = block_size_units.equals("KB") ? 1024
				: (block_size_units.equals("MB") ? (1024 * 1024) : 1);
		return block_size * multiplier;
	}

	public int count_connections()
	{
		return Integer.parseInt(input_connection_count.getSelectedItem().toString());
	}

	public int copies()
	{
		return Integer.parseInt(input_copies_count.getSelectedItem().toString());
	}

	public long time_seconds()
	{
		int days = Integer.parseInt(input_time_days.getSelectedItem().toString());
		int hours = Integer.parseInt(input_time_hours.getSelectedItem().toString());
		return (days * 24 + hours) * 60 * 60;
	}

	public void enable()
	{
		input_block_size.setEnabled(true);
		input_block_unit.setEnabled(true);

		input_connection_count.setEnabled(true);
		input_connection_type.setEnabled(true);

		input_copies_count.setEnabled(true);

		input_time_days.setEnabled(true);
		input_time_hours.setEnabled(true);

		input_location.setEnabled(true);
		input_depots_all.setEnabled(true);
		button_reconnect.setEnabled(true);
		button_add_depot.setEnabled(true);
	}

	public void disable()
	{
		input_block_size.setEnabled(false);
		input_block_unit.setEnabled(false);

		input_connection_count.setEnabled(false);
		input_connection_type.setEnabled(false);

		input_copies_count.setEnabled(false);

		input_time_days.setEnabled(false);
		input_time_hours.setEnabled(false);

		input_location.setEnabled(false);
		input_depots_all.setEnabled(false);
		button_reconnect.setEnabled(false);
		button_add_depot.setEnabled(false);
	}

	private static final int column_index_checkbox = 0;
	private static final int column_index_depot = 1;
	private static final int column_index_status = 2;

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JLabel button_add_depot;
	private javax.swing.JButton button_reconnect;
	private javax.swing.JComboBox<Integer> input_block_size;
	private javax.swing.JSpinner input_block_unit;
	private javax.swing.JComboBox<Integer> input_connection_count;
	private javax.swing.JSpinner input_connection_type;
	private javax.swing.JComboBox<Integer> input_copies_count;
	private javax.swing.JCheckBox input_depots_all;
	private javax.swing.JSpinner input_location;
	private javax.swing.JComboBox<Integer> input_time_days;
	private javax.swing.JComboBox<Integer> input_time_hours;
	private javax.swing.JLabel label_block_size;
	private javax.swing.JLabel label_connection_count;
	private javax.swing.JLabel label_connection_type;
	private javax.swing.JLabel label_copies_count;
	private javax.swing.JLabel label_depots;
	private javax.swing.JLabel label_duration;
	private javax.swing.JLabel label_duration_days;
	private javax.swing.JLabel label_duration_hours;
	private javax.swing.JScrollPane scrollpane_depots;
	public javax.swing.JTable table_depots;

	// End of variables declaration//GEN-END:variables

	public static class ConnectionStatusRenderer extends DefaultTableCellRenderer
	{
		public ConnectionStatusRenderer()
		{
			setHorizontalAlignment(JLabel.CENTER);
		}

		@Override
		protected void setValue(Object value)
		{
			if (value instanceof Icon) {
				this.setIcon((Icon) value);
			} else {
				System.out.println(value);
			}
		}
	}
}
