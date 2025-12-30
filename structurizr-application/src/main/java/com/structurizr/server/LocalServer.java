package com.structurizr.server;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.util.Properties;

import static com.structurizr.configuration.StructurizrProperties.DATA_DIRECTORY;

public class LocalServer extends AbstractServer {

	public static void main(String[] args) {
		Properties properties = new Properties();

		if (args.length > 1) {
			File structurizrDataDirectory = new File(args[1]);
			properties.setProperty(DATA_DIRECTORY, structurizrDataDirectory.getAbsolutePath());
		}

		Configuration.init(Profile.Local, properties);
		Configuration.getInstance().banner(LocalServer.class);

		SpringApplication app = new SpringApplication(LocalServer.class);
		app.setAdditionalProfiles("command-local");
		app.run(args);
	}

	@EventListener
	public void onApplicationEvent(final ServletWebServerInitializedEvent event) {
		Configuration.getInstance().setWebUrl("http://localhost:" + event.getWebServer().getPort());
	}

}