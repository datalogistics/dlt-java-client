package edu.crest.dlt.ui.utils;

import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

public class TableRowStatusRenderer extends DefaultTableCellRenderer
{
	private static final Logger log = Logger.getLogger(TableRowStatusRenderer.class.getClass().getName());
	
	public TableRowStatusRenderer() 
	{
		setHorizontalAlignment(JLabel.CENTER);
	}

	@Override
	protected void setValue(Object value)
	{
		if (value instanceof Icon) {
			this.setIcon((Icon) value);
		} else {
			log.warning("Unknown FileStatus object [" + value + "]");
		}
	}
}
