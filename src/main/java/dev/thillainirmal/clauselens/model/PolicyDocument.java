package dev.thillainirmal.clauselens.model;

import java.time.Instant;
import java.util.List;

public record PolicyDocument(
        String id,
        String fingerprint,
        String title,
        String department,
        String jurisdiction,
        String version,
        Instant ingestedAt,
        List<String> sections
) {
    public PolicyDocument {
        sections = List.copyOf(sections);
    }
}
