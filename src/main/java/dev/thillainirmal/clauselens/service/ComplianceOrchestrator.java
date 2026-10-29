package dev.thillainirmal.clauselens.service;

import dev.thillainirmal.clauselens.model.AgentStep;
import dev.thillainirmal.clauselens.model.Evidence;
import dev.thillainirmal.clauselens.model.QueryRequest;
import dev.thillainirmal.clauselens.model.QueryResponse;
import dev.thillainirmal.clauselens.model.RiskFinding;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

@Service
public class ComplianceOrchestrator {
    private final RetrievalService retrieval;

    public ComplianceOrchestrator(RetrievalService retrieval) {
        this.retrieval = retrieval;
    }

    public QueryResponse evaluate(QueryRequest request) {
        List<AgentStep> trace = new ArrayList<>();
        trace.add(new AgentStep("Scope Agent", "complete",
                "Applied department=" + request.normalizedDepartment() + " and jurisdiction="
                        + request.normalizedJurisdiction() + " metadata boundaries."));
        List<Evidence> evidence = retrieval.search(request.question(), request.normalizedDepartment(),
                request.normalizedJurisdiction(), 5);
        trace.add(new AgentStep("Retrieval Agent", evidence.isEmpty() ? "no-evidence" : "complete",
                evidence.isEmpty() ? "No relevant policy chunks passed the similarity floor."
                        : "Ranked " + evidence.size() + " evidence chunks with local cosine similarity."));

        if (evidence.isEmpty()) {
            trace.add(new AgentStep("Grounding Guard", "blocked", "Refused to invent an answer without evidence."));
            return new QueryResponse("I could not find enough evidence in the indexed policies. Upload or select a relevant policy before acting.",
                    0, false, List.of(), List.of(), trace);
        }

        List<RiskFinding> risks = assessRisks(request.question(), evidence);
        trace.add(new AgentStep("Risk Agent", "complete",
                risks.isEmpty() ? "No explicit high-risk trigger detected." : "Flagged " + risks.size() + " review item(s)."));
        trace.add(new AgentStep("Citation Guard", "complete", "Every claim is tied to a visible policy excerpt."));

        int confidence = (int) Math.min(96, Math.round(58 + evidence.get(0).score() * 55 + evidence.size() * 3));
        return new QueryResponse(composeAnswer(evidence, risks), confidence, true, evidence, risks, trace);
    }

    private String composeAnswer(List<Evidence> evidence, List<RiskFinding> risks) {
        StringBuilder answer = new StringBuilder("Policy evidence indicates:\n");
        IntStream.range(0, Math.min(3, evidence.size())).forEach(index -> {
            Evidence item = evidence.get(index);
            answer.append("• ").append(item.excerpt()).append(" [")
                    .append(item.documentTitle()).append(" ").append(item.section()).append("]\n");
        });
        if (!risks.isEmpty()) {
            answer.append("\nReview required: ").append(risks.get(0).recommendation());
        }
        return answer.toString().trim();
    }

    private List<RiskFinding> assessRisks(String question, List<Evidence> evidence) {
        String material = (question + " " + evidence.stream().map(Evidence::excerpt).reduce("", String::concat))
                .toLowerCase(Locale.ROOT);
        List<RiskFinding> risks = new ArrayList<>();
        if (containsAny(material, "personal data", "customer data", "retention", "delete", "backup")) {
            risks.add(new RiskFinding("HIGH", "Privacy and retention impact",
                    "The request touches controlled data lifecycle requirements.",
                    "Record the data owner, legal basis and deletion or hold decision before execution."));
        }
        if (containsAny(material, "vendor", "sub-processor", "contract", "third-party")) {
            risks.add(new RiskFinding("MEDIUM", "Third-party assurance required",
                    "The evidence contains supplier security obligations.",
                    "Confirm the current assessment, breach terms and approved sub-processors."));
        }
        if (containsAny(material, "ai", "automated decision", "model", "generated code")) {
            risks.add(new RiskFinding("HIGH", "Responsible AI control",
                    "Human oversight or model-governance requirements apply.",
                    "Document the owner, evaluation evidence, human review and rollback path."));
        }
        return risks;
    }

    private boolean containsAny(String value, String... terms) {
        for (String term : terms) if (value.contains(term)) return true;
        return false;
    }
}
