package io.github.topikachu.properties.reload;

import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.cloud.bootstrap.BootstrapImportSelectorConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class ReloadablePropertiesApplicationListener implements ApplicationListener<ApplicationStartingEvent>, Ordered {

	@Override
	public void onApplicationEvent(ApplicationStartingEvent event) {
		if (!event.getSpringApplication().getAllSources().contains(BootstrapImportSelectorConfiguration.class)) {
			ApplicationHolder.setSpringApplication(event.getSpringApplication());
		}

	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

}
