/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.crest.dlt.ui.main;

import java.awt.Color;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.commons.cli.ParseException;

import edu.crest.dlt.ui.utils.CommandLineOptionParser;
import edu.crest.dlt.utils.Configuration;

/**
 * @author Rohit
 */
public class UploadDownloadFrame extends javax.swing.JFrame
{
	private static final Logger log = Logger.getLogger(UploadDownloadFrame.class.getName());

	/**
	 * Creates new form UploadDownloadFrame
	 */
	public UploadDownloadFrame()
	{
		setTitle(Configuration.dlt_ui_title);

		initComponents();
		setLocationRelativeTo(null); // for centering

		log.info("started application. (says \"hola\" ^_^)");
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
	// <editor-fold defaultstate="collapsed"
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    pane_tabs = new javax.swing.JTabbedPane();
    panel_download = new edu.crest.dlt.ui.down.DownloadPanel();
    panel_upload = new edu.crest.dlt.ui.up.UploadPanel();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        formWindowClosing(evt);
      }
    });

    pane_tabs.addTab("Download", panel_download);
    pane_tabs.addTab("Upload", panel_upload);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(pane_tabs)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(pane_tabs, javax.swing.GroupLayout.PREFERRED_SIZE, 642, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(0, 0, Short.MAX_VALUE))
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

	private void formWindowClosing(java.awt.event.WindowEvent evt)
	{// GEN-FIRST:event_formWindowClosing
		String confirmationMessage = "Are you sure you want to close the window?\nCAUTION: This will terminate all running tasks.";
		int answer = JOptionPane.showConfirmDialog(new JFrame(), confirmationMessage, "",
				JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			log.info("closing application after user confirmation. (says \"ciao\" ^_^)");
			System.exit(WindowConstants.DISPOSE_ON_CLOSE);
		}
	}// GEN-LAST:event_formWindowClosing

	public static void loadCommandLineConfiguration(String args[]) throws ParseException
	{
		CommandLineOptionParser parser = new CommandLineOptionParser(args);
		Configuration.dlt_username = parser.getUsername();
		log.info("Username      : " + Configuration.dlt_username);

		Configuration.dlt_password = parser.getPassword();
		log.info("Password      : " + Configuration.dlt_password);

		Configuration.dlt_file_paths = parser.getFiles();
		log.info("Files         : " + Configuration.dlt_file_paths.toString());

		Configuration.dlt_exnode_transfer_size_default = parser.getTransferSize();
		log.info("Transfer Size : " + parser.getTransferSize());

		Configuration.dlt_exnode_transfer_connections_default = parser.getNumOfConnections();
		log.info("Threads       : " + parser.getNumOfConnections());
	}

	/**
	 * @param args
	 *          the command line arguments
	 * @throws ParseException
	 */
	public static void main(String args[]) throws ParseException
	{
		Configuration.load();
		loadCommandLineConfiguration(args);

		/* Set the Nimbus look and feel */
		// <editor-fold defaultstate="collapsed"
		// desc=" Look and feel setting code (optional) ">
		/*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the
		 * default look and feel. For details see
		 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager
					.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(UploadDownloadFrame.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(UploadDownloadFrame.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(UploadDownloadFrame.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(UploadDownloadFrame.class.getName()).log(
					java.util.logging.Level.SEVERE, null, ex);
		}
		// </editor-fold>

		UIManager.getLookAndFeelDefaults()
				.put("List[Selected].textBackground", new Color(57, 105, 138));
		UIManager.getLookAndFeelDefaults().put("List[Selected].textForeground", Color.WHITE);

		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				new UploadDownloadFrame().setVisible(true);
			}
		});
	}

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTabbedPane pane_tabs;
  private edu.crest.dlt.ui.down.DownloadPanel panel_download;
  private edu.crest.dlt.ui.up.UploadPanel panel_upload;
  // End of variables declaration//GEN-END:variables
}
