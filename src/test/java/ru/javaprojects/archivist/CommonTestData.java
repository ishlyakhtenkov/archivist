package ru.javaprojects.archivist;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class CommonTestData {
    public static final String KEYWORD = "keyword";

    public static MultiValueMap<String, String> getPageableParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("page", "0");
        params.add("size", "2");
        return params;
    }
}
