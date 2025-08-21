package com.eris.fintrack.application.service.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@Primary
@RequiredArgsConstructor
public class CloudinaryStorageService implements FileStorageService {

    private final Cloudinary cloudinary;

    @Override
    public void init() {
    }

    @Override
    public String save(MultipartFile file, String subDirectory) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "fintrack/" + subDirectory,
                    "resource_type", "auto"
            ));

            return (String) uploadResult.get("public_id");

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file with Cloudinary", e);
        }
    }

    @Override
    public Resource load(String storageKey) {
        throw new UnsupportedOperationException("Load as Resource is not supported for Cloudinary. Use a URL retrieval method instead.");
    }

    @Override
    public void delete(String storageKey) {
        try {
            cloudinary.uploader().destroy(storageKey, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file from Cloudinary", e);
        }
    }

    @Override
    public String getUrl(String storageKey) {
        return cloudinary.url().generate(storageKey);
    }
}