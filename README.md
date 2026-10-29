<div align="center">

# ClauseLens AI

### Evidence-first policy intelligence for teams that cannot afford hallucinations

[![CI](https://github.com/thillainirmal-tech/ClauseLens-AI/actions/workflows/ci.yml/badge.svg)](https://github.com/thillainirmal-tech/ClauseLens-AI/actions/workflows/ci.yml)
[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

</div>

ClauseLens AI turns policy documents into defensible, cited compliance briefs. Four transparent agents scope retrieval, rank evidence, detect control risks and verify citations. It runs offline with deterministic local embeddings, so the demo needs no paid model or API key.

## Why this project

Traditional document chat can sound certain while using the wrong policy, department or jurisdiction. ClauseLens treats retrieval boundaries and citations as product requirements:

- **PDF ingestion** with type validation, text extraction, chunking and SHA-256 duplicate protection
- **Metadata-scoped retrieval** by department and jurisdiction
- **Local vector search** using deterministic hashed embeddings and cosine similarity
- **Evidence-only answers** that refuse when retrieval is weak
- **Risk findings** for privacy, supplier assurance and responsible AI controls
- **Visible agent trace** showing every decision step
- **Production signals** through health probes and Prometheus metrics

## Architecture

```text
PDF / seeded policy
        │
        ▼
 extract → fingerprint → chunk → metadata index
                                    │
question → Scope Agent → Retrieval Agent → Risk Agent → Citation Guard
                                    │
                                    ▼
                       grounded brief + evidence
```

This project applies the robust ideas behind production RAG—fingerprinting, chunking, metadata filters, ranked retrieval and grounded generation—without copying a course application or requiring external infrastructure.

## Run locally

Requirements: Java 17 and Maven 3.9+.

```bash
mvn spring-boot:run
```

Open [http://localhost:8081](http://localhost:8081).

Useful endpoints:

- `GET /api/documents` — list indexed policies
- `POST /api/documents` — upload a PDF with optional metadata
- `POST /api/query` — generate an evidence brief
- `GET /api/demo` — deterministic governance demo
- `GET /actuator/health` — health probe
- `GET /actuator/prometheus` — metrics

Example:

```bash
curl -X POST http://localhost:8081/api/query \
  -H "Content-Type: application/json" \
  -d '{"question":"Can customer data be sent to a public AI tool?","department":"Engineering","jurisdiction":"Global"}'
```

## Upload a policy

```bash
curl -X POST http://localhost:8081/api/documents \
  -F "file=@synthetic-policy.pdf" \
  -F "department=Security" \
  -F "jurisdiction=EU" \
  -F "version=1.0"
```

Only synthetic or public documents should be used. Uploaded text is stored in process memory and disappears when the application stops.

## Test

```bash
mvn verify
```

The test suite covers metadata isolation, retrieval quality, refusal behavior, risk detection, citations, validation and REST responses.

## Docker

```bash
docker compose up --build
```

## Roadmap

- [ ] Persistent pgvector adapter behind the existing retrieval interface
- [ ] Page-level PDF citations and highlighted evidence
- [ ] Version conflict and policy supersession detection
- [ ] OPA policy-as-code export
- [ ] OpenTelemetry traces for the agent pipeline
- [ ] Optional Spring AI provider adapter

## Responsible use

ClauseLens AI is an educational decision-support system, not legal advice. Evidence may be incomplete or outdated. A qualified human must review the source policy and applicable law before acting. Never upload credentials, personal data or confidential production documents.

Contributions are welcome. Start with [CONTRIBUTING.md](CONTRIBUTING.md) and an open issue.

## License

Apache License 2.0.
