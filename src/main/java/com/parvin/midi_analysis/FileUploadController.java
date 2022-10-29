package com.parvin.midi_analysis;

import static com.parvin.midi_analysis.FilenameUtils.filenameHasExtension;
import static com.parvin.midi_analysis.StaticStrings.MESSAGE;
import static com.parvin.midi_analysis.StaticStrings.TEMP_DIRECTORY;
import static com.parvin.midi_analysis.StaticStrings.UPLOADED_FILES;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.stream.Stream;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FileUploadController {
	@PostMapping("/upload")
	public String handleUpload(HttpSession session, 
			@RequestParam("file") MultipartFile file, 
			RedirectAttributes redirectAttributes) {
		@SuppressWarnings("unchecked")
		Set<File> uploadedFiles = (Set<File>) session.getAttribute(UPLOADED_FILES);
		Path tempDirectory = (Path) session.getAttribute(TEMP_DIRECTORY);
		String originalFilename = file.getOriginalFilename();
		if (filenameHasExtension(originalFilename, "mid", "midi")) {
			try {
				File uploadedFile = tempDirectory.resolve(file.getOriginalFilename()).toFile();
				file.transferTo(uploadedFile);
				uploadedFiles.add(uploadedFile);
				redirectAttributes.addFlashAttribute(MESSAGE, "Uploaded file successfully!");
			} catch (IOException e) {
				e.printStackTrace();
				redirectAttributes.addFlashAttribute(MESSAGE, "Upload failed.");
			}
		} else if (filenameHasExtension(originalFilename, "zip")) {
			try {
				File zipFile = tempDirectory.resolve(file.getOriginalFilename()).toFile();
				file.transferTo(zipFile);
				try (Stream<Path> paths = Files.walk(FileSystems.newFileSystem(zipFile.toPath()).getPath("/"))) {
					paths.filter(path -> path.getNameCount() > 0 // Filter out the root element.
							&& (filenameHasExtension(path.getFileName().toString(), "mid", "midi")))
					.forEach(path -> {
						File uploadedFile = tempDirectory.resolve(path.getFileName().toString()).toFile();
						try {
							Files.copy(path, uploadedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
							uploadedFiles.add(uploadedFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
				}
			} catch (IOException e) {
				e.printStackTrace();
				redirectAttributes.addFlashAttribute(MESSAGE, "Upload failed.");
			}
			redirectAttributes.addFlashAttribute(MESSAGE, "Uploaded files successfully!");
		} else {
			redirectAttributes.addFlashAttribute(MESSAGE, "Uploaded file must be a .MID, .MIDI, or .ZIP file!");
		}
		return "redirect:/";
	}
}
