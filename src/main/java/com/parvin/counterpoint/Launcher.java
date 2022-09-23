package com.parvin.counterpoint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sound.midi.InvalidMidiDataException;

import com.parvin.counterpoint.analysis.Analysis;
import com.parvin.counterpoint.analysis.Analyzer;
import com.parvin.counterpoint.events.ContrapuntalMotion;
import com.parvin.counterpoint.events.MotionEvent;

public final class Launcher {

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
			boolean isDirectory = file.isDirectory();
			List<Analysis> analyses;
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

			long numberOfSimilarEvents = 0L;
			long numberOfParallelEvents = 0L;
			long numberOfContraryEvents = 0L;
			for (Analysis analysis : analyses) {
				numberOfSimilarEvents += analysis.getNumberOfSimilarMotionEvents();
				numberOfParallelEvents += analysis.getNumberOfParallelMotionEvents();
				numberOfContraryEvents += analysis.getNumberOfContraryMotionEvents();
			}
			System.out.println("Similar motion events: " + numberOfSimilarEvents);
			System.out.println("Parallel motion events: " + numberOfParallelEvents);
			System.out.println("Contrary motion events: " + numberOfContraryEvents);

			double ratio = (numberOfSimilarEvents + numberOfParallelEvents) / (double) numberOfContraryEvents;
			System.out.println("Ratio of similar and parallel motion events to contrary motion events: " + ratio);

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
	
	private static long getNumberOfMotionEventsOfType(List<MotionEvent> motionEvents, ContrapuntalMotion type) {
		return motionEvents.stream()
				.filter(event -> event.getMotion() == type)
				.count();
	}

	private static List<MotionEvent> getPortionOfAnalysis(Analysis analysis, long firstTick, long lastTick) {
		return analysis.getMotionEvents()
				.stream()
				.filter(e -> e.getTick() >= firstTick && e.getTick() <= lastTick)
				.collect(Collectors.toList());
	}
}
