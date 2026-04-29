package com.plm.service;

import com.plm.config.CacheConfig;
import com.plm.dto.DocumentDTO;
import com.plm.dto.DocumentRevisionDTO;
import com.plm.entity.Document;
import com.plm.entity.DocumentRevision;
import com.plm.mapper.DocumentMapper;
import com.plm.mapper.DocumentRevisionMapper;
import com.plm.repository.DocumentRepository;
import com.plm.repository.DocumentRevisionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentRevisionRepository documentRevisionRepository;
    private final DocumentMapper documentMapper;
    private final DocumentRevisionMapper documentRevisionMapper;

    @Value("${plm.upload.dir:uploads/documents}")
    private String uploadDir;

    // ==================== Document CRUD ====================

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.DOCUMENTS_CACHE, allEntries = true)
    public DocumentDTO createDocument(DocumentDTO documentDTO) {
        log.info("Creating document with number: {}", documentDTO.getDocumentNumber());

        if (documentRepository.existsByDocumentNumber(documentDTO.getDocumentNumber())) {
            throw new IllegalArgumentException("Document number already exists: " + documentDTO.getDocumentNumber());
        }

        Document document = documentMapper.toEntity(documentDTO);
        document = documentRepository.save(document);

        log.info("Document created with id: {}", document.getId());
        return documentMapper.toDto(document);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.DOCUMENTS_CACHE, key = "#id")
    public DocumentDTO getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + id));
        return documentMapper.toDto(document);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.DOCUMENTS_CACHE, key = "#documentNumber")
    public DocumentDTO getDocumentByNumber(String documentNumber) {
        Document document = documentRepository.findByDocumentNumber(documentNumber)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with number: " + documentNumber));
        return documentMapper.toDto(document);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.DOCUMENTS_CACHE, allEntries = true)
    public DocumentDTO updateDocument(Long id, DocumentDTO documentDTO) {
        log.info("Updating document with id: {}", id);

        Document existing = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + id));

        // Check if document number is being changed and if new number already exists
        if (!existing.getDocumentNumber().equals(documentDTO.getDocumentNumber())
                && documentRepository.existsByDocumentNumber(documentDTO.getDocumentNumber())) {
            throw new IllegalArgumentException("Document number already exists: " + documentDTO.getDocumentNumber());
        }

        existing.setDocumentNumber(documentDTO.getDocumentNumber());
        existing.setTitle(documentDTO.getTitle());
        existing.setDocumentType(documentDTO.getDocumentType());
        existing.setDescription(documentDTO.getDescription());

        existing = documentRepository.save(existing);
        log.info("Document updated with id: {}", id);

        return documentMapper.toDto(existing);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.DOCUMENTS_CACHE, allEntries = true)
    public void deleteDocument(Long id) {
        log.info("Deleting document with id: {}", id);

        if (!documentRepository.existsById(id)) {
            throw new EntityNotFoundException("Document not found with id: " + id);
        }

        documentRepository.deleteById(id);
        log.info("Document soft-deleted with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentDTO> listDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable).map(documentMapper::toDto);
    }

    // ==================== Revision Management ====================

    @Override
    @Transactional
    public DocumentRevisionDTO createRevision(Long documentId, DocumentRevisionDTO revisionDTO) {
        log.info("Creating revision for document id: {}", documentId);

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));

        // Unmark previous latest revision
        documentRevisionRepository.findByDocumentIdAndIsLatestRevisionTrue(documentId)
                .ifPresent(prev -> {
                    prev.setIsLatestRevision(false);
                    documentRevisionRepository.save(prev);
                });

        DocumentRevision revision = documentRevisionMapper.toEntity(revisionDTO);
        revision.setDocument(document);
        revision.setIsLatestRevision(true);

        // Auto-increment iteration if same revision exists
        String rev = revisionDTO.getRevision();
        List<DocumentRevision> existingRevisions = documentRevisionRepository.findByDocumentIdOrderByRevisionDesc(documentId);
        int maxIteration = existingRevisions.stream()
                .filter(r -> r.getRevision().equals(rev))
                .mapToInt(DocumentRevision::getIteration)
                .max()
                .orElse(0);
        revision.setIteration(maxIteration + 1);

        revision = documentRevisionRepository.save(revision);
        log.info("Revision created with id: {} for document id: {}", revision.getId(), documentId);

        return documentRevisionMapper.toDto(revision);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentRevisionDTO> listRevisions(Long documentId) {
        if (!documentRepository.existsById(documentId)) {
            throw new EntityNotFoundException("Document not found with id: " + documentId);
        }

        List<DocumentRevision> revisions = documentRevisionRepository.findByDocumentIdOrderByRevisionDesc(documentId);
        return documentRevisionMapper.toDtoList(revisions);
    }

    // ==================== File Operations ====================

    @Override
    @Transactional
    public DocumentRevisionDTO uploadFile(Long documentId, Long revisionId, MultipartFile file) throws IOException {
        log.info("Uploading file for document id: {}, revision id: {}", documentId, revisionId);

        DocumentRevision revision = documentRevisionRepository.findById(revisionId)
                .orElseThrow(() -> new EntityNotFoundException("Document revision not found with id: " + revisionId));

        if (!revision.getDocument().getId().equals(documentId)) {
            throw new IllegalArgumentException("Revision does not belong to document");
        }

        if (revision.getRevisionLocked()) {
            throw new IllegalStateException("Cannot upload file to a locked revision");
        }

        // Create upload directory if not exists
        Path uploadPath = Paths.get(uploadDir, String.valueOf(documentId), String.valueOf(revisionId));
        Files.createDirectories(uploadPath);

        // Generate unique filename to avoid conflicts
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID() + extension;

        // Save file
        Path filePath = uploadPath.resolve(storedFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Update revision with file metadata
        revision.setFilePath(filePath.toString());
        revision.setFileName(originalFilename);
        revision.setFileSize(file.getSize());
        revision.setContentType(file.getContentType());

        revision = documentRevisionRepository.save(revision);
        log.info("File uploaded for revision id: {}, stored as: {}", revisionId, storedFilename);

        return documentRevisionMapper.toDto(revision);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadFile(Long documentId, Long revisionId) throws IOException {
        log.info("Downloading file for document id: {}, revision id: {}", documentId, revisionId);

        DocumentRevision revision = documentRevisionRepository.findById(revisionId)
                .orElseThrow(() -> new EntityNotFoundException("Document revision not found with id: " + revisionId));

        if (!revision.getDocument().getId().equals(documentId)) {
            throw new IllegalArgumentException("Revision does not belong to document");
        }

        if (revision.getFilePath() == null) {
            throw new EntityNotFoundException("No file attached to this revision");
        }

        Path filePath = Paths.get(revision.getFilePath());
        if (!Files.exists(filePath)) {
            throw new EntityNotFoundException("File not found on disk: " + revision.getFileName());
        }

        return Files.readAllBytes(filePath);
    }
}
