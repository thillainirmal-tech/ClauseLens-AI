package dev.thillainirmal.clauselens.web;

import dev.thillainirmal.clauselens.model.DocumentSummary;
import dev.thillainirmal.clauselens.model.PolicyDocument;
import dev.thillainirmal.clauselens.model.QueryRequest;
import dev.thillainirmal.clauselens.model.QueryResponse;
import dev.thillainirmal.clauselens.service.ComplianceOrchestrator;
import dev.thillainirmal.clauselens.service.PdfIngestionService;
import dev.thillainirmal.clauselens.service.PolicyKnowledgeBase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ClauseLensController {
    private final PolicyKnowledgeBase knowledgeBase;
    private final PdfIngestionService ingestion;
    private final ComplianceOrchestrator orchestrator;

    public ClauseLensController(PolicyKnowledgeBase knowledgeBase, PdfIngestionService ingestion,
                                ComplianceOrchestrator orchestrator) {
        this.knowledgeBase = knowledgeBase;
        this.ingestion = ingestion;
        this.orchestrator = orchestrator;
    }

    @GetMapping("/documents")
    public List<DocumentSummary> documents() {
        return knowledgeBase.summaries();
    }

    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentSummary upload(@RequestParam MultipartFile file,
                                  @RequestParam(required = false) String department,
                                  @RequestParam(required = false) String jurisdiction,
                                  @RequestParam(required = false) String version) {
        PolicyDocument document = ingestion.ingest(file, department, jurisdiction, version);
        return DocumentSummary.from(document);
    }

    @PostMapping("/query")
    public QueryResponse query(@Valid @RequestBody QueryRequest request) {
        return orchestrator.evaluate(request);
    }

}
