package ru.javaprojects.archivist.tools;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class GroupAddSendingResult {
    private final List<String> notFoundDocuments = new ArrayList<>();
    private final List<String> alreadySentDocuments = new ArrayList<>();
    private final List<String> sentDocuments = new ArrayList<>();

    public void addNotFoundDocuments(Collection<String> decimalNumbers) {
        notFoundDocuments.addAll(decimalNumbers);
    }

    public void addAlreadySentDocument(String decimalNumber) {
        alreadySentDocuments.add(decimalNumber);
    }

    public void addSentDocuments(Collection<String> decimalNumbers) {
        sentDocuments.addAll(decimalNumbers);
    }
}
