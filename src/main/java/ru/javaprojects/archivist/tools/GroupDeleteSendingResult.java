package ru.javaprojects.archivist.tools;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class GroupDeleteSendingResult {
    private final List<String> notHaveSendingDocuments;
    private final List<String> deletedSendingDocuments;

    public void addDeletedSendingDocument(String decimalNumber) {
        deletedSendingDocuments.add(decimalNumber);
    }

    public void addNotHaveSendingDocuments(Collection<String> decimalNumbers) {
        notHaveSendingDocuments.addAll(decimalNumbers);
    }

    public GroupDeleteSendingResult() {
        this.notHaveSendingDocuments = new ArrayList<>();
        this.deletedSendingDocuments = new ArrayList<>();
    }
}
