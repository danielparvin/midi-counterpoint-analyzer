package com.parvin.midi_analysis;

import static com.parvin.midi_analysis.StaticStrings.REPORT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.sound.midi.InvalidMidiDataException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.parvin.midi_analysis.counterpoint.Analysis;
import com.parvin.midi_analysis.counterpoint.Analyzer;

@Controller
public class ReportController {
	@GetMapping(REPORT)
	public String getReport(HttpSession session) {
		return "report";
	}
	
	@PostMapping(REPORT)
	public String analyzeAllUploadedFiles(HttpSession session, RedirectAttributes redirectAttributes) {
		List<Analysis> analyses = new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<File> uploadedFiles = (List<File>) session.getAttribute(SessionHandler.UPLOADED_MIDI_FILES_SET);
		for (File file: uploadedFiles) {
			try {
				Analyzer analyzer = new Analyzer(file);
				analyses.addAll(analyzer.analyzeAllTracks());
			} catch (InvalidMidiDataException | IOException e) {
				e.printStackTrace();
			}
		}
		session.setAttribute(SessionHandler.COUNTERPOINT_ANALYSES_LIST, analyses);
		return "redirect:/report";
	}
	
}
