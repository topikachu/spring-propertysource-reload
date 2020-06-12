package io.github.topikachu.properties.reload.example.web;

import io.github.topikachu.properties.reload.example.config.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class DemoController {


	@Autowired
	private ApplicationProperties applicationProperties;
	@Autowired
	private Environment environment;

	@GetMapping("/greeting")
	public String hello() {
		return applicationProperties.getName() + " " + applicationProperties.getBar();

	}
}
