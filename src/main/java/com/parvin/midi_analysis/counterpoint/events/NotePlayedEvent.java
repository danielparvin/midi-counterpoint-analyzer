package com.parvin.midi_analysis.counterpoint.events;

/**
 * Represents the moment in time (the "tick") at which a note is played.<br>
 * This should <b>not</b> include MIDI Note On events with a velocity of zero.
 * @author dparvin
 */
public record NotePlayedEvent(int note, long tick) { }