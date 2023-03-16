package com.parvin.midi_analysis.counterpoint;

import java.util.Collections;
import java.util.List;

import com.parvin.midi_analysis.counterpoint.events.MotionEvent;

/**
 * Represents the counterpoint analysis of two MIDI tracks.
 * @author dparvin
 */
public record Analysis(int trackNumber,
		int comparisonTrackNumber,
		String filename,
		List<MotionEvent> motionEvents, 
		long ticks) {
	
	@Override
	public List<MotionEvent> motionEvents() {
		return Collections.unmodifiableList(motionEvents);
	}
}
