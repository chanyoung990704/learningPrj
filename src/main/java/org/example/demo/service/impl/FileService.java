package org.example.demo.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.demo.domain.File;
import org.example.demo.repository.FileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final FileRepository fileRepository;
    private final FileStore fileStore; // FileStore 의존성 주입

    /**
     * 다중 파일 업로드 처리
     */
    @Transactional
    public List<File> saveFiles(List<MultipartFile> multipartFiles) {
        List<File> files = fileStore.storeFiles(multipartFiles);
        return fileRepository.saveAll(files);
    }

    /**
     * 단일 파일 업로드 처리
     */
    @Transactional
    public File saveFile(MultipartFile multipartFile) throws IOException {
        File file = fileStore.storeFile(multipartFile);
        return fileRepository.save(file);
    }

    /**
     * 특정 파일 조회
     */
    public File getFile(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("해당 파일이 존재하지 않습니다. ID: " + fileId));
    }

}
