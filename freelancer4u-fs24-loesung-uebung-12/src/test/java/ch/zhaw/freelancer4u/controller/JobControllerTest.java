package ch.zhaw.freelancer4u.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import ch.zhaw.freelancer4u.model.Job;
import ch.zhaw.freelancer4u.model.JobType;
import ch.zhaw.freelancer4u.repository.JobRepository;
import ch.zhaw.freelancer4u.security.TestSecurityConfig;

@SpringBootTest
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class JobControllerTest {
    
    @Autowired
    private MockMvc mvc;

    @Autowired
    JobRepository jobRepository;

    private static final String TEST_STRING = "TEST-abc...xyz";
    private static ObjectMapper mapper = new ObjectMapper();
    private static ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();

    @Test
    @Order(1)
    @WithMockUser
    public void testCreateJob() throws Exception {
        // create a test job and convert to Json
        Job job = new Job();
        job.setDescription(TEST_STRING);
        job.setJobType(JobType.TEST);
        job.setEarnings(3.1415);
        var jsonBody = ow.writeValueAsString(job);

        // POST Json to service with authorization header
        mvc.perform(post("/api/job")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonBody)
        .header(HttpHeaders.AUTHORIZATION, "Bearer token"))
        .andDo(print())
        .andExpect(status().isCreated())
        .andReturn();
    }

    @Test
    @Order(2)
    @WithMockUser
    public void testGetAllJob() throws Exception {
        var json = getAllJobs();
        
        // assert list of jobs is not empty and result contains test string
        assertFalse(json.isEmpty());
        assertTrue(json.contains(TEST_STRING));
    }

    @Test
    @Order(3)
    @WithMockUser
    public void testDeleteJobs() throws Exception {
        // analyse json response and delete all test data jobs
        var json = getAllJobs();
        JsonNode jsonNode = mapper.readTree(json);
        var content = jsonNode.get("content");
        for (var x : content) {
            var id = x.get("id");
            var description = x.get("description");
            if (description.asText().equals(TEST_STRING)) {
                jobRepository.deleteById(id.asText());
            }
        }
        
        // reload jobs and assert no test data
        json = getAllJobs();
        System.out.println(json);
        assertFalse(json.contains("\"" + TEST_STRING + "\""));
    }

    private String getAllJobs() throws Exception {
        var result = mvc.perform(get("/api/job")
        .param("pageSize", String.valueOf(Integer.MAX_VALUE))
        .contentType(MediaType.TEXT_PLAIN))
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();
        return result.getResponse().getContentAsString();
    }

}
