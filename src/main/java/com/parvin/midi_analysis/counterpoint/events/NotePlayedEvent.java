package com.parvin.midi_analysis.counterpoint.events;

/**
 * Represents the moment in time (the "tick") at which a note is played.<br>
 * This should <b>not</b> include MIDI Note On events with a velocity of zero.
 * @author dparvin
 */
public final class NotePlayedEvent {
	private final int note;
	private final long tick;
	
	/**
	 * Make a new event representing the moment in time (the "tick") at which a note is played.
	 * @param note The raw MIDI byte number of the note occurring in this event.
	 * @param tick The raw MIDI byte number representing the time at which this event occurs.
	 */
	public NotePlayedEvent(int note, long tick) {
		this.note = note;
		this.tick = tick;
	}

	/**
	 * Get the raw MIDI byte number of the note occurring in this event.
	 * @return Byte number of note.
	 */
	public int getNote() {
		return note;
	}

	/**
	 * Get the raw MIDI byte number representing the time (the "tick") at which this event occurs.
	 * @return Raw tick number of event.
	 */
	public long getTick() {
		return tick;
	}
}
