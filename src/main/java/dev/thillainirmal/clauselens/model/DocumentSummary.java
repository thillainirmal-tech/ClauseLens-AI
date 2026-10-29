package dev.thillainirmal.clauselens.model;

import java.time.Instant;

public record DocumentSummary(
        String id,
        String title,
        String department,
        String jurisdiction,
        String version,
        int chunkCount,
        Instant ingestedAt
) {
    public static DocumentSummary from(PolicyDocument document) {
        return new DocumentSummary(document.id(), document.title(), document.department(),
                document.jurisdiction(), document.version(), document.sections().size(), document.ingestedAt());
    }
}
