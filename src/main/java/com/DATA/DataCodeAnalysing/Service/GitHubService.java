package com.DATA.DataCodeAnalysing.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface GitHubService {

	public String uploadImage(MultipartFile file);
	

	public String uploadDocument(MultipartFile file);


	public String updateImage(MultipartFile file, String imagePath);

}
