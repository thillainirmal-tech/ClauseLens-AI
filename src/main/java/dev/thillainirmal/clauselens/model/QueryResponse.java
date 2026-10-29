package dev.thillainirmal.clauselens.model;

import java.util.List;

public record QueryResponse(
        String answer,
        int confidence,
        boolean grounded,
        List<Evidence> evidence,
        List<RiskFinding> risks,
        List<AgentStep> trace
) {
}
