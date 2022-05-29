package com.parvin.counterpoint;

import java.util.Collections;
import java.util.List;

/**
 * TODO
 * @author dparvin
 *
 */
public class Report {
	int trackNumber;
	int comparisonTrackNumber;
	List<MotionEvent> motionEvents;

	/**
	 * TODO
	 * @param trackNumber
	 * @param comparisonTrackNumber
	 * @param motionEvents
	 */
	public Report(int trackNumber, int comparisonTrackNumber, List<MotionEvent> motionEvents) {
		this.trackNumber = trackNumber;
		this.comparisonTrackNumber = comparisonTrackNumber;
		this.motionEvents = motionEvents;
	}

	/**
	 * TODO
	 * @return
	 */
	public int getTrackNumber() {
		return trackNumber;
	}

	/**
	 * TODO
	 * @return
	 */
	public int getComparisonTrackNumber() {
		return comparisonTrackNumber;
	}

	/**
	 * TODO
	 * @return
	 */
	public List<MotionEvent> getContrapuntalMotionEvents() {
		return Collections.unmodifiableList(motionEvents);
	}
}
