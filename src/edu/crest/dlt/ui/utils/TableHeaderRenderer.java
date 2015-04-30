/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.ui.utils;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import edu.crest.dlt.ui.utils.img.Icons;

public class TableHeaderRenderer extends DefaultTableCellRenderer
{
	public static TableHeaderRenderer header_file = new TableHeaderRenderer("File", Icons.icon_file,
			JLabel.LEFT);
	public static TableHeaderRenderer header_checkbox = new TableHeaderRenderer(null,
			Icons.icon_deselect_all);
	public static TableHeaderRenderer header_status = new TableHeaderRenderer(null, Icons.icon_info);
	public static TableHeaderRenderer header_depot = new TableHeaderRenderer("Depot",
			Icons.icon_depot, JLabel.LEFT);
	public static TableHeaderRenderer header_depot_connections = new TableHeaderRenderer(null,
			Icons.icon_depot_connected);
	public static TableHeaderRenderer header_speed = new TableHeaderRenderer("MB/s", Icons.icon_speed);
	public static TableHeaderRenderer header_success = new TableHeaderRenderer(null,
			Icons.icon_success);
	public static TableHeaderRenderer header_failure = new TableHeaderRenderer(null,
			Icons.icon_failure);

	String text = null;
	Icon icon = null;
	int alignment = JLabel.CENTER;

	public TableHeaderRenderer(String text, Icon icon)
	{
		this.text = text;
		this.icon = icon;
	}

	public TableHeaderRenderer(String text, Icon icon, int alignment)
	{
		this(text, icon);
		this.alignment = alignment;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column)
	{
		if (table != null) {
			JTableHeader header = table.getTableHeader();
			if (header != null) {
				setForeground(header.getForeground());
				setBackground(header.getBackground());
				setFont(header.getFont());
				setBorder(header.getBorder());
				setLocale(header.getLocale());
			}
		}

		setText(this.text);
		setIcon(this.icon);

		setHorizontalAlignment(alignment);
		return this;
	}
}
