package com.parvin.counterpoint;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

/**
 * TODO
 * @author dparvin
 *
 */
public class Launcher {
	/**
	 * TODO
	 * @param args
	 */
	public static void main(String[] args) {
		File midiFile = new File(args[0]);
		try {
			Sequence midiSequence = MidiSystem.getSequence(midiFile);
			List<Report> reports = new ReportsGenerator(midiSequence.getTracks()).generateReports();
			for (Report report : reports) {
				ReportAnalyzer analyzer = new ReportAnalyzer(report);
				int similarNumber = analyzer.countSimilarAndParallelMotionEvents();
				int contraryNumber = analyzer.countContraryMotionEvents();
				double ratio = similarNumber / (double) contraryNumber;
				System.out.println("Similar motion: " + similarNumber
						+ ". Contrary motion: " + contraryNumber
						+ ". Similar/Contrary ratio: " + ratio + ".");
			}
		} catch (InvalidMidiDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
