package com.eris.fintrack.api.attachment.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AttachmentResponse {
    private UUID id;
    private String fileName;
    private String mimeType;
    private long size;
    private String url;
}
