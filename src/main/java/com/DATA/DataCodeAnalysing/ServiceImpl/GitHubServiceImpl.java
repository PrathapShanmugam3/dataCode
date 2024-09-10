package com.DATA.DataCodeAnalysing.ServiceImpl;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import com.DATA.DataCodeAnalysing.Service.GitHubService;

@Service
public class GitHubServiceImpl implements GitHubService {

    @Value("${github.token}")
    private String githubToken;

    @Value("${github.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public GitHubServiceImpl() {
        // Initialize RestTemplate
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String uploadImage(MultipartFile file) {
        String originalFileName = "image";  // Base filename
        String fileExtension = ".jpg";      // Assuming JPG for simplicity
        String fileName = originalFileName + fileExtension;
        String filePath = "images/" + fileName; // Initial path within the repo
        String sha = null; // To hold the file SHA if it exists
        int fileCounter = 1; // To create image1, image2, etc.

        byte[] fileContent;
        try {
            fileContent = file.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to read file content.";
        }

        String base64Content = Base64.getEncoder().encodeToString(fileContent);

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.set("Content-Type", "application/json");

        // Check for an available filename
        boolean fileExists = true;
        while (fileExists) {
            try {
                ResponseEntity<String> getFileResponse = restTemplate.exchange(apiUrl + "/" + filePath, HttpMethod.GET, new HttpEntity<>(headers), String.class);
                if (getFileResponse.getStatusCode() == HttpStatus.OK) {
                    // File exists, so increment the filename
                    fileCounter++;
                    fileName = originalFileName + fileCounter + fileExtension;
                    filePath = "images/" + fileName;
                }
            } catch (Exception e) {
                // If an exception occurs (like a 404 Not Found), the file doesn't exist, so we can use this name
                fileExists = false;
            }
        }

        // Prepare the JSON payload
        String json = "{\"message\":\"Upload image " + fileName + "\",\"content\":\"" + base64Content + "\"}";

        // Send the PUT request to upload the file
        HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
        String url = apiUrl + "/" + filePath;

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
            if (response.getStatusCode() == HttpStatus.CREATED) {
                String fileUrl = "https://github.com/PrathapShanmugam3/imageStore/raw/main/" + filePath;
                return "Image uploaded successfully. File URL: " + fileUrl;
            } else {
                return "Failed to upload image. Status code: " + response.getStatusCode() + ". Response: " + response.getBody();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while uploading the image.";
        }
    }
    

    @Override
    public String updateImage(MultipartFile file, String imagePath) {
        if (file == null || imagePath == null || imagePath.isEmpty()) {
            return "Invalid input.";
        }

        // Extract the file extension
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            return "Invalid file.";
        }

        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String baseFileName = "image";  // Base filename for your images
        String fileName = baseFileName + fileExtension;

        // Prepare file content
        byte[] fileContent;
        try {
            fileContent = file.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to read file content.";
        }

        String base64Content = Base64.getEncoder().encodeToString(fileContent);

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.set("Content-Type", "application/json");

        // Clean up imagePath
        String cleanedImagePath = cleanUpImagePath(imagePath);

        // Debugging: Print cleanedImagePath value
        System.out.println("Debug: cleanedImagePath = " + cleanedImagePath);

        // Check if the file exists and delete it if it does
        String deleteFileUrl = apiUrl + "/" + cleanedImagePath;
        try {
            ResponseEntity<String> getFileResponse = restTemplate.exchange(deleteFileUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (getFileResponse.getStatusCode() == HttpStatus.OK) {
                // File exists, manually extract the SHA from the JSON response
                String responseBody = getFileResponse.getBody();
                String sha = extractShaFromResponse(responseBody);

                if (sha != null) {
                    String deletePayload = "{\"message\":\"Delete file " + fileName + "\",\"sha\":\"" + sha + "\"}";
                    HttpEntity<String> deleteRequest = new HttpEntity<>(deletePayload, headers);
                    ResponseEntity<String> deleteResponse = restTemplate.exchange(deleteFileUrl, HttpMethod.DELETE, deleteRequest, String.class);
                    if (deleteResponse.getStatusCode() == HttpStatus.OK || deleteResponse.getStatusCode() == HttpStatus.NO_CONTENT) {
                        System.out.println("Debug: Old image deleted successfully.");
                    } else {
                        return "Failed to delete the existing file. Status code: " + deleteResponse.getStatusCode() + ". Response: " + deleteResponse.getBody();
                    }
                } else {
                    return "Failed to extract SHA from the response.";
                }
            } else if (getFileResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                // File does not exist, proceed to upload
                System.out.println("Debug: File does not exist, proceeding to upload.");
            } else {
                return "Failed to check file existence. Status code: " + getFileResponse.getStatusCode() + ". Response: " + getFileResponse.getBody();
            }
        } catch (Exception e) {
            // If the file doesn't exist (404 Not Found), proceed to upload
            if (!e.getMessage().contains("404")) {
                e.printStackTrace();
                return "Error occurred while checking the file existence.";
            }
        }

        // Prepare the JSON payload for uploading the new file
        String json = "{\"message\":\"Update image " + fileName + "\",\"content\":\"" + base64Content + "\"}";
        String uploadUrl = apiUrl + "/" + cleanedImagePath;
        HttpEntity<String> uploadRequestEntity = new HttpEntity<>(json, headers);

        // Upload the new file
        try {
            ResponseEntity<String> uploadResponse = restTemplate.exchange(uploadUrl, HttpMethod.PUT, uploadRequestEntity, String.class);
            if (uploadResponse.getStatusCode() == HttpStatus.CREATED) {
                // Construct the correct file URL
                String fileUrl = "https://github.com/PrathapShanmugam3/imageStore/raw/main/" + cleanedImagePath;

                // Debugging: Print final file URL
                System.out.println("Debug: fileUrl = " + fileUrl);

                return "Image updated successfully. File URL: " + fileUrl;
            } else {
                return "Failed to update image. Status code: " + uploadResponse.getStatusCode() + ". Response: " + uploadResponse.getBody();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while updating the image.";
        }
    }

    private String cleanUpImagePath(String imagePath) {
        // Remove any unwanted parts from imagePath
        if (imagePath.startsWith("https://github.com") || imagePath.startsWith("http://")) {
            int startIndex = imagePath.indexOf("images/");
            if (startIndex != -1) {
                return imagePath.substring(startIndex);
            }
        }
        return imagePath;
    }

    private String extractShaFromResponse(String responseBody) {
        // Simple string search to extract the "sha" field from the response
        String shaKey = "\"sha\":\"";
        int shaStart = responseBody.indexOf(shaKey);
        if (shaStart != -1) {
            shaStart += shaKey.length();
            int shaEnd = responseBody.indexOf("\"", shaStart);
            if (shaEnd != -1) {
                return responseBody.substring(shaStart, shaEnd);
            }
        }
        return null; // sha not found
    }

    private String cleanUpDocumentPath(String documentPath) {
        // Remove any unwanted parts from imagePath
        if (documentPath.startsWith("https://github.com") || documentPath.startsWith("http://")) {
            int startIndex = documentPath.indexOf("documents/");
            if (startIndex != -1) {
                return documentPath.substring(startIndex);
            }
        }
        return documentPath;
    }


	@Override
	public String updateDocument(MultipartFile file, String documentPath) {
		
        if (file == null || documentPath == null || documentPath.isEmpty()) {
            return "Invalid input.";
        }

        // Extract the file extension
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            return "Invalid file.";
        }
        
        System.out.println(originalFileName);

        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String baseFileName = "document";  // Base filename for your images
        String fileName = baseFileName + fileExtension;
        
        System.out.println(fileName);

        // Prepare file content
        byte[] fileContent;
        try {
            fileContent = file.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to read file content.";
        }

        String base64Content = Base64.getEncoder().encodeToString(fileContent);

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.set("Content-Type", "application/json");

        System.out.println(documentPath);
        
        String cleanedDocPath = cleanUpDocumentPath(documentPath);
        
        System.out.println(cleanedDocPath);
        

        // Debugging: Print cleanedImagePath value
        System.out.println("Debug: cleanedImagePath = " + cleanedDocPath);

        // Check if the file exists and delete it if it does
        String deleteFileUrl = apiUrl + "/" + cleanedDocPath;
        try {
            ResponseEntity<String> getFileResponse = restTemplate.exchange(deleteFileUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            if (getFileResponse.getStatusCode() == HttpStatus.OK) {
                // File exists, manually extract the SHA from the JSON response
                String responseBody = getFileResponse.getBody();
                String sha = extractShaFromResponse(responseBody);

                if (sha != null) {
                    String deletePayload = "{\"message\":\"Delete file " + fileName + "\",\"sha\":\"" + sha + "\"}";
                    HttpEntity<String> deleteRequest = new HttpEntity<>(deletePayload, headers);
                    ResponseEntity<String> deleteResponse = restTemplate.exchange(deleteFileUrl, HttpMethod.DELETE, deleteRequest, String.class);
                    if (deleteResponse.getStatusCode() == HttpStatus.OK || deleteResponse.getStatusCode() == HttpStatus.NO_CONTENT) {
                        System.out.println("Debug: Old image deleted successfully.");
                    } else {
                        return "Failed to delete the existing file. Status code: " + deleteResponse.getStatusCode() + ". Response: " + deleteResponse.getBody();
                    }
                } else {
                    return "Failed to extract SHA from the response.";
                }
            } else if (getFileResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
                // File does not exist, proceed to upload
                System.out.println("Debug: File does not exist, proceeding to upload.");
            } else {
                return "Failed to check file existence. Status code: " + getFileResponse.getStatusCode() + ". Response: " + getFileResponse.getBody();
            }
        } catch (Exception e) {
            // If the file doesn't exist (404 Not Found), proceed to upload
            if (!e.getMessage().contains("404")) {
                e.printStackTrace();
                return "Error occurred while checking the file existence.";
            }
        }

        // Prepare the JSON payload for uploading the new file
        String json = "{\"message\":\"Update image " + fileName + "\",\"content\":\"" + base64Content + "\"}";
        String uploadUrl = apiUrl + "/documents/" + fileName;
        HttpEntity<String> uploadRequestEntity = new HttpEntity<>(json, headers);

        // Upload the new file
        try {
            ResponseEntity<String> uploadResponse = restTemplate.exchange(uploadUrl, HttpMethod.PUT, uploadRequestEntity, String.class);
            if (uploadResponse.getStatusCode() == HttpStatus.CREATED) {
                // Construct the correct file URL
                String fileUrl = "https://github.com/PrathapShanmugam3/imageStore/raw/main/" + "documents/" + fileName;

                // Debugging: Print final file URL
                System.out.println("Debug: fileUrl = " + fileUrl);

                return "Image updated successfully. File URL: " + fileUrl;
            } else {
                return "Failed to update image. Status code: " + uploadResponse.getStatusCode() + ". Response: " + uploadResponse.getBody();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while updating the image.";
        }
	
	}

	@Override
	public String uploadDocument(MultipartFile file) {
		String originalFileName = "document";  // Base filename
        String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String fileName = originalFileName + fileExtension;
        String filePath = "documents/" + fileName; // Initial path within the repo
        String sha = null; // To hold the file SHA if it exists
        int fileCounter = 1; // To create image1, image2, etc.

        byte[] fileContent;
        try {
            fileContent = file.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to read file content.";
        }

        String base64Content = Base64.getEncoder().encodeToString(fileContent);

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        headers.set("Content-Type", "application/json");

        // Check for an available filename
        boolean fileExists = true;
        while (fileExists) {
            try {
                ResponseEntity<String> getFileResponse = restTemplate.exchange(apiUrl + "/" + filePath, HttpMethod.GET, new HttpEntity<>(headers), String.class);
                if (getFileResponse.getStatusCode() == HttpStatus.OK) {
                    // File exists, so increment the filename
                    fileCounter++;
                    fileName = originalFileName + fileCounter + fileExtension;
                    filePath = "documents/" + fileName;
                }
            } catch (Exception e) {
                // If an exception occurs (like a 404 Not Found), the file doesn't exist, so we can use this name
                fileExists = false;
            }
        }

        // Prepare the JSON payload
        String json = "{\"message\":\"Upload document " + fileName + "\",\"content\":\"" + base64Content + "\"}";

        // Send the PUT request to upload the file
        HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);
        String url = apiUrl + "/" + filePath;

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
            if (response.getStatusCode() == HttpStatus.CREATED) {
                String fileUrl = "https://github.com/PrathapShanmugam3/imageStore/raw/main/" + filePath;
                return "document uploaded successfully. File URL: " + fileUrl;
            } else {
                return "Failed to upload document. Status code: " + response.getStatusCode() + ". Response: " + response.getBody();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while uploading the document.";
        }	}

	

	
}
