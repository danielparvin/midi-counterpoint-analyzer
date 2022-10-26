package com.parvin.midi_analysis;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UploadsListController {
	@GetMapping("/uploaded-files")
	public String getUploadedFilesPage() {
		return "uploaded-files";
	}
}
