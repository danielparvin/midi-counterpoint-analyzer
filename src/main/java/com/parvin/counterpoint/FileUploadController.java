package com.parvin.counterpoint;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.midi.InvalidMidiDataException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.parvin.counterpoint.analysis.Analyzer;

@Controller
public class FileUploadController {
	@GetMapping("/")
	public String getHome() {
		return "uploadForm";
	}
	
	@PostMapping("/")
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
