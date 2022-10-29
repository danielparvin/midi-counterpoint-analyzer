package com.parvin.midi_analysis;

public class FilenameUtils {
	/**
	 * Determine whether or not a given filename has a given (case-insensitive) extension.
	 * @param filename Filename to check (e.g. "fugue1.mid" or "midi-files.zip")
	 * @param extension Case-insensitive extension to compare (e.g. "mid" or "midi")
	 * @param otherExtensions Optional, additional extensions to compare with the filename
	 * @return true if the filename ends in any of the provided extensions; false otherwise
	 */
	public static boolean filenameHasExtension(String filename, String extension, String... otherExtensions) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex > 0) {
			String actualExtension = filename.substring(dotIndex + 1);
			if (actualExtension.equalsIgnoreCase(extension)) {
				return true;
			}
			for (String otherExtension: otherExtensions) {
				if (actualExtension.equalsIgnoreCase(otherExtension)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private FilenameUtils() throws IllegalStateException {
		throw new IllegalStateException("Utility class!");
	}
}
