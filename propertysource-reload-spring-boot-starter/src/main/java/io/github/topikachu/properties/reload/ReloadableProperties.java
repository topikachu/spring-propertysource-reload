package io.github.topikachu.properties.reload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static io.github.topikachu.properties.reload.ReloadableProperties.StrategyType.REFRESH;
import static io.github.topikachu.properties.reload.ReloadableProperties.StrategyType.RESTART_OR_SHUTDOWN;

@ConfigurationProperties(prefix = "propertysource.reload")
@Setter
@Getter
public class ReloadableProperties {

	private Duration pollInterval = Duration.ofSeconds(5);

	private List<String> propertiesFiles = Collections.emptyList();

	private boolean ignoreResourceNotFound = true;

	private boolean ignoreResourceLoadError = true;

	private ReloadStrategy strategy = ReloadStrategy.REFRESH_SCOPE;

	private Duration maxWaitForShutdown = Duration.ofSeconds(2);

	private Duration maxWaitForSystemExit = Duration.ofSeconds(5);

	private boolean enabled = true;

	public enum StrategyType {

		REFRESH, RESTART_OR_SHUTDOWN

	}

	@AllArgsConstructor
	@Getter
	public enum ReloadStrategy {

		REFRESH_SCOPE(REFRESH), REFRESH_ENVIRONMENT(REFRESH), EXIT_APPLICATION(
				RESTART_OR_SHUTDOWN), EXIT_APPLICATION_FORCE(RESTART_OR_SHUTDOWN), RESTART(RESTART_OR_SHUTDOWN);

		private StrategyType strategyType;

	}

}
