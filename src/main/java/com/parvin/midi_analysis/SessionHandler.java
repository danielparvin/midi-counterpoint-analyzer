package com.parvin.midi_analysis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.session.HttpSessionCreatedEvent;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.stereotype.Component;

import com.parvin.midi_analysis.counterpoint.Analysis;

@Component
public class SessionHandler {
	// Default session attribute values
	private static final int DEFAULT_HISTOGRAM_BIN_SIZE = 10;
	
	// Session attribute names
	public static final String COUNTERPOINT_ANALYSES_LIST = "counterpointAnalyses";
	public static final String COUNTERPOINT_HISTOGRAM_CSV_PATH = "counterpointHistogramCsv";
	public static final String COUNTERPOINT_HISTOGRAM_PNG_PATH = "counterpointHistogramPng";
	public static final String COUNTERPOINT_PIE_CHART_PNG_PATH = "counterpointPieChartPng";
	public static final String HISTOGRAM_BIN_SIZE_INT = "histogramBinSize";
	public static final String TEMP_DIRECTORY_PATH = "tempDirectory";
	public static final String TOTAL_CONTRARY_EVENTS_LONG = "totalContraryEvents";
	public static final String TOTAL_OBLIQUE_EVENTS_LONG = "totalObliqueEvents";
	public static final String TOTAL_SIMILAR_EVENTS_LONG = "totalSimilarEvents";
	public static final String UPLOADED_MIDI_FILES_SET = "uploadedFiles"; // TODO Refactor to use a List instead, for more advanced file-selection analysis options.
	
	@Bean
	public HttpSessionEventPublisher httpSessionEventPublisher() {
		return new HttpSessionEventPublisher();
	}

	@Bean
	public ApplicationListener<HttpSessionCreatedEvent> loginListener() {
		return event -> {
			HttpSession session = event.getSession();
			session.setAttribute(COUNTERPOINT_ANALYSES_LIST, new ArrayList<Analysis>());
			session.setAttribute(HISTOGRAM_BIN_SIZE_INT, DEFAULT_HISTOGRAM_BIN_SIZE);
			session.setAttribute(UPLOADED_MIDI_FILES_SET, new TreeSet<File>());
			try {
				Path tempDirectory = Files.createTempDirectory("temp");
				session.setAttribute(TEMP_DIRECTORY_PATH, tempDirectory);
				Path counterpointHistogramCsv = Files.createTempFile(tempDirectory, "counterpoint-histogram", ".csv");
				session.setAttribute(COUNTERPOINT_HISTOGRAM_CSV_PATH, counterpointHistogramCsv);
				Path counterpointHistogramPng = Files.createTempFile(tempDirectory, "counterpoint-histogram", ".png");
				session.setAttribute(COUNTERPOINT_HISTOGRAM_PNG_PATH, counterpointHistogramPng);
				Path counterpointPieChartPng = Files.createTempFile(tempDirectory, "counterpoint-pie-chart", ".png");
				session.setAttribute(COUNTERPOINT_PIE_CHART_PNG_PATH, counterpointPieChartPng);
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
			Set<File> uploadedFiles = (Set<File>) session.getAttribute(UPLOADED_MIDI_FILES_SET);
			for (File file: uploadedFiles) {
				try {
					Files.deleteIfExists(file.toPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Path counterpointHistogramCsv = (Path) session.getAttribute(COUNTERPOINT_HISTOGRAM_CSV_PATH);			
			Path counterpointHistogramPng = (Path) session.getAttribute(COUNTERPOINT_HISTOGRAM_PNG_PATH);
			Path counterpointPieChartPng = (Path) session.getAttribute(COUNTERPOINT_PIE_CHART_PNG_PATH);
			Path tempDirectory = (Path) session.getAttribute(TEMP_DIRECTORY_PATH);
			try {
				Files.deleteIfExists(counterpointHistogramCsv);
				Files.deleteIfExists(counterpointHistogramPng);
				Files.deleteIfExists(counterpointPieChartPng);
				Files.deleteIfExists(tempDirectory);
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
	}
}
