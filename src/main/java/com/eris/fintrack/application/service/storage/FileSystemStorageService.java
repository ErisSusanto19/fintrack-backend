package com.eris.fintrack.application.service.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

//@Service
public class FileSystemStorageService implements FileStorageService {

    private final Path rootLocation;

    public FileSystemStorageService(@Value("${file.storage.upload-dir}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
    }

    @Override
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String save(MultipartFile file, String subDirectory) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }

            Path userDirectory = this.rootLocation.resolve(subDirectory);
            Files.createDirectories(userDirectory);

            String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;

            Path destinationFile = userDirectory.resolve(Paths.get(uniqueFileName)).normalize().toAbsolutePath();

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            return subDirectory + "/" + uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    @Override
    public Resource load(String storageKey) {
        try {
            Path file = rootLocation.resolve(storageKey);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + storageKey);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + storageKey, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Path file = rootLocation.resolve(storageKey);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file.", e);
        }
    }

    @Override
    public String getUrl(String storageKey) {
        return "/api/v1/some-download-path/" + storageKey;
    }
}