package com.plm.service;

import com.plm.dto.DocumentDTO;
import com.plm.dto.DocumentRevisionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DocumentService {

    // Document CRUD
    DocumentDTO createDocument(DocumentDTO documentDTO);

    DocumentDTO getDocumentById(Long id);

    DocumentDTO getDocumentByNumber(String documentNumber);

    DocumentDTO updateDocument(Long id, DocumentDTO documentDTO);

    void deleteDocument(Long id);

    Page<DocumentDTO> listDocuments(Pageable pageable);

    // Revision management
    DocumentRevisionDTO createRevision(Long documentId, DocumentRevisionDTO revisionDTO);

    List<DocumentRevisionDTO> listRevisions(Long documentId);

    // File operations
    DocumentRevisionDTO uploadFile(Long documentId, Long revisionId, MultipartFile file) throws IOException;

    byte[] downloadFile(Long documentId, Long revisionId) throws IOException;
}
