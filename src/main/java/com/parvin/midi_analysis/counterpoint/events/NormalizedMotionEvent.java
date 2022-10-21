package com.parvin.midi_analysis.counterpoint.events;

/**
 * Represents a {@link MotionEvent} with timing normalized (between 0 and 100) by the total number of ticks 
 * in the enclosing track.
 */
public final class NormalizedMotionEvent extends MotionEvent {
	private final double normalizedTiming;

	public NormalizedMotionEvent(ContrapuntalMotion motion, long tick, long totalTicks) {
		super(motion, tick);
		normalizedTiming = tick / (double) totalTicks;
	}

	public double getNormalizedTiming() {
		return normalizedTiming;
	}
}
