package com.eris.fintrack.infrastructure.persistence;

import com.eris.fintrack.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
}
