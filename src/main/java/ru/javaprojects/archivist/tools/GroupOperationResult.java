package ru.javaprojects.archivist.tools;

import java.util.Collection;

public record GroupOperationResult(Collection<String> processedDocuments, Collection<String> notProcessedDocuments) {
}
