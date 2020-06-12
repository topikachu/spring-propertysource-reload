package io.github.topikachu.properties.reload;


import lombok.Builder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

@Builder
public class ReloadablePropertySourceLocator implements PropertySourceLocator {
	private final Log logger = LogFactory.getLog(getClass());

	private ResourceLoader resourceLoader;
	private ReloadableProperties reloadProperties;
	public static final DefaultPropertySourceFactory FACTORY = new DefaultPropertySourceFactory();


	@Override
	public PropertySource<?> locate(Environment environment) {
		return null;
	}

	@Override
	public Collection<PropertySource<?>> locateCollection(Environment environment) {
		return reloadProperties.getPropertiesFiles().stream()
				.filter(location -> location != null && !location.trim().equals(""))
				.map(location -> {
					try {
						String resolvedLocation = environment.resolveRequiredPlaceholders("file:" + location);
						Resource resource = this.resourceLoader.getResource(resolvedLocation);
						return FACTORY.createPropertySource(location, new EncodedResource(resource));
					} catch (IllegalArgumentException | IOException ex) {
						// Placeholders not resolvable or resource not found when trying to open it
						if (reloadProperties.isIgnoreResourceNotFound()) {
							if (logger.isInfoEnabled()) {
								logger.info("Properties location [" + location + "] not resolvable: " + ex.getMessage());
							}
						} else {
							throw new ReloadableException(ex);
						}
						return null;
					}
				})
				.filter(resource -> resource != null)
				.collect(Collectors.toList());

	}

}
