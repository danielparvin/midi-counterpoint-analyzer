package com.parvin.counterpoint;

import java.util.Collections;
import java.util.List;

import com.parvin.counterpoint.events.Motion;
import com.parvin.counterpoint.events.MotionEvent;

/**
 * Represents the counterpoint analysis of two MIDI tracks.
 * @author dparvin
 */
public class Analysis {
	private int trackNumber;
	private int comparisonTrackNumber;
	private List<MotionEvent> motionEvents;
	private long numberOfSimilarAndParallelMotionEvents;
	private long numberOfContraryMotionEvents;

	/**
	 * Generate a counterpoint analysis of two MIDI tracks.
	 * @param trackNumber
	 * @param comparisonTrackNumber
	 * @param motionEvents
	 */
	public Analysis(int trackNumber, int comparisonTrackNumber, List<MotionEvent> motionEvents) {
		this.trackNumber = trackNumber;
		this.comparisonTrackNumber = comparisonTrackNumber;
		this.motionEvents = motionEvents;
		this.numberOfSimilarAndParallelMotionEvents = countSimilarAndParallelMotionEvents();
		this.numberOfContraryMotionEvents = countContraryMotionEvents();
	}

	public long getNumberOfSimilarAndParallelMotionEvents() {
		return numberOfSimilarAndParallelMotionEvents;
	}

	public long getNumberOfContraryMotionEvents() {
		return numberOfContraryMotionEvents;
	}

	public int getTrackNumber() {
		return trackNumber;
	}

	public int getComparisonTrackNumber() {
		return comparisonTrackNumber;
	}

	public List<MotionEvent> getContrapuntalMotionEvents() {
		return Collections.unmodifiableList(motionEvents);
	}
	
	private long countContraryMotionEvents() {
		return motionEvents.stream()
				.filter(event -> event.getMotion() == Motion.CONTRARY)
				.count();
	}

	private long countSimilarAndParallelMotionEvents() {
		return motionEvents.stream()
				.filter(
						event -> event.getMotion() == Motion.PARALLEL
						|| event.getMotion() == Motion.SIMILAR)
				.count();
	}
}
