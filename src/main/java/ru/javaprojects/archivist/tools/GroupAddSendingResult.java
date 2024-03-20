package ru.javaprojects.archivist.tools;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class GroupAddSendingResult {
    private final List<String> notFoundDocuments;
    private final List<String> alreadySentDocuments;
    private final List<String> sentDocuments;

    public void addNotFoundDocuments(Collection<String> decimalNumbers) {
        notFoundDocuments.addAll(decimalNumbers);
    }

    public void addAlreadySentDocument(String decimalNumber) {
        alreadySentDocuments.add(decimalNumber);
    }

    public void addSentDocuments(Collection<String> decimalNumbers) {
        sentDocuments.addAll(decimalNumbers);
    }

    public GroupAddSendingResult() {
        notFoundDocuments = new ArrayList<>();
        alreadySentDocuments = new ArrayList<>();
        sentDocuments = new ArrayList<>();
    }
}
