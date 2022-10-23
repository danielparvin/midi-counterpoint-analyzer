package com.parvin.midi_analysis.counterpoint;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import com.parvin.midi_analysis.counterpoint.events.ContrapuntalMotion;
import com.parvin.midi_analysis.counterpoint.events.NormalizedMotionEvent;

public class CounterpointHistogramMaker {
	private List<Analysis> analyses;
	private int binSize;

	public CounterpointHistogramMaker(List<Analysis> analyses, int binSize) {
		this.analyses = Collections.unmodifiableList(analyses);
		this.binSize  = binSize;
	}

	public JFreeChart generateNormalizedHistogram() {
		HistogramDataset contraryHistogram = new HistogramDataset(0, 100, binSize);
		HistogramDataset similarHistogram = new HistogramDataset(0, 100, binSize);
		HistogramDataset obliqueHistogram = new HistogramDataset(0, 100, binSize);

		for (Analysis analysis: analyses) {
			final long totalTicks = analysis.getTicks();
			List<NormalizedMotionEvent> normalizedMotionEvents = analysis.getMotionEvents().stream()
					.map(event -> new NormalizedMotionEvent(event.getMotion(), event.getTick(), totalTicks))
					.collect(Collectors.toList());
			for (NormalizedMotionEvent normalizedEvent: normalizedMotionEvents) {
				ContrapuntalMotion motionType = normalizedEvent.getMotion();
				switch(motionType) {
				case CONTRARY: 
					contraryHistogram.addObservation(normalizedEvent.getNormalizedTiming());
					break;
				case PARALLEL:
				case SIMILAR:
					similarHistogram.addObservation(normalizedEvent.getNormalizedTiming());
					break;
				case OBLIQUE:
					obliqueHistogram.addObservation(normalizedEvent.getNormalizedTiming());
					break;
				}
			}
		}
		
		XYIntervalSeriesCollection xyCollection = new XYIntervalSeriesCollection();
		xyCollection.addSeries(contraryHistogram.toXYIntervalSeries("Contrary Motion Events"));
		xyCollection.addSeries(similarHistogram.toXYIntervalSeries("Similar Motion Events"));
		xyCollection.addSeries(obliqueHistogram.toXYIntervalSeries("Oblique Motion Events"));
		
		return ChartFactory.createHistogram("Frequency of Contrapuntal Motion Events", "% Total Length", 
				"Number of Events", xyCollection, PlotOrientation.HORIZONTAL, true, false, false);
	}
}
