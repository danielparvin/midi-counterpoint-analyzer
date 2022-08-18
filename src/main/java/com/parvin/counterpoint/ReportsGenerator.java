package com.parvin.counterpoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 * TODO
 * @author dparvin
 *
 */
public class ReportsGenerator {
	private Track[] tracks;
	
	/**
	 * TODO
	 * @param tracks
	 */
	public ReportsGenerator(Track[] tracks) {
		this.tracks = tracks;
	}
	
	/**
	 * TODO
	 * @return
	 */
	public List<Report> generateReports() {
		List<Report> contrapuntalMotionReports = new ArrayList<>();
		if (tracks.length < 2) {
			return contrapuntalMotionReports;
		}
		
		for (int track = 0; track < tracks.length; track++) {
			for (int comparisonTrack = track + 1; comparisonTrack < tracks.length; comparisonTrack++) {
				List<MotionEvent> motionEvents = getMotionEvents(tracks[track], tracks[comparisonTrack]);
				contrapuntalMotionReports.add(new Report(track, comparisonTrack, motionEvents));
			}
		}
		
		return contrapuntalMotionReports;
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

	private List<MotionEvent> getMotionEvents(List<IntervalEvent> events, 
			List<IntervalEvent> comparisonEvents) {
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
				Motion motion = getMotion(eventInterval, comparisonEventInterval);
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

	private Motion getMotion(int interval, int comparisonInterval) {
		if (interval == comparisonInterval) {
			return Motion.PARALLEL; // TODO Implement more logic to account for loose parallel motion.
		} else if ((interval > 0 && comparisonInterval > 0) 
				|| (interval < 0 && comparisonInterval < 0)) {
			return Motion.SIMILAR;
		} else if (interval == 0 || comparisonInterval == 0) {
			return Motion.OBLIQUE;
		} else {
			return Motion.CONTRARY;
		}
	}
}
