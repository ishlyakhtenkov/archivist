package ru.javaprojects.archivist.albums;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.MatcherFactory;
import ru.javaprojects.archivist.albums.model.Album;
import ru.javaprojects.archivist.albums.model.Issuance;
import ru.javaprojects.archivist.albums.to.IssuanceTo;

import java.time.LocalDate;
import java.util.List;

import static java.time.Month.*;
import static ru.javaprojects.archivist.CommonTestData.ID;
import static ru.javaprojects.archivist.albums.model.Stamp.STAMP_1;
import static ru.javaprojects.archivist.albums.model.Stamp.STAMP_2;
import static ru.javaprojects.archivist.departments.DepartmentTestData.*;
import static ru.javaprojects.archivist.documents.DocumentTestData.*;

public class AlbumTestData {
    public static final MatcherFactory.Matcher<Album> ALBUM_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Album.class);

    public static final MatcherFactory.Matcher<Issuance> ISSUANCE_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Issuance.class);


    public static final long ALBUM1_ID = 100057;
    public static final long ALBUM2_ID = 100058;
    public static final long ALBUM3_ID = 100059;
    public static final long ALBUM4_ID = 100060;

    public static final Album album1 = new Album(ALBUM1_ID, document1, STAMP_1);
    public static final Album album2 = new Album(ALBUM2_ID, document1, STAMP_2);
    public static final Album album3 = new Album(ALBUM3_ID, document2, STAMP_1);
    public static final Album album4 = new Album(ALBUM4_ID, document3, STAMP_1);

    public static final String ALBUMS_ATTRIBUTE = "albums";
    public static final String ALBUM_ATTRIBUTE = "album";
    public static final String ALBUM_TO_ATTRIBUTE = "albumTo";
    public static final String STAMPS_ATTRIBUTE = "stamps";

    public static final String STAMP = "stamp";
    public static final String RETURNED = "returned";

    public static final long ALBUM1_ISSUANCE1_ID = 100070;
    public static final long ALBUM1_ISSUANCE2_ID = 100071;
    public static final long ALBUM1_ISSUANCE3_ID = 100073;
    public static final long ALBUM1_ISSUANCE4_ID = 100074;
    public static final long ALBUM1_ISSUANCE5_ID = 100076;

    public static final Issuance album1Issuance1 = new Issuance(ALBUM1_ISSUANCE1_ID, album1, dep2Employee2,
            LocalDate.of(2021, MAY, 28), LocalDate.of(2021, MAY, 30));

    public static final Issuance album1Issuance2 = new Issuance(ALBUM1_ISSUANCE2_ID, album1, dep2Employee2,
            LocalDate.of(2021, JULY, 14), LocalDate.of(2021, JULY, 16));

    public static final Issuance album1Issuance3 = new Issuance(ALBUM1_ISSUANCE3_ID, album1, dep2Employee1,
            LocalDate.of(2021, AUGUST, 17), LocalDate.of(2021, AUGUST, 24));

    public static final Issuance album1Issuance4 = new Issuance(ALBUM1_ISSUANCE4_ID, album1, dep2Employee2,
            LocalDate.of(2021, OCTOBER, 11), LocalDate.of(2021, OCTOBER, 28));

    public static final Issuance album1Issuance5 = new Issuance(ALBUM1_ISSUANCE5_ID, album1, dep2Employee1,
            LocalDate.of(2022, FEBRUARY, 14));

    static {
        album1.setIssuances(List.of(album1Issuance5, album1Issuance4, album1Issuance3, album1Issuance2, album1Issuance1));
    }

    public static Album getNewAlbum() {
        return new Album(null, document2, STAMP_2);
    }

    public static MultiValueMap<String, String> getNewAlbumParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Album newAlbum = getNewAlbum();
        params.add(DECIMAL_NUMBER, newAlbum.getMainDocument().getDecimalNumber());
        params.add(STAMP, newAlbum.getStamp().name());
        return params;
    }

    public static MultiValueMap<String, String> getNewAlbumInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(DECIMAL_NUMBER, "<p>decNumber</p>");
        params.add(STAMP, getNewAlbum().getStamp().name());
        return params;
    }

    public static Album getUpdatedAlbum() {
        return new Album(ALBUM3_ID, document2, STAMP_2);
    }

    public static MultiValueMap<String, String> getUpdatedAlbumParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Album updatedAlbum = getUpdatedAlbum();
        params.add(ID, String.valueOf(ALBUM3_ID));
        params.add(DECIMAL_NUMBER, updatedAlbum.getMainDocument().getDecimalNumber());
        params.add(STAMP, updatedAlbum.getStamp().name());
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedAlbumInvalidParams() {
        MultiValueMap<String, String> params = getUpdatedAlbumParams();
        params.set(DECIMAL_NUMBER, "<p>decNumber</p>");
        return params;
    }

    public static LocalDate CORRECT_RETURN_DATE = LocalDate.of(2023, MARCH, 4);
    public static LocalDate RETURN_DATE_BEFORE_ISSUE_DATE = LocalDate.of(2021, MARCH, 4);
    public static LocalDate ISSUE_DATE_BEFORE_LAST_ISSUE_DATE = LocalDate.of(2019, APRIL, 14);

    public static Issuance getNewIssuance() {
        return new Issuance(null, album2, dep2Employee1, LocalDate.of(2023, MARCH, 3));
    }

    public static IssuanceTo getNewIssuanceTo() {
        return new IssuanceTo(ALBUM2_ID, DEP2_EMPLOYEE1_ID, LocalDate.of(2023, MARCH, 3));
    }
}
