package com.parvin.midi_analysis.counterpoint.events;

/**
 * Represents the interval (in positive or negative half steps) from the previous note to the current note
 * at a specified time (or "tick").
 * @author dparvin
 */
public record IntervalEvent(int interval, long tick) { }