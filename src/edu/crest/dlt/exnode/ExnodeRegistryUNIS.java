/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.exnode;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import edu.crest.dlt.exception.CreateResourceException;
import edu.crest.dlt.utils.Configuration;

public class ExnodeRegistryUNIS
{
	private static Logger log = Logger.getLogger(ExnodeRegistryUNIS.class.getName());

	private static final String REQUEST_TYPE = "POST";

	private URL url;

	public ExnodeRegistryUNIS(URL url)
	{
		this.url = url;
	}

	/**
	 * @param exnode
	 * @algorithm connect to the UNIS instance; HTTP POST the exnode's json;
	 * @return selfRef of the newly registered exnode
	 * @throws CreateResourceException
	 */
	public String register_exnode(Exnode exnode) throws CreateResourceException
	{
		OutputStreamWriter writer_stream_out = null;
		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			log.info(this + ": [CONNECTED]");

			connection.setConnectTimeout(Configuration.dlt_exnode_registry_unis_request_timeout);
			connection.setRequestMethod(REQUEST_TYPE);
			connection.setRequestProperty("Content-Type",
					Configuration.dlt_exnode_registry_unis_content_type);
			connection.setDoOutput(true);

			writer_stream_out = new OutputStreamWriter(connection.getOutputStream());
			writer_stream_out.write(exnode.json().toString());
			writer_stream_out.flush();
			log.info(this + ": [WRITTEN] " + exnode);

			if (connection.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
				JsonReader reader_json = Json.createReader(connection.getInputStream());
				JsonObject response_json = reader_json.readObject();

				String exnode_reference = response_json.get("selfRef").toString();
				log.info(this + ": [DONE] registered " + exnode + " as " + exnode_reference);
				return exnode_reference;
			} else {
				throw new IOException();
			}
		} catch (Exception e) {
			log.severe(this + ": [FAILED] could not register " + exnode);
			throw new CreateResourceException(url.toString());
		} finally {
			if (writer_stream_out != null) {
				try {
					writer_stream_out.close();
				} catch (IOException e) {
					log.severe(this + ": failed to close output stream.");
				}
			}
		}
	}

	/**
	 * @param directory
	 * @algorithm connect to the UNIS instance; HTTP POST the directory's json;
	 * @return id of the newly registered directory
	 * @throws CreateResourceException
	 */
	public JsonObject register_directory(Directory directory) throws CreateResourceException
	{
		OutputStreamWriter writer_stream_out = null;
		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			log.info(this + ": [CONNECTED]");

			connection.setConnectTimeout(Configuration.dlt_exnode_registry_unis_request_timeout);
			connection.setRequestMethod(REQUEST_TYPE);
			connection.setRequestProperty("Content-Type",
					Configuration.dlt_exnode_registry_unis_content_type);
			connection.setDoOutput(true);

			writer_stream_out = new OutputStreamWriter(connection.getOutputStream());
			writer_stream_out.write(directory.json().toString());
			writer_stream_out.flush();
			log.info(this + ": [WRITTEN] " + directory);

			if (connection.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
				JsonReader reader_json = Json.createReader(connection.getInputStream());
				JsonObject response_json = reader_json.readObject();

				String directory_reference = response_json.get("selfRef").toString();
				log.info(this + ": [DONE] registered/found " + directory + " as " + directory_reference);
				return response_json;
			} else {
				throw new IOException();
			}
		} catch (Exception e) {
			log.severe(this + ": [FAILED] could not register " + directory);
			throw new CreateResourceException(url.toString());
		} finally {
			if (writer_stream_out != null) {
				try {
					writer_stream_out.close();
				} catch (IOException e) {
					log.severe(this + ": failed to close output stream.");
				}
			}
		}
	}

	public String toString()
	{
		return "UNIS [" + url.toString() + "]";
	}
}
