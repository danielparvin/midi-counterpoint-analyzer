package com.parvin.midi_analysis;

import static com.parvin.midi_analysis.StaticStrings.MESSAGE;
import static com.parvin.midi_analysis.StaticStrings.TEMP_DIRECTORY;
import static com.parvin.midi_analysis.StaticStrings.UPLOADED_FILES;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

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
		List<File> uploadedFiles = (List<File>) session.getAttribute(UPLOADED_FILES);
		Path tempDirectory = (Path) session.getAttribute(TEMP_DIRECTORY);
		String originalFilename = file.getOriginalFilename();
		if (hasMidiExtension(originalFilename)) {
			try {
				File uploadedFile = tempDirectory.resolve(file.getOriginalFilename()).toFile();
				file.transferTo(uploadedFile);
				uploadedFiles.add(uploadedFile);
				redirectAttributes.addFlashAttribute(MESSAGE, "Uploaded file successfully!");
			} catch (IOException e) {
				e.printStackTrace();
				redirectAttributes.addFlashAttribute(MESSAGE, "Upload failed.");
			}
		} else if (hasZipExtension(originalFilename)) {
			try {
				File zipFile = tempDirectory.resolve(file.getOriginalFilename()).toFile();
				file.transferTo(zipFile);
				try (FileSystem fileSystem = FileSystems.newFileSystem(zipFile.toPath())) {
					Files.walk(fileSystem.getPath("/"))
					.filter(p -> p.getNameCount() > 0  && (hasMidiExtension(p.getFileName().toString())))
					.forEach(p -> {
						File uploadedFile = tempDirectory.resolve(p.getFileName().toString()).toFile();
						try {
							Files.copy(p, uploadedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
							uploadedFiles.add(uploadedFile);
						} catch (IOException e) {
							e.printStackTrace(); // TODO Add flash attributes depending on successes/failures.
						}
					});
				}
			} catch (IOException e1) {
				e1.printStackTrace();
				redirectAttributes.addFlashAttribute(MESSAGE, "Upload failed.");
			}
		} else {
			redirectAttributes.addFlashAttribute(MESSAGE, "Uploaded file must be a .MID, .MIDI, or .ZIP file!");
		}
		return "redirect:/";
	}

	private boolean hasMidiExtension(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex > 0) {
			String extension = filename.substring(dotIndex);
			return extension.equalsIgnoreCase(".mid") || extension.equalsIgnoreCase(".midi");
		} else {
			return false;
		}
	}

	private boolean hasZipExtension(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex > 0) {
			String extension = filename.substring(dotIndex);
			return extension.equalsIgnoreCase(".zip");
		} else {
			return false;
		}
	}
}
