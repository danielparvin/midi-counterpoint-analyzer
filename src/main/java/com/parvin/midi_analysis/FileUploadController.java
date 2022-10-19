package com.parvin.midi_analysis;

import static com.parvin.midi_analysis.StaticStrings.MESSAGE;
import static com.parvin.midi_analysis.StaticStrings.MID;
import static com.parvin.midi_analysis.StaticStrings.MIDI;
import static com.parvin.midi_analysis.StaticStrings.UPLOADED_FILES;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FileUploadController {
	@GetMapping("/upload")
	public String getHome() {
		return "uploadForm";
	}

	@PostMapping("/upload")
	public String handleUpload(HttpSession session, 
			@RequestParam("file") MultipartFile file, 
			RedirectAttributes redirectAttributes) {
		@SuppressWarnings("unchecked")
		List<File> uploadedFiles = (List<File>) session.getAttribute(UPLOADED_FILES);
		String filenameExtension = getFilenameExtension(file.getOriginalFilename());
		if (filenameExtension.equalsIgnoreCase(MID) || filenameExtension.equalsIgnoreCase(MIDI)) {
			try {
				File sessionFile = convertToTempMidFile(file);
				uploadedFiles.add(sessionFile);
				redirectAttributes.addFlashAttribute(MESSAGE, "Uploaded file successfully!");
			} catch (IOException e) {
				e.printStackTrace();
				redirectAttributes.addFlashAttribute(MESSAGE, "Upload failed.");
			}
		} else if (filenameExtension == "zip") {
			uploadedFiles.addAll(extractFilesFrom(file));
			redirectAttributes.addFlashAttribute(MESSAGE, "Uploaded and extracted ZIP file successfully!");
		} else {
			redirectAttributes.addFlashAttribute(MESSAGE, "Uploaded file must be a .MID, .MIDI, or .ZIP file!");
		}
		return "redirect:/upload";
	}

	private File convertToTempMidFile(MultipartFile multipartFile) throws IOException {
		try (InputStream inputStream = multipartFile.getInputStream()) {
			File tempFile = Files.createTempFile("temp", ".mid").toFile();
			tempFile.deleteOnExit();
			multipartFile.transferTo(tempFile);
			return tempFile;
		}
	}

	private List<File> extractFilesFrom(MultipartFile zipFile) {
		List<File> files = new ArrayList<>();
		try (ZipInputStream zipInputStream = new ZipInputStream(zipFile.getInputStream())) {
			byte[] buffer = new byte[1024];
			ZipEntry zipEntry = zipInputStream.getNextEntry();
			while (zipEntry != null) {
				File tempFile = File.createTempFile("upload", ".mid");
				tempFile.deleteOnExit();
				BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));
				int count;
				while ((count = zipInputStream.read(buffer, 0, buffer.length)) != -1) {
					outputStream.write(buffer, 0, count);
				}
				outputStream.close();
				zipInputStream.closeEntry();
				files.add(tempFile);
				zipEntry = zipInputStream.getNextEntry();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return files;
	}

	private String getFilenameExtension(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
	}
}
