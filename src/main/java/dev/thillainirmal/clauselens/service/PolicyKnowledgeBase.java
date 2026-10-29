package dev.thillainirmal.clauselens.service;

import dev.thillainirmal.clauselens.model.DocumentSummary;
import dev.thillainirmal.clauselens.model.PolicyDocument;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PolicyKnowledgeBase {
    private final Map<String, PolicyDocument> documents = new ConcurrentHashMap<>();

    @PostConstruct
    void seed() {
        add("Remote Work & Access Policy", "HR", "Global", "3.2", List.of(
                "Remote access requires company-managed devices, full-disk encryption, automatic security updates and multi-factor authentication.",
                "Employees may work outside their home country for no more than 20 business days without written HR and tax approval.",
                "Confidential meetings must not be conducted in public spaces. Printed confidential material must be securely destroyed.",
                "Access must be removed within four hours of an employee termination notice."));
        add("EU Customer Data Retention Standard", "Security", "EU", "2.4", List.of(
                "Production customer records must be deleted or irreversibly anonymized 30 days after the contractual retention period expires.",
                "Security audit logs are retained for 365 days. Access is limited to the security operations and internal audit teams.",
                "A legal hold overrides routine deletion only when it has a named owner, documented scope and review date.",
                "Backups containing expired personal data must age out within 90 days and must not be restored into production without privacy review."));
        add("Third-Party Security Standard", "Procurement", "Global", "5.1", List.of(
                "Vendors processing restricted data require a security assessment before contract signature and annually thereafter.",
                "Critical vendors must notify the company of a confirmed breach within 24 hours and provide an incident report within five business days.",
                "Sub-processors require prior written approval and must accept security obligations equivalent to the primary vendor.",
                "Contracts must include deletion certification, audit rights and a tested business-continuity plan."));
        add("Responsible AI Use Policy", "Engineering", "Global", "1.3", List.of(
                "Customer personal data, credentials and proprietary source code must not be submitted to unapproved public AI services.",
                "High-impact automated decisions require documented human review, appeal handling and quarterly bias evaluation.",
                "Teams must maintain model purpose, owner, data lineage, evaluation evidence and rollback instructions in the AI system register.",
                "AI-generated code requires the same security review, testing and license checks as human-authored code."));
    }

    public synchronized PolicyDocument add(String title, String department, String jurisdiction,
                                           String version, List<String> sections) {
        String material = title + department + jurisdiction + version + String.join("\n", sections);
        String fingerprint = sha256(material);
        return documents.values().stream()
                .filter(existing -> existing.fingerprint().equals(fingerprint))
                .findFirst()
                .orElseGet(() -> {
                    PolicyDocument document = new PolicyDocument(UUID.randomUUID().toString(), fingerprint,
                            title, department, jurisdiction, version, Instant.now(), sections);
                    documents.put(document.id(), document);
                    return document;
                });
    }

    public List<PolicyDocument> documents() {
        return documents.values().stream()
                .sorted(Comparator.comparing(PolicyDocument::title))
                .toList();
    }

    public List<DocumentSummary> summaries() {
        return documents().stream().map(DocumentSummary::from).toList();
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to fingerprint document", exception);
        }
    }
}
