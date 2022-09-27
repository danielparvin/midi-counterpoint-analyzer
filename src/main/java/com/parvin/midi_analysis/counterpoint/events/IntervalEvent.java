package com.parvin.midi_analysis.counterpoint.events;

/**
 * Represents the interval (in positive or negative half steps) from the previous note to the current note 
 * at a specified time (or "tick").
 * @author dparvin
 */
public final class IntervalEvent {
	private final int interval;
	private final long tick;
	
	public IntervalEvent(int interval, long tick) {
		this.interval = interval;
		this.tick = tick;
	}
	
	public int getInterval() {
		return interval;
	}

	public long getTick() {
		return tick;
	}
}
