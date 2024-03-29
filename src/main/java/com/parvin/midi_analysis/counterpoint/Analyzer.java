package com.parvin.midi_analysis.counterpoint;

import static com.parvin.midi_analysis.counterpoint.events.ContrapuntalMotion.CONTRARY;
import static com.parvin.midi_analysis.counterpoint.events.ContrapuntalMotion.OBLIQUE;
import static com.parvin.midi_analysis.counterpoint.events.ContrapuntalMotion.PARALLEL;
import static com.parvin.midi_analysis.counterpoint.events.ContrapuntalMotion.SIMILAR;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import com.parvin.midi_analysis.counterpoint.events.ContrapuntalMotion;
import com.parvin.midi_analysis.counterpoint.events.IntervalEvent;
import com.parvin.midi_analysis.counterpoint.events.MotionEvent;
import com.parvin.midi_analysis.counterpoint.events.NotePlayedEvent;

/**
 * A tool for performing counterpoint analysis on a MIDI file's {@link Track tracks}.
 * @author dparvin
 */
public final class Analyzer {
	private final String filename;
	private final Track[] tracks;

	public Analyzer(String originalFilename, InputStream midiStream) throws InvalidMidiDataException, IOException {
		filename = originalFilename;
		Sequence midiSequence = MidiSystem.getSequence(midiStream);
		tracks = midiSequence.getTracks();
	}

	public Analyzer(File midiFile) throws InvalidMidiDataException, IOException {
		filename = midiFile.getName();
		Sequence midiSequence = MidiSystem.getSequence(midiFile);
		tracks = midiSequence.getTracks();
	}

	public List<Analysis> analyzeAllTracks() {
		List<Analysis> analyses = new ArrayList<>();
		if (tracks.length < 2) { // We need at least two tracks to compare to each other.
			return analyses;
		}

		// Compare each track to each other (e.g. 0:1, 0:2, 0:3, 1:2, 1:3, 2:3 for four tracks).
		for (int trackNum = 0; trackNum < tracks.length; trackNum++) {
			for (int comparisonTrackNum = trackNum + 1; comparisonTrackNum < tracks.length; comparisonTrackNum++) {
				analyses.addAll(analyzeTracks(trackNum, comparisonTrackNum));
			}
		}

		return analyses;
	}

	/**
	 * Perform a counterpoint analysis of two tracks. (The order of the primary and comparison tracks does not matter.)
	 * @param trackNumber Index of the primary track.
	 * @param comparisonTrackNumber Index of the comparison track.
	 * @return Analysis of the contrapuntal motion between the two tracks.
	 * @throws InvalidMidiDataException
	 */
	public List<Analysis> analyzeTracks(int trackNumber, int comparisonTrackNumber) {
		List<Analysis> analyses = new ArrayList<>();
		List<MotionEvent> motionEvents = getMotionEvents(tracks[trackNumber], tracks[comparisonTrackNumber]);
		long ticks = Math.max(tracks[trackNumber].ticks(), tracks[comparisonTrackNumber].ticks());
		analyses.add(new Analysis(trackNumber, comparisonTrackNumber, filename, motionEvents, ticks));
		return analyses;
	}

	public String getFilename() {
		return filename;
	}

	public int getNumberOfTracks() {
		return tracks.length;
	}

	/**
	 * Determine the type of {@link ContrapuntalMotion contrapuntal motion} between two intervals.<br>
	 * Similar and parallel are here considered exclusive types of motion (i.e., parallel is not a specific kind of
	 * similar motion, but is distinct).<br>
	 * The motion is considered parallel if the two intervals are exactly the same or within a half step.
	 * @param interval Primary interval.
	 * @param comparisonInterval Comparison interval.
	 * @return One of oblique, parallel, similar, or contrary {@link ContrapuntalMotion motions}.
	 */
	private ContrapuntalMotion getContrapuntalMotion(int interval, int comparisonInterval) {
		if (interval == 0 || comparisonInterval == 0) {
			return OBLIQUE;
		} else if (interval == comparisonInterval) {
			return PARALLEL;
		} else if (Math.max(interval, comparisonInterval) > 0
				&& Math.min(interval, comparisonInterval) < 0) {
			return CONTRARY;
		} else if (Math.max(interval, comparisonInterval) - Math.min(interval, comparisonInterval) == 1) {
			return PARALLEL;
		} else {
			return SIMILAR;
		}
	}

