package io.github.topikachu.properties.reload;

import lombok.Builder;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.cloud.context.refresh.ContextRefresher;

import java.io.File;

@Builder
public class ReloadableWatch {

	private ReloadableProperties reloadProperties;
	private ContextRefresher contextRefresher;

	public void start() throws Exception {
		FileAlterationMonitor monitor = new FileAlterationMonitor(reloadProperties.getPollInterval());
		reloadProperties.getPropertiesFiles().stream()
				.forEach(propertyFile -> {
					File file = new File(propertyFile);
					File folder = file.getParentFile();
					FileAlterationObserver observer = new FileAlterationObserver(folder, new NameFileFilter(file.getName()));
					observer.addListener(new FileAlterationListenerAdaptor() {
						@Override
						public void onFileChange(File file) {
							contextRefresher.refresh();
						}

						@Override
						public void onFileCreate(File file) {
							contextRefresher.refresh();
						}

						@Override
						public void onFileDelete(File file) {
							contextRefresher.refresh();
						}
					});
					monitor.addObserver(observer);
				});
		monitor.start();
	}


}
