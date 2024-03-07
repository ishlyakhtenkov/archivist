package ru.javaprojects.archivist.globalsearch;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SearchResult {
    private String header;
    private String groupName;
    private Long entityId;
    private String matchValue;
}
