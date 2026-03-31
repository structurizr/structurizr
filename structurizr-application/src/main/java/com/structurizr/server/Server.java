package com.structurizr.server;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.StructurizrProperties;
import com.structurizr.view.ThemeUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.structurizr.configuration.StructurizrProperties.*;

public class Server extends AbstractServer {

	public static void main(String[] args) {
		Properties properties = new Properties();

		if (args.length > 1) {
			File structurizrDataDirectory = new File(args[1]);
			properties.setProperty(DATA_DIRECTORY, structurizrDataDirectory.getAbsolutePath());
		}

		Configuration.initServer(properties);
		ThemeUtils.installThemes(new File(Configuration.getInstance().getProperty(THEMES)));

		List<String> profiles = new ArrayList<>();
		profiles.add("command-server");
		profiles.add("authentication-" + Configuration.getInstance().getProperty(AUTHENTICATION_IMPLEMENTATION));
		profiles.add("session-" + Configuration.getInstance().getProperty(StructurizrProperties.SESSION_IMPLEMENTATION));

		try {
			Class.forName("com.structurizr.server.web.api.AdminApiController");
		} catch (ClassNotFoundException cnfe) {
			profiles.add("open-core");
		}

		SpringApplication app = new SpringApplication(Server.class);
		app.setAdditionalProfiles(profiles.toArray(new String[0]));
		app.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) event -> Configuration.getInstance().banner(Server.class));
		app.run(args);
	}

}