package com.parvin.midi_analysis;

import static com.parvin.midi_analysis.StaticStrings.TEMP_DIRECTORY;
import static com.parvin.midi_analysis.StaticStrings.UPLOADED_FILES;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;

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
		return event -> {
			event.getSession().setAttribute(UPLOADED_FILES, new TreeSet<File>());
			try {
				Path tempDirectory = Files.createTempDirectory("temp");
				event.getSession().setAttribute(TEMP_DIRECTORY, tempDirectory);
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
	}

	@Bean
	public ApplicationListener<HttpSessionDestroyedEvent> logoutListener() {
		return event -> {
			HttpSession session = event.getSession();
			@SuppressWarnings("unchecked")
			Set<File> uploadedFiles = (Set<File>) session.getAttribute(UPLOADED_FILES);
			for (File file: uploadedFiles) {
				try {
					Files.deleteIfExists(file.toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Path tempDirectory = (Path) session.getAttribute(TEMP_DIRECTORY);
			try {
				Files.deleteIfExists(tempDirectory);
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
	}
}
