package io.github.topikachu.properties.reload;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.File;
import java.util.Set;

@Getter
public class PropertySourceReloadEvent extends ApplicationEvent {

	private final FileEvent fileEvent;
	protected File file;
	protected Set<String> keys;


	@Builder
	public PropertySourceReloadEvent(Object source, File file, Set<String> keys, FileEvent fileEvent) {
		super(source);
		this.file = file;
		this.keys = keys;
		this.fileEvent = fileEvent;
	}

	public enum FileEvent {
		CHANGE,
		CREATE,
		DELETE
	}
}
