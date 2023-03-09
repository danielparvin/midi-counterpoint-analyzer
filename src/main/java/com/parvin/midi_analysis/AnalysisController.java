package com.parvin.midi_analysis;

import static com.parvin.midi_analysis.SessionHandler.COUNTERPOINT_ANALYSES_LIST;
import static com.parvin.midi_analysis.SessionHandler.COUNTERPOINT_HISTOGRAM_CSV_PATH;
import static com.parvin.midi_analysis.SessionHandler.COUNTERPOINT_HISTOGRAM_PNG_PATH;
import static com.parvin.midi_analysis.SessionHandler.COUNTERPOINT_PIE_CHART_PNG_PATH;
import static com.parvin.midi_analysis.SessionHandler.HISTOGRAM_BIN_SIZE_INT;
import static com.parvin.midi_analysis.SessionHandler.TOTAL_CONTRARY_EVENTS_LONG;
import static com.parvin.midi_analysis.SessionHandler.TOTAL_OBLIQUE_EVENTS_LONG;
import static com.parvin.midi_analysis.SessionHandler.TOTAL_SIMILAR_EVENTS_LONG;
import static com.parvin.midi_analysis.SessionHandler.UPLOADED_MIDI_FILES_SET;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import javax.sound.midi.InvalidMidiDataException;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.parvin.midi_analysis.counterpoint.Analysis;
import com.parvin.midi_analysis.counterpoint.Analyzer;
import com.parvin.midi_analysis.counterpoint.CounterpointHistogramMaker;
import com.parvin.midi_analysis.counterpoint.Reporter;
import com.parvin.midi_analysis.counterpoint.events.ContrapuntalMotion;

@Controller
public class AnalysisController {
	public static final Color RED = Color.decode("#ff6347"); // TODO Move this somewhere more appropriate.
	public static final Color ORANGE = Color.decode("#ff6900");
	public static final Color BLUE = Color.decode("#227bff");
	public static final Color GREEN = Color.decode("#00d084");
	private static final String OBLIQUE_MOTION_EVENTS = "Oblique Motion Events";
	private static final String SIMILAR_MOTION_EVENTS = "Similar Motion Events";
	private static final String CONTRARY_MOTION_EVENTS = "Contrary Motion Events";

	@GetMapping("/analysis/csv")
	@ResponseBody
	public ResponseEntity<byte[]> getHistogramCsv(HttpSession session) {
		return getCsvFileByAttributeName(session, COUNTERPOINT_HISTOGRAM_CSV_PATH);
	}

	@GetMapping("/analysis/histogram")
	@ResponseBody
	public ResponseEntity<byte[]> getHistogramImage(HttpSession session) {
		return getPngImageByAttributeName(session, COUNTERPOINT_HISTOGRAM_PNG_PATH);
	}

	@GetMapping("/analysis/pie-chart")
	@ResponseBody
	public ResponseEntity<byte[]> getPieChartImage(HttpSession session) {
		return getPngImageByAttributeName(session, COUNTERPOINT_PIE_CHART_PNG_PATH);
	}

