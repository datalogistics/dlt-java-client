package edu.crest.dlt.ui.utils;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.crest.dlt.utils.Configuration;

public class CommandLineOptionParser {
	Options cmdLnOpt;
	CommandLineParser parser;
	CommandLine line;
	static final String syntax = "DltDownloadClient [options]  ...[FILE] ...";

	public CommandLineOptionParser(String[] args) throws ParseException {
		// TODO Auto-generated constructor stub
		cmdLnOpt = new Options();
		parser = new GnuParser();
		addOptions();
		parse(args);
	}

	private void addOptions() {
		cmdLnOpt.addOption(OptionBuilder.withArgName("username").hasArg()
				.withDescription("Username of the user").create("username"));

		cmdLnOpt.addOption(OptionBuilder.withArgName("password").hasArg()
				.withDescription("Password for user").create("password"));

		cmdLnOpt.addOption(OptionBuilder.withArgName("transfer_size").hasArg()
				.withDescription("Chunk size in bytes").create("transfer_size"));

		cmdLnOpt.addOption(OptionBuilder.withArgName("num_connections").hasArg()
				.withDescription("Number of threads to use ")
				.create("num_connections"));

	}

	private void parse(String[] args) throws ParseException {
		line = parser.parse(cmdLnOpt, args);
	}

	public String getUsername() {
		return line.getOptionValue("username","");
	}

	public String getPassword() {
		return line.getOptionValue("password","");
	}

	public long getTransferSize() {
		long ret;
		try {
			ret = Integer.parseInt(line.getOptionValue("transfer_size"));
		} catch (NumberFormatException e) {
			ret = Configuration.dlt_exnode_transfer_size_default;
		}
		return ret;
	}

	public int getNumOfConnections() {
		int ret;
		try {
			ret = Integer.parseInt(line.getOptionValue("num_connections"));
		} catch (NumberFormatException e) {
			ret = Configuration.dlt_exnode_transfer_connections_default;
		}
		return ret;
	}

	public List<String> getFiles() {
		return line.getArgList();
	}

	public void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(syntax, cmdLnOpt);
	}
}
