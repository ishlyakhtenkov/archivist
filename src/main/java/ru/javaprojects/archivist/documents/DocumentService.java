package ru.javaprojects.archivist.documents;

import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.archivist.changenotices.model.Change;
import ru.javaprojects.archivist.changenotices.model.ChangeNotice;
import ru.javaprojects.archivist.changenotices.repository.ChangeNoticeRepository;
import ru.javaprojects.archivist.changenotices.repository.ChangeRepository;
import ru.javaprojects.archivist.common.error.IllegalRequestDataException;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.common.util.FileUtil;
import ru.javaprojects.archivist.common.util.validation.NotAutoGenerated;
import ru.javaprojects.archivist.companies.CompanyRepository;
import ru.javaprojects.archivist.documents.model.*;
import ru.javaprojects.archivist.documents.repository.*;
import ru.javaprojects.archivist.documents.to.ApplicabilityTo;
import ru.javaprojects.archivist.documents.to.ChangeTo;
import ru.javaprojects.archivist.documents.to.SendingTo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static ru.javaprojects.archivist.documents.model.Status.*;

@Service
@RequiredArgsConstructor
@Validated({NotAutoGenerated.class, Default.class})
public class DocumentService {
    private final DocumentRepository repository;
    private final ApplicabilityRepository applicabilityRepository;
    private final ContentRepository contentRepository;
    private final SubscriberRepository subscriberRepository;
    private final SendingRepository sendingRepository;
    private final CompanyRepository companyRepository;
    private final InvoiceRepository invoiceRepository;
    private final LetterRepository letterRepository;
    private final ChangeRepository changeRepository;
    private final ChangeNoticeRepository changeNoticeRepository;

    @Value("${content-path.documents}")
    private String contentPath;

    public Page<Document> getAll(Pageable pageable) {
        return repository.findAllByAutoGeneratedIsFalseOrderByDecimalNumber(pageable);
    }

    public Page<Document> getAll(Pageable pageable, String keyword) {
        return repository.findAllByDecimalNumberContainsIgnoreCaseAndAutoGeneratedIsFalseOrderByDecimalNumber(pageable, keyword);
    }

    public Document get(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Entity with id=" + id + " not found"));
    }

    public Document getByDecimalNumber(String decimalNumber) {
        return repository.findByDecimalNumberIgnoreCase(decimalNumber)
                .orElseThrow(() -> new NotFoundException("Not found document with decimal number=" + decimalNumber));
    }

    @Transactional
    public void createOrUpdate(@Valid Document document) {
        Assert.notNull(document, "document must not be null");
        document.setDecimalNumber(document.getDecimalNumber().toUpperCase());
        if (!document.isNew()) {
            repository.getExisted(document.id());
        } else {
            repository.findByDecimalNumberAndAutoGeneratedIsTrueIgnoreCase(document.getDecimalNumber())
                    .ifPresent(dbDocument -> document.setId(dbDocument.getId()));
        }
        repository.save(document);
    }

    @Transactional
    public void delete(long id) {
        Document document = repository.getExisted(id);
        repository.delete(document);
        FileUtil.deleteDir(contentPath + document.getDecimalNumber());
    }

    public List<Applicability> getApplicabilities(long documentId) {
        repository.getExisted(documentId);
        return applicabilityRepository.findAllByDocumentId(documentId);
    }

    public void deleteApplicability(long id) {
        applicabilityRepository.deleteExisted(id);
    }

    @Transactional
    public Applicability createApplicability(ApplicabilityTo applicabilityTo) {
        Assert.notNull(applicabilityTo, "applicabilityTo must not be null");
        Document document = repository.getExisted(applicabilityTo.getDocumentId());
        Document applicability = repository.findByDecimalNumberIgnoreCase(applicabilityTo.getDecimalNumber())
                .orElseGet(() -> repository.save(Document.autoGenerate(applicabilityTo.getDecimalNumber())));
        return applicabilityRepository.save(new Applicability(null, document, applicability, applicabilityTo.isPrimal()));
    }

    public Content getLatestContent(long documentId) {
        repository.getExisted(documentId);
        return contentRepository.findFirstByDocument_IdOrderByChangeNumberDesc(documentId).orElse(null);
    }

    public List<Content> getAllContents(long documentId) {
        repository.getExisted(documentId);
        return contentRepository.findByDocument_IdOrderByChangeNumberDesc(documentId);
    }

