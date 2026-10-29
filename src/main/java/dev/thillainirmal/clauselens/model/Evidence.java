package dev.thillainirmal.clauselens.model;

public record Evidence(
        String documentId,
        String documentTitle,
        String department,
        String jurisdiction,
        String section,
        String excerpt,
        double score
) {
}
