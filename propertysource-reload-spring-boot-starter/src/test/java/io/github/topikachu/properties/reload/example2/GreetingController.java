package io.github.topikachu.properties.reload.example2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class GreetingController {

	@Autowired
	private GreetingProperties greetingProperties;

	@GetMapping("/greeting")
	public String greeting() {
		return "Hello " + greetingProperties.getName();
	}

}
