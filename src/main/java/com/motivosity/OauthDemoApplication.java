package com.motivosity;

import org.springframework.boot.SpringApplication;

public class OauthDemoApplication {

	/**
	 * Start this class. This starts a Spring Boot app with Tomcat deployed with this sample app.
	 * You can access the app here: http://localhost:9080
	 */
	public static void main(String[] args) {
		SpringApplication.run(DemoController.class, args);
	}

}
