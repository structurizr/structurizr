package com.structurizr.server;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.view.ThemeUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

import java.io.File;
import java.util.Properties;

import static com.structurizr.configuration.StructurizrProperties.*;

public class Server extends AbstractServer {

	public static void main(String[] args) {
		Properties properties = new Properties();

		if (args.length > 1) {
			File structurizrDataDirectory = new File(args[1]);
			properties.setProperty(DATA_DIRECTORY, structurizrDataDirectory.getAbsolutePath());
		}

		Configuration.init(Profile.Server, properties);
		ThemeUtils.registerThemes(new File(Configuration.getInstance().getProperty(THEMES)));

		SpringApplication app = new SpringApplication(Server.class);
		app.setAdditionalProfiles(
				"command-server",
				"authentication-" + Configuration.getInstance().getProperty(AUTHENTICATION_IMPLEMENTATION),
				"session-" + Configuration.getInstance().getProperty(StructurizrProperties.SESSION_IMPLEMENTATION));

		app.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) event -> Configuration.getInstance().banner(Server.class));
		app.run(args);
	}

}