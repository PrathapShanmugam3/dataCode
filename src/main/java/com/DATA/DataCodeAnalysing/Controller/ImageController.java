package com.DATA.DataCodeAnalysing.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.DATA.DataCodeAnalysing.Service.GitHubService;

@RestController
@RequestMapping("/files")
public class ImageController {
	@Autowired
    private GitHubService gitHubService;

    @PostMapping("/upload/image")
    public String uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            return gitHubService.uploadImage(file);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @PostMapping("/upload/document")
    public String uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            return gitHubService.uploadDocument(file);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    @PutMapping("/upload/image")
    public String updateImage(@RequestParam("file") MultipartFile file,@RequestParam("imagePath") String imagePath) {
        try {
            return gitHubService.updateImage(file,imagePath);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


}
