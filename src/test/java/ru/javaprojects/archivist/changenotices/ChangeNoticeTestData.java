package ru.javaprojects.archivist.changenotices;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.MatcherFactory;
import ru.javaprojects.archivist.changenotices.model.Change;
import ru.javaprojects.archivist.changenotices.model.ChangeNotice;
import ru.javaprojects.archivist.changenotices.to.ChangeNoticeTo;
import ru.javaprojects.archivist.changenotices.to.ChangeTo;
import ru.javaprojects.archivist.documents.model.ContentFile;
import ru.javaprojects.archivist.documents.model.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static java.time.Month.*;
import static ru.javaprojects.archivist.CommonTestData.ID;
import static ru.javaprojects.archivist.CommonTestData.NAME_PARAM;
import static ru.javaprojects.archivist.changenotices.model.ChangeReasonCode.DESIGN_IMPROVEMENTS;
import static ru.javaprojects.archivist.changenotices.model.ChangeReasonCode.QUALITY_IMPROVEMENT;
import static ru.javaprojects.archivist.departments.DepartmentTestData.*;
import static ru.javaprojects.archivist.documents.DocumentTestData.*;

public class ChangeNoticeTestData {
    public static final MatcherFactory.Matcher<ChangeNotice> CHANGE_NOTICE_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(ChangeNotice.class);

    public static final MatcherFactory.Matcher<ChangeNoticeTo> CHANGE_NOTICE_TO_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(ChangeNoticeTo.class);

    public static final long CHANGE_NOTICE_1_ID = 100047L;
    public static final long CHANGE_NOTICE_2_ID = 100048L;
    public static final long CHANGE_NOTICE_3_ID = 100049L;

    public static final long CHANGE_NOTICE_1_CHANGE_1_ID = 100050L;
    public static final long CHANGE_NOTICE_1_CHANGE_2_ID = 100052L;

    public static final String CHANGE_NOTICES_ATTRIBUTE = "changeNotices";
    public static final String CHANGE_NOTICE_ATTRIBUTE = "changeNotice";
    public static final String CHANGE_NOTICE_TO_ATTRIBUTE = "changeNoticeTo";

    public static final ChangeNotice changeNotice1 = new ChangeNotice(CHANGE_NOTICE_1_ID, "VUIA.SK.591", LocalDate.of(2020, JUNE, 18),
            DESIGN_IMPROVEMENTS, false, department1, new ContentFile("VUIA.SK.591.pdf", "VUIA.SK.591/VUIA.SK.591.pdf"));
    static {
        changeNotice1.addChange(new Change(CHANGE_NOTICE_1_CHANGE_1_ID, document1, 1));
        changeNotice1.addChange(new Change(CHANGE_NOTICE_1_CHANGE_2_ID, document2, 1));
    }
    public static final ChangeNotice changeNotice2 = new ChangeNotice(CHANGE_NOTICE_2_ID, "VUIA.TN.429", LocalDate.of(2021, DECEMBER, 14),
            QUALITY_IMPROVEMENT, false, department3, new ContentFile("VUIA.TN.429.pdf", "VUIA.TN.429/VUIA.TN.429.pdf"));
    public static final ChangeNotice changeNotice3 = new ChangeNotice(CHANGE_NOTICE_3_ID, "VUIA.SK.592", LocalDate.of(2020, SEPTEMBER, 11),
            null, true, null, null);

    public static final MockMultipartFile CHANGE_NOTICE_FILE = new MockMultipartFile("file", "VUIA.SK.600.pdf",
            MediaType.APPLICATION_PDF_VALUE, "VUIA.SK.600 change notice content".getBytes());

    public static ChangeNoticeTo getNewTo() {
        return new ChangeNoticeTo(null, "VUIA.SK.600", LocalDate.of(2023, NOVEMBER, 29), DESIGN_IMPROVEMENTS, department1,
                CHANGE_NOTICE_FILE, List.of(new ChangeTo(null, document1.getDecimalNumber(), 3), new ChangeTo(null, "VUIA.611222.001", 1)));
    }

