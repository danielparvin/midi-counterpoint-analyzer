package com.parvin.counterpoint.analysis;

import java.util.Collections;
import java.util.List;

import com.parvin.counterpoint.events.MotionEvent;

import static com.parvin.counterpoint.events.ContrapuntalMotion.*;

/**
 * Represents the counterpoint analysis of two MIDI tracks.
 * @author dparvin
 */
public final class Analysis {
	private final int comparisonTrackNumber;
	private final String filename;
	private final List<MotionEvent> motionEvents;
	private final long ticks;
	private final int trackNumber;
	
	/**
	 * Generate a counterpoint analysis of two MIDI tracks.
	 * @param filename The name of the MIDI file.
	 * @param ticks The max length of the two tracks in MIDI ticks.
	 * @param trackNumber The primary track.
	 * @param comparisonTrackNumber The comparison track.
	 * @param motionEvents Contrapuntal motion events ocurring at specific ticks.
	 */
	public Analysis(String filename, long ticks, int trackNumber, int comparisonTrackNumber, 
			List<MotionEvent> motionEvents) {
		this.filename = filename;
		this.ticks = ticks;
		this.trackNumber = trackNumber;
		this.comparisonTrackNumber = comparisonTrackNumber;
		this.motionEvents = motionEvents;
	}

	public int getComparisonTrackNumber() {
		return comparisonTrackNumber;
	}

	/**
	 * Get the name of the MIDI file containing the two tracks.
	 * @return Name of MIDI file (e.g. "music.midi" or "bach.mid").
	 */
	public String getFilename() {
		return filename;
	}

	public List<MotionEvent> getMotionEvents() {
		return Collections.unmodifiableList(motionEvents);
	}

	public long getNumberOfContraryMotionEvents() {
		return motionEvents.stream()
				.filter(event -> event.getMotion() == CONTRARY)
				.count();
	}
	
	public long getNumberOfObliqueMotionEvents() {
		return motionEvents.stream()
				.filter(event -> event.getMotion() == OBLIQUE)
				.count();
	}
	
	public long getNumberOfParallelMotionEvents() {
		return motionEvents.stream()
				.filter(event -> event.getMotion() == PARALLEL)
				.count();
	}
	
	/**
	 * Get the number of similar (but <b>not</b> parallel) motion events.
	 * @return Number of similar motion events.
	 */
	public long getNumberOfSimilarMotionEvents() {
		return motionEvents.stream()
				.filter(event -> event.getMotion() == SIMILAR)
				.count();
	}
	
	/**
	 * Get the length of the analysis in MIDI ticks.
	 * @return Length in ticks.
	 */
	public long getTicks() {
		return ticks;
	}

	public int getTrackNumber() {
		return trackNumber;
	}
}
