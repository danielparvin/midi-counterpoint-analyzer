package com.parvin.counterpoint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import com.parvin.counterpoint.events.IntervalEvent;
import com.parvin.counterpoint.events.ContrapuntalMotion;
import com.parvin.counterpoint.events.MotionEvent;
import com.parvin.counterpoint.events.NoteOnEvent;

import static com.parvin.counterpoint.events.ContrapuntalMotion.*;

public class Analyzer {
	private Track[] tracks;
	
	public Analyzer(File midiFile) throws InvalidMidiDataException, IOException {
		Sequence midiSequence = MidiSystem.getSequence(midiFile);
		tracks = midiSequence.getTracks();
	}
	
	public List<Analysis> analyze() {
		List<Analysis> analyses = new ArrayList<>();
		if (tracks.length < 2) {
			return analyses;
		}
		
		// Compare each track to each other (e.g. 0:1, 0:2, 0:3, 1:2, 1:3, 2:3 for four tracks).
		for (int trackNum = 0; trackNum < tracks.length; trackNum++) {
			for (int comparisonTrackNum = trackNum + 1; comparisonTrackNum < tracks.length; comparisonTrackNum++) {
				List<MotionEvent> motionEvents = getMotionEvents(tracks[trackNum], tracks[comparisonTrackNum]);
				analyses.add(new Analysis(trackNum, comparisonTrackNum, motionEvents));
			}
		}
		
		return analyses;
	}

	private List<NoteOnEvent> getNoteOnEvents(Track track) {
		List<NoteOnEvent> noteOnEvents = new ArrayList<>();
		for (int i = 0; i < track.size(); i++) {
			MidiEvent event = track.get(i);
			if (event.getMessage() instanceof ShortMessage) {
				ShortMessage shortMessage = (ShortMessage) event.getMessage();
				if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {
					noteOnEvents.add(new NoteOnEvent(shortMessage.getData1(), event.getTick()));
				}
			}
		}

		return noteOnEvents;
	}

	private List<IntervalEvent> getIntervalEvents(List<NoteOnEvent> noteOnEvents) {
		List<IntervalEvent> intervalEvents = new ArrayList<>();
		if (noteOnEvents.size() < 2) {
			return intervalEvents;
		}

		int previousNote = noteOnEvents.get(0).getNote();
		long previousTick = noteOnEvents.get(0).getTick();
		for (int i = 1; i < noteOnEvents.size(); i++) {
			NoteOnEvent noteOnEvent = noteOnEvents.get(i);
			int currentNote = noteOnEvent.getNote();
			long currentTick = noteOnEvent.getTick();
			if (currentTick > previousTick) {
				intervalEvents.add(new IntervalEvent(currentNote - previousNote, currentTick));
			} else if (currentTick == previousTick) {
				if (currentNote > previousNote) {
					intervalEvents.remove(intervalEvents.size() - 1);
					intervalEvents.add(new IntervalEvent(currentNote - previousNote, currentTick));
				}
			} else {
				throw new IllegalArgumentException(""); // TODO
			}
			previousNote = currentNote;
			previousTick = currentTick;
		}

		return intervalEvents;
	}

	private List<MotionEvent> getMotionEvents(List<IntervalEvent> events, List<IntervalEvent> comparisonEvents) {
		List<MotionEvent> motionEvents = new ArrayList<>();
		int eventInterval = 0;
		int comparisonEventInterval = 0;
		long eventTick = -1;
		long comparisonEventTick = -1;
		Iterator<IntervalEvent> comparisonEventsIterator = comparisonEvents.iterator();
		IntervalEvent comparisonEvent;
		for (IntervalEvent event : events) {
			eventInterval = event.getInterval();
			eventTick = event.getTick();
			while (comparisonEventsIterator.hasNext() && comparisonEventTick < eventTick) {
				comparisonEvent = comparisonEventsIterator.next();
				comparisonEventInterval = comparisonEvent.getInterval();
				comparisonEventTick = comparisonEvent.getTick();
			}
			if (eventTick == comparisonEventTick) {
				ContrapuntalMotion motion = getContrapuntalMotion(eventInterval, comparisonEventInterval);
				motionEvents.add(new MotionEvent(motion, comparisonEventTick));
			}
		}

		return motionEvents;
	}

	private List<MotionEvent> getMotionEvents(Track track, Track comparisonTrack) {
		List<IntervalEvent> intervalEvents = getIntervalEvents(getNoteOnEvents(track));
		List<IntervalEvent> comparisonIntervalEvents = getIntervalEvents(getNoteOnEvents(comparisonTrack));
		return getMotionEvents(intervalEvents, comparisonIntervalEvents);
	}

	/**
	 * Determine the type of {@link ContrapuntalMotion contrapuntal motion} between two intervals.<br>
	 * Similar and parallel are exclusive types of motion.<br>
	 * The motion is considered parallel if the two intervals are exactly the same or within a half step.
	 * @param interval
	 * @param comparisonInterval
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
}
