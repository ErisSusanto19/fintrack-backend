package com.eris.fintrack.api.mapper;

import com.eris.fintrack.api.attachment.dto.AttachmentResponse;
import com.eris.fintrack.application.service.storage.FileStorageService;
import com.eris.fintrack.domain.Attachment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class AttachmentMapper {
    protected FileStorageService fileStorageService;

    @Autowired
    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Mapping(target = "url", expression = "java(fileStorageService.getUrl(attachment.getStorageKey()))")
    public abstract AttachmentResponse toDto(Attachment attachment);
}
