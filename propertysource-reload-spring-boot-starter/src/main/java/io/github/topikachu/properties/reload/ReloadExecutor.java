package io.github.topikachu.properties.reload;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static io.github.topikachu.properties.reload.ReloadableProperties.ReloadStrategy.*;

@Builder
public class ReloadExecutor {

	private ReloadableProperties reloadableProperties;

	private ContextRefresher contextRefresher;

	private ApplicationEventPublisher applicationEventPublisher;

	private ConfigurableApplicationContext applicationContext;

	@SuppressFBWarnings("DM_EXIT")
	public void executeReload(File file, PropertySourceReloadEvent.FileEvent event) {
		Set<String> keys;
		if (reloadableProperties.getStrategy() == REFRESH_ENVIRONMENT) {
			keys = contextRefresher.refreshEnvironment();
		}
		else if (reloadableProperties.getStrategy() == REFRESH_SCOPE) {
			keys = contextRefresher.refresh();
		}
		else if (reloadableProperties.getStrategy() == EXIT_APPLICATION) {
			waitForShutdown();
			SpringApplication.exit(applicationContext);
			return;
		}
		else if (reloadableProperties.getStrategy() == EXIT_APPLICATION_FORCE) {
			waitForShutdown();
			int status = SpringApplication.exit(applicationContext);
			System.exit(status);
			return;
		}
		else {
			throw new ReloadableException("Not a valid reload strategy " + reloadableProperties.getStrategy());
		}
		if (reloadableProperties.getStrategy() == REFRESH_ENVIRONMENT
				|| reloadableProperties.getStrategy() == REFRESH_SCOPE) {
			applicationEventPublisher.publishEvent(
					PropertySourceReloadEvent.builder().file(file).keys(keys).fileEvent(event).source(this).build());
		}

	}

	@SneakyThrows
	private void waitForShutdown() {
		final long waitMillis = ThreadLocalRandom.current()
				.nextLong(reloadableProperties.getMaxWaitForShutdown().toMillis());
		Thread.sleep(waitMillis);
	}

}
