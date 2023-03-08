package com.parvin.midi_analysis.counterpoint;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.ClusteredXYBarRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import com.parvin.midi_analysis.AnalysisController;
import com.parvin.midi_analysis.counterpoint.events.ContrapuntalMotion;
import com.parvin.midi_analysis.counterpoint.events.NormalizedMotionEvent;

public class CounterpointHistogramMaker {
	private Collection<Analysis> analyses;
	private int binSize;

	public CounterpointHistogramMaker(Collection<Analysis> analyses, int binSize) {
		this.analyses = Collections.unmodifiableCollection(analyses);
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
					.toList();
			for (NormalizedMotionEvent normalizedEvent: normalizedMotionEvents) {
				ContrapuntalMotion motionType = normalizedEvent.getMotion();
				switch(motionType) {
					case CONTRARY: 
						contraryHistogram.addObservation(normalizedEvent.getNormalizedTiming());
						break;
					case PARALLEL, SIMILAR:
						similarHistogram.addObservation(normalizedEvent.getNormalizedTiming());
						break;
					case OBLIQUE:
						obliqueHistogram.addObservation(normalizedEvent.getNormalizedTiming());
						break;
				}
			}
		}

		XYIntervalSeriesCollection xyCollection = new XYIntervalSeriesCollection();
		XYIntervalSeries contraryMotionSeries = contraryHistogram.toXYIntervalSeries("Contrary Motion Events");
		XYIntervalSeries similarMotionSeries = similarHistogram.toXYIntervalSeries("Similar Motion Events");
		XYIntervalSeries obliqueMotionSeries = obliqueHistogram.toXYIntervalSeries("Oblique Motion Events");
		xyCollection.addSeries(contraryMotionSeries);
		xyCollection.addSeries(similarMotionSeries);
		xyCollection.addSeries(obliqueMotionSeries);
		XYBarRenderer barRenderer = new ClusteredXYBarRenderer();
		barRenderer.setSeriesPaint(xyCollection.indexOf(contraryMotionSeries.getKey()), AnalysisController.RED);
		barRenderer.setSeriesPaint(xyCollection.indexOf(similarMotionSeries.getKey()), AnalysisController.BLUE);
		barRenderer.setSeriesPaint(xyCollection.indexOf(obliqueMotionSeries.getKey()), AnalysisController.GREEN);
		NumberAxis xAxis = new NumberAxis("% Total Length");
		xAxis.setRange(0.0, 100.0);
		NumberAxis yAxis = new NumberAxis("Number of Events");
		XYPlot plot = new XYPlot(xyCollection, xAxis, yAxis, barRenderer);
		plot.getRenderer(xyCollection.indexOf("Contrary Motion Events"));
		plot.getRenderer().setSeriesPaint(binSize, null);
		
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.LIGHT_GRAY);
		boolean showLegend = false;
		JFreeChart histogram = new JFreeChart("Frequency of Contrapuntal Motion Events", 
				JFreeChart.DEFAULT_TITLE_FONT,
				plot, 
				showLegend);
		// new StandardChartTheme("JFree").apply(histogram); // TODO Finish styling this histogram chart.
		return histogram;
	}
}
