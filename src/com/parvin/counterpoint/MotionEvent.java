package com.parvin.counterpoint;

/**
 * TODO
 * @author dparvin
 *
 */
public class MotionEvent {
	private Motion motion;
	private long tick;
	
	/**
	 * TODO
	 * @param motion
	 * @param tick
	 */
	public MotionEvent(Motion motion, long tick) {
		this.motion = motion;
		this.tick = tick;
	}

	/**
	 * TODO
	 * @return
	 */
	public Motion getContrapuntalMotion() {
		return motion;
	}

	/**
	 * TODO
	 * @param motion
	 */
	public void setMotion(Motion motion) {
		this.motion = motion;
	}

	/**
	 * TODO
	 * @return
	 */
	public long getTick() {
		return tick;
	}

	/**
	 * TODO
	 * @param tick
	 */
	public void setTick(long tick) {
		this.tick = tick;
	}
}