    public static ChangeNotice getNew() {
        ChangeNoticeTo newTo = getNewTo();
        ChangeNotice changeNotice = new ChangeNotice(null, newTo.getName(), newTo.getReleaseDate(), newTo.getChangeReasonCode(), newTo.getDeveloper(),
                new ContentFile(newTo.getFile().getOriginalFilename(), newTo.getName() + "/" + newTo.getFile().getOriginalFilename()));
        changeNotice.addChange(new Change(null, document1, 3));
        changeNotice.addChange(new Change(null, Document.autoGenerate("VUIA.611222.001"), 1));
        return changeNotice;
    }

    public static ChangeNoticeTo getUpdatedTo() {
        return new ChangeNoticeTo(CHANGE_NOTICE_1_ID, "VUIA.SK.595", LocalDate.of(2020, JUNE, 20), QUALITY_IMPROVEMENT, department2,
                List.of(new ChangeTo(CHANGE_NOTICE_1_CHANGE_1_ID, document1.getDecimalNumber(), 1), new ChangeTo(CHANGE_NOTICE_1_CHANGE_2_ID, document2.getDecimalNumber(), 1),
                        new ChangeTo(null, "VUIA.611222.001", 1)));
    }

    public static ChangeNotice getUpdated() {
        ChangeNoticeTo updatedTo = getUpdatedTo();
        ChangeNotice changeNotice = new ChangeNotice(updatedTo.getId(), updatedTo.getName(), updatedTo.getReleaseDate(),
                updatedTo.getChangeReasonCode(), updatedTo.getDeveloper(),
                new ContentFile("VUIA.SK.591.pdf", updatedTo.getName() + "/VUIA.SK.591.pdf"));
        changeNotice.addChange(new Change(CHANGE_NOTICE_1_CHANGE_1_ID, document1, 1));
        changeNotice.addChange(new Change(CHANGE_NOTICE_1_CHANGE_2_ID, document2, 1));
        changeNotice.addChange(new Change(null, Document.autoGenerate("VUIA.611222.001"), 1));
        return changeNotice;
    }


    public static MultiValueMap<String, String> getNewParams() throws IOException {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        ChangeNoticeTo newChangeNoticeTo = getNewTo();
        params.add(NAME_PARAM, newChangeNoticeTo.getName());
        params.add("releaseDate", newChangeNoticeTo.getReleaseDate().toString());
        params.add("changeReasonCode", newChangeNoticeTo.getChangeReasonCode().name());
        params.add(DEVELOPER_PARAM, String.valueOf(newChangeNoticeTo.getDeveloper().getId()));
        params.add("changes[0].decimalNumber", document1.getDecimalNumber());
        params.add("changes[0].changeNumber", String.valueOf(3));
        params.add("changes[1].decimalNumber", "VUIA.611222.001");
        params.add("changes[1].changeNumber", String.valueOf(1));
        return params;
    }

    public static MultiValueMap<String, String> getNewInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(NAME_PARAM, "A");
        params.add("releaseDate", "");
        params.add("changeReasonCode", QUALITY_IMPROVEMENT.name());
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedParams() throws IOException {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        ChangeNoticeTo updatedChangeNoticeTo = getUpdatedTo();
        params.add(ID, String.valueOf(CHANGE_NOTICE_1_ID));
        params.add(NAME_PARAM, updatedChangeNoticeTo.getName());
        params.add("releaseDate", updatedChangeNoticeTo.getReleaseDate().toString());
        params.add("changeReasonCode", updatedChangeNoticeTo.getChangeReasonCode().name());
        params.add(DEVELOPER_PARAM, String.valueOf(updatedChangeNoticeTo.getDeveloper().getId()));
        params.add("changes[0].id", String.valueOf(CHANGE_NOTICE_1_CHANGE_1_ID));
        params.add("changes[0].decimalNumber", document1.getDecimalNumber());
        params.add("changes[0].changeNumber", String.valueOf(1));
        params.add("changes[1].id", String.valueOf(CHANGE_NOTICE_1_CHANGE_2_ID));
        params.add("changes[1].decimalNumber", document2.getDecimalNumber());
        params.add("changes[1].changeNumber", String.valueOf(1));
        params.add("changes[2].decimalNumber", "VUIA.611222.001");
        params.add("changes[2].changeNumber", String.valueOf(1));
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedInvalidParams() {
        MultiValueMap<String, String> params = getNewInvalidParams();
        params.add(ID, String.valueOf(CHANGE_NOTICE_1_ID));
        return params;
    }
}
