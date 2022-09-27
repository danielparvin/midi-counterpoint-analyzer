package com.parvin.midi_analysis;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.midi.InvalidMidiDataException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.parvin.midi_analysis.counterpoint.Analyzer;

@Controller
public class FileUploadController {
	@GetMapping("/upload")
	public String getHome() {
		return "uploadForm";
	}
	
	@PostMapping("/upload")
	public String handleUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		try (InputStream inputStream = file.getInputStream()) {
			new Analyzer(file.getOriginalFilename(), inputStream);
		} catch (IOException | InvalidMidiDataException e) {
			e.printStackTrace();
		}
		
		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");
		return "redirect:/";
	}
}
