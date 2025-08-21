package com.eris.fintrack.application.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    void init();
    String save(MultipartFile file, String subDirectory);
    Resource load(String storageKey);
    void delete(String storageKey);
    String getUrl(String storageKey);
}