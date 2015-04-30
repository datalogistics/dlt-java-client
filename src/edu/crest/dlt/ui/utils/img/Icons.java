/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.ui.utils.img;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

public class Icons
{
	private static final Logger log = Logger.getLogger(Icons.class.getClass().getName());

	public static final Icon icon_depot_connected = Icons.load("dot-green.png");// ("connected.png");
	public static final Icon icon_depot_not_connected = Icons.load("dot-red.png");// ("disconnected.png");
	public static final Icon icon_success = Icons.load("check.png");
	public static final Icon icon_failure = Icons.load("cross.png");
	public static final Icon icon_download = Icons.load("download.png");
	public static final Icon icon_upload = Icons.load("upload.png");
	public static final Icon icon_abort = Icons.load("abort-hand.png");
	public static final Icon icon_map = Icons.load("globe.png");
	public static final Icon icon_plus = Icons.load("plus.png");
	public static final Icon icon_minus = Icons.load("minus.png");
	public static final Icon icon_refresh = Icons.load("refresh.png");
	public static final Icon icon_select_all = Icons.load("check-box.png");// all.png
	public static final Icon icon_deselect_all = Icons.load("uncheck-box.png");// none.png
	public static final Icon icon_directory_empty = Icons.load("directory-empty.png");
	public static final Icon icon_processing = Icons.load("rotate-2.png");
	private static final Icon icon_spinning = Icons.load("rotate.gif");
	public static final Icon icon_ready = Icons.load("ready.png");
	public static final Icon icon_file = Icons.load("file.png");
	public static final Icon icon_file_upload = Icons.load("file-upload.png");
	public static final Icon icon_file_download = Icons.load("file-download.png");
	public static final Icon icon_file_error = Icons.load("file-error.png");
	public static final Icon icon_file_write = Icons.load("file-write.png");
	public static final Icon icon_depot = Icons.load("depot-cloud.png");
	public static final Icon icon_info = Icons.load("information.png");
	public static final Icon icon_speed = Icons.load("speedometer.png");

	private static ImageIcon load(String path_to_icon)
	{
		try {
			return new ImageIcon(Icons.class.getResource(path_to_icon));
		} catch (Exception e) {
			StringTokenizer tokenizer = new StringTokenizer(Icons.class.getName(), ".");
			StringBuffer name_package = new StringBuffer();

			while (tokenizer.hasMoreTokens()) {
				String path_ahead = tokenizer.nextToken();
				if (path_ahead.equals(Icons.class.getSimpleName())) {
					break;
				}
				name_package.append(path_ahead).append(".");
			}

			log.warning("failed to find/load image-icon " + name_package.toString() + path_to_icon);
			return new ImageIcon();
		}
	}
	
	public static final Icon icon_processing(JComponent component)
	{
		return new RotatingIcon(icon_spinning, component);
	}
}
