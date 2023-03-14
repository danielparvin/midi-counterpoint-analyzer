package com.parvin.midi_analysis.counterpoint.events;

/**
 * Represents a {@link ContrapuntalMotion} occurring at a specific time.
 * @author dparvin
 */
public class MotionEvent {
	private ContrapuntalMotion motion;
	private long tick;

	public MotionEvent(ContrapuntalMotion motion, long tick) {
		this.motion = motion;
		this.tick = tick;
	}

	public ContrapuntalMotion getMotion() {
		return motion;
	}

	public void setMotion(ContrapuntalMotion motion) {
		this.motion = motion;
	}

	public long getTick() {
		return tick;
	}

	public void setTick(long tick) {
		this.tick = tick;
	}
}
