package com.structurizr.playground;

import com.structurizr.configuration.Configuration;
import com.structurizr.configuration.Profile;
import jakarta.annotation.PreDestroy;
import jakarta.servlet.Filter;
import org.apache.catalina.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.descriptor.web.JspConfigDescriptorImpl;
import org.apache.tomcat.util.descriptor.web.JspPropertyGroup;
import org.apache.tomcat.util.descriptor.web.JspPropertyGroupDescriptorImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.structurizr.configuration.StructurizrProperties.DATA_DIRECTORY;

@EnableAutoConfiguration(exclude = { SessionAutoConfiguration.class })
@ComponentScan(basePackages = "com.structurizr.playground")
@SpringBootConfiguration
public class Server extends SpringBootServletInitializer {

	public static void main(String[] args) {
		File structurizrDataDirectory = new File(Configuration.DEFAULT_STRUCTURIZR_DATA_DIRECTORY);
		if (args.length > 1) {
			structurizrDataDirectory = new File(args[1]);
		}

		Properties properties = new Properties();
		properties.setProperty(DATA_DIRECTORY, structurizrDataDirectory.getAbsolutePath());

		Configuration.init(Profile.Playground, properties);
		Configuration.getInstance().banner(Server.class);

		SpringApplication app = new SpringApplication(Server.class);
		app.run(args);
	}

	@Bean
	FilterRegistrationBean<? extends Filter> characterEncodingFilterRegistration() {
		CharacterEncodingFilter filter = new CharacterEncodingFilter();
		filter.setEncoding(StandardCharsets.UTF_8.name());
		filter.setForceEncoding(true);

		FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(filter);
		registrationBean.addUrlPatterns("/*");

		return registrationBean;
	}

	@Bean
	FilterRegistrationBean<? extends Filter> resourceUrlEncodingFilterRegistration() {
		ResourceUrlEncodingFilter filter = new ResourceUrlEncodingFilter();

		FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(filter);
		registrationBean.addUrlPatterns("/*");

		return registrationBean;
	}

	@Bean
	ConfigurableServletWebServerFactory configurableServletWebServerFactory ( ) {
		return new TomcatServletWebServerFactory() {
			@Override
			protected void postProcessContext(Context context) {
				super.postProcessContext(context);

				JspPropertyGroup jspPropertyGroup = new JspPropertyGroup();
				jspPropertyGroup.addUrlPattern("*.jsp");
				jspPropertyGroup.setPageEncoding("UTF-8");
				jspPropertyGroup.setScriptingInvalid("true");
				jspPropertyGroup.addIncludePrelude("/WEB-INF/fragments/prelude.jspf");
				jspPropertyGroup.addIncludeCoda("/WEB-INF/fragments/coda.jspf");
				jspPropertyGroup.setTrimWhitespace("true");
				jspPropertyGroup.setDefaultContentType("text/html");

				context.setJspConfigDescriptor(
						new JspConfigDescriptorImpl(
								List.of(new JspPropertyGroupDescriptorImpl(jspPropertyGroup)),
								Collections.emptyList()
						)
				);
			}
		};
	}

	@PreDestroy
	void stop() {
		Log log = LogFactory.getLog(Server.class);

		log.info("********************************************************");
		log.info(" Stopping Structurizr");
		log.info("********************************************************");
	}

}