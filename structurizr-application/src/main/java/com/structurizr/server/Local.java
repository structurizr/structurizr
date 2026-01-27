package com.structurizr.server;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import com.structurizr.view.ThemeUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;

import java.io.File;
import java.util.Properties;

import static com.structurizr.configuration.StructurizrProperties.DATA_DIRECTORY;
import static com.structurizr.configuration.StructurizrProperties.THEMES;

public class Local extends AbstractServer {

	public static void main(String[] args) {
		Properties properties = new Properties();

		if (args.length > 1) {
			File structurizrDataDirectory = new File(args[1]);
			properties.setProperty(DATA_DIRECTORY, structurizrDataDirectory.getAbsolutePath());
		}

		Configuration.init(Profile.Local, properties);
		ThemeUtils.registerThemes(new File(Configuration.getInstance().getProperty(THEMES)));

		SpringApplication app = new SpringApplication(Local.class);
		app.setAdditionalProfiles("command-local");
		app.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) event -> Configuration.getInstance().banner(Local.class));
		app.run(args);
	}

	@EventListener
	public void onApplicationEvent(final ServletWebServerInitializedEvent event) {
		Configuration.getInstance().setWebUrl("http://localhost:" + event.getWebServer().getPort());
	}

}