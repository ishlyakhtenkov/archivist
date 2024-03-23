package ru.javaprojects.archivist.tools;

import ru.javaprojects.archivist.documents.model.Content;

import java.util.List;

public record GroupContent(List<Content> contents, GroupOperationResult operationResult) {
}
