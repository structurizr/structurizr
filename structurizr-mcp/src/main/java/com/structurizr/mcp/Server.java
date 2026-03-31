package com.structurizr.mcp;

import org.apache.commons.cli.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Server {

	private static final String DSL = "dsl";
	private static final String SERVER_CREATE = "server-create";
	private static final String SERVER_READ = "server-read";
	private static final String SERVER_UPDATE = "server-update";
	private static final String SERVER_DELETE = "server-delete";
	private static final String PLANTUML = "plantuml";
	private static final String MERMAID = "mermaid";

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

		List<String> profiles = new ArrayList<>();

		Options options = new Options();

		Option option = new Option(DSL, DSL, false, "Structurizr DSL tools (validate, parse, inspect) - see https://docs.structurizr.com/dsl");
		option.setRequired(false);
		options.addOption(option);

		option = new Option(SERVER_CREATE, SERVER_CREATE, false, "Structurizr server create tools - see https://docs.structurizr.com/server");
		option.setRequired(false);
		options.addOption(option);

		option = new Option(SERVER_READ, SERVER_READ, false, "Structurizr server read tools - see https://docs.structurizr.com/server");
		option.setRequired(false);
		options.addOption(option);

		option = new Option(SERVER_UPDATE, SERVER_UPDATE, false, "Structurizr server update tools - see https://docs.structurizr.com/server");
		option.setRequired(false);
		options.addOption(option);

		option = new Option(SERVER_DELETE, SERVER_DELETE, false, "Structurizr server delete tools - see https://docs.structurizr.com/server");
		option.setRequired(false);
		options.addOption(option);

		option = new Option(PLANTUML, PLANTUML, false, "PlantUML exports - see https://docs.structurizr.com/export/plantuml and https://docs.structurizr.com/export/c4plantuml");
		option.setRequired(false);
		options.addOption(option);

		option = new Option(MERMAID, MERMAID, false, "Mermaid exports - see https://docs.structurizr.com/export/mermaid");
		option.setRequired(false);
		options.addOption(option);

		try {
			CommandLineParser commandLineParser = new DefaultParser();
			CommandLine cmd = commandLineParser.parse(options, args);

			if (cmd.hasOption(DSL)) {
				profiles.add(DSL);
			}

			if (cmd.hasOption(SERVER_CREATE)) {
				profiles.add(SERVER_CREATE);
			}

			if (cmd.hasOption(SERVER_READ)) {
				profiles.add(SERVER_READ);
			}

			if (cmd.hasOption(SERVER_UPDATE)) {
				profiles.add(SERVER_UPDATE);
			}

			if (cmd.hasOption(SERVER_DELETE)) {
				profiles.add(SERVER_DELETE);
			}

			if (cmd.hasOption(PLANTUML)) {
				profiles.add(PLANTUML);
			}

			if (cmd.hasOption(MERMAID)) {
				profiles.add(MERMAID);
			}
        } catch (ParseException e) {
            log.warn(e.getMessage());
        }

		if (profiles.isEmpty()) {
			log.warn("No tools were configured - configuring DSL tools");
			profiles.add(DSL);
		}

		SpringApplication app = new SpringApplication(Server.class);
		app.setAdditionalProfiles(profiles.toArray(new String[0]));
		app.run(args);
	}

}