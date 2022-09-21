package com.parvin.counterpoint;

import java.util.Collections;
import java.util.List;

import com.parvin.counterpoint.events.ContrapuntalMotion;
import com.parvin.counterpoint.events.MotionEvent;

/**
 * Represents the counterpoint analysis of two MIDI tracks.
 * @author dparvin
 */
public class Analysis {
	private String filename; // TODO Record the name of the MIDI file related to this analysis.
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
				.filter(event -> event.getMotion() == ContrapuntalMotion.CONTRARY)
				.count();
	}

	private long countSimilarAndParallelMotionEvents() {
		return motionEvents.stream()
				.filter(
						event -> event.getMotion() == ContrapuntalMotion.PARALLEL
						|| event.getMotion() == ContrapuntalMotion.SIMILAR)
				.count();
	}
}
