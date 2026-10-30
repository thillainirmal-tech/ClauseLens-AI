package dev.thillainirmal.clauselens.service;

import dev.thillainirmal.clauselens.model.PolicyDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class PdfIngestionService {
    private static final int CHUNK_SIZE = 750;
    private static final long MAX_BYTES = 15L * 1024 * 1024;
    private static final int MAX_PAGES = 500;
    private final PolicyKnowledgeBase knowledgeBase;

    public PdfIngestionService(PolicyKnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public PolicyDocument ingest(MultipartFile file, String department, String jurisdiction, String version) {
        validate(file);
        try (PDDocument pdf = Loader.loadPDF(file.getBytes())) {
            if (pdf.getNumberOfPages() > MAX_PAGES) throw new IllegalArgumentException("PDF exceeds the 500-page safety limit");
            String text = new PDFTextStripper().getText(pdf).replace("\u0000", " ").trim();
            if (text.isBlank()) throw new IllegalArgumentException("The PDF contains no extractable text");
            return knowledgeBase.add(cleanName(file.getOriginalFilename()), defaulted(department, "General"),
                    defaulted(jurisdiction, "Global"), defaulted(version, "1.0"), chunk(text));
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("The PDF could not be read", exception);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Choose a non-empty PDF");
        if (file.getSize() > MAX_BYTES) throw new IllegalArgumentException("PDF exceeds the 15 MB safety limit");
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are accepted");
        }
    }

    private List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        String normalized = text.replaceAll("\\s+", " ");
        for (int start = 0; start < normalized.length(); start += CHUNK_SIZE) {
            int end = Math.min(normalized.length(), start + CHUNK_SIZE);
            if (end < normalized.length()) {
                int boundary = normalized.lastIndexOf(' ', end);
                if (boundary > start + 250) end = boundary;
            }
            chunks.add(normalized.substring(start, end).trim());
            start = end - CHUNK_SIZE;
        }
        return chunks;
    }

    private String cleanName(String name) {
        return name == null ? "Uploaded policy" : name.replaceFirst("(?i)\\.pdf$", "").replaceAll("[-_]+", " ");
    }

    private String defaulted(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
