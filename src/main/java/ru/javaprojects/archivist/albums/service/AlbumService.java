package ru.javaprojects.archivist.albums.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.archivist.albums.model.Album;
import ru.javaprojects.archivist.albums.model.Issuance;
import ru.javaprojects.archivist.albums.model.Stamp;
import ru.javaprojects.archivist.albums.repository.AlbumRepository;
import ru.javaprojects.archivist.albums.repository.IssuanceRepository;
import ru.javaprojects.archivist.albums.to.AlbumTo;
import ru.javaprojects.archivist.albums.to.IssuanceTo;
import ru.javaprojects.archivist.common.error.IllegalRequestDataException;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.departments.model.Employee;
import ru.javaprojects.archivist.departments.repository.EmployeeRepository;
import ru.javaprojects.archivist.documents.model.Document;
import ru.javaprojects.archivist.documents.repository.DocumentRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class AlbumService {
    private final AlbumRepository repository;
    private final DocumentRepository documentRepository;
    private final IssuanceRepository issuanceRepository;
    private final EmployeeRepository employeeRepository;

    public Page<Album> getAll(Pageable pageable) {
        Page<Long> albumsIds = repository.findAllAlbumsIdsWithPagination(pageable);
        List<Album> albums = repository.findAllByIds(albumsIds.getContent());
        albums.forEach(a -> a.getIssuances().removeIf(i -> i.getReturned() != null));
        return new PageImpl<>(albums, pageable, albumsIds.getTotalElements());
    }

    public Page<Album> getAll(Pageable pageable, String keyword) {
        Page<Long> albumsIds = repository.findAllAlbumsIdsByKeywordWithPagination(pageable, keyword);
        List<Album> albums = repository.findAllByIds(albumsIds.getContent());
        albums.forEach(a -> a.getIssuances().removeIf(i -> i.getReturned() != null));
        return new PageImpl<>(albums, pageable, albumsIds.getTotalElements());
    }

    public Album get(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Entity with id=" + id + " not found"));
    }

    public Album getByDecimalNumberAndStamp(String decimalNumber, Stamp stamp) {
        Assert.notNull(decimalNumber, "decimalNumber must not be null");
        Assert.notNull(stamp, "stamp must not be null");
        return repository.findByMainDocument_DecimalNumberAndStamp(decimalNumber, stamp)
                .orElseThrow(() -> new NotFoundException("Album with decimal number=" + decimalNumber +
                        " and stamp=" + stamp + " not found"));
    }

    public Album getWithIssuances(long id) {
        return repository.findByIdWithIssuances(id)
                .orElseThrow(() -> new NotFoundException("Entity with id=" + id + " not found"));
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

    public List<Issuance> getIssuances(long albumId) {
        repository.getExisted(albumId);
        List<Issuance> issuances = issuanceRepository.findAllByAlbumId(albumId);
        if (!issuances.isEmpty() && issuances.get(issuances.size() - 1).getReturned() == null) {
            issuances.add(0, issuances.remove(issuances.size() - 1));
        }
        return issuances;
    }

    public void deleteIssuance(long id) {
        issuanceRepository.deleteExisted(id);
    }

    @Transactional
    public void returnAlbum(long id, LocalDate returned) {
        Assert.notNull(returned, "returned date must not be null");
        repository.getExisted(id);
        if (returned.isAfter(LocalDate.now())) {
            throw new IllegalRequestDataException("Return date can not be greater than today date");
        }
        Issuance issuance = issuanceRepository.findByAlbum_IdAndReturnedIsNull(id)
                .orElseThrow(() -> new IllegalRequestDataException("Album is already in the Archive"));
        if (issuance.getIssued().isAfter(returned)) {
            throw new IllegalRequestDataException("Return date must be greater than issue date");
        }
        issuance.setReturned(returned);
    }

    public Issuance issueAlbum(IssuanceTo issuanceTo) {
        if (issuanceTo.getIssued().isAfter(LocalDate.now())) {
            throw new IllegalRequestDataException("Issue date can not be greater than today date");
        }
        Assert.notNull(issuanceTo, "issuanceTo must not be null");
        issuanceRepository.findFirstByAlbum_IdOrderByIssuedDesc(issuanceTo.getAlbumId())
                .ifPresent(lastIssuance -> {
                    if (lastIssuance.getReturned() == null) {
                        throw new IllegalRequestDataException("Album is already issued");
                    }
                    if (issuanceTo.getIssued().isBefore(lastIssuance.getIssued())) {
                        throw new IllegalRequestDataException("Issue date must be greater than last issue date");
                    }
                });
        Album album = repository.getExisted(issuanceTo.getAlbumId());
        Employee employee = employeeRepository.getExisted(issuanceTo.getEmployeeId());
        return issuanceRepository.save(new Issuance(null, album, employee, issuanceTo.getIssued()));
    }
}
