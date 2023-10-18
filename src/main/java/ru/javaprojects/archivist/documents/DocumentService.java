package ru.javaprojects.archivist.documents;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.documents.model.Applicability;
import ru.javaprojects.archivist.documents.model.Document;
import ru.javaprojects.archivist.documents.repository.ApplicabilityRepository;
import ru.javaprojects.archivist.documents.repository.DocumentRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class DocumentService {
    private final DocumentRepository repository;
    private final ApplicabilityRepository applicabilityRepository;

    public Page<Document> getAll(Pageable pageable) {
        return repository.findAllByOrderByDecimalNumber(pageable);
    }

    public Page<Document> getAll(Pageable pageable, String keyword) {
        return repository.findAllByDecimalNumberContainsIgnoreCaseOrderByDecimalNumber(pageable, keyword);
    }

    public Document get(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Entity with id=" + id + " not found"));
    }

    public Document getByDecimalNumber(String decimalNumber) {
        return repository.findByDecimalNumberIgnoreCase(decimalNumber)
                .orElseThrow(() -> new NotFoundException("Not found department with decimal number=" + decimalNumber));
    }

    public void createOrUpdate(Document document) {
        Assert.notNull(document, "document must not be null");
        if (document.getId() != null) {
            repository.getExisted(document.getId());
        }
        repository.save(document);
    }

    public void delete(long id) {
        repository.deleteExisted(id);
    }

    public List<Applicability> getApplicabilities(long id) {
        repository.getExisted(id);
        return applicabilityRepository.findAllByDocumentId(id);
    }

    public void deleteApplicability(long id) {
        applicabilityRepository.deleteExisted(id);
    }
}
