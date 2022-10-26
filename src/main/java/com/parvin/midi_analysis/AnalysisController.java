package com.parvin.midi_analysis;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalysisController {
	@GetMapping("/analysis")
	public String getAnalysisPage() {
		return "analysis";
	}
}
