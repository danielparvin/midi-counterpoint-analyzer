package com.parvin.midi_analysis;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoController {
	@Autowired
	SessionHandler sessionHandler;
	
	@GetMapping("/demo")
	public String getDemoAnalysis(HttpSession session) throws IOException {
		sessionHandler.deleteUploadedFiles();
		sessionHandler.deleteAnalysisCsvAndPngFiles();
		sessionHandler.clearCounterpointAnalyses();
		Set<Path> uploadedFiles = sessionHandler.getUploadedMidiPaths();
		Path tempDirectory = sessionHandler.getTempDirectoryPath();
		try (InputStream inputStream = getClass().getResourceAsStream("/static/midi/row-your-boat.mid")) {
			Path copyOfDemoFile = tempDirectory.resolve("row-your-boat.mid");
			Files.copy(inputStream, copyOfDemoFile, StandardCopyOption.REPLACE_EXISTING);
			uploadedFiles.add(copyOfDemoFile);
		}
		return "redirect:/analyze";
	}
}
