package com.parvin.counterpoint.events;

/**
 * Represents a {@link Motion} occurring at a specific time.
 * @author dparvin
 */
public class MotionEvent {
	private Motion motion;
	private long tick;
	
	public MotionEvent(Motion motion, long tick) {
		this.motion = motion;
		this.tick = tick;
	}

	public Motion getMotion() {
		return motion;
	}

	public void setMotion(Motion motion) {
		this.motion = motion;
	}

	public long getTick() {
		return tick;
	}

	public void setTick(long tick) {
		this.tick = tick;
	}
}
