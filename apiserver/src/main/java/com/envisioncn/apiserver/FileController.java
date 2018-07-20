package com.envisioncn.apiserver;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class FileController {
		
	@Autowired
    ConfigBean configBean;

	@RequestMapping(value = "/download", method = RequestMethod.GET)
    ResponseEntity<?> downloadFile(@RequestParam("filename") String filename)
            throws IOException {
        System.out.println("filename:"+filename);
		FileSystemResource file = new FileSystemResource(filename);
		
		//Check if the file is expired
		if(Utils.expired(filename,configBean.getExpiration())){
			String error = "Error: This data file has been expired.";
			return new ResponseEntity<String>(error,HttpStatus.NOT_FOUND);
		}
		
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getFilename()));
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.contentLength())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(file.getInputStream()));
    }
}
