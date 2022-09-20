package com.parvin.counterpoint;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;

public class Launcher {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Enter the full path of a MIDI file as an argument!");
			return;
		}
		File midiFile = new File(args[0]);
		if (midiFile.isDirectory()) {
			System.out.println("Enter the full path of a MIDI file, not a directory!");
			return;
		}
		if (!midiFile.exists()) {
			System.out.println("MIDI file \"" + midiFile + "\" does not exist!");
			return;
		}
		
		try {
			List<Analysis> analyses = new Analyzer(midiFile).analyze();
			for (Analysis analysis : analyses) {
				long numberOfSimilarEvents = analysis.getNumberOfSimilarAndParallelMotionEvents();
				long numberOfContraryEvents = analysis.getNumberOfContraryMotionEvents();
				double ratio = numberOfSimilarEvents / (double) numberOfContraryEvents;
				System.out.println("Similar motion events: " + numberOfSimilarEvents);
				System.out.println("Contrary motion events: " + numberOfContraryEvents);
				System.out.println("Ratio of similar motion events to contrary motion events: " + ratio);
			}
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
