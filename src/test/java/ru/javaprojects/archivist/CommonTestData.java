package ru.javaprojects.archivist;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class CommonTestData {
    public static final String USER_MAIL = "user@gmail.com";
    public static final String ADMIN_MAIL = "admin@gmail.com";
    public static final String ARCHIVIST_MAIL = "archivist@gmail.com";

    public static final String KEYWORD = "keyword";
    public static final String ACTION = "action";
    public static final String ID = "id";

    public static final long NOT_FOUND = 100;
    public static final String ENTITY_NOT_FOUND = "Entity with id=" + NOT_FOUND + " not found";

    public static final String NAME_PARAM = "name";

    public static final String FALSE = "false";
    public static final String TRUE = "true";

    public static MultiValueMap<String, String> getPageableParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("page", "0");
        params.add("size", "2");
        return params;
    }
}
