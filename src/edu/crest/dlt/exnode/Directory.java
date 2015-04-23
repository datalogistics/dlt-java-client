/*******************************************************************************
 * Copyright (c) : See the COPYRIGHT file in top-level/project directory
 *******************************************************************************/
package edu.crest.dlt.exnode;

import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import edu.crest.dlt.exception.CreateResourceException;
import edu.crest.dlt.exnode.metadata.Metadata;
import edu.crest.dlt.exnode.metadata.MetadataContainer;
import edu.crest.dlt.exnode.metadata.MetadataInteger;
import edu.crest.dlt.exnode.metadata.MetadataString;
import edu.crest.dlt.utils.Configuration;

public class Directory extends MetadataContainer
{
	public static final Logger log = Logger.getLogger(Directory.class.getName());

	// private static Map<String, Directory> directories = new HashMap<String,
	// Directory>();

	public Directory parent = null;

	public static Directory directory(String path_from_root)
	{
		if (path_from_root == null || path_from_root.trim().length() == 0) {
			return null;
		}

		StringTokenizer tokenizer = new StringTokenizer(path_from_root, "/\\.");

		Directory directory = null;
		while (tokenizer.hasMoreTokens()) {
			String directory_name = tokenizer.nextToken();

			/* previous directory will be parent of the new directory */
			directory = new Directory(directory, directory_name);

			if (!directory.create_or_fetch()) {
				return null;
			}
		}

		return directory;
	}

	private Directory(Directory parent, String name)
	{
		this.parent = parent;

		// add(new MetadataString("parent", parent != null ? parent.id() : null));
		add(new MetadataString("name", name));
		add(new MetadataString("mode", "directory"));
		add(new MetadataInteger("size", 0));
	}

	public String id()
	{
		return get("id") != null ? get("id").getString() : null;
	}
	
	public String name()
	{
		return get("name").getString();
	}

	public JsonObject json()
	{
		JsonObjectBuilder json_builder = Json.createObjectBuilder();

		Iterator<?> i = iterator();
		while (i.hasNext()) {
			Metadata metadata = (Metadata) i.next();
			if (metadata instanceof MetadataString) {
				json_builder.add(metadata.name, metadata.getString());
			} else if (metadata instanceof MetadataInteger) {
				json_builder.add(metadata.name, metadata.getInteger());
			}
		}

		/* if "parent" not already set */
//		if (id() == null || get("id") == null || get("parent").getString().length() != id().length()) {
		if (get("parent") == null && parent != null) {
			json_builder.add("parent", parent.id());
		} else {
			json_builder.addNull("parent");
		}

		return json_builder.build();
	}
	
	public void json(JsonObject json)
	{
		add(json, "name", MetadataString.class);
		add(json, "parent", MetadataString.class);
		add(json, "created", MetadataInteger.class);
		add(json, "modified", MetadataInteger.class);
		add(json, "selfRef", MetadataString.class);
		add(json, "ts", MetadataInteger.class);
		add(json, "mode", MetadataString.class);
		add(json, "$schema", MetadataString.class);
		add(json, "id", MetadataString.class);
		add(json, "size", MetadataInteger.class);
	}

	public boolean create_or_fetch()
	{
		try {
			long time_now = new Date().getTime();
			add(new MetadataInteger("created", time_now));
			add(new MetadataInteger("modified", time_now));

			json(Configuration.dlt_exnode_registry_unis.register_directory(this));
			return true;
		} catch (CreateResourceException e) {
			log.warning(this + ": [FAILED] unable to create/fetch.");
			return false;
		}
	}

	public String toString()
	{
		StringBuffer strbuf = new StringBuffer();
		strbuf.append("Directory [");
		String path = name();
		Directory directory_previous = parent;
		while (directory_previous != null) {
			path = directory_previous.name() + "/" + path;
			directory_previous = directory_previous.parent;
		}
		strbuf.append("/").append(path);
		strbuf.append("]");
		return strbuf.toString();
	}
}
