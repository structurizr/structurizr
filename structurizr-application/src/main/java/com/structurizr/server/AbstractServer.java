package com.structurizr.server;

import jakarta.annotation.PreDestroy;
import org.apache.catalina.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.descriptor.web.JspConfigDescriptorImpl;
import org.apache.tomcat.util.descriptor.web.JspPropertyGroup;
import org.apache.tomcat.util.descriptor.web.JspPropertyGroupDescriptorImpl;
import org.springframework.ai.mcp.server.common.autoconfigure.McpServerStatelessAutoConfiguration;
import org.springframework.ai.mcp.server.common.autoconfigure.annotations.McpServerAnnotationScannerAutoConfiguration;
import org.springframework.ai.mcp.server.common.autoconfigure.annotations.StatelessServerSpecificationFactoryAutoConfiguration;
import org.springframework.ai.mcp.server.webmvc.autoconfigure.McpServerStatelessWebMvcAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Collections;
import java.util.List;

@EnableAutoConfiguration(
		exclude = {
				McpServerStatelessWebMvcAutoConfiguration.class,
				McpServerStatelessAutoConfiguration.class,
				StatelessServerSpecificationFactoryAutoConfiguration.class,
				McpServerAnnotationScannerAutoConfiguration.class
		})
@ComponentScan(basePackages = "com.structurizr.server")
@SpringBootConfiguration
@EnableScheduling
public abstract class AbstractServer extends SpringBootServletInitializer {

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