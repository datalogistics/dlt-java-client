/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.utils;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import edu.crest.dlt.ui.utils.img.Icons;

public class Status
{
	public enum ui_status {
		depot_connected, depot_not_connected, download_ready, upload_ready, metadata_error, mappings_inaccessible, not_enough_depots, file_not_found, download_failed, upload_failed, transfer_failed, transfer_sucess, transfer_aborting, transfer_aborted, processing, downloading, uploading, file_registering
	}

	private static Map<ui_status, String> status_text = new HashMap<ui_status, String>()
	{
		{
			put(ui_status.depot_connected, "");
			put(ui_status.depot_not_connected, "Connection to depot failed. Try to refresh.");

			put(ui_status.download_ready, "Ready to download file.");
			put(ui_status.upload_ready, "Ready to upload file.");
			put(ui_status.metadata_error, "Failed to parse file metadata.");
			put(ui_status.mappings_inaccessible,
					"One or more mapping(s) of the file not currently accessible.");
			put(ui_status.not_enough_depots,
					"Not enough depot(s) accessible for hosting required copies of file.");
			put(ui_status.file_not_found, "Invalid file path or failed to read file.");

			put(ui_status.download_failed, "Failed to download file.");
			put(ui_status.upload_failed, "Failed to publish file.");

			put(ui_status.transfer_failed, "Failed to transfer file.");
			put(ui_status.transfer_aborting, "File transfer aborted/cancelled.");
			put(ui_status.transfer_aborted, "File transfer aborted/cancelled.");
			put(ui_status.transfer_sucess, "File transfer successful.");

			put(ui_status.processing, "Processing. Please wait.");
			put(ui_status.downloading, "File download in progress.");
			put(ui_status.uploading, "File upload in progress.");
			put(ui_status.file_registering, "Registering file metadata.");
		}
	};

	private static Map<ui_status, Icon> status_icon = new HashMap<ui_status, Icon>()
	{
		{
			put(ui_status.depot_connected, Icons.icon_depot_connected);
			put(ui_status.depot_not_connected, Icons.icon_depot_not_connected);

			put(ui_status.download_ready, Icons.icon_processing);
			put(ui_status.upload_ready, Icons.icon_processing);
			put(ui_status.metadata_error, Icons.icon_failure);
			put(ui_status.mappings_inaccessible, Icons.icon_depot_not_connected);
			put(ui_status.not_enough_depots, Icons.icon_processing);
			put(ui_status.file_not_found, Icons.icon_failure);

			put(ui_status.download_failed, Icons.icon_failure);
			put(ui_status.upload_failed, Icons.icon_depot_not_connected);

			put(ui_status.transfer_failed, Icons.icon_failure);
			put(ui_status.transfer_aborting, Icons.icon_processing);
			put(ui_status.transfer_aborted, Icons.icon_failure);
			put(ui_status.transfer_sucess, Icons.icon_success);

			put(ui_status.processing, Icons.icon_processing);
			put(ui_status.downloading, Icons.icon_file_download);
			put(ui_status.uploading, Icons.icon_file_upload);
			put(ui_status.file_registering, Icons.icon_file_write);
		}
	};

	public static String message(ui_status status)
	{
		return status_text.containsKey(status) ? status_text.get(status) : "";
	}

	public static Icon icon(ui_status status)
	{
		return status_text.containsKey(status) ? status_icon.get(status) : new ImageIcon();
	}
}
