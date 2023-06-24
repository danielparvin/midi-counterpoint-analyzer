package com.parvin.midi_analysis;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import javax.sound.midi.InvalidMidiDataException;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.parvin.midi_analysis.counterpoint.Analysis;
import com.parvin.midi_analysis.counterpoint.Analyzer;
import com.parvin.midi_analysis.counterpoint.CounterpointHistogramMaker;
import com.parvin.midi_analysis.counterpoint.Reporter;
import com.parvin.midi_analysis.counterpoint.events.ContrapuntalMotion;

@Controller
public class AnalysisController {
	public static final int HEIGHT_PX = 800;
	public static final int WIDTH_PX = 800;
	public static final Color RED = Color.decode("#ff6347");
	public static final Color BLUE = Color.decode("#227bff");
	public static final Color GREEN = Color.decode("#00d084");
	private static final String OBLIQUE_MOTION_EVENTS = "Oblique Motion Events";
	private static final String SIMILAR_MOTION_EVENTS = "Similar Motion Events";
	private static final String CONTRARY_MOTION_EVENTS = "Contrary Motion Events";
	
	@Autowired
	private SessionHandler sessionHandler;

	@GetMapping("/analysis/csv")
	@ResponseBody
	public ResponseEntity<byte[]> getHistogramCsv() {
		Path histogramPath = sessionHandler.getCounterpointHistogramCsvPath();
		return getCsvFileFromPath(histogramPath);
	}

	@GetMapping("/analysis/histogram")
	@ResponseBody
	public ResponseEntity<byte[]> getHistogramImage() {
		Path histogramPath = sessionHandler.getCounterpointHistogramPngPath();
		return getPngFileFromPath(histogramPath);
	}

