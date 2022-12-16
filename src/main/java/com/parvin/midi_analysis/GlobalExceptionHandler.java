package com.parvin.midi_analysis;

import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {
	@Value("${spring.servlet.multipart.max-file-size}")
	private String maxFileSize;

	@ExceptionHandler(SizeLimitExceededException.class)
	public String handleSizeLimitExceededException(Exception e, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("message",
				"The file exceeds the maximum file size of " + maxFileSize + "!");
		return "redirect:/";
	}
}