    @Transactional
    public void deleteContent(long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Entity with id=" + id + " not found"));;
        contentRepository.delete(content);
        content.getFiles()
                .forEach(file -> FileUtil.delete(contentPath + file.getFileLink()));
        FileUtil.delete(contentPath + generateFilePath(content.getDocument(), content.getChangeNumber()));
        FileUtil.deleteDirIfEmpty(contentPath + content.getDocument().getDecimalNumber());
    }

    @Transactional
    public Content createContent(long documentId, int changeNumber, MultipartFile[] files) {
        Assert.notNull(files, "files must not be null");
        Document document = repository.getExisted(documentId);
        List<ContentFile> contentFiles = Arrays.stream(files)
                .map(file -> new ContentFile(file.getOriginalFilename(), generateFileLink(document, changeNumber, file)))
                .toList();
        Content content = contentRepository.saveAndFlush(new Content(null, changeNumber, document, contentFiles));
        for (MultipartFile file : files) {
            FileUtil.upload(file, contentPath + generateFilePath(document, changeNumber), file.getOriginalFilename());
        }
        return content;
    }

    private String generateFileLink(Document document, int changeNumber, MultipartFile file) {
        return generateFilePath(document, changeNumber) + file.getOriginalFilename();
    }

    private String generateFilePath(Document document, int changeNumber) {
        return document.getDecimalNumber() + "/" + changeNumber + "/";
    }

    public List<Subscriber> getSubscribers(long documentId) {
        repository.getExisted(documentId);
        return subscriberRepository.findAllByDocumentId(documentId);
    }

    public List<Sending> getSendings(long documentId, long companyId) {
        repository.getExisted(documentId);
        companyRepository.getExisted(companyId);
        return sendingRepository.findAllByDocumentIdAndCompanyId(documentId, companyId);
    }

    @Transactional
    public Sending createSending(SendingTo sendingTo) {
        Assert.notNull(sendingTo, "sendingTo must not be null");
        Document document = repository.getExisted(sendingTo.getDocumentId());
        Invoice invoice = invoiceRepository.findByNumberAndDate(sendingTo.getInvoiceNumber(), sendingTo.getInvoiceDate())
                .orElseGet(() -> {
                    Letter letter;
                    if (sendingTo.getLetterNumber() == null || sendingTo.getLetterNumber().isBlank()) {
                        letter = letterRepository.save(new Letter(null, null, sendingTo.getLetterDate(),
                                companyRepository.getExisted(sendingTo.getCompanyId())));
                    } else {
                        letter = letterRepository.findByNumber(sendingTo.getLetterNumber()).orElseGet(() ->
                                letterRepository.save(new Letter(null, sendingTo.getLetterNumber(), sendingTo.getLetterDate(),
                                        companyRepository.getExisted(sendingTo.getCompanyId()))));
                    }
                    return invoiceRepository.save(new Invoice(null, sendingTo.getInvoiceNumber(),
                            sendingTo.getInvoiceDate(), sendingTo.getStatus(), letter));
                });
        if (!Objects.equals(invoice.getLetter().getCompany().getId(), sendingTo.getCompanyId())) {
            throw new IllegalRequestDataException("specified invoice exists and addressed to " + invoice.getLetter().getCompany().getName());
        }
        if (invoice.getStatus() != sendingTo.getStatus()) {
            throw new IllegalRequestDataException("specified invoice exists and has " + invoice.getStatus() + " status");
        }
        subscriberRepository.findByDocument_IdAndCompany_Id(sendingTo.getDocumentId(), sendingTo.getCompanyId())
                .ifPresentOrElse((subscriber) -> updateSubscriberIfNecessary(subscriber, sendingTo),
                        () -> subscriberRepository.save(new Subscriber(null, document,
                                companyRepository.getExisted(sendingTo.getCompanyId()),
                                sendingTo.getStatus() != UNACCOUNTED_COPY, sendingTo.getStatus())));
        return sendingRepository.save(new Sending(null, document, invoice));
    }

    private void updateSubscriberIfNecessary(Subscriber subscriber, SendingTo sendingTo) {
        if (sendingTo.getStatus().ordinal() > subscriber.getStatus().ordinal()) {
            subscriber.setStatus(sendingTo.getStatus());
        }
        if (!subscriber.isSubscribed()) {
            if (sendingTo.getStatus() == ACCOUNTED_COPY || sendingTo.getStatus() == DUPLICATE) {
                if (subscriber.getUnsubscribeTimestamp() == null) {
                    subscriber.setSubscribed(true);
                } else if (sendingTo.getInvoiceDate().isAfter(subscriber.getUnsubscribeTimestamp().toLocalDate())) {
                    subscriber.setSubscribed(true);
                    subscriber.setUnsubscribeTimestamp(null);
                    subscriber.setUnsubscribeReason(null);
                }
            }
        }
    }

