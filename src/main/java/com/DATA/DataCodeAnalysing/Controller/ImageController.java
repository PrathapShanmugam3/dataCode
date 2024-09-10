package com.DATA.DataCodeAnalysing.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.DATA.DataCodeAnalysing.Service.GitHubService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/files")
public class ImageController {
	@Autowired
	private GitHubService gitHubService;

	@PostMapping(value = "/upload/image", headers = ("Content-Type=multipart/*"), consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String uploadImage(@RequestPart("file") MultipartFile file) {
		try {
			return gitHubService.uploadImage(file);
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}

	@PostMapping(value = "/upload/document", headers = ("Content-Type=multipart/*"), consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String uploadDocument(@RequestPart("file") MultipartFile file) {
		try {
			return gitHubService.uploadDocument(file);
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}

	@PutMapping(value = "/update/image", headers = ("Content-Type=multipart/*"), consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String updateImage(@RequestPart("file") MultipartFile file, @RequestParam("imagePath") String imagePath) {
		try {
			return gitHubService.updateImage(file, imagePath);
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}
	
	@PutMapping(value = "/update/document", headers = ("Content-Type=multipart/*"), consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String updateDocument(@RequestPart("file") MultipartFile file, @RequestParam("documentPath") String documentPath) {
		try {
			return gitHubService.updateDocument(file, documentPath);
		} catch (Exception e) {
			return "Error: " + e.getMessage();
		}
	}

}
