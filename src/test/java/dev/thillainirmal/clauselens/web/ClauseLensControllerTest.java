package dev.thillainirmal.clauselens.web;

import dev.thillainirmal.clauselens.ClauseLensApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ClauseLensApplication.class)
@AutoConfigureMockMvc
class ClauseLensControllerTest {
    @Autowired MockMvc mockMvc;

    @Test
    void exposesIndexedDocuments() throws Exception {
        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void rejectsBlankQuestion() throws Exception {
        mockMvc.perform(post("/api/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"\",\"department\":\"all\",\"jurisdiction\":\"all\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("A non-empty question is required"));
    }
}
