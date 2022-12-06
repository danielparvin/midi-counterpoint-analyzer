package com.parvin.midi_analysis;

import static com.parvin.midi_analysis.SessionHandler.COUNTERPOINT_ANALYSES_LIST;
import static com.parvin.midi_analysis.SessionHandler.COUNTERPOINT_HISTOGRAM_PNG_PATH;
import static com.parvin.midi_analysis.SessionHandler.HISTOGRAM_BIN_SIZE_INT;
import static com.parvin.midi_analysis.SessionHandler.TOTAL_CONTRARY_EVENTS_LONG;
import static com.parvin.midi_analysis.SessionHandler.TOTAL_OBLIQUE_EVENTS_LONG;
import static com.parvin.midi_analysis.SessionHandler.TOTAL_SIMILAR_EVENTS_LONG;
import static com.parvin.midi_analysis.SessionHandler.UPLOADED_MIDI_FILES_SET;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;
import javax.sound.midi.InvalidMidiDataException;

import org.jfree.chart.JFreeChart;
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
	@GetMapping("/analysis/histogram")
	@ResponseBody
	public ResponseEntity<byte[]> getImage(HttpSession session) {
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
	public String getAnalysisPage(HttpSession session) { // TODO Refactor to simplify.
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
		int binSize = (int) session.getAttribute(HISTOGRAM_BIN_SIZE_INT);
		CounterpointHistogramMaker counterpointHistogramMaker = new CounterpointHistogramMaker(analyses, binSize);
		JFreeChart histogram = counterpointHistogramMaker.generateNormalizedHistogram();
		BufferedImage bufferedImage = histogram.createBufferedImage(800, 800, 700, 700, null); // TODO Make these variable.
		File histogramPng = ((Path) session.getAttribute(COUNTERPOINT_HISTOGRAM_PNG_PATH)).toFile();
		try {
			if (ImageIO.write(bufferedImage, "PNG", histogramPng)) {
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "analysis";
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
}
