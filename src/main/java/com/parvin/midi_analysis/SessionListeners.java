package com.parvin.midi_analysis;

import static com.parvin.midi_analysis.StaticStrings.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SessionListeners {
	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

	@Bean
	public ApplicationListener<HttpSessionCreatedEvent> loginListener() {
		return new ApplicationListener<HttpSessionCreatedEvent>() {
			@Override
			public void onApplicationEvent(HttpSessionCreatedEvent event) {
				event.getSession().setAttribute(UPLOADED_FILES, new ArrayList<File>());
				try {
					Path tempDirectory = Files.createTempDirectory("temp");
					event.getSession().setAttribute(TEMP_DIRECTORY, tempDirectory);
				} catch (IOException e) {
					e.printStackTrace(); // TODO Figure out what to do in this case.
				}
			}
		};
	}

	@Bean
	public ApplicationListener<HttpSessionDestroyedEvent> logoutListener() {
		return new ApplicationListener<HttpSessionDestroyedEvent>() {
			@SuppressWarnings("unchecked")
			@Override
			public void onApplicationEvent(HttpSessionDestroyedEvent event) {
				HttpSession session = event.getSession();
				List<File> uploadedFiles = new ArrayList<>();
				if (session.getAttribute(UPLOADED_FILES) != null) {
					uploadedFiles = (List<File>) session.getAttribute(UPLOADED_FILES);
				}
				for (File file: uploadedFiles) {
					try {
						Files.deleteIfExists(file.toPath());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
	}
}
