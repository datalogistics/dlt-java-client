/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.crest.dlt.ui.up;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import edu.crest.dlt.exception.CreateResourceException;
import edu.crest.dlt.exnode.Exnode;
import edu.crest.dlt.exnode.Exnode.service_exnode;
import edu.crest.dlt.exnode.metadata.Metadata;
import edu.crest.dlt.exnode.metadata.MetadataDouble;
import edu.crest.dlt.exnode.metadata.MetadataInteger;
import edu.crest.dlt.exnode.metadata.MetadataList;
import edu.crest.dlt.exnode.metadata.MetadataString;
import edu.crest.dlt.ibp.Depot;
import edu.crest.dlt.ui.down.DownloadPanel;
import edu.crest.dlt.ui.utils.img.Icons;
import edu.crest.dlt.utils.Configuration;

/**
 *
 * @author Rohit
 */
public class UploadPanel extends javax.swing.JPanel
{
	private static final Logger log = Logger.getLogger(DownloadPanel.class.getName());

	private Map<String, Exnode> map_filename_exnode;

	/**
	 * Creates new form UploaderPanel
	 */
	public UploadPanel()
	{
		initComponents();
		// panel_transfer_settings.depot(Depot.depot("hello.world", 1));

		map_filename_exnode = new HashMap<String, Exnode>();
		panel_files.disable();
		new Thread()
		{
			public void run()
			{
				while (true) {
					synchronized (panel_files) {
						try {
							panel_files.wait();

							panel_files.disable();
							/* setup obtained exnodes for upload */
							setup_exnodes();

							/* publish ready exnodes for upload (by filename) */
							publish_uploads();
							panel_files.enable();
						} catch (InterruptedException e) {
						}
					}
				}

				// panel_transfer_settings.enable();
			}
		}.start();
		panel_files.enable();
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
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents()
	{

		panel_files = new edu.crest.dlt.ui.up.FilesPanel();
		panel_transfer_settings = new edu.crest.dlt.ui.up.TransferSettingsPanel();
		panel_transfer_progress = new edu.crest.dlt.ui.utils.TransferProgressPanel();
		button_upload = new javax.swing.JButton();
		button_map_view = new javax.swing.JButton();
		button_cancel = new javax.swing.JButton();
		button_close = new javax.swing.JButton();

		setPreferredSize(new java.awt.Dimension(500, 474));

		panel_files.setMinimumSize(new java.awt.Dimension(500, 176));

		panel_transfer_progress.setMinimumSize(new java.awt.Dimension(500, 154));

		button_upload.setIcon(Icons.icon_upload);
		button_upload.setText("Upload");
		button_upload.addMouseMotionListener(new java.awt.event.MouseMotionAdapter()
		{
			public void mouseMoved(java.awt.event.MouseEvent evt)
			{
				button_uploadMouseMoved(evt);
			}
		});
		button_upload.addMouseListener(new java.awt.event.MouseAdapter()
		{
			public void mouseClicked(java.awt.event.MouseEvent evt)
			{
				button_upload_clicked(evt);
			}
		});

		button_map_view.setIcon(Icons.icon_map);
		button_map_view.setText("View");
		button_map_view.addMouseListener(new java.awt.event.MouseAdapter()
		{
			public void mouseClicked(java.awt.event.MouseEvent evt)
			{
				button_map_view_clicked(evt);
			}
		});

		button_cancel.setIcon(Icons.icon_abort);
		button_cancel.setText("Cancel");
		button_cancel.addMouseListener(new java.awt.event.MouseAdapter()
		{
			public void mouseClicked(java.awt.event.MouseEvent evt)
			{
				button_cancel_clicked(evt);
			}
		});

		button_close.setIcon(Icons.icon_failure);
		button_close.setText("Close");
		button_close.addMouseListener(new java.awt.event.MouseAdapter()
		{
			public void mouseClicked(java.awt.event.MouseEvent evt)
			{
				button_close_clicked(evt);
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(panel_files, javax.swing.GroupLayout.Alignment.TRAILING,
						javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
				.addComponent(panel_transfer_settings, javax.swing.GroupLayout.Alignment.TRAILING,
						javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE)
				.addComponent(panel_transfer_progress, javax.swing.GroupLayout.Alignment.TRAILING,
						javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
						Short.MAX_VALUE)
				.addGroup(
						layout
								.createSequentialGroup()
								.addComponent(button_upload)
								.addGap(18, 18, 18)
								.addComponent(button_map_view)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(button_cancel).addGap(18, 18, 18).addComponent(button_close)));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(
						layout
								.createSequentialGroup()
								.addComponent(panel_files, javax.swing.GroupLayout.PREFERRED_SIZE, 191,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(panel_transfer_settings, javax.swing.GroupLayout.DEFAULT_SIZE, 215,
										Short.MAX_VALUE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(panel_transfer_progress, javax.swing.GroupLayout.DEFAULT_SIZE, 182,
										Short.MAX_VALUE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(
										layout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(
														layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																.addComponent(button_close).addComponent(button_cancel))
												.addGroup(
														javax.swing.GroupLayout.Alignment.TRAILING,
														layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
																.addComponent(button_upload).addComponent(button_map_view)))
								.addContainerGap()));
	}// </editor-fold>//GEN-END:initComponents

	private void button_uploadMouseMoved(java.awt.event.MouseEvent evt)
	{// GEN-FIRST:event_button_uploadMouseMoved
		setup_exnodes();
	}// GEN-LAST:event_button_uploadMouseMoved

	private void button_upload_clicked(java.awt.event.MouseEvent evt)
	{// GEN-FIRST:event_button_upload_clicked
		Thread uploader = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				panel_transfer_settings.disable();
				button_upload.setEnabled(false);
				List<String> files_to_upload = panel_files.files_selected();
				int count_files_to_upload = files_to_upload.size();

				for (String file_to_upload : files_to_upload) {
					try {
						Exnode exnode_to_upload = map_filename_exnode.get(file_to_upload);
						if (exnode_to_upload == null) {
							log.severe("failed to retrieve exnode for : " + file_to_upload);
							panel_files.status_file(file_to_upload, "Failed (Metadata)");
							continue;
						}

						long bytes_to_upload = exnode_to_upload.length();
						int copies = panel_transfer_settings.copies();

						/* Update the number of copies for the exnode */
						exnode_to_upload.add(new MetadataInteger("number_of_copies", copies));

						panel_transfer_progress.clear();
						panel_transfer_progress.filename(file_to_upload);
						panel_transfer_progress.size(bytes_to_upload);
						exnode_to_upload.add(panel_transfer_progress);

						panel_files.status_file(file_to_upload, "In Progress");
						if (exnode_to_upload.write(
								new HashSet<Depot>(panel_transfer_settings.depots_selected()), copies,
								panel_transfer_settings.time_seconds(), panel_transfer_settings.transfer_size(),
								null, panel_transfer_settings.count_connections())) {
							panel_files.status_file(file_to_upload, "Registering");

							/* Register the new exnode with the registry-service (UNIS) */
							String selfRef = Configuration.dlt_exnode_registry_unis
									.register_exnode(exnode_to_upload);
							
							System.out.println("Published to selfRef " + selfRef);
							
//							if (selfRef != null) {
//								JOptionPane.showMessageDialog(null, "Published file " + selfRef,
//										"Upload Sucessful", JOptionPane.PLAIN_MESSAGE);
//							} else {
//								throw new CreateResourceException("selfRef=null");
//							}

							panel_files.status_file(file_to_upload, "Done");
						} else {
							String previousStatus = panel_files.status_file(file_to_upload);
							if ("In Progress".equals(previousStatus)) {
								panel_files.status_file(file_to_upload, "Failed");
							} else if ("Cancelling".equals(previousStatus) || "Cancelled".equals(previousStatus)) {
								panel_files.status_file(file_to_upload, "Cancelled");
							}
						}
					} catch (Exception e) {
						log.warning("failed to upload " + file_to_upload + ". " + e.getMessage());
						panel_files.status_file(file_to_upload, "Failed");
					}
				}

				button_upload.setEnabled(true);
				panel_transfer_settings.enable();
			}
		});
		uploader.start();
	}// GEN-LAST:event_button_upload_clicked

	private void button_map_view_clicked(java.awt.event.MouseEvent evt)
	{// GEN-FIRST:event_button_map_view_clicked
		String url_map = Configuration.dlt_ui_progress_map_view_url;
		try {
			Desktop.getDesktop().browse(new URI(url_map));
		} catch (IOException | URISyntaxException e) {
			log.warning("failed to reach " + url_map + ". " + e);
		}
	}// GEN-LAST:event_button_map_view_clicked

	private void button_cancel_clicked(java.awt.event.MouseEvent evt)
	{// GEN-FIRST:event_button_cancel_clicked
		String confirmationMessage = "Are you sure you want to cancel the running tasks?\nNOTE: Completed tasks will not be undone.";
		int answer = JOptionPane.showConfirmDialog(new JFrame(), confirmationMessage, "",
				JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			List<String> files_to_cancel = panel_files.files_selected();
			// int count = files_to_cancel.size();

			for (String file_selected : files_to_cancel) {
				try {
					Exnode exnode_to_cancel = map_filename_exnode.get(file_selected);
					if (exnode_to_cancel == null) {
						throw new Exception("failed to retrieve exnode for file name : " + file_selected);
					}

					// setTitle(Configuration.bd_ui_title + " (Cancelling " + count +
					// ")");

					panel_files.status_file(file_selected, "Cancelling");
					exnode_to_cancel.transfer_cancel();
					panel_files.status_file(file_selected, "Cancelled");
					panel_files.deselect_file(file_selected);
				} catch (Exception e) {
				}
				// count--;
			}
			panel_files.deselect_files_all();
			panel_transfer_progress.clear();

			// setTitle(Configuration.bd_ui_title + " (Download)");

			panel_transfer_settings.enable();
			panel_files.enable();
			button_upload.setEnabled(true);

			Toolkit.getDefaultToolkit().beep();
		}
	}// GEN-LAST:event_button_cancel_clicked

	private void button_close_clicked(java.awt.event.MouseEvent evt)
	{// GEN-FIRST:event_button_close_clicked
		String confirmationMessage = "Are you sure you want to close the window?\nCAUTION: This will terminate all running tasks.";
		int answer = JOptionPane.showConfirmDialog(new JFrame(), confirmationMessage, "",
				JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			log.info("closing application after user confirmation. (says \"ciao\" ^_^)");
			System.exit(WindowConstants.DISPOSE_ON_CLOSE);
		}
	}// GEN-LAST:event_button_close_clicked

	private void publish_uploads()
	{
		panel_files.remove_files_all();

		for (Map.Entry<String, Exnode> entry : map_filename_exnode.entrySet()) {
			if (entry.getValue() == null) {
				/* if exnode not found, fail the file-"path" */
				panel_files.add_file(entry.getKey(), "I/O error");
			} else {
				/* if exnode is not in ready state yet, */
				if (!entry.getValue().accessible(service_exnode.write)) {
					try {
						/* wait for connection-setup timeout */
						Thread.sleep(Configuration.dlt_depot_connect_timeout);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (!entry.getValue().accessible(service_exnode.write)) {
					/*
					 * if exnode is still not ready, declare the file-"name" waiting for
					 * depots
					 */
					panel_files.add_file(entry.getKey(), "Waiting");
				} else {
					/* else publish file-"name" for download */
					panel_files.add_file(entry.getKey(), "Ready");
				}
			}
			// panel_files.add_file(entry.getKey(), (entry.getValue() != null ?
			// "Ready" : "Failed"));
		}
	}

	private void setup_exnodes()
	{
		/* remove entries of files removed by user */
		Set<String> files_published_old = map_filename_exnode.keySet();
		Set<String> files_published_new = new HashSet<String>(panel_files.files());
		for (String file_published_old : files_published_old) {
			if (!files_published_new.contains(file_published_old)) {
				map_filename_exnode.remove(file_published_old);
			}
		}

		/* add and/or setup exnodes for the files chosen by user */
		files_published_old = map_filename_exnode.keySet();
		for (String file_published_new : files_published_new) {
			Exnode exnode;
			if (!files_published_old.contains(file_published_new)) {
				try {
					/* Initialize a new exnode */
					Exnode exnode_to_upload = new Exnode(file_published_new);
					exnode_to_upload.add(new MetadataDouble(Configuration.dlt_ui_title + " Publisher Client",
							0.0));
					exnode_to_upload.add(new MetadataInteger("original_filesize", exnode_to_upload.length()));
					exnode_to_upload.add(new MetadataString("status", "NEW"));
					Metadata metadata_filetype = new MetadataList("Type");
					metadata_filetype.add(new MetadataString("Name", "logistical_file"));
					metadata_filetype.add(new MetadataString("Version", "0"));
					exnode_to_upload.add(metadata_filetype);

					map_filename_exnode.put(file_published_new, exnode_to_upload);
				} catch (FileNotFoundException e) {
					log.warning("failed to setup exnode for file " + files_published_new + ". " + e);
					map_filename_exnode.put(file_published_new, null);
				}
			}
			exnode = map_filename_exnode.get(file_published_new);
			exnode.setup_write(new HashSet<Depot>(panel_transfer_settings.depots_selected()),
					panel_transfer_settings.copies(), panel_transfer_settings.time_seconds(),
					panel_transfer_settings.transfer_size(), null);
		}
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton button_cancel;
	private javax.swing.JButton button_close;
	private javax.swing.JButton button_map_view;
	private javax.swing.JButton button_upload;
	private edu.crest.dlt.ui.up.FilesPanel panel_files;
	private edu.crest.dlt.ui.utils.TransferProgressPanel panel_transfer_progress;
	private edu.crest.dlt.ui.up.TransferSettingsPanel panel_transfer_settings;
	// End of variables declaration//GEN-END:variables
}
