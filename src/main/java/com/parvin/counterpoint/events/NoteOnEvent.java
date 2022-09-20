package com.parvin.counterpoint.events;

/**
 * TODO
 * @author dparvin
 *
 */
public class NoteOnEvent {
	private int note;
	private long tick;

	/**
	 * TODO
	 * @param note
	 * @param tick
	 */
	public NoteOnEvent(int note, long tick) {
		this.note = note;
		this.tick =tick;
	}

	/**
	 * TODO
	 * @return
	 */
	public int getNote() {
		return note;
	}

	/**
	 * TODO
	 * @param note
	 */
	public void setNote(int note) {
		this.note = note;
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
