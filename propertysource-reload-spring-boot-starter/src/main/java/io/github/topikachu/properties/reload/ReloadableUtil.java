package io.github.topikachu.properties.reload;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public abstract class ReloadableUtil {

	static Stream<String> getSourcesAsStream(List<String>... sources) {
		return Arrays.asList(sources).stream().flatMap(Collection::stream)
				.filter(location -> location != null && !location.trim().equals("")).distinct();
	}

}
