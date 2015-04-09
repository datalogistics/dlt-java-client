/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.crest.dlt.ui.up;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerListModel;

import edu.crest.dlt.ibp.Depot;
import edu.crest.dlt.ibp.DepotLocatorLbone;
import edu.crest.dlt.utils.Configuration;

/**
 * @author Rohit
 */
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

		new Thread()
		{
			public void run()
			{
				for (DepotLocatorLbone lbone_server : Configuration.dlt_depot_locators_lbone) {
					/* fetch location-wise depots */
					for (String location : Configuration.dlt_depot_locations) {
						for (int count_depots = 10; count_depots > 0; count_depots -= 2) {
							try {
								if (lbone_server.depots(10, 1, 0, 2 * 24 * 60 * 60, location).size() >= count_depots - 1) {
									break; // break when the maximum number of depots are obtained
								}
							} catch (Exception e) {
							}
						}
					}

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

				// for (Depot depot : Depot.depots()) {
				// depot(depot);
				// }
			}
		}.start();
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
        scroller_depots = new javax.swing.JScrollPane();
        input_depots = new javax.swing.JList<Depot>();
        input_depots_all = new javax.swing.JCheckBox();
        label_depots = new javax.swing.JLabel();
        input_location = new javax.swing.JSpinner(new SpinnerListModel(Configuration.dlt_depot_locations.toArray()));

        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "File Transfer Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N

        label_connection_type.setText("Connection Type");

        input_connection_type.setModel(new javax.swing.SpinnerListModel(new String[] {"Dial-Up/ISDN", "DSL/Cable/T1", "< 100 Mbps", "> 100 Mbps"}));
        input_connection_type.setEditor(new javax.swing.JSpinner.ListEditor(input_connection_type));
        input_connection_type.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                input_connection_type_changed(evt);
            }
        });

        label_block_size.setText("Block Size");

        input_block_size.setEditable(true);
        input_block_size.setMaximumRowCount(15);
        input_block_size.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "4", "5", "8", "10", "16", "32", "64", "128", "256", "512", "1024" }));
        input_block_size.setSelectedIndex(11);
        input_block_size.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                input_block_size_changed(evt);
            }
        });

        input_block_unit.setModel(new javax.swing.SpinnerListModel(new String[] {"KB", "MB"}));
        input_block_unit.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                input_block_unit_changed(evt);
            }
        });

        label_connection_count.setText("# of Connections");

        input_connection_count.setEditable(true);
        input_connection_count.setMaximumRowCount(10);
        input_connection_count.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        input_connection_count.setToolTipText("");

        button_reconnect.setText("Reconnect");
        button_reconnect.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                button_reconnect_clicked(evt);
            }
        });

        label_copies_count.setText("# of Copies");

        input_copies_count.setEditable(true);
        input_copies_count.setMaximumRowCount(10);
        input_copies_count.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5" }));
        input_copies_count.setToolTipText("");

        label_duration.setText("Duration");

        input_time_days.setEditable(true);
        input_time_days.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7" }));
        input_time_days.setSelectedIndex(2);

        label_duration_days.setText("day(s)");

        input_time_hours.setEditable(true);
        input_time_hours.setMaximumRowCount(15);
        input_time_hours.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));

        label_duration_hours.setText("hr");

        input_depots.setModel(new DepotListModel());
        input_depots.setCellRenderer(new DepotCellRenderer());
        scroller_depots.setViewportView(input_depots);

        input_depots_all.setText("all");
        input_depots_all.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                input_depots_all_clicked(evt);
            }
        });

        label_depots.setText("Depots");

        input_location.setModel(new javax.swing.SpinnerListModel(new String[] {"AL", "IN"}));
        input_location.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                input_location_changed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(label_connection_type)
                        .addComponent(label_depots, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(label_block_size, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addComponent(input_location, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(input_connection_type, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(label_duration))
                            .addComponent(label_connection_count, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(input_connection_count, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(input_time_days, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(label_copies_count)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(label_duration_days)
                                .addGap(29, 29, 29)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(input_copies_count, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(input_time_hours, 0, 1, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(label_duration_hours))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(input_block_size, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(input_block_unit, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(scroller_depots)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(input_depots_all)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(button_reconnect)))
                .addGap(2, 2, 2))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(label_connection_type)
                        .addComponent(input_connection_type, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(label_duration)
                        .addComponent(input_time_days, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(label_duration_days, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(input_time_hours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(label_duration_hours, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(label_block_size, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(input_block_unit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(input_block_size, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(label_copies_count)
                        .addComponent(input_copies_count, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(input_connection_count, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(label_connection_count, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(scroller_depots, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(button_reconnect)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(input_depots_all)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(label_depots)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(input_location, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
    }// </editor-fold>//GEN-END:initComponents

	private void input_depots_all_clicked(java.awt.event.ActionEvent evt)
	{// GEN-FIRST:event_input_depots_all_clicked
		/* deselect all depots */
		if (!input_depots_all.isSelected()) {
			input_depots.clearSelection();
			return;
		} 
		
		/* select all depots */
		DepotListModel depots = (DepotListModel) input_depots.getModel();
		List<Integer> selected_idxs = new ArrayList<Integer>();
		for (int d = 0; d < depots.getSize(); d++) {
			selected_idxs.add(d);
		}
		int[] selected_idxs_arr = new int[selected_idxs.size()];
		for (int s = 0; s < selected_idxs.size(); s++) {
			selected_idxs_arr[s] = selected_idxs.get(s);
		}
		input_depots.setSelectedIndices(selected_idxs_arr);
	}// GEN-LAST:event_input_depots_all_clicked

	private void input_location_changed(javax.swing.event.ChangeEvent evt)
	{// GEN-FIRST:event_input_location_changed
		DepotListModel depots = (DepotListModel) input_depots.getModel();
		if (!input_depots_all.isSelected()) {
			/* clear currently selected depots */
			input_depots.clearSelection();
			
			/* select depots belonging to the selected location */
			List<Integer> selected_idxs = new ArrayList<Integer>();
			for (int d = 0; d < depots.getSize(); d++) {
				if (depots.getElementAt(d).locations.contains(input_location.getValue())) {
					selected_idxs.add(d);
					// input_depots.setSelectedIndex(d);
				}
			}
			int[] selected_idxs_arr = new int[selected_idxs.size()];
			for (int s = 0; s < selected_idxs.size(); s++) {
				selected_idxs_arr[s] = selected_idxs.get(s);
			}
			input_depots.setSelectedIndices(selected_idxs_arr);
		}
	}// GEN-LAST:event_input_location_changed

	private void button_reconnect_clicked(java.awt.event.MouseEvent evt)
	{// GEN-FIRST:event_button_reconnect_clicked
		for (Map.Entry<String, Depot> depot_entry : Depot.depots.entrySet()) {
			depot_entry.getValue().setup();
			// depot(depot_entry.getValue());
		}
		// synchronized (this) {
		// this.notify();
		// }
	}// GEN-LAST:event_button_reconnect_clicked

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

	public void depot(Depot depot)
	{
		// DepotListModel depots = (DepotListModel) input_depots.getModel();
		// if (!depots.contains(depot)) {
		// depots.add(depot);
		// }
	}

	public List<Depot> depots()
	{
		DepotListModel depots = (DepotListModel) input_depots.getModel();
		return depots.all();
	}

	public List<Depot> depots_selected()
	{
		DepotListModel depots = (DepotListModel) input_depots.getModel();
		if (input_depots_all.isSelected()) {
			return depots.all();
		}

		List<Depot> depots_selected = new ArrayList<Depot>();
		for (int selected_idx : input_depots.getSelectedIndices()) {
			depots_selected.add(depots.getElementAt(selected_idx));
		}
		return depots_selected;
	}

	public void enable()
	{
		input_block_size.setEnabled(true);
		input_block_unit.setEnabled(true);
		input_connection_count.setEnabled(true);
		input_connection_type.setEnabled(true);
	}

	public void disable()
	{
		input_block_size.setEnabled(false);
		input_block_unit.setEnabled(false);
		input_connection_count.setEnabled(false);
		input_connection_type.setEnabled(false);
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_reconnect;
    private javax.swing.JComboBox<Integer> input_block_size;
    private javax.swing.JSpinner input_block_unit;
    private javax.swing.JComboBox<Integer> input_connection_count;
    private javax.swing.JSpinner input_connection_type;
    private javax.swing.JComboBox<Integer> input_copies_count;
    private javax.swing.JList<Depot> input_depots;
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
    private javax.swing.JScrollPane scroller_depots;
    // End of variables declaration//GEN-END:variables

	private class DepotCellRenderer extends JLabel implements ListCellRenderer<Depot>
	{
		public DepotCellRenderer()
		{
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends Depot> depots, Depot depot,
				int depot_idx, boolean depot_is_selected, boolean depot_has_focus)
		{
			StringBuffer locations = new StringBuffer();
			int count = depot.locations.size();
			locations.append(count > 0 ? "[" : "");
			for (String location : depot.locations) {
				locations.append(location).append(--count > 0 ? "/" : "");
			}
			locations.append(locations.length() > 0 ? "] " : "");
			setText(locations.toString() + depot.toString()
					+ (depot.connected() ? "" : " (not connected)"));

			Color color_background = depot_is_selected ? depots.getSelectionBackground() : depots
					.getBackground();
			Color color_foreground = depot_is_selected ? depots.getSelectionForeground() : depots
					.getForeground();
			setBackground(color_background);
			setForeground(color_foreground);

			return this;
		}
	}

	private class DepotListModel extends AbstractListModel<Depot>
	{
		// private List<Depot> depots = new ArrayList<Depot>();

		@Override
		public Depot getElementAt(int idx)
		{
			return Depot.depots().get(idx);
		}

		public List<Depot> all()
		{
			return Depot.depots();
		}

		// public synchronized void add(Depot depot)
		// {
		// if (!contains(depot)) {
		// depots.add(depot);
		// }
		// }

		// public synchronized boolean contains(Depot depot)
		// {
		// for (Depot depot_in_list : depots) {
		// if (depot_in_list.toString().compareTo(depot.toString()) == 0) {
		// return true;
		// }
		// }
		// return false;
		// return depots.contains(depot);
		// return true;
		// }

		@Override
		public int getSize()
		{
			return Depot.depots().size();
		}
	}
}