	/**
	 * Calculate the intervals from the first note to the second note, on to the last note.<br>
	 * When multiple notes occur at the same time, use the highest note of the chord to calculate the interval.
	 * @param notePlayedEvents
	 * @return List of IntervalEvents
	 */
	private List<IntervalEvent> getIntervalEvents(List<NotePlayedEvent> notePlayedEvents) {
		List<IntervalEvent> intervalEvents = new ArrayList<>();
		if (notePlayedEvents.size() < 2) {
			return intervalEvents;
		}

		int previousNote = notePlayedEvents.get(0).note();
		long previousTick = notePlayedEvents.get(0).tick();
		for (int i = 1; i < notePlayedEvents.size(); i++) { // The first interval is between the first and second notes.
			NotePlayedEvent noteOnEvent = notePlayedEvents.get(i);
			int currentNote = noteOnEvent.note();
			long currentTick = noteOnEvent.tick();
			if (currentTick > previousTick) {
				intervalEvents.add(new IntervalEvent(currentNote - previousNote, currentTick));
			} else if (currentTick == previousTick && currentNote > previousNote) { // Use the highest note of the tick.
				intervalEvents.remove(intervalEvents.size() - 1);
				intervalEvents.add(new IntervalEvent(currentNote - previousNote, currentTick));
			}
			previousNote = currentNote;
			previousTick = currentTick;
		}

		return intervalEvents;
	}

	private List<MotionEvent> getMotionEvents(Track track, Track comparisonTrack) {
		List<MotionEvent> motionEvents = new ArrayList<>();
		List<IntervalEvent> intervalEvents = getIntervalEvents(getNoteOnEvents(track));
		List<IntervalEvent> comparisonIntervalEvents = getIntervalEvents(getNoteOnEvents(comparisonTrack));
		int eventInterval = 0;
		int comparisonEventInterval = 0;
		long eventTick = -1;
		long comparisonEventTick = -1;
		Iterator<IntervalEvent> comparisonEventsIterator = comparisonIntervalEvents.iterator();
		IntervalEvent comparisonEvent;
		for (IntervalEvent event : intervalEvents) {
			eventInterval = event.interval();
			eventTick = event.tick();
			while (comparisonEventsIterator.hasNext() && comparisonEventTick < eventTick) {
				comparisonEvent = comparisonEventsIterator.next();
				comparisonEventInterval = comparisonEvent.interval();
				comparisonEventTick = comparisonEvent.tick();
			}
			if (eventTick == comparisonEventTick) {
				ContrapuntalMotion motion = getContrapuntalMotion(eventInterval, comparisonEventInterval);
				motionEvents.add(new MotionEvent(motion, comparisonEventTick));
			}
		}

		return motionEvents;
	}

	private List<NotePlayedEvent> getNoteOnEvents(Track track) {
		List<NotePlayedEvent> noteOnEvents = new ArrayList<>();
		for (int i = 0; i < track.size(); i++) {
			MidiEvent event = track.get(i);
			if (event.getMessage() instanceof ShortMessage shortMessage 
					&& shortMessage.getCommand() == ShortMessage.NOTE_ON
					// (A Note On message with a velocity of zero often substitutes for a Note Off message.)
					&& shortMessage.getData2() != 0) {
				noteOnEvents.add(new NotePlayedEvent(shortMessage.getData1(), event.getTick()));
			}
		}

		return noteOnEvents;
	}
}
