package com.parvin.midi_analysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.stereotype.Component;

import com.parvin.midi_analysis.counterpoint.Analysis;

@Component
public class SessionHandler {	
	public static final String MESSAGE = "message";
	
	// Default session attribute values
	private static final int DEFAULT_HISTOGRAM_BIN_SIZE = 10;
	
	// Session attribute names
	private static final String COUNTERPOINT_ANALYSES = "counterpointAnalyses";
	private static final String COUNTERPOINT_HISTOGRAM_CSV = "counterpointHistogramCsv";
	private static final String COUNTERPOINT_HISTOGRAM_PNG = "counterpointHistogramPng";
	private static final String COUNTERPOINT_PIE_CHART_PNG = "counterpointPieChartPng";
	private static final String HISTOGRAM_BIN_SIZE = "histogramBinSize";
	private static final String TEMP_DIRECTORY = "tempDirectory";
	private static final String TOTAL_CONTRARY_EVENTS = "totalContraryEvents";
	private static final String TOTAL_OBLIQUE_EVENTS = "totalObliqueEvents";
	private static final String TOTAL_SIMILAR_EVENTS = "totalSimilarEvents";
	private static final String UPLOADED_MIDI_FILES = "uploadedFiles";
	
	@Autowired
	private HttpSession session;
	
	public void clearCounterpointAnalyses() {
		@SuppressWarnings("unchecked")
		List<Analysis> analyses = (List<Analysis>) session.getAttribute(COUNTERPOINT_ANALYSES);
		analyses.clear();
	}

	public void deleteAnalysisCsvAndPngFiles(HttpSession session) {
		@SuppressWarnings("unchecked")
		Path counterpointHistogramCsv = (Path) session.getAttribute(COUNTERPOINT_HISTOGRAM_CSV);
		Path counterpointHistogramPng = (Path) session.getAttribute(COUNTERPOINT_HISTOGRAM_PNG);
		Path counterpointPieChartPng = (Path) session.getAttribute(COUNTERPOINT_PIE_CHART_PNG);
		try {
			Files.deleteIfExists(counterpointHistogramCsv);
			Files.deleteIfExists(counterpointHistogramPng);
			Files.deleteIfExists(counterpointPieChartPng);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteUploadedFiles(HttpSession session) {
		@SuppressWarnings("unchecked")
		Set<Path> uploadedFiles = (Set<Path>) session.getAttribute(UPLOADED_MIDI_FILES);
		for (Path path: uploadedFiles) {
			try {
				Files.deleteIfExists(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		uploadedFiles.clear();
	}

	public int getHistogramBinSize() {
		return (int) session.getAttribute(HISTOGRAM_BIN_SIZE);
	}
	
	@SuppressWarnings("unchecked")
	public List<Analysis> getCounterpointAnalyses() {
		return (List<Analysis>) session.getAttribute(COUNTERPOINT_ANALYSES); // TODO Return immutable collection.
	}
	
	@SuppressWarnings("unchecked")
	public Set<Path> getUploadedMidiPaths() {
		return (TreeSet<Path>) session.getAttribute(UPLOADED_MIDI_FILES);
	}
	
	public Path getCounterpointHistogramCsvPath() {
		return (Path) session.getAttribute(COUNTERPOINT_HISTOGRAM_CSV);
	}
	
	public Path getCounterpointHistogramPngPath() {
		return (Path) session.getAttribute(COUNTERPOINT_HISTOGRAM_PNG);
	}
	
	public Path getCounterpointPieChartPngPath() {
		return (Path) session.getAttribute(COUNTERPOINT_PIE_CHART_PNG);
	}
	
	public long getNumberOfContraryMotionEvents() {
		if (session.getAttribute(TOTAL_CONTRARY_EVENTS) != null) {
			return (long) session.getAttribute(TOTAL_CONTRARY_EVENTS);
		} else {
			return 0L;
		}
	}

	public long getNumberOfObliqueMotionEvents() {
		if (session.getAttribute(TOTAL_OBLIQUE_EVENTS) != null) {
			return (long) session.getAttribute(TOTAL_OBLIQUE_EVENTS);
		} else {
			return 0L;
		}
	}

	public long getNumberOfSimilarMotionEvents() {
		if (session.getAttribute(TOTAL_SIMILAR_EVENTS) != null) {
			return (long) session.getAttribute(TOTAL_SIMILAR_EVENTS);
		} else {
			return 0L;
		}
	}

	public Path getTempDirectoryPath() {
		return (Path) session.getAttribute(TEMP_DIRECTORY);
	}

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

	@Bean
	public ApplicationListener<HttpSessionCreatedEvent> loginListener() {
		return event -> {
			HttpSession createdSession = event.getSession();
			createdSession.setAttribute(COUNTERPOINT_ANALYSES, new ArrayList<Analysis>());
			createdSession.setAttribute(HISTOGRAM_BIN_SIZE, DEFAULT_HISTOGRAM_BIN_SIZE);
			createdSession.setAttribute(UPLOADED_MIDI_FILES, new TreeSet<Path>());
			try {
				Path tempDirectory = Files.createTempDirectory("session-");
				createdSession.setAttribute(TEMP_DIRECTORY, tempDirectory);
				Path counterpointHistogramCsv = Files.createTempFile(tempDirectory, "counterpoint-histogram", ".csv");
				createdSession.setAttribute(COUNTERPOINT_HISTOGRAM_CSV, counterpointHistogramCsv);
				Path counterpointHistogramPng = Files.createTempFile(tempDirectory, "counterpoint-histogram", ".png");
				createdSession.setAttribute(COUNTERPOINT_HISTOGRAM_PNG, counterpointHistogramPng);
				Path counterpointPieChartPng = Files.createTempFile(tempDirectory, "counterpoint-pie-chart", ".png");
				createdSession.setAttribute(COUNTERPOINT_PIE_CHART_PNG, counterpointPieChartPng);
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
	}

	@Bean
	public ApplicationListener<HttpSessionDestroyedEvent> logoutListener() {
		return event -> {
			HttpSession destroyedSession = event.getSession();
			deleteUploadedFiles(destroyedSession);
			deleteAnalysisCsvAndPngFiles(destroyedSession);
			deleteTempDirectory(destroyedSession);
		};
	}

	public void setNumberOfContraryMotionEvents(long numberOfContraryMotionEvents) {
		session.setAttribute(TOTAL_CONTRARY_EVENTS, numberOfContraryMotionEvents);
	}

	public void setNumberOfObliqueMotionEvents(long numberOfObliqueMotionEvents) {
		session.setAttribute(TOTAL_OBLIQUE_EVENTS, numberOfObliqueMotionEvents);
	}

	public void setNumberOfSimilarMotionEvents(long numberOfSimilarMotionEvents) {
		session.setAttribute(TOTAL_SIMILAR_EVENTS, numberOfSimilarMotionEvents);
	}

	private void deleteTempDirectory(HttpSession session) {
		Path tempDirectory = (Path) session.getAttribute(TEMP_DIRECTORY);
		try {
			Files.deleteIfExists(tempDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
