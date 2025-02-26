package org.example.demo.service.impl;

import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    public void init() {
        // 애플리케이션 시작 시 기본 업로드 디렉토리 생성
        createDirectoryIfNotExists(uploadDir);
    }

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

        // 디렉토리가 존재하는지 확인하고 없으면 생성
        createDirectoryIfNotExists(uploadDir);

        String originalName = multipartFile.getOriginalFilename();
        String storedName = createStoredName(originalName);

        // 파일 저장 경로 생성
        java.io.File targetFile = new java.io.File(getFullPath(storedName));

        // 상위 디렉토리가 존재하는지 확인하고 생성
        java.io.File parentDir = targetFile.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }

        multipartFile.transferTo(targetFile);

        File file = org.example.demo.domain.File.builder()
                .originalName(originalName)
                .storedName(storedName)
                .fileSize(multipartFile.getSize())
                .fileType(multipartFile.getContentType())
                .build();

        return file;
    }

    public String createStoredName(String originalName) {
        String ext = extractExtension(originalName);
        String uuid = UUID.randomUUID().toString();
        return uuid + ext;
    }

    // 확장자 추출 메서드
    private String extractExtension(String originalName) {
        if (originalName == null || originalName.isEmpty() || !originalName.contains(".")) {
            return "";
        }
        return originalName.substring(originalName.lastIndexOf("."));
    }

    // 디렉토리 생성 메서드
    private void createDirectoryIfNotExists(String directoryPath) {
        java.io.File directory = new java.io.File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Directory created: " + directoryPath);
            } else {
                System.err.println("Failed to create directory: " + directoryPath);
            }
        }
    }
}
