package com.structurizr.server.web;

import com.structurizr.configuration.StructurizrProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.CssLinkResourceTransformer;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import java.io.File;
import java.util.concurrent.TimeUnit;

@Configuration
public class Config implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/js/structurizr*", "/static/css/structurizr*")
                .addResourceLocations("classpath:/static/static/js/", "classpath:/static/static/css/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                .resourceChain(false)
                .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"))
                .addTransformer(new CssLinkResourceTransformer());

        registry.addResourceHandler("/static/js/*", "/static/css/*")
                .addResourceLocations("classpath:/static/static/js/", "classpath:/static/static/css/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                .resourceChain(false);

        registry.addResourceHandler("/static/themes/**")
                .addResourceLocations("file:" + new File(com.structurizr.configuration.Configuration.getInstance().getProperty(StructurizrProperties.THEMES)).getAbsolutePath() + File.separator)
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                .resourceChain(false);
    }

}