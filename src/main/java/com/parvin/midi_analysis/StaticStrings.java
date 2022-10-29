package com.parvin.midi_analysis;

public class StaticStrings {
	// Endpoints
	public static final String REPORT = "/report";
	
	// Flash attributes
	public static final String MESSAGE = "message";

	// Session attributes
	public static final String COUNTERPOINT_ANALYSES = "counterpointAnalyses";
	public static final String UPLOADED_FILES = "uploadedFiles";
	public static final String TEMP_DIRECTORY = "tempDirectory";
	
	private StaticStrings() throws IllegalStateException {
		throw new IllegalStateException("Utility class!");
	}
}
