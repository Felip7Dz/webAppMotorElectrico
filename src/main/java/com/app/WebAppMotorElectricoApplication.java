package com.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@EnableCaching
@ConfigurationProperties
@Configuration
@SpringBootApplication
@ComponentScan(basePackages = "com.app.*")
public class WebAppMotorElectricoApplication extends SpringBootServletInitializer {

	private static final Class<WebAppMotorElectricoApplication> applicationClass = WebAppMotorElectricoApplication.class;
	private static final Log LOG = LogFactory.getLog(applicationClass);

	public static void main(String[] args) {
		try {
			LOG.info("Arranca ...");
			SpringApplication.run(applicationClass, args);
			LOG.info("Arrancado ...");
		} catch (Exception e) {
			LOG.error("Error en la aplicación ...", e);
		}
	}

}
