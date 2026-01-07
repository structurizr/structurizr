package com.structurizr.server;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.configuration.StructurizrProperties;
import org.springframework.boot.SpringApplication;

import java.io.File;
import java.util.Properties;

import static com.structurizr.configuration.StructurizrProperties.AUTHENTICATION_IMPLEMENTATION;
import static com.structurizr.configuration.StructurizrProperties.DATA_DIRECTORY;

public class Server extends AbstractServer {

	public static void main(String[] args) {
		Properties properties = new Properties();

		if (args.length > 1) {
			File structurizrDataDirectory = new File(args[1]);
			properties.setProperty(DATA_DIRECTORY, structurizrDataDirectory.getAbsolutePath());
		}

		Configuration.init(Profile.Server, properties);
		Configuration.getInstance().banner(Server.class);

		SpringApplication app = new SpringApplication(Server.class);
		app.setAdditionalProfiles(
				"command-server",
				"authentication-" + Configuration.getInstance().getProperty(AUTHENTICATION_IMPLEMENTATION),
				"session-" + Configuration.getInstance().getProperty(StructurizrProperties.SESSION_IMPLEMENTATION));
		app.run(args);
	}

}