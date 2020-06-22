package io.github.topikachu.properties.reload;

import org.springframework.boot.SpringApplication;

public abstract class ApplicationHolder {

	private static SpringApplication springApplication;

	public static SpringApplication getSpringApplication() {
		return springApplication;
	}

	public static void setSpringApplication(SpringApplication springApplication) {
		ApplicationHolder.springApplication = springApplication;
	}

}
