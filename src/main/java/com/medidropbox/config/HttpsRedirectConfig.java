package com.medidropbox.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * HTTPS Redirect Configuration
 * Forces HTTPS in production (compliance requirement)
 * 
 * Note: For production, it's recommended to use a reverse proxy (nginx, ALB) 
 * for SSL termination instead of handling it in the application
 */
@Configuration
@Profile("production")
public class HttpsRedirectConfig {
    
    @Value("${server.http.port:8080}")
    private int httpPort;
    
    @Value("${server.port:8443}")
    private int httpsPort;
    
    /**
     * Redirect HTTP to HTTPS
     * Creates an HTTP connector that redirects all traffic to HTTPS
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainerCustomizer() {
        return factory -> {
            Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
            connector.setScheme("http");
            connector.setPort(httpPort);
            connector.setSecure(false);
            connector.setRedirectPort(httpsPort);
            factory.addAdditionalTomcatConnectors(connector);
        };
    }
}
