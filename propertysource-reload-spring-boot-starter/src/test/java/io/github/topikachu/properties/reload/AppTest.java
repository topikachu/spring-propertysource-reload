package io.github.topikachu.properties.reload;

import io.github.topikachu.properties.reload.example.App;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = App.class)
@AutoConfigureWebTestClient

public class AppTest {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@SpyBean
	private PropertySourceReloadEventListener propertySourceReloadEventListener;

	private static File file = new File("test-config/app.properties");

	@BeforeClass
	static public void initConfigFile() throws IOException {

		FileUtils.touch(file);
		Properties properties = new Properties();
		properties.setProperty("bean.name", "World");
		try (FileOutputStream fos = new FileOutputStream(file)) {
			properties.store(fos, "gen by test");
			fos.flush();
		}
	}

	@Test
	@SneakyThrows
	public void testApp() {
		{
			ResponseEntity<String> response = this.testRestTemplate.getForEntity("/greeting", String.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isEqualTo("Hello World");
		}
		Properties properties = new Properties();
		properties.setProperty("bean.name", "World2");
		try (FileOutputStream fos = new FileOutputStream(file)) {
			properties.store(fos, "gen by test");
			fos.flush();
		}
		Thread.sleep(6000);
		ArgumentCaptor<PropertySourceReloadEvent> captor = ArgumentCaptor.forClass(PropertySourceReloadEvent.class);

		verify(propertySourceReloadEventListener, times(1)).onRefresh(captor.capture());
		PropertySourceReloadEvent captorPropertySourceReloadEvent = captor.getValue();
		assertThat(captorPropertySourceReloadEvent.getKeys()).containsOnly("bean.name");
		assertThat(captorPropertySourceReloadEvent.getFileEvent())
				.isEqualTo(PropertySourceReloadEvent.FileEvent.CHANGE);
		assertThat(captorPropertySourceReloadEvent.getFile()).hasName("app.properties");
		{
			ResponseEntity<String> response = this.testRestTemplate.getForEntity("/greeting", String.class);
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			assertThat(response.getBody()).isEqualTo("Hello World2");
		}
	}

	public static class PropertySourceReloadEventListener {

		@EventListener(PropertySourceReloadEvent.class)
		public void onRefresh(PropertySourceReloadEvent event) {

		}

	}

}
