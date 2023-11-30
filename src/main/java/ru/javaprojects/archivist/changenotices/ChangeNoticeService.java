package ru.javaprojects.archivist.changenotices;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.archivist.changenotices.model.Change;
import ru.javaprojects.archivist.changenotices.model.ChangeNotice;
import ru.javaprojects.archivist.changenotices.repository.ChangeNoticeRepository;
import ru.javaprojects.archivist.changenotices.repository.ChangeRepository;
import ru.javaprojects.archivist.changenotices.to.ChangeNoticeTo;
import ru.javaprojects.archivist.common.error.IllegalRequestDataException;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.common.util.FileUtil;
import ru.javaprojects.archivist.documents.model.ContentFile;

import java.nio.file.Files;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangeNoticeService {
    private final ChangeNoticeRepository repository;
    private final ChangeNoticeUtil changeNoticeUtil;
    private final ChangeRepository changeRepository;

    @Value("${content-path.change-notices}")
    private String contentPath;

    public Page<ChangeNotice> getAll(Pageable pageable) {
        return repository.findAllByAutoGeneratedIsFalseOrderByName(pageable);
    }

    public Page<ChangeNotice> getAll(Pageable pageable, String keyword) {
        return repository.findAllByNameContainsIgnoreCaseAndAutoGeneratedIsFalseOrderByName(pageable, keyword);
    }

    @Transactional
    public ChangeNotice create(ChangeNoticeTo changeNoticeTo) {
        Assert.notNull(changeNoticeTo, "changeNoticeTo must not be null");
        if (changeNoticeTo.getFile() == null) {
            throw new IllegalRequestDataException("Change notice file is not present");
        }
        ChangeNotice changeNotice = repository.findByNameIgnoreCaseAndAutoGeneratedIsTrue(changeNoticeTo.getName())
                .orElseGet(() -> changeNoticeUtil.createNewFromTo(changeNoticeTo));
        if (!changeNotice.isNew()) {
            if (!changeNotice.getReleaseDate().equals(changeNoticeTo.getReleaseDate())) {
                throw new IllegalRequestDataException("Autogenerated change notice has different release date");
            }
            changeNoticeUtil.updateFromTo(changeNotice, changeNoticeTo);
        }
        repository.saveAndFlush(changeNotice);
        String fileName = changeNoticeTo.getFile().getOriginalFilename();
        FileUtil.upload(changeNoticeTo.getFile(), contentPath + changeNoticeTo.getName() + "/", fileName);
        return changeNotice;
    }

    @Transactional
    public ChangeNotice update(ChangeNoticeTo changeNoticeTo) {
        Assert.notNull(changeNoticeTo, "changeNoticeTo must not be null");
        ChangeNotice changeNotice = repository.findWithChangesById(changeNoticeTo.getId())
                .orElseThrow(() -> new NotFoundException("Entity with id=" + changeNoticeTo.getId() + " not found"));
        if (!changeNoticeTo.getName().equals(changeNotice.getName())) {
            repository.deleteByNameIgnoreCaseAndAutoGeneratedIsTrue(changeNoticeTo.getName());
            repository.flush();
        }
        String oldName = changeNotice.getName();
        ContentFile oldFile = changeNotice.getFile();
        repository.saveAndFlush(changeNoticeUtil.updateFromTo(changeNotice, changeNoticeTo));
        if (changeNoticeTo.getFile() != null) {
            FileUtil.deleteDir(contentPath + oldName);
            String fileName = changeNoticeTo.getFile().getOriginalFilename();
            FileUtil.upload(changeNoticeTo.getFile(), contentPath + changeNoticeTo.getName() + "/", fileName);
        } else if (!changeNoticeTo.getName().equals(oldName)) {
            FileUtil.moveFile(contentPath + oldName + "/" + oldFile.getFileName(),
                    contentPath + changeNoticeTo.getName());
            FileUtil.deleteDirIfEmpty(contentPath + oldName);
        }
        return changeNotice;
    }

    public ChangeNotice get(long id) {
        return repository.getExisted(id);
    }

    public ChangeNotice getWithChanges(long id) {
        return repository.findWithChangesAndDeveloperById(id)
                .orElseThrow(() -> new NotFoundException("Entity with id=" + id + " not found"));
    }

    public ChangeNotice getByName(String name) {
        return repository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("Change notice " + name + " not found"));
    }

    public ChangeNotice getWithChangesByName(String name) {
        return repository.findWithChangesAndDeveloperByName(name)
                .orElseThrow(() -> new NotFoundException("Change notice " + name + " not found"));
    }

    @Transactional
    public void delete(long id) {
        ChangeNotice changeNotice = repository.getExisted(id);
        repository.deleteExisted(changeNotice.id());
        FileUtil.delete(contentPath + changeNotice.getFile().getFileLink());
        FileUtil.deleteDirIfEmpty(contentPath + changeNotice.getName());
    }

    public List<Change> getChanges(String changeNoticeName) {
        return changeRepository.findAllByAutogeneratedChangeNoticeName(changeNoticeName);
    }
}
