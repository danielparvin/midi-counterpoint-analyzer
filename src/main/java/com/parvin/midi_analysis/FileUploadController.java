package com.parvin.midi_analysis;

import static com.parvin.midi_analysis.SessionHandler.TEMP_DIRECTORY_PATH;
import static com.parvin.midi_analysis.SessionHandler.UPLOADED_MIDI_FILES_SET;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FileUploadController {
	public static final String MESSAGE = "message";
	
	@PostMapping("/upload")
	public String handleUpload(HttpSession session, 
			@RequestParam("file") MultipartFile file, 
			RedirectAttributes redirectAttributes) {
		@SuppressWarnings("unchecked")
		Set<Path> uploadedFiles = (Set<Path>) session.getAttribute(UPLOADED_MIDI_FILES_SET);
		Path tempDirectory = (Path) session.getAttribute(TEMP_DIRECTORY_PATH);
		String originalFilename = file.getOriginalFilename();
		if (filenameHasExtension(originalFilename, "mid", "midi")) {
			handleMidiFile(file, redirectAttributes, uploadedFiles, tempDirectory);
		} else if (filenameHasExtension(originalFilename, "zip")) {
			handleZipFile(file, redirectAttributes, uploadedFiles, tempDirectory);
		} else {
			redirectAttributes.addFlashAttribute(MESSAGE, "Uploaded file must be a .MID, .MIDI, or .ZIP file!");
		}
		return "redirect:/";
	}

	private void handleMidiFile(MultipartFile file, RedirectAttributes redirectAttributes, Set<Path> uploadedFiles,
			Path tempDirectory) {
		try {
			Path uploadedFile = tempDirectory.resolve(file.getOriginalFilename());
			file.transferTo(uploadedFile);
			uploadedFiles.add(uploadedFile);
			redirectAttributes.addFlashAttribute(MESSAGE, "Uploaded file successfully!");
		} catch (IOException e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute(MESSAGE, "Upload failed.");
		}
	}

	private void handleZipFile(MultipartFile file, RedirectAttributes redirectAttributes, Set<Path> uploadedFiles,
			Path tempDirectory) {
		try {
			File zipFile = tempDirectory.resolve(file.getOriginalFilename()).toFile();
			file.transferTo(zipFile);
			try (Stream<Path> paths = Files.walk(FileSystems.newFileSystem(zipFile.toPath()).getPath("/"))) {
				List<Path> midiPaths = paths.filter(path -> path.getNameCount() > 0 // Filter out the root element.
						&& (filenameHasExtension(path.getFileName().toString(), "mid", "midi")))
				.collect(Collectors.toList());
				if (midiPaths.isEmpty()) {
					redirectAttributes.addFlashAttribute(MESSAGE, "No MIDI file found in ZIP file!");
					return;
				}
				for (Path midiPath : midiPaths) {
					Path uploadedFile = tempDirectory.resolve(midiPath.getFileName().toString());
					try {
						Files.copy(midiPath, uploadedFile, StandardCopyOption.REPLACE_EXISTING);
						uploadedFiles.add(uploadedFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute(MESSAGE, "Upload failed.");
		}
		redirectAttributes.addFlashAttribute(MESSAGE, "Uploaded files successfully!");
	}
	
	/**
	 * Determine whether or not a given filename has a given (case-insensitive) extension.
	 * @param filename Filename to check (e.g. "fugue1.mid" or "midi-files.zip")
	 * @param extension Case-insensitive extension to compare (e.g. "mid" or "midi")
	 * @param otherExtensions Optional, additional extensions to compare with the filename
	 * @return true if the filename ends in any of the provided extensions; false otherwise
	 */
	private boolean filenameHasExtension(String filename, String extension, String... otherExtensions) {
		if (filename == null || extension == null) {
			return false;
		}
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex > 0) {
			String actualExtension = filename.substring(dotIndex + 1);
			if (actualExtension.equalsIgnoreCase(extension)) {
				return true;
			}
			for (String otherExtension : otherExtensions) {
				if (actualExtension.equalsIgnoreCase(otherExtension)) {
					return true;
				}
			}
		}
		return false;
	}
}
