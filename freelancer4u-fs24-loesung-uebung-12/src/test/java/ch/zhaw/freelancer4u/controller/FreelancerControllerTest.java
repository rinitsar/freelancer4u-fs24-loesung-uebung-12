package ch.zhaw.freelancer4u.controller;

import static org.junit.jupiter.api.Assertions.assertNull;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import ch.zhaw.freelancer4u.model.Freelancer;
import ch.zhaw.freelancer4u.repository.FreelancerRepository;
import ch.zhaw.freelancer4u.security.TestSecurityConfig;

@SpringBootTest
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class FreelancerControllerTest {
    
    @Autowired
    private MockMvc mvc;

    @Autowired
    FreelancerRepository freelancerRepository;

    private static final String TEST_EMAIL = "test.abc.xyz@gmail.com";
    private static final String TEST_STRING = "TEST-abc...xyz";
    private static ObjectMapper mapper = new ObjectMapper();
    private static ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();

    @Test
    @Order(1)
    @WithMockUser
    public void testCreateFreelancer() throws Exception {
        // create a test freelancer and convert to Json
        Freelancer freelancer = new Freelancer(TEST_EMAIL, TEST_STRING);
        var jsonBody = ow.writeValueAsString(freelancer);

        // POST Json to service with authorization header
        mvc.perform(post("/api/freelancer")
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
    public void testDeleteFreelancer() throws Exception {
        Freelancer result = freelancerRepository.findFirstByEmail(TEST_EMAIL);
        if (result != null) {
            freelancerRepository.deleteById(result.getId());
        }

        result = freelancerRepository.findFirstByEmail(TEST_EMAIL);
        assertNull(result);
    }    

    
}
