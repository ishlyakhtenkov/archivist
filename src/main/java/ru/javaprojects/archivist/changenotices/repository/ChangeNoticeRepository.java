package ru.javaprojects.archivist.changenotices.repository;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.changenotices.model.ChangeNotice;
import ru.javaprojects.archivist.common.NamedRepository;

@Transactional(readOnly = true)
public interface ChangeNoticeRepository extends NamedRepository<ChangeNotice> {

}
