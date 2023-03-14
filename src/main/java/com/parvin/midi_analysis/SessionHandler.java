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
	@Autowired
	private HttpSession session;
	
	// Default session attribute values
	private static final int DEFAULT_HISTOGRAM_BIN_SIZE = 10;

	// Session attribute names
	private static final String COUNTERPOINT_ANALYSES_LIST = "counterpointAnalyses";
	private static final String COUNTERPOINT_HISTOGRAM_CSV_PATH = "counterpointHistogramCsv";
	private static final String COUNTERPOINT_HISTOGRAM_PNG_PATH = "counterpointHistogramPng";
	private static final String COUNTERPOINT_PIE_CHART_PNG_PATH = "counterpointPieChartPng";
	private static final String HISTOGRAM_BIN_SIZE_INT = "histogramBinSize";
	public static final String MESSAGE = "message";
	private static final String TEMP_DIRECTORY_PATH = "tempDirectory";
	private static final String TOTAL_CONTRARY_EVENTS_LONG = "totalContraryEvents";
	private static final String TOTAL_OBLIQUE_EVENTS_LONG = "totalObliqueEvents";
	private static final String TOTAL_SIMILAR_EVENTS_LONG = "totalSimilarEvents";
	private static final String UPLOADED_MIDI_FILES_SET = "uploadedFiles";
	
	public int getHistogramBinSize() {
		return (int) session.getAttribute(HISTOGRAM_BIN_SIZE_INT);
	}
	
	@SuppressWarnings("unchecked")
	public List<Analysis> getCounterpointAnalyses() {
		return (List<Analysis>) session.getAttribute(COUNTERPOINT_ANALYSES_LIST); // TODO Return immutable collection.
	}
	
	public void clearCounterpointAnalyses() {
		List<Analysis> analyses = (List<Analysis>) session.getAttribute(COUNTERPOINT_ANALYSES_LIST);
		analyses.clear();
	}
	
	@SuppressWarnings("unchecked")
	public Set<Path> getUploadedMidiPaths() {
		return (TreeSet<Path>) session.getAttribute(UPLOADED_MIDI_FILES_SET);
	}
	
	public Path getCounterpointHistogramCsvPath() {
		return (Path) session.getAttribute(COUNTERPOINT_HISTOGRAM_CSV_PATH);
	}
	
	public Path getCounterpointHistogramPngPath() {
		return (Path) session.getAttribute(COUNTERPOINT_HISTOGRAM_PNG_PATH);
	}
	
	public Path getCounterpointPieChartPngPath() {
		return (Path) session.getAttribute(COUNTERPOINT_PIE_CHART_PNG_PATH);
	}
	
	public Path getTempDirectoryPath() {
		return (Path) session.getAttribute(TEMP_DIRECTORY_PATH);
	}

	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

	@Bean
	public ApplicationListener<HttpSessionCreatedEvent> loginListener() {
		return event -> {
			HttpSession createdSession = event.getSession();
			createdSession.setAttribute(COUNTERPOINT_ANALYSES_LIST, new ArrayList<Analysis>());
			createdSession.setAttribute(HISTOGRAM_BIN_SIZE_INT, DEFAULT_HISTOGRAM_BIN_SIZE);
			createdSession.setAttribute(UPLOADED_MIDI_FILES_SET, new TreeSet<Path>());
			try {
				Path tempDirectory = Files.createTempDirectory("session-");
				createdSession.setAttribute(TEMP_DIRECTORY_PATH, tempDirectory);
				Path counterpointHistogramCsv = Files.createTempFile(tempDirectory, "counterpoint-histogram", ".csv");
				createdSession.setAttribute(COUNTERPOINT_HISTOGRAM_CSV_PATH, counterpointHistogramCsv);
				Path counterpointHistogramPng = Files.createTempFile(tempDirectory, "counterpoint-histogram", ".png");
				createdSession.setAttribute(COUNTERPOINT_HISTOGRAM_PNG_PATH, counterpointHistogramPng);
				Path counterpointPieChartPng = Files.createTempFile(tempDirectory, "counterpoint-pie-chart", ".png");
				createdSession.setAttribute(COUNTERPOINT_PIE_CHART_PNG_PATH, counterpointPieChartPng);
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

	public long getNumberOfContraryMotionEvents() {
		if (session.getAttribute(TOTAL_CONTRARY_EVENTS_LONG) != null) {
			return (long) session.getAttribute(TOTAL_CONTRARY_EVENTS_LONG);
		} else {
			return 0L;
		}
	}
	
	public long getNumberOfSimilarMotionEvents() {
		if (session.getAttribute(TOTAL_SIMILAR_EVENTS_LONG) != null) {
			return (long) session.getAttribute(TOTAL_SIMILAR_EVENTS_LONG);
		} else {
			return 0L;
		}
	}
	
	public long getNumberOfObliqueMotionEvents() {
		if (session.getAttribute(TOTAL_OBLIQUE_EVENTS_LONG) != null) {
			return (long) session.getAttribute(TOTAL_OBLIQUE_EVENTS_LONG);
		} else {
			return 0L;
		}
	}

	public void setNumberOfContraryMotionEvents(long numberOfContraryMotionEvents) {
		session.setAttribute(TOTAL_CONTRARY_EVENTS_LONG, numberOfContraryMotionEvents);
	}

	public void setNumberOfSimilarMotionEvents(long numberOfSimilarMotionEvents) {
		session.setAttribute(TOTAL_SIMILAR_EVENTS_LONG, numberOfSimilarMotionEvents);
	}

	public void setNumberOfObliqueMotionEvents(long numberOfObliqueMotionEvents) {
		session.setAttribute(TOTAL_OBLIQUE_EVENTS_LONG, numberOfObliqueMotionEvents);
	}

	public void deleteUploadedFiles(HttpSession session) { // TODO Refactor.
		@SuppressWarnings("unchecked")
		Set<Path> uploadedFiles = (Set<Path>) session.getAttribute(UPLOADED_MIDI_FILES_SET);
		for (Path path: uploadedFiles) {
			try {
				Files.deleteIfExists(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		uploadedFiles.clear();
	}

	public void deleteAnalysisCsvAndPngFiles(HttpSession session) {
		Path counterpointHistogramCsv = (Path) session.getAttribute(COUNTERPOINT_HISTOGRAM_CSV_PATH);
		Path counterpointHistogramPng = (Path) session.getAttribute(COUNTERPOINT_HISTOGRAM_PNG_PATH);
		Path counterpointPieChartPng = (Path) session.getAttribute(COUNTERPOINT_PIE_CHART_PNG_PATH);
		try {
			Files.deleteIfExists(counterpointHistogramCsv);
			Files.deleteIfExists(counterpointHistogramPng);
			Files.deleteIfExists(counterpointPieChartPng);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void deleteTempDirectory(HttpSession session) {
		Path tempDirectory = (Path) session.getAttribute(TEMP_DIRECTORY_PATH);
		try {
			Files.deleteIfExists(tempDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
