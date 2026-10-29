package dev.thillainirmal.clauselens.service;

import dev.thillainirmal.clauselens.model.QueryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ComplianceOrchestratorTest {
    private ComplianceOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        PolicyKnowledgeBase knowledgeBase = new PolicyKnowledgeBase();
        knowledgeBase.seed();
        orchestrator = new ComplianceOrchestrator(new RetrievalService(knowledgeBase));
    }

    @Test
    void producesGroundedAnswerWithCitationsAndRisk() {
        var result = orchestrator.evaluate(new QueryRequest(
                "Can we send customer data to a public AI model?", "Engineering", "Global"));
        assertThat(result.grounded()).isTrue();
        assertThat(result.evidence()).isNotEmpty();
        assertThat(result.risks()).extracting("title").contains("Responsible AI control");
        assertThat(result.answer()).contains("[Responsible AI Use Policy");
        assertThat(result.trace()).hasSize(4);
    }

    @Test
    void refusesToInventWhenNoEvidenceExists() {
        var result = orchestrator.evaluate(new QueryRequest(
                "zxqv orbital banana telescope", "Security", "EU"));
        assertThat(result.grounded()).isFalse();
        assertThat(result.confidence()).isZero();
        assertThat(result.trace()).extracting("status").contains("blocked");
    }
}
