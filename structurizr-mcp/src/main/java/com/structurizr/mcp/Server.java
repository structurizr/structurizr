package com.structurizr.mcp;

import org.apache.commons.cli.*;
import org.apache.commons.cli.help.HelpFormatter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Server {

	private static final String DSL = "dsl";
	private static final String SERVER = "server";

	public static void main(String[] args) throws Exception {
		Log log = LogFactory.getLog(Server.class);

		log.info("***********************************************************************************");
		log.info("  _____ _                   _              _          ");
		log.info(" / ____| |                 | |            (_)         ");
		log.info("| (___ | |_ _ __ _   _  ___| |_ _   _ _ __ _ _____ __ ");
		log.info(" \\___ \\| __| '__| | | |/ __| __| | | | '__| |_  / '__|");
		log.info(" ____) | |_| |  | |_| | (__| |_| |_| | |  | |/ /| |   ");
		log.info("|_____/ \\__|_|   \\__,_|\\___|\\__|\\__,_|_|  |_/___|_|   ");
		log.info("                                                      ");
		log.info("v" + new Version().getBuildNumber());
		log.info("***********************************************************************************");

		Options options = new Options();

		Option option = new Option(DSL, DSL, false, "Structurizr DSL tools (validate, parse, inspect) - see https://docs.structurizr.com/dsl");
		option.setRequired(false);
		options.addOption(option);

		option = new Option(SERVER, SERVER, false, "Structurizr server tools - see https://docs.structurizr.com/server");
		option.setRequired(false);
		options.addOption(option);

		CommandLineParser commandLineParser = new DefaultParser();
		CommandLine cmd = commandLineParser.parse(options, args);

		List<String> profiles = new ArrayList<>();

		if (cmd.hasOption(DSL)) {
			profiles.add(DSL);
		}

		if (cmd.hasOption(SERVER)) {
			profiles.add(SERVER);
		}

		if (profiles.isEmpty()) {
			log.fatal("No tools were configured");
			HelpFormatter formatter = HelpFormatter.builder().setShowSince(false).get();
			formatter.printHelp("mcp", "Structurizr v" + new Version().getBuildNumber(), options, "", true);

			System.exit(1);
		}

		SpringApplication app = new SpringApplication(Server.class);

		app.setAdditionalProfiles(profiles.toArray(new String[0]));
		app.run(args);
	}

}