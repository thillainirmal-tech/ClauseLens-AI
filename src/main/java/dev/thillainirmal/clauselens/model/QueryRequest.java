package dev.thillainirmal.clauselens.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QueryRequest(
        @NotBlank @Size(max = 1200) String question,
        @Size(max = 80) String department,
        @Size(max = 80) String jurisdiction
) {
    public String normalizedDepartment() {
        return department == null || department.isBlank() ? "all" : department.trim();
    }

    public String normalizedJurisdiction() {
        return jurisdiction == null || jurisdiction.isBlank() ? "all" : jurisdiction.trim();
    }
}
