package com.parvin.counterpoint;

/**
 * TODO
 * @author dparvin
 *
 */
public class IntervalEvent {
	private int interval;
	private long tick;
	
	/**
	 * TODO
	 * @param interval
	 * @param tick
	 */
	public IntervalEvent(int interval, long tick) {
		this.interval = interval;
		this.tick = tick;
	}

	/**
	 * TODO
	 * @return
	 */
	public int getInterval() {
		return interval;
	}

	/**
	 * TODO
	 * @return
	 */
	public long getTick() {
		return tick;
	}
}
