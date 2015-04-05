package edu.crest.dlt.transfer;

/**
 * @author rkhapare
 * @created 2/22/2015
 */

//import java.net.Socket;
import java.util.logging.Logger;

import javax.json.JsonException;

import org.json.JSONObject;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import edu.crest.dlt.exnode.Exnode;
import edu.crest.dlt.ibp.Depot;
import edu.crest.dlt.ui.down.DownloadPanel;
import edu.crest.dlt.utils.Configuration;

public class MapProgressListener implements ProgressListener
{
	private static Logger log = Logger.getLogger(MapProgressListener.class.getName());
	
	private final String session_id;
	private final String filename;
	private final Exnode exnode;
	private DownloadPanel client;

	private Socket mapSocket = null;

	public MapProgressListener(String session_id, Exnode exnode, DownloadPanel downloadPanel)
	{
		this.session_id = session_id;
		this.filename = exnode.filename();
		this.exnode = exnode;
		this.client = downloadPanel;

		try
		{
			IO.Options opts = new IO.Options();
			opts.forceNew = true;
			this.mapSocket = IO.socket(Configuration.bd_ui_progress_map_view_url, opts);

			mapSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener()
			{
				@Override
				public void call(Object... objects)
				{
					sendIdentity();
					log.info(this + ": connected.");
				}
			});

			mapSocket.connect();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void sendMessage(String type, JSONObject message)
	{
		log.info("Sending " + type + " -> " + message.toString());

		mapSocket.emit(type, message, new Ack()
		{
			@Override
			public void call(Object... args)
			{
				System.out.printf("Got an ack\n");
				System.exit(-1);
			}
		});
	}
	
	private void sendIdentity()
	{
		try
		{
			/* Create the message */
			JSONObject registerMessage = new JSONObject();
			registerMessage.put("sessionId", session_id)
						   .put("filename", filename)
						   .put("size", exnode.length())
						   .put("connections", client.transfer_settings_connections())
						   .put("timestamp", System.currentTimeMillis());
			
			sendMessage("peri_download_register", registerMessage);
		}
		catch (JsonException e)
		{
			e.printStackTrace();
		}
	}

	private void sendTransfer(Depot depot, long offset, long amtRead)//, String speed, String depotSpeed)
	{
		try
		{
			JSONObject transferMessage = new JSONObject();
			transferMessage.put("sessionId", session_id)
						   .put("host", depot.host)
						   .put("offset", offset)
						   .put("length", amtRead)
						   .put("timestamp", System.currentTimeMillis());

			sendMessage("peri_download_pushdata", transferMessage);
		}
		catch (JsonException e)
		{
			e.printStackTrace();
		}
	}

	private void sendClear()
	{
		try
		{
			JSONObject clearMessage = new JSONObject();
			clearMessage.put("sessionId", session_id)
						.put("timestamp", System.currentTimeMillis());

			sendMessage("peri_download_clear", clearMessage);
		}
		catch (JsonException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void progressDone(ProgressEvent success)
	{
		sendClear();
	}

	@Override
	public synchronized void progressError(ProgressEvent error)
	{
		sendClear();
	}

	@Override
	public synchronized void progressUpdated(ProgressEvent update)
	{
		sendTransfer(update.depot, update.transfer_offset, update.transfer_length);
	}
	
	public String toString() {
		return "Map Visualizer #" + session_id;
	}
}