    @Transactional
    public void unsubscribe(long subscriberId, String unsubscribeReason) {
        Assert.notNull(unsubscribeReason, "unsubscribeReason must not be null");
        Subscriber subscriber = subscriberRepository.getExisted(subscriberId);
        subscriber.setSubscribed(false);
        subscriber.setUnsubscribeReason(unsubscribeReason);
        subscriber.setUnsubscribeTimestamp(LocalDateTime.now());
    }

    @Transactional
    public void resubscribe(long subscriberId) {
        Subscriber subscriber = subscriberRepository.getExisted(subscriberId);
        subscriber.setSubscribed(true);
        subscriber.setUnsubscribeReason(null);
        subscriber.setUnsubscribeTimestamp(null);
    }

    @Transactional
    public void deleteSending(long sendingId) {
        Sending sending = sendingRepository.findByIdWithInvoice(sendingId)
                .orElseThrow(() -> new NotFoundException("Entity with id=" + sendingId + " not found"));
        sendingRepository.delete(sending);
        if (sendingRepository.countAllByInvoice_Id(sending.getInvoice().id()) == 0) {
            invoiceRepository.delete(sending.getInvoice());
            if (invoiceRepository.countAllByLetter_Id(sending.getInvoice().getLetter().id()) == 0) {
                letterRepository.delete(sending.getInvoice().getLetter());
            }
        }
        List<Sending> sendings = sendingRepository.findAllByDocumentIdAndCompanyId(sending.getDocument().id(),
                sending.getInvoice().getLetter().getCompany().id());
        if (sendings.isEmpty()) {
            subscriberRepository.deleteByDocument_IdAndCompany_Id(sending.getDocument().id(),
                    sending.getInvoice().getLetter().getCompany().id());
        } else {
            Status status = sendings.stream().map(s -> s.getInvoice().getStatus()).max(Comparator.naturalOrder()).orElseThrow(() -> new IllegalStateException("no sendings to define subscriber status"));
            Subscriber subscriber = subscriberRepository.findByDocument_IdAndCompany_Id(sending.getDocument().id(),
                    sending.getInvoice().getLetter().getCompany().id()).orElseThrow(() -> new NotFoundException("Subscriber not found"));
            subscriber.setStatus(status);
        }
    }

    public List<Change> getChanges(long documentId) {
        repository.getExisted(documentId);
        return changeRepository.findAllByDocument_IdOrderByChangeNumberDesc(documentId);
    }

    @Transactional
    public Change createChange(ChangeTo changeTo) {
        Assert.notNull(changeTo, "changeTo must not be null");
        Document document = repository.getExisted(changeTo.getDocumentId());
        ChangeNotice changeNotice = changeNoticeRepository.findWithChangesByNameIgnoreCase(changeTo.getChangeNoticeName())
                .orElseGet(() -> ChangeNotice.autoGenerate(changeTo.getChangeNoticeName(), changeTo.getChangeNoticeDate()));
        if (!changeNotice.getReleaseDate().equals(changeTo.getChangeNoticeDate())) {
            throw new IllegalRequestDataException("Change notice " + changeNotice.getName() +
                    " already exists and has release date: " + changeNotice.getReleaseDate());
        }
        changeNotice.addChange(new Change(changeTo.getId(), document, changeNotice, changeTo.getChangeNumber()));
        changeNoticeRepository.save(changeNotice);
        return changeNotice.getChanges().get(changeNotice.getChanges().size() - 1);
    }

    @Transactional
    public void deleteChange(long changeId) {
        Change change = changeRepository.findById(changeId)
                .orElseThrow(() -> new NotFoundException("Entity with id=" + changeId + " not found"));
        changeRepository.delete(change);
        ChangeNotice changeNotice = change.getChangeNotice();
        if (changeNotice.isAutoGenerated() && changeRepository.countAllByChangeNotice_Id(changeNotice.id()) == 0) {
            changeNoticeRepository.deleteExisted(changeNotice.id());
        }
    }
}
