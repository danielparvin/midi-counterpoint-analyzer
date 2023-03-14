package com.parvin.midi_analysis.counterpoint.events;

/**
 * Represents a {@link MotionEvent} with timing normalized (between 0 and 100) by the total number of ticks
 * in the enclosing track.
 */
public final class NormalizedMotionEvent extends MotionEvent {
	private final double normalizedTiming;

	public NormalizedMotionEvent(ContrapuntalMotion motion, long tick, long totalTicks) {
		super(motion, tick);
		normalizedTiming = (tick / (double) totalTicks) * 100.0 ;
	}

	/**
	 * Get the normalized time at which this event took place (between 0.0 and 100.0, inclusive).
	 * @return Normalized time (between 0.0 and 100.0, inclusive).
	 */
	public double getNormalizedTiming() {
		return normalizedTiming;
	}
}
