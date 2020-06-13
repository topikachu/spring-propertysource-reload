package io.github.topikachu.properties.reload.example.web;

import io.github.topikachu.properties.reload.PropertySourceReloadEvent;
import io.github.topikachu.properties.reload.example.config.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.event.EventListener;
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

	@Value("${http.name}")
	private String name;

	@GetMapping("/greeting")
	public String hello() {
		return name + " " + applicationProperties.getBar();

	}


	@EventListener(PropertySourceReloadEvent.class)
	public void onRefresh(PropertySourceReloadEvent event) {
		System.out.println(String.join(",", event.getKeys()));
		System.out.println(event.getFile().getName());
		System.out.println(event.getFileEvent());
	}
}
