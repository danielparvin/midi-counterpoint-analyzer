package com.parvin.counterpoint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sound.midi.InvalidMidiDataException;

import com.parvin.counterpoint.analysis.Analysis;
import com.parvin.counterpoint.analysis.Analyzer;
import com.parvin.counterpoint.analysis.Reporter;

import static com.parvin.counterpoint.events.ContrapuntalMotion.*;

public final class Launcher {
	private static final String DIGITS = "(\\p{Digit}+)";
	private static final String HEX_DIGITS = "(\\p{XDigit}+)";
	private static final String EXP = "[eE][+-]?" + DIGITS;
	/**
	 * Floating-point String pattern from Javadoc of {@code Double.valueOf(String)} method.
	 */
	private static final String FP_REGEX =
			("[\\x00-\\x20]*" +
					"[+-]?(" +
					"NaN|" +
					"Infinity|" +
					"((("+DIGITS+"(\\.)?("+DIGITS+"?)("+EXP+")?)|" +
					"(\\.("+DIGITS+")("+EXP+")?)|" +
					"((" +
					"(0[xX]" + HEX_DIGITS + "(\\.)?)|" +
					"(0[xX]" + HEX_DIGITS + "?(\\.)" + HEX_DIGITS + ")" +
					")[pP][+-]?" + DIGITS + "))" +
					"[fFdD]?))" +
					"[\\x00-\\x20]*");
	private static final Pattern FLOATING_POINT = Pattern.compile(FP_REGEX);


	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Enter the full path of a MIDI file or folder of MIDI files as an argument!");
			return;
		}

		File file = new File(args[0]);
		if (!file.exists()) {
			System.out.println("\"" + file + "\" does not exist!");
			return;
		}

		try {
			List<Analysis> analyses;
			boolean isDirectory = file.isDirectory();
			if (isDirectory) {
				List<File> midiFiles = List.of(file.listFiles(f -> f.getName().toLowerCase().endsWith(".mid")
						|| f.getName().toLowerCase().endsWith(".midi")));
				if (midiFiles.isEmpty()) {
					System.out.println("No MIDI files were found in " + file + "!");
				}
				analyses = analyzeMultipleFiles(midiFiles);
			} else {
				analyses = new Analyzer(file).analyzeAllTracks();
			}

			// Report on sections of the analyses.
			if (args.length > 1) {
				List<Reporter> reporters = analyses.stream()
						.map(a -> new Reporter(a))
						.collect(Collectors.toList());
				
				// Convert Strings args to section beginning and section ending percentage value pairs.
				String[] sectionArgs = Arrays.copyOfRange(args, 1, args.length);
				double[] sections = new double[sectionArgs.length];
				for (int i = 0; i < sectionArgs.length; i++) {
					String sectionString = sectionArgs[i];
					if (isFloatingPoint(sectionString)) {
						sections[i] = Double.valueOf(sectionString);
					} else {
						throw new IllegalArgumentException(""); // TODO
					}
				}
				
				// Report on each section.
				for (int i = 0; i <= sections.length - 2; i += 2) {
					double fromPercentage = sections[i];
					double toPercentage = sections[i + 1];
					long numberOfSimilarEvents = 0L;
					long numberOfParallelEvents = 0L;
					long numberOfContraryEvents = 0L;

					for (Reporter reporter: reporters) {
						numberOfSimilarEvents += reporter
								.countMotionEventsOfTypeInRange(SIMILAR, fromPercentage, toPercentage);
						numberOfParallelEvents += reporter
								.countMotionEventsOfTypeInRange(PARALLEL, fromPercentage, toPercentage);
						numberOfContraryEvents += reporter
								.countMotionEventsOfTypeInRange(CONTRARY, fromPercentage, toPercentage);
					}
					System.out.println("Total number of similar motion events from " + fromPercentage + "% "
							+ "to " + toPercentage + "% = " + numberOfSimilarEvents);
					System.out.println("Total number of parallel motion events from " + fromPercentage + "% "
							+ "to " + toPercentage + "% = " + numberOfParallelEvents);
					System.out.println("Total number of contrary motion events from " + fromPercentage + "% "
							+ "to " + toPercentage + "% = " + numberOfContraryEvents);

					double ratio = (numberOfSimilarEvents + numberOfParallelEvents) / (double) numberOfContraryEvents;
					System.out.println("Ratio of similar + parallel motion to contrary motion = " + ratio);
				}
			}
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static List<Analysis> analyzeMultipleFiles(List<File> midiFiles) 
			throws InvalidMidiDataException, IOException {
		List<Analysis> analyses = new ArrayList<>(midiFiles.size());
		for (File midiFile: midiFiles) {
			Analyzer analyzer = new Analyzer(midiFile);
			analyses.addAll(analyzer.analyzeAllTracks());			
		}
		return analyses;
	}

	private static boolean isFloatingPoint(String s) {
		return FLOATING_POINT.matcher(s).matches();
	}
}
