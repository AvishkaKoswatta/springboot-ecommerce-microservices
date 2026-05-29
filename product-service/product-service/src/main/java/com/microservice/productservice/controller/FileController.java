package com.microservice.productservice.controller;

//import com.microservice.productservice.service.FileStorageService;
import com.microservice.productservice.service.S3Service;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;

import java.util.Map;

//@RestController
//@RequestMapping("/files")
//@RequiredArgsConstructor
//public class FileController {
//
//    private final FileStorageService fileStorageService;
//
//    @PostMapping("/upload")
//    public Map<String,String> upload(
//            @RequestParam("file") MultipartFile file
//    ){
//
//        String url=fileStorageService.saveFile(file);
//
//        return Map.of(
//                "url",
//                url
//        );
//    }
//}

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        String url = s3Service.uploadFile(file);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of("url", url)
        ));
    }
}