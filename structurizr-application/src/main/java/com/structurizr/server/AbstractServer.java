package com.structurizr.server;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.Filter;
import org.apache.catalina.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.descriptor.web.JspConfigDescriptorImpl;
import org.apache.tomcat.util.descriptor.web.JspPropertyGroup;
import org.apache.tomcat.util.descriptor.web.JspPropertyGroupDescriptorImpl;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@EnableAutoConfiguration(exclude = { SessionAutoConfiguration.class })
@ComponentScan(basePackages = "com.structurizr.server")
@SpringBootConfiguration
@EnableScheduling
public abstract class AbstractServer extends SpringBootServletInitializer {

	@Bean
	public FilterRegistrationBean<? extends Filter> characterEncodingFilterRegistration() {
		CharacterEncodingFilter filter = new CharacterEncodingFilter();
		filter.setEncoding(StandardCharsets.UTF_8.name());
		filter.setForceEncoding(true);

		FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(filter);
		registrationBean.addUrlPatterns("/*");

		return registrationBean;
	}

	@Bean
	public FilterRegistrationBean<? extends Filter> resourceUrlEncodingFilterRegistration() {
		ResourceUrlEncodingFilter filter = new ResourceUrlEncodingFilter();

		FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(filter);
		registrationBean.addUrlPatterns("/*");

		return registrationBean;
	}

	@Bean
	public ConfigurableServletWebServerFactory configurableServletWebServerFactory ( ) {
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
	public void stop() {
		Log log = LogFactory.getLog(AbstractServer.class);

		log.info("********************************************************");
		log.info(" Stopping Structurizr");
		log.info("********************************************************");
	}

}