package com.structurizr.mcp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Server {

	public static void main(String[] args) {
		Log log = LogFactory.getLog(Server.class);

		log.info("***********************************************************************************");
		log.info("  _____ _                   _              _          ");
		log.info(" / ____| |                 | |            (_)         ");
		log.info("| (___ | |_ _ __ _   _  ___| |_ _   _ _ __ _ _____ __ ");
		log.info(" \\___ \\| __| '__| | | |/ __| __| | | | '__| |_  / '__|");
		log.info(" ____) | |_| |  | |_| | (__| |_| |_| | |  | |/ /| |   ");
		log.info("|_____/ \\__|_|   \\__,_|\\___|\\__|\\__,_|_|  |_/___|_|   ");
		log.info("                                                      ");
		log.info("v" + new Version().getBuildNumber());
		log.info("***********************************************************************************");

		SpringApplication.run(Server.class, args);
	}

}