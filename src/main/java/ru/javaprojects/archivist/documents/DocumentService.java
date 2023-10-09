package ru.javaprojects.archivist.documents;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.javaprojects.archivist.common.error.exception.NotFoundException;
import ru.javaprojects.archivist.documents.model.Document;

@Service
@AllArgsConstructor
public class DocumentService {
    private final DocumentRepository repository;

    public Page<Document> getAll(Pageable pageable) {
        return repository.findAllByOrderByDecimalNumber(pageable);
    }

    public Page<Document> getAll(Pageable pageable, String keyword) {
        return repository.findAllByDecimalNumberContainsIgnoreCaseOrderByDecimalNumber(pageable, keyword);
    }

    public Document get(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Entity with id=" + id + " not found"));
    }
}
