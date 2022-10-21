package com.parvin.midi_analysis.counterpoint;

import java.util.Collections;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYIntervalSeries;

import com.parvin.midi_analysis.counterpoint.events.MotionEvent;
import com.parvin.midi_analysis.counterpoint.events.NormalizedMotionEvent;

public class CounterpointHistogramMaker {
	private List<Analysis> analyses;
	private int binPercentage;
	
	public CounterpointHistogramMaker(List<Analysis> analyses, int binPercentage) {
		this.analyses = Collections.unmodifiableList(analyses);
		this.binPercentage  = binPercentage;
	}
	
	public JFreeChart generateHistogram() {
		XYIntervalSeries contraryMotionEvents = new XYIntervalSeries("Contrary Motion Events");
		XYIntervalSeries similarMotionEvents = new XYIntervalSeries("Similar Motion Events");
		XYIntervalSeries obliqueMotionEvents = new XYIntervalSeries("Oblique Motion Events");
		for (Analysis analysis: analyses) {
			long totalTicks = analysis.getTicks();
			analysis.getMotionEvents().stream()
			.map(event -> new NormalizedMotionEvent(event.getMotion(), event.getTick(), totalTicks))
			;// TODO
		}
		
		return ChartFactory.createHistogram("title here", "x axis label", "y axis label", null, 
				PlotOrientation.HORIZONTAL, true, false, false);
	}
}
