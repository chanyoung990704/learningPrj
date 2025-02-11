package org.example.demo.service.impl;

import org.example.demo.domain.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FileStore {

    @Value("${upload.dir}")
    private String uploadDir; // 파일 저장 경로

    public String getFullPath(String fileName) {
        return uploadDir + fileName;
    }

    public List<File> storeFiles(List<MultipartFile> multipartFiles){
        return multipartFiles.stream().filter(file -> !file.isEmpty())
                .map(file -> {
                    try {
                        return storeFile(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    public File storeFile(MultipartFile multipartFile) throws IOException {
        if(multipartFile.isEmpty()){
            return null;
        }

        String originalName = multipartFile.getOriginalFilename();
        String storedName = createStoredName(originalName);
        multipartFile.transferTo(new java.io.File(getFullPath(storedName)));

        File file = org.example.demo.domain.File.builder()
                .originalName(originalName)
                .storedName(storedName)
                .fileSize(multipartFile.getSize())
                .build();

        return file;
    }

    public String createStoredName(String originalName) {
        String ext = originalName.substring(originalName.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }
}
