package ru.javaprojects.archivist.tools;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class GroupUnsubscribeResult {
    private final List<String> notHaveSubscriberDocuments;
    private final List<String> unsubscribedSubscriberDocuments;

    public GroupUnsubscribeResult() {
        this.notHaveSubscriberDocuments = new ArrayList<>();
        this.unsubscribedSubscriberDocuments = new ArrayList<>();
    }
}
