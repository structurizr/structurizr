package com.structurizr.server.web.configuration;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.util.Set;

@org.springframework.context.annotation.Configuration
@EnableWebSecurity
@Profile("command-local")
class LocalConfiguration {

    private static final Set<String> PERMITTED_LOCAL_SERVER_NAMES = Set.of("localhost", "127.0.0.1");

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests.anyRequest().permitAll()
                );

        http
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                );

        return http.build();
    }

    @Bean
    public FilterRegistrationBean<? extends Filter> localFilterRegistration() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new Filter() {
            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
            }

            @Override
            public void destroy() {
            }

            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
                HttpServletRequest request = (HttpServletRequest)servletRequest;
                String serverName = request.getServerName();

                if (!PERMITTED_LOCAL_SERVER_NAMES.contains(serverName)) {
                    throw new RuntimeException("Local mode is only designed to run via localhost URLs (actual URL was " + serverName + ")");
                }

                filterChain.doFilter(servletRequest, servletResponse);
            }
        });
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }

}