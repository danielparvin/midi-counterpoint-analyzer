package com.parvin.counterpoint.events;

public class IntervalEvent {
	private int interval;
	private long tick;
	
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