	public ResponseEntity<byte[]> getPngImageByAttributeName(HttpSession session, String attributeName) {
		Path histogramPath = (Path) session.getAttribute(attributeName);
		if (!histogramPath.toFile().exists()) {
			return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
		}
		
		try {
			byte[] imageBytes = Files.readAllBytes(histogramPath);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_PNG); 
			headers.setContentLength(imageBytes.length);
			return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public ResponseEntity<byte[]> getCsvFileByAttributeName(HttpSession session, String attributeName) {
		Path histogramPath = (Path) session.getAttribute(attributeName);
		if (!histogramPath.toFile().exists()) {
			return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
		}
		
		try {
			byte[] imageBytes = Files.readAllBytes(histogramPath);
			HttpHeaders headers = new HttpHeaders();
			headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=counterpoint-histogram.csv");
			headers.set(HttpHeaders.CONTENT_TYPE, "text/csv");
			headers.setContentLength(imageBytes.length);
			return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/image/{filename}")
	@ResponseBody
	public ResponseEntity<byte[]> getImage(HttpSession session, @PathVariable String filename) {
		Path histogramPath = (Path) session.getAttribute(COUNTERPOINT_HISTOGRAM_PNG_PATH);
		if (!histogramPath.toFile().exists()) {
			return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
		}
		
		try {
			byte[] imageBytes = Files.readAllBytes(histogramPath);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_PNG); 
			headers.setContentLength(imageBytes.length);
			return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/analysis")
	public String getAnalysisPage(HttpSession session) {
		return "analysis";
	}

	@GetMapping("/analyze")
	public String analyze(HttpSession session) {
		@SuppressWarnings("unchecked")
		Set<Path> midiFiles = (TreeSet<Path>) session.getAttribute(UPLOADED_MIDI_FILES_SET);
		List<Analysis> analyses = new ArrayList<>();
		for (Path file : midiFiles) {
			try {
				Analyzer analyzer = new Analyzer(file.toFile());
				analyses.addAll(analyzer.analyzeAllTracks());
			} catch (InvalidMidiDataException|IOException e) {
				e.printStackTrace();
			}
		}
		session.setAttribute(COUNTERPOINT_ANALYSES_LIST, analyses);
		recordAnalysisStatsInSession(session, analyses);
		JFreeChart pieChart = generatePieChartFromSessionMidiFiles(session);
		writeAndSavePieChartToSession(session, pieChart);
		int binSize = (int) session.getAttribute(HISTOGRAM_BIN_SIZE_INT);
		CounterpointHistogramMaker counterpointHistogramMaker = new CounterpointHistogramMaker(analyses, binSize);
		JFreeChart histogram = counterpointHistogramMaker.generateNormalizedHistogram();
		try {
			saveHistogramCsvInSession(session, histogram);
		} catch (IOException e) {
			// TODO Handle IOException.
		}
		BufferedImage bufferedImage = histogram.createBufferedImage(800, 800, null); // TODO Make params variable.
		File histogramPng = ((Path) session.getAttribute(COUNTERPOINT_HISTOGRAM_PNG_PATH)).toFile();
		try {
			ImageIO.write(bufferedImage, "PNG", histogramPng);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "redirect:/analysis";
	}

	private void saveHistogramCsvInSession(HttpSession session, JFreeChart histogram) throws IOException { // TODO Refactor the session saving from the file generation
		XYIntervalSeriesCollection dataset = (XYIntervalSeriesCollection) histogram.getXYPlot().getDataset();
		Map<String, Double> eventsForBin = new LinkedHashMap<>(); // Preserve insertion order.
		for (int seriesNumber = 0; seriesNumber < dataset.getSeriesCount(); seriesNumber++) {
			XYIntervalSeries series = dataset.getSeries(seriesNumber);
			String seriesName = (String) series.getKey();
			for (int itemNumber = 0; itemNumber < series.getItemCount(); itemNumber++) {
				double xLow = series.getXLowValue(itemNumber);
				double xHigh = series.getXHighValue(itemNumber);
				String columnName = xLow + "-" + xHigh + "% (" + seriesName + ")";
				double y = series.getYValue(itemNumber);
				eventsForBin.merge(columnName, y, Double::sum);
			}
		}
		Path counterpointHistogramCsvPath = (Path) session.getAttribute(COUNTERPOINT_HISTOGRAM_CSV_PATH);
		try (FileWriter writer = new FileWriter(counterpointHistogramCsvPath.toFile())) {
			for (String bin : eventsForBin.keySet()) { // Write header row.
				writer.write(bin);
				writer.write(',');
			}
			writer.write('\n');
			for (Double value : eventsForBin.values()) {
				writer.write(value.toString());
				writer.write(',');
			}
		}
	}
	
	private void writeAndSavePieChartToSession(HttpSession session, JFreeChart pieChart) {
		BufferedImage bufferedImage = pieChart.createBufferedImage(800, 800, null); // TODO Make these variable.
		File pieChartPng = ((Path) session.getAttribute(COUNTERPOINT_PIE_CHART_PNG_PATH)).toFile();
		try {
			ImageIO.write(bufferedImage, "PNG", pieChartPng);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private JFreeChart generatePieChartFromSessionMidiFiles(HttpSession session) {
		long numContraryMotionEvents = (long) session.getAttribute(TOTAL_CONTRARY_EVENTS_LONG);
		long numSimilarMotionEvents = (long) session.getAttribute(TOTAL_SIMILAR_EVENTS_LONG);
		long numObliqueMotionEvents = (long) session.getAttribute(TOTAL_OBLIQUE_EVENTS_LONG);
		return getPieChartOf(null, numContraryMotionEvents, numSimilarMotionEvents, numObliqueMotionEvents);
	}

	private void recordAnalysisStatsInSession(HttpSession session, List<Analysis> analyses) {
		long numberOfContraryMotionEvents = 0L;
		long numberOfSimilarMotionEvents = 0L;
		long numberOfObliqueMotionEvents = 0L;
		for (Analysis analysis : analyses) {
			Reporter reporter = new Reporter(analysis);
			numberOfContraryMotionEvents += reporter.countMotionEventsOfType(ContrapuntalMotion.CONTRARY);
			numberOfSimilarMotionEvents += reporter.countMotionEventsOfType(ContrapuntalMotion.SIMILAR);
			numberOfObliqueMotionEvents += reporter.countMotionEventsOfType(ContrapuntalMotion.OBLIQUE);
		}
		session.setAttribute(TOTAL_CONTRARY_EVENTS_LONG, numberOfContraryMotionEvents);
		session.setAttribute(TOTAL_SIMILAR_EVENTS_LONG, numberOfSimilarMotionEvents);
		session.setAttribute(TOTAL_OBLIQUE_EVENTS_LONG, numberOfObliqueMotionEvents);
	}

	private JFreeChart getPieChartOf(String title, // TODO Refactor to make this more modular if possible.
			long numContraryMotionEvents,
			long numSimilarMotionEvents,
			long numObliqueMotionEvents) {
		DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
		dataset.setValue(CONTRARY_MOTION_EVENTS, numContraryMotionEvents);
		dataset.setValue(SIMILAR_MOTION_EVENTS, numSimilarMotionEvents);
		dataset.setValue(OBLIQUE_MOTION_EVENTS, numObliqueMotionEvents);
		PiePlot<String> plot = new PiePlot<>(dataset);
		plot.setLabelGenerator(null);
		plot.setBackgroundPaint(Color.LIGHT_GRAY);
		plot.setSectionPaint(CONTRARY_MOTION_EVENTS, RED);
		plot.setSectionPaint(SIMILAR_MOTION_EVENTS, BLUE);
		plot.setSectionPaint(OBLIQUE_MOTION_EVENTS, GREEN);
		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
		chart.setBackgroundPaint(Color.WHITE);
		return chart;
	}
}
