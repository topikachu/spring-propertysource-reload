package io.github.topikachu.properties.reload;

import io.github.topikachu.properties.reload.example.App;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = App.class)
@ActiveProfiles(profiles = "files")
@Import({ AppTest.LockConfiguration.class, AppTest.PropertySourceReloadEventListener.class })
public class AppTest {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	private Lock lock;

	@Autowired
	private Condition condition;

	@SpyBean
	private PropertySourceReloadEventListener propertySourceReloadEventListener;

	private static File appProperties = new File("test-config/app.properties");

	private static File app2Properties = new File("test-config/app2.properties");

	@BeforeClass
	static public void initConfigFile() throws IOException {
		FileUtils.forceMkdir(new File("test-config"));
		FileUtils.cleanDirectory(new File("test-config"));

		FileUtils.touch(appProperties);
		writePropertiesFileWithValue(appProperties, "bean.name", "World");
		FileUtils.touch(app2Properties);
	}

	private static void writePropertiesFileWithValue(File file, String s, String word) throws IOException {
		Properties properties = new Properties();
		properties.setProperty(s, word);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			properties.store(fos, "gen by test");
			fos.flush();
		}
	}

	@Test()
	@SneakyThrows
	public void testApp() {

		assertGreetingApiWithContent("Hello World");

		writePropertiesFileWithValue(appProperties, "bean.name", "World2");
		// writePropertiesFileWithValue(app2Properties, "bean.name2", "World2");

		lock.lock();
		condition.await();
		lock.unlock();

		assertPropertySourceReloadEvent();

		assertGreetingApiWithContent("Hello World2");

	}

	private void assertPropertySourceReloadEvent() {
		ArgumentCaptor<PropertySourceReloadEvent> captor = ArgumentCaptor.forClass(PropertySourceReloadEvent.class);
		verify(propertySourceReloadEventListener, times(1)).onRefresh(captor.capture());
		PropertySourceReloadEvent captorPropertySourceReloadEvent = captor.getValue();
		assertThat(captorPropertySourceReloadEvent.getKeys()).contains("bean.name");
		assertThat(captorPropertySourceReloadEvent.getFileEvent())
				.isEqualTo(PropertySourceReloadEvent.FileEvent.CHANGE);
		assertThat(captorPropertySourceReloadEvent.getFile()).hasName("app.properties");
	}

	private void assertGreetingApiWithContent(String content) {
		ResponseEntity<String> response = this.testRestTemplate.getForEntity("/greeting", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(content);
	}

	@Configuration
	static class LockConfiguration {

		@Bean
		public Lock lock() {
			return new ReentrantLock();
		}

		@Bean
		public Condition condition(Lock lock) {
			return lock.newCondition();
		}

	}

	@Component
	public static class PropertySourceReloadEventListener {

		@Autowired
		private Lock lock;

		@Autowired
		private Condition condition;

		@EventListener(PropertySourceReloadEvent.class)
		public void onRefresh(PropertySourceReloadEvent event) {
			lock.lock();
			condition.signalAll();
			lock.unlock();
		}

	}

}
