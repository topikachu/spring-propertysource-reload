package io.github.topikachu.properties.reload;

import lombok.Builder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;

@Builder
@Order
public class ReloadablePropertySourceLocator implements PropertySourceLocator {

	private final Log logger = LogFactory.getLog(getClass());

	private ResourceLoader resourceLoader;

	private ReloadableProperties reloadProperties;

	private List<PropertySourceLoader> propertySourceLoaders;

	@PostConstruct
	public void init() {
		this.propertySourceLoaders = SpringFactoriesLoader.loadFactories(PropertySourceLoader.class,
				getClass().getClassLoader());
	}

	@Override
	public PropertySource<?> locate(Environment environment) {
		return null;
	}

	@Override
	public Collection<PropertySource<?>> locateCollection(Environment environment) {
		CompositePropertySource reloadablePropertySources = new CompositePropertySource("ReloadablePropertySources");
		reloadProperties.getPropertiesFiles().stream()
				.filter(location -> location != null && !location.trim().equals("")).map(location -> {
					try {
						String resolvedLocation = environment.resolveRequiredPlaceholders("file:" + location);
						Resource resource = this.resourceLoader.getResource(resolvedLocation);
						if (!resource.exists()) {
							if (reloadProperties.isIgnoreResourceNotFound()) {
								if (logger.isInfoEnabled()) {
									logger.info("Can't configuration find file [" + location + "]");
								}
								return null;
							}
							else {
								throw new ReloadableException("Can't find the configuration file: [" + location + "]");
							}
						}
						for (PropertySourceLoader loader : this.propertySourceLoaders) {
							if (canLoadFileExtension(loader, location)) {
								return loader.load(location, resource);
							}
						}
						throw new ReloadableException("Not a valid configuration file name: [" + location + "]");
					}
					catch (Exception ex) {
						if (reloadProperties.isIgnoreResourceLoadError()) {
							if (logger.isInfoEnabled()) {
								logger.info("Can't load configuration file [" + location + "]" + ex.getMessage());
							}
							return null;
						}
						else {
							throw new ReloadableException("Can't load the configuration file: [" + location + "]", ex);
						}
					}

				}).filter(Objects::nonNull).flatMap(List::stream).filter(Objects::nonNull)
				.forEach(reloadablePropertySources::addPropertySource);
		return Collections.singletonList(reloadablePropertySources);
	}

	private boolean canLoadFileExtension(PropertySourceLoader loader, String name) {
		return Arrays.stream(loader.getFileExtensions())
				.anyMatch((fileExtension) -> StringUtils.endsWithIgnoreCase(name, fileExtension));
	}

}
