package io.github.topikachu.properties.reload;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Builder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.restart.RestartEndpoint;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static io.github.topikachu.properties.reload.ReloadableProperties.ReloadStrategy.*;
import static io.github.topikachu.properties.reload.ReloadableProperties.StrategyType.REFRESH;
import static io.github.topikachu.properties.reload.ReloadableProperties.StrategyType.RESTART_OR_SHUTDOWN;

@Builder
@SuppressFBWarnings("DM_EXIT")
public class ReloadExecutor {

	private final Log logger = LogFactory.getLog(getClass());

	private ReloadableProperties reloadableProperties;

	private ContextRefresher contextRefresher;

	private ApplicationEventPublisher applicationEventPublisher;

	private ConfigurableApplicationContext applicationContext;

	private RestartEndpoint restartEndpoint;

	public void executeReload(File file, PropertySourceReloadEvent.FileEvent event) {
		if (reloadableProperties.getStrategy().getStrategyType() == REFRESH) {
			refresh(file, event);
		}
		else if (reloadableProperties.getStrategy().getStrategyType() == RESTART_OR_SHUTDOWN) {
			restartOrShutdown();
		}
		else {
			throw new ReloadableException("Not a valid reload strategy " + reloadableProperties.getStrategy());
		}

	}

	private void restartOrShutdown() {
		long waitMillis = ThreadLocalRandom.current().nextLong(reloadableProperties.getMaxWaitForShutdown().toMillis());
		try {
			Thread.sleep(waitMillis);
		}
		catch (InterruptedException e) {
		}
		if (reloadableProperties.getStrategy() == RESTART) {
			Optional.ofNullable(restartEndpoint).ifPresent(RestartEndpoint::restart);
		}
		else if (reloadableProperties.getStrategy() == EXIT_APPLICATION) {
			SpringApplication.exit(applicationContext);
		}
		else if (reloadableProperties.getStrategy() == EXIT_APPLICATION_FORCE) {
			try {
				SpringApplication.exit(applicationContext);
			}
			finally {
				Thread shutdownThread = new Thread(() -> {
					long waitForSystemExitMillis = reloadableProperties.getMaxWaitForSystemExit().toMillis();
					try {
						Thread.sleep(waitForSystemExitMillis);
					}
					catch (InterruptedException e) {
					}
					logger.warn("Not a graceful shutdown. Execute System#exit");
					System.exit(0);
					return;
				});
				shutdownThread.setName("shutdown");
				shutdownThread.setDaemon(true);
				shutdownThread.start();
			}
		}
		else {
			throw new ReloadableException("Not a valid reload strategy " + reloadableProperties.getStrategy());
		}

	}

	private void refresh(File file, PropertySourceReloadEvent.FileEvent event) {
		Set<String> keys;
		if (reloadableProperties.getStrategy() == REFRESH_ENVIRONMENT) {
			keys = contextRefresher.refreshEnvironment();
		}
		else if (reloadableProperties.getStrategy() == REFRESH_SCOPE) {
			keys = contextRefresher.refresh();
		}
		else {
			throw new ReloadableException("Not a valid reload strategy " + reloadableProperties.getStrategy());
		}
		applicationEventPublisher.publishEvent(
				PropertySourceReloadEvent.builder().file(file).keys(keys).fileEvent(event).source(this).build());
	}

}
