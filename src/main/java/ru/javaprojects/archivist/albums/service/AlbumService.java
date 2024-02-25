package ru.javaprojects.archivist.albums.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.archivist.albums.model.Album;
import ru.javaprojects.archivist.albums.repository.AlbumRepository;
import ru.javaprojects.archivist.albums.to.AlbumTo;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.documents.model.Document;
import ru.javaprojects.archivist.documents.repository.DocumentRepository;

@Service
@AllArgsConstructor
public class AlbumService {
    private final AlbumRepository repository;
    private final DocumentRepository documentRepository;

    public Page<Album> getAll(Pageable pageable) {
        return repository.findAllWithPagination(pageable);
    }

    public Page<Album> getAll(Pageable pageable, String keyword) {
        return repository.findAllByKeywordWithPagination(pageable, keyword);
    }

    public Album get(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Entity with id=" + id + " not found"));
    }

    @Transactional
    public Album create(AlbumTo albumTo) {
        Assert.notNull(albumTo, "albumTo must not be null");
        Document document = documentRepository.findByDecimalNumberIgnoreCase(albumTo.getDecimalNumber())
                .orElseGet(() -> documentRepository.save(Document.autoGenerate(albumTo.getDecimalNumber().toUpperCase())));
        return repository.save(new Album(null, document, albumTo.getStamp()));
    }

    @Transactional
    public void update(AlbumTo albumTo) {
        Assert.notNull(albumTo, "albumTo must not be null");
        Album album = get(albumTo.getId());
        if (!album.getMainDocument().getDecimalNumber().equalsIgnoreCase(albumTo.getDecimalNumber())) {
            Document document = documentRepository.findByDecimalNumberIgnoreCase(albumTo.getDecimalNumber())
                    .orElseGet(() -> documentRepository.save(Document.autoGenerate(albumTo.getDecimalNumber().toUpperCase())));
            album.setMainDocument(document);
        }
        album.setStamp(albumTo.getStamp());
    }

    public void delete(long id) {
        repository.deleteExisted(id);
    }
}