	@GetMapping("/analysis/pie-chart")
	@ResponseBody
	public ResponseEntity<byte[]> getPieChartImage(HttpSession session) {
		Path pieChartPath = sessionHandler.getCounterpointPieChartPngPath();
		return getPngFileFromPath(pieChartPath);
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

	private ResponseEntity<byte[]> getCsvFileFromPath(Path path) {
		if (!path.toFile().exists()) {
			return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
		}
	
		try {
			byte[] imageBytes = Files.readAllBytes(path);
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
	
	private ResponseEntity<byte[]> getPngFileFromPath(Path path) {
		if (!path.toFile().exists()) {
			return new ResponseEntity<>(null, null, HttpStatus.NOT_FOUND);
		}

		try {
			byte[] imageBytes = Files.readAllBytes(path);
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
	public String analyze(HttpSession session, RedirectAttributes redirectAttributes) {
		Set<Path> midiFiles = sessionHandler.getUploadedMidiPaths();
		List<Analysis> analyses = sessionHandler.getCounterpointAnalyses();
		for (Path file : midiFiles) {
			try {
				Analyzer analyzer = new Analyzer(file.toFile());
				analyses.addAll(analyzer.analyzeAllTracks());
			} catch (InvalidMidiDataException|IOException e) {
				e.printStackTrace();
			}
		}
		recordAnalysisStatsInSession(analyses);
		JFreeChart pieChart = generatePieChartFromSessionMidiFiles();
		writeAndSavePieChartToSession(pieChart);
		int binSize = sessionHandler.getHistogramBinSize();
		CounterpointHistogramMaker counterpointHistogramMaker = new CounterpointHistogramMaker(analyses, binSize);
		JFreeChart histogram = counterpointHistogramMaker.generateNormalizedHistogram();
		try {
			saveHistogramCsvInSession(histogram);
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedImage bufferedImage = histogram.createBufferedImage(WIDTH_PX, HEIGHT_PX, null);
		File histogramPng = sessionHandler.getCounterpointHistogramPngPath().toFile();
		try {
			ImageIO.write(bufferedImage, "PNG", histogramPng);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "redirect:/analysis";
	}

	/**
	 *
	 * @param histogram A histogram with a dataset containing series of an equal number of items.
	 * @throws IOException
	 */
	private void saveHistogramCsvInSession(JFreeChart histogram) throws IOException {
		XYIntervalSeriesCollection dataset = (XYIntervalSeriesCollection) histogram.getXYPlot().getDataset();
		Path counterpointHistogramCsvPath = sessionHandler.getCounterpointHistogramCsvPath();
		try (FileWriter writer = new FileWriter(counterpointHistogramCsvPath.toFile())) {
			// Write header row.
			writer.write("Bin");
			writer.write(',');
			for (int seriesNumber = 0; seriesNumber < dataset.getSeriesCount(); seriesNumber++) {
				writer.write((String) dataset.getSeries(seriesNumber).getKey()); // e.g. "Contrary Motion Events"
				writer.write(',');
			}
			writer.write('\n');

			if (dataset.getSeriesCount() < 1) {
				throw new IllegalStateException("The histogram's dataset does not include any series!");
			}
			XYIntervalSeries modelSeries = dataset.getSeries(0);
			int numberOfBins = modelSeries.getItemCount();
			for (int binNumber = 0; binNumber < numberOfBins; binNumber++) {
				double xLow = modelSeries.getXLowValue(binNumber);
				double xHigh = modelSeries.getXHighValue(binNumber);
				writer.write(xLow + "-" + xHigh + "%");
				writer.write(',');
				for (int seriesNumber = 0; seriesNumber < dataset.getSeriesCount(); seriesNumber++) {
					XYIntervalSeries series = dataset.getSeries(seriesNumber);
					writer.write(String.valueOf((int) series.getYValue(binNumber)));
					writer.write(',');
				}
				writer.write('\n');
			}
		}
	}

	private void writeAndSavePieChartToSession(JFreeChart pieChart) {
		BufferedImage bufferedImage = pieChart.createBufferedImage(WIDTH_PX, HEIGHT_PX, null);
		File pieChartPng = sessionHandler.getCounterpointPieChartPngPath().toFile();
		try {
			ImageIO.write(bufferedImage, "PNG", pieChartPng);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private JFreeChart generatePieChartFromSessionMidiFiles() {
		
		long numContraryMotionEvents = sessionHandler.getNumberOfContraryMotionEvents();
		long numSimilarMotionEvents = sessionHandler.getNumberOfSimilarMotionEvents();
		long numObliqueMotionEvents = sessionHandler.getNumberOfObliqueMotionEvents();
		List<PieChartSegment> components = new ArrayList<>();
		components.add(new PieChartSegment(CONTRARY_MOTION_EVENTS, numContraryMotionEvents, RED));
		components.add(new PieChartSegment(SIMILAR_MOTION_EVENTS, numSimilarMotionEvents, BLUE));
		components.add(new PieChartSegment(OBLIQUE_MOTION_EVENTS, numObliqueMotionEvents, GREEN));
		return createPieChart(null, components, false);
	}

	private void recordAnalysisStatsInSession(List<Analysis> analyses) {
		long numberOfContraryMotionEvents = 0L;
		long numberOfSimilarMotionEvents = 0L;
		long numberOfObliqueMotionEvents = 0L;
		for (Analysis analysis : analyses) {
			Reporter reporter = new Reporter(analysis);
			numberOfContraryMotionEvents += reporter.countMotionEventsOfType(ContrapuntalMotion.CONTRARY);
			numberOfSimilarMotionEvents += reporter.countMotionEventsOfType(ContrapuntalMotion.SIMILAR);
			numberOfObliqueMotionEvents += reporter.countMotionEventsOfType(ContrapuntalMotion.OBLIQUE);
		}
		sessionHandler.setNumberOfContraryMotionEvents(numberOfContraryMotionEvents);
		sessionHandler.setNumberOfSimilarMotionEvents(numberOfSimilarMotionEvents);
		sessionHandler.setNumberOfObliqueMotionEvents(numberOfObliqueMotionEvents);
	}

	private JFreeChart createPieChart(String title, List<PieChartSegment> segments, boolean createLegend) {
		DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
		for (PieChartSegment segment: segments) {
			dataset.setValue(segment.key(), segment.value());
		}
		PiePlot<String> plot = new PiePlot<>(dataset);
		plot.setLabelGenerator(null);
		plot.setBackgroundPaint(Color.LIGHT_GRAY);
		for (PieChartSegment segment: segments) {
			plot.setSectionPaint(segment.key(), segment.color());
		}
		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, createLegend);
		chart.setBackgroundPaint(Color.WHITE);
		return chart;
	}
}
