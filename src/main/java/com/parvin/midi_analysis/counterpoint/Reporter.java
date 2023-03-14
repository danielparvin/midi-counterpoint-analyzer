package com.parvin.midi_analysis.counterpoint;

import java.util.List;

import com.parvin.midi_analysis.counterpoint.events.ContrapuntalMotion;
import com.parvin.midi_analysis.counterpoint.events.MotionEvent;

/**
 * A helper class to facilitate reporting the statistics of an {@link Analysis}.
 * @author dparvin
 *
 */
public class Reporter {
	private static final double ZERO = 0.0;
	private static final double ONE_HUNDRED = 100.0;
	private final Analysis analysis;

	public Reporter(Analysis analysis) {
		this.analysis = analysis;
	}

	public long countMotionEventsOfType(ContrapuntalMotion type) {
		return countMotionEventsOfTypeInRange(type, ZERO, ONE_HUNDRED);
	}

	public long countMotionEventsOfTypeInRange(ContrapuntalMotion type, double fromPercentage, double toPercentage) {
		if (fromPercentage < ZERO || fromPercentage >= toPercentage || toPercentage > ONE_HUNDRED) {
			throw new IllegalArgumentException("Invalid percentage inputs "
					+ fromPercentage + " and " + toPercentage + "!\n"
					+ "Ensure that fromPercentage is at least 0.0, "
					+ "that fromPercentage is less than toPercentage, "
					+ "and that toPercentage is no greater than 100.0.\n");
		}

		List<MotionEvent> motionEvents;
		if (fromPercentage == ZERO && toPercentage == ONE_HUNDRED) {
			motionEvents = analysis.getMotionEvents();
		} else {
			long totalTicks = analysis.getTicks();
			// Convert percentage section threshold values to absolute (tick) values.
			long firstTick = (long) (fromPercentage / ONE_HUNDRED * totalTicks);
			long lastTick = (long) (toPercentage / ONE_HUNDRED * totalTicks);
			motionEvents = getPortionOfAnalysis(firstTick, lastTick);
		}

		return motionEvents.stream().filter(event -> event.getMotion() == type).count();
	}

	private List<MotionEvent> getPortionOfAnalysis(long firstTick, long lastTick) {
		return analysis.getMotionEvents()
				.stream()
				.filter(e -> e.getTick() >= firstTick && e.getTick() <= lastTick)
				.toList();
	}
}
