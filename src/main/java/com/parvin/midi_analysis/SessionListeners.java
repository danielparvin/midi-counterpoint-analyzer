package com.parvin.midi_analysis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
				event.getSession().setAttribute(StaticStrings.UPLOADED_FILES, new ArrayList<File>());
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
				if (session.getAttribute(StaticStrings.UPLOADED_FILES) != null) {
					uploadedFiles = (List<File>) session.getAttribute(StaticStrings.UPLOADED_FILES);
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
