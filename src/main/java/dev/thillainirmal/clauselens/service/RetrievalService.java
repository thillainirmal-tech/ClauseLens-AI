package dev.thillainirmal.clauselens.service;

import dev.thillainirmal.clauselens.model.Evidence;
import dev.thillainirmal.clauselens.model.PolicyDocument;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class RetrievalService {
    private static final int DIMENSIONS = 384;
    private final PolicyKnowledgeBase knowledgeBase;

    public RetrievalService(PolicyKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public List<Evidence> search(String question, String department, String jurisdiction, int limit) {
        double[] query = vectorize(question);
        List<Evidence> matches = new ArrayList<>();
        for (PolicyDocument document : knowledgeBase.documents()) {
            if (!matchesFilter(document.department(), department)
                    || !matchesFilter(document.jurisdiction(), jurisdiction)) {
                continue;
            }
            for (int index = 0; index < document.sections().size(); index++) {
                String section = document.sections().get(index);
                double score = cosine(query, vectorize(section));
                if (score >= 0.08) {
                    matches.add(new Evidence(document.id(), document.title(), document.department(),
                            document.jurisdiction(), "§" + (index + 1), section, round(score)));
                }
            }
        }
        return matches.stream()
                .sorted(Comparator.comparingDouble(Evidence::score).reversed())
                .limit(limit)
                .toList();
    }

    private boolean matchesFilter(String actual, String requested) {
        return requested == null || requested.isBlank() || requested.equalsIgnoreCase("all")
                || actual.equalsIgnoreCase(requested) || actual.equalsIgnoreCase("Global");
    }

    double[] vectorize(String text) {
        double[] vector = new double[DIMENSIONS];
        Arrays.stream(text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").split("\\s+"))
                .filter(token -> token.length() > 2)
                .forEach(token -> {
                    int first = Math.floorMod(token.hashCode(), DIMENSIONS);
                    int second = Math.floorMod((token + "#").hashCode(), DIMENSIONS);
                    vector[first] += 1.0;
                    vector[second] += 0.35;
                });
        double norm = Math.sqrt(Arrays.stream(vector).map(value -> value * value).sum());
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) vector[i] /= norm;
        }
        return vector;
    }

    private double cosine(double[] left, double[] right) {
        double total = 0;
        for (int i = 0; i < left.length; i++) total += left[i] * right[i];
        return total;
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
