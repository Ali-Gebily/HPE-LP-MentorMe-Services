package com.livingprogress.mentorme.controllers;

import com.livingprogress.mentorme.BaseTest;
import com.livingprogress.mentorme.entities.*;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Comparator;
import java.util.stream.IntStream;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * The test cases for <code>InstitutionalProgramController</code>
 */
public class InstitutionalProgramControllerTest extends BaseTest {
    /**
     * The sample entity json.
     */
    private static String sample;

    /**
     * The demo entity json.
     */
    private static String demo;

    /**
     * All entities json.
     */
    private static String entities;

    /**
     * Read related json.
     *
     * @throws Exception throws if any error happens.
     */
    @BeforeClass
    public static void setupClass() throws Exception {
        sample = readFile("institutionalProgram1.json");
        demo = readFile("demo-institutionalProgram.json");
        entities = readFile("institutionalPrograms.json");
    }

    /**
     * Test get method.
     *
     * @throws Exception throws if any error happens.
     */
    @Test
    public void get() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/institutionalPrograms/1")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().json(sample));
    }

    /**
     * Test create method.
     *
     * @throws Exception throws if any error happens.
     */
    @Test
    public void create() throws Exception {
        InstitutionalProgram demoEntity = objectMapper.readValue(demo, InstitutionalProgram.class);
        assertNull(demoEntity.getCreatedOn());
        assertNull(demoEntity.getLastModifiedOn());
        checkEntities(demoEntity.getUsefulLinks());
        checkEntities(demoEntity.getResponsibilities());
        checkEntities(demoEntity.getGoals());
        demoEntity.getGoals().forEach(g->{
            checkEntities(g.getTasks());
            checkEntities(g.getUsefulLinks());
        });
        // create without documents
        String res = mockMvc.perform(MockMvcRequestBuilders.post("/institutionalPrograms")
                                                           .params(getInstitutionalProgramParams(demoEntity))
                                                           .contentType(MediaType.MULTIPART_FORM_DATA))
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.id").isNumber())
                            .andExpect(jsonPath("$.createdOn").exists())
                            .andExpect(jsonPath("$.lastModifiedOn").exists())
                            .andExpect(jsonPath("$.documents", Matchers.hasSize(0)))
                            .andReturn()
                            .getResponse()
                            .getContentAsString();
        final InstitutionalProgram result = objectMapper.readValue(res, InstitutionalProgram.class);
        demoEntity.setId(result.getId());
        demoEntity.setCreatedOn(result.getCreatedOn());
        demoEntity.setLastModifiedOn(result.getLastModifiedOn());
        verifyEntities(demoEntity.getUsefulLinks(), result.getUsefulLinks());
        verifyEntities(demoEntity.getResponsibilities(), result.getResponsibilities());
        verifyEntities(demoEntity.getGoals(), result.getGoals());
        IntStream.range(0, demoEntity.getGoals()
                                     .size()).forEach(idx -> {
            Goal goal1 = demoEntity.getGoals().get(idx);
            Goal goal2 = result.getGoals().get(idx);
            verifyEntities(goal1.getTasks(), goal2.getTasks());
            verifyEntities(goal1.getUsefulLinks(), goal2.getUsefulLinks());
        });
        assertEquals(1, result.getLocale().getId());
        result.setLocale(null);
        assertEquals(objectMapper.writeValueAsString(demoEntity), objectMapper.writeValueAsString(result));
        verifyDocuments(0);
        // upload file
        demoEntity.setId(0);
        demoEntity.setCreatedOn(null);
        demoEntity.setDocuments(null);
        res = mockAuthMvc.perform(MockMvcRequestBuilders.fileUpload("/institutionalPrograms")
                                                    .file(FILE1)
                                                    .file(FILE2)
                                                    .header(AUTH_HEADER_NAME, institutionAdminToken)
                                                    .params(getInstitutionalProgramParams(demoEntity))
                                                    .contentType(MediaType.MULTIPART_FORM_DATA))
                     .andExpect(status().isCreated())
                     .andExpect(jsonPath("$.id").isNumber())
                     .andExpect(jsonPath("$.createdOn").exists())
                     .andExpect(jsonPath("$.documents", Matchers.hasSize(2)))
                     .andReturn()
                     .getResponse()
                     .getContentAsString();
        final InstitutionalProgram result2 = objectMapper.readValue(res, InstitutionalProgram.class);
        verifyDocuments(2);
        mockMvc.perform(MockMvcRequestBuilders.get("/institutionalPrograms/" + result2.getId())
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.documents", Matchers.hasSize(2)));
        // test null nested properties
        demoEntity.setId(0);
        demoEntity.setDocuments(null);
        demoEntity.setGoals(null);
        demoEntity.setResponsibilities(null);
        mockMvc.perform(MockMvcRequestBuilders.post("/institutionalPrograms")
                                              .params(getInstitutionalProgramParams(demoEntity))
                                              .contentType(MediaType.MULTIPART_FORM_DATA))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").isNumber())
               .andExpect(jsonPath("$.createdOn").exists())
               .andExpect(jsonPath("$.documents", Matchers.hasSize(0)))
               .andExpect(jsonPath("$.goals", Matchers.hasSize(0)))
               .andExpect(jsonPath("$.responsibilities", Matchers.hasSize(0)));
    }

    /**
     * Test update method.
     *
     * @throws Exception throws if any error happens.
     */
    @Test
    public void update() throws Exception {
        InstitutionalProgram demoEntity = objectMapper.readValue(demo, InstitutionalProgram.class);
        checkEntities(demoEntity.getUsefulLinks());
        checkEntities(demoEntity.getResponsibilities());
        checkEntities(demoEntity.getGoals());
        demoEntity.getGoals().forEach(g->{
            checkEntities(g.getTasks());
            checkEntities(g.getUsefulLinks());
        });
        // try to update created on
        demoEntity.setCreatedOn(sampleFutureDate);
        demoEntity.setLastModifiedOn(sampleFutureDate);
        demoEntity.setId(1);
        // update without documents
        String res = mockMvc.perform(MockMvcRequestBuilders.post("/institutionalPrograms/1")
                                                           .params(getInstitutionalProgramParams(demoEntity))
                                                           .contentType(MediaType.MULTIPART_FORM_DATA))
                            .andExpect(status().isOk())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();
        InstitutionalProgram result = objectMapper.readValue(res, InstitutionalProgram.class);
        // will not update created on during updating
        assertNotEquals(sampleFutureDate, result.getCreatedOn());
        demoEntity.setCreatedOn(result.getCreatedOn());
        assertNotEquals(sampleFutureDate, result.getLastModifiedOn());
        demoEntity.setLastModifiedOn(result.getLastModifiedOn());
        verifyEntities(demoEntity.getUsefulLinks(), result.getUsefulLinks());
        verifyEntities(demoEntity.getResponsibilities(), result.getResponsibilities());
        verifyEntities(demoEntity.getGoals(), result.getGoals());
        IntStream.range(0, demoEntity.getGoals()
                                 .size()).forEach(idx -> {
            Goal goal1 = demoEntity.getGoals().get(idx);
            Goal goal2 = result.getGoals().get(idx);

            verifyEntities(goal1.getTasks(), goal2.getTasks());
            verifyEntities(goal1.getUsefulLinks(), goal2.getUsefulLinks());
        });
        demoEntity.setInstitution(result.getInstitution());
        demoEntity.setGoals(result.getGoals());
        demoEntity.setUsefulLinks(result.getUsefulLinks());
        assertEquals(1, result.getLocale().getId());
        result.setLocale(null);
        assertEquals(objectMapper.writeValueAsString(demoEntity), objectMapper.writeValueAsString(result));
        // upload file
        mockAuthMvc.perform(MockMvcRequestBuilders.fileUpload("/institutionalPrograms/1")
                                                  .file(FILE1)
                                              .file(FILE2)
                                                  .header(AUTH_HEADER_NAME, institutionAdminToken)
                                                  .params(getInstitutionalProgramParams(demoEntity))
                                              .contentType(MediaType.MULTIPART_FORM_DATA))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").isNumber())
               .andExpect(jsonPath("$.createdOn").exists())
               .andExpect(jsonPath("$.documents", Matchers.hasSize(2)));
        verifyDocuments(2);
        mockMvc.perform(MockMvcRequestBuilders.get("/institutionalPrograms/1")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.documents", Matchers.hasSize(2)));
    }

    /**
     * Test delete method.
     *
     * @throws Exception throws if any error happens.
     */
    @Test
    public void delete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/institutionalPrograms/1"))
               .andExpect(status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.get("/institutionalPrograms/1"))
               .andExpect(status().isNotFound());
        mockMvc.perform(MockMvcRequestBuilders.delete("/institutionalPrograms/1"))
               .andExpect(status().isNotFound());
    }

    /**
     * Test search method.
     *
     * @throws Exception throws if any error happens.
     */
    @Test
    public void search() throws Exception {
        SearchResult<InstitutionalProgram> result = readSearchResult(entities, InstitutionalProgram.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/institutionalPrograms?sortColumn=id&sortOrder=ASC")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().json(entities));
        SearchResult<InstitutionalProgram> result1 = getSearchResult
                ("/institutionalPrograms?pageNumber=1&pageSize=2&sortColumn=id&sortOrder=ASC", InstitutionalProgram
                        .class);
        assertEquals(result.getTotal(), result1.getTotal());
        assertEquals(getTotalPages(result.getTotal(), 2), result1.getTotalPages());
        assertArrayEquals(result.getEntities()
                                .stream()
                                .skip(2)
                                .limit(2)
                                .map(InstitutionalProgram::getId)
                                .toArray(),
                result1.getEntities()
                       .stream()
                       .map(InstitutionalProgram::getId)
                       .toArray());
        SearchResult<InstitutionalProgram> result2 = getSearchResult
                ("/institutionalPrograms?pageNumber=1&pageSize=2&sortColumn=id&sortOrder=DESC", InstitutionalProgram
                        .class);
        assertEquals(result.getTotal(), result2.getTotal());
        assertEquals(getTotalPages(result.getTotal(), 2), result2.getTotalPages());
        assertArrayEquals(result.getEntities()
                                .stream()
                                .sorted(Comparator.comparing(IdentifiableEntity::getId)
                                                  .reversed())
                                .skip(2)
                                .limit(2)
                                .map(InstitutionalProgram::getId)
                                .toArray(),
                result2.getEntities()
                       .stream()
                       .map(InstitutionalProgram::getId)
                       .toArray());

        SearchResult<InstitutionalProgram> result3 = getSearchResult
                ("/institutionalPrograms?pageNumber=2&pageSize=2&sortColumn=programName&sortOrder=DESC",
                        InstitutionalProgram.class);
        assertEquals(result.getTotal(), result2.getTotal());
        assertEquals(getTotalPages(result.getTotal(), 2), result2.getTotalPages());
        assertArrayEquals(result.getEntities()
                                .stream()
                                .sorted(Comparator.comparing(InstitutionalProgram::getProgramName)
                                                  .reversed())
                                .skip(4)
                                .limit(2)
                                .map(InstitutionalProgram::getId)
                                .toArray(),
                result3.getEntities()
                       .stream()
                       .map(InstitutionalProgram::getId)
                       .toArray());
        mockMvc.perform(MockMvcRequestBuilders.get("/institutionalPrograms?programName=programName5")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.total").value(1))
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.entities", Matchers.hasSize(1)))
               .andExpect(jsonPath("$.entities[0].id").value(5));
        mockMvc.perform(MockMvcRequestBuilders.get("/institutionalPrograms?institutionId=3")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.total").value(1))
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.entities", Matchers.hasSize(1)))
               .andExpect(jsonPath("$.entities[0].id").value(3));
        mockMvc.perform(MockMvcRequestBuilders.get("/institutionalPrograms?minDurationInDays=20")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.total").value(1))
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.entities", Matchers.hasSize(1)))
               .andExpect(jsonPath("$.entities[0].id").value(1));
        mockMvc.perform(MockMvcRequestBuilders.get("/institutionalPrograms?maxDurationInDays=5")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.total").value(1))
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.entities", Matchers.hasSize(1)))
               .andExpect(jsonPath("$.entities[0].id").value(6));
        mockMvc.perform(MockMvcRequestBuilders.get
                ("/institutionalPrograms?pageNumber=0&pageSize=2&sortColumn=programName&sortOrder=DESC&programName" +
                        "=programName1&institutionId=1&minDurationInDays=1&maxDurationInDays=100")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.total").value(1))
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.entities", Matchers.hasSize(1)))
               .andExpect(jsonPath("$.entities[0].id").value(1));

        // filter by locale
        mockMvc.perform(MockMvcRequestBuilders.get
                ("/institutionalPrograms?sortColumn=programName&sortOrder=ASC&locale=en")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(4))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.entities", Matchers.hasSize(4)))
                .andExpect(jsonPath("$.entities[0].id").value(1));
        mockMvc.perform(MockMvcRequestBuilders.get
                ("/institutionalPrograms?sortColumn=programName&sortOrder=ASC&locale=es")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.entities", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.entities[0].id").value(3));
    }

    /**
     * Test getProgramMentees method.
     *
     * @throws Exception throws if any error happens.
     */
    @Test
    public void getProgramMentees() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/institutionalPrograms/1/mentees")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().json(readFile("getProgramMentees.json")));
        mockMvc.perform(MockMvcRequestBuilders.get("/institutionalPrograms/999/mentees")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound());
    }

    /**
     * Test getProgramMentors method.
     *
     * @throws Exception throws if any error happens.
     */
    @Test
    public void getProgramMentors() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/institutionalPrograms/1/mentors")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().json(readFile("getProgramMentors.json")));
        mockMvc.perform(MockMvcRequestBuilders.get("/institutionalPrograms/999/mentors")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isNotFound());
    }

    /**
     * Test clone method.
     *
     * @throws Exception throws if any error happens.
     */
    @Test
    public void cloneTest() throws Exception {
        final String menteeMentorIds = "{ \"menteeId\": 4 , \"mentorId\": 5 }";
        String res = mockMvc.perform(MockMvcRequestBuilders.post("/institutionalPrograms/1/clone")
                                                           .contentType(MediaType.APPLICATION_JSON)
                                                           .content(menteeMentorIds))
                            .andExpect(status().isOk())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();
        
        InstitutionalProgram sampleEntity = objectMapper.readValue(sample, InstitutionalProgram.class);
        MenteeMentorProgram result = objectMapper.readValue(res, MenteeMentorProgram.class);
        
        verifyEntities(sampleEntity.getDocuments(), result.getDocuments());
        verifyEntities(sampleEntity.getUsefulLinks(), result.getUsefulLinks());
        verifyEntity(sampleEntity, result.getInstitutionalProgram());
        assertEquals(4L, result.getMentee().getId());
        assertEquals(5L, result.getMentor().getId());
        
        IntStream.range(0, sampleEntity.getGoals().size()).forEach(idx -> {
            Goal goal1 = sampleEntity.getGoals().get(idx);
            MenteeMentorGoal goal2 = result.getGoals().get(idx);
            
            verifyEntity(goal1, goal2.getGoal());
            
            IntStream.range(0, goal1.getTasks().size()).forEach(idy -> {
                Task task1 = goal1.getTasks().get(idy);
                MenteeMentorTask task2 = goal2.getTasks().get(idy);
                
                verifyEntity(task1, task2.getTask());
            });
        });
        
        
        IntStream.range(0, sampleEntity.getResponsibilities().size()).forEach(idx -> {
            Responsibility resp1 = sampleEntity.getResponsibilities().get(idx);
            MenteeMentorResponsibility resp2 = result.getResponsibilities().get(idx);
            
            assertEquals(resp1.getDate(), resp2.getDate());
            assertEquals(resp1.getMenteeResponsibility(), resp2.getMenteeResponsibility());
            assertEquals(resp1.getMentorResponsibility(), resp2.getMentorResponsibility());
            assertEquals(resp1.getNumber(), resp2.getNumber());
            assertEquals(resp1.getTitle(), resp2.getTitle());
        });
        
        mockMvc.perform(MockMvcRequestBuilders.post("/institutionalPrograms/999/clone")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content(menteeMentorIds))
               .andExpect(status().isNotFound());
    }
}
