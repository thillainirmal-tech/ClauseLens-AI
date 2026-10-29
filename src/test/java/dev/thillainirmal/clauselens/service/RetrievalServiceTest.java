package dev.thillainirmal.clauselens.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RetrievalServiceTest {
    private PolicyKnowledgeBase knowledgeBase;
    private RetrievalService retrieval;

    @BeforeEach
    void setUp() {
        knowledgeBase = new PolicyKnowledgeBase();
        knowledgeBase.seed();
        retrieval = new RetrievalService(knowledgeBase);
    }

    @Test
    void retrievesResponsibleAiEvidence() {
        var results = retrieval.search("Can customer data be submitted to a public AI service?",
                "Engineering", "Global", 5);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).documentTitle()).contains("AI");
    }

    @Test
    void appliesDepartmentMetadataFilter() {
        var results = retrieval.search("How quickly must access be removed after termination?",
                "HR", "Global", 5);
        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(item ->
                item.department().equals("HR") || item.department().equals("Global"));
    }

    @Test
    void doesNotReturnUnrelatedEvidence() {
        var results = retrieval.search("zxqv orbital banana telescope", "Security", "EU", 5);
        assertThat(results).isEmpty();
    }
}
