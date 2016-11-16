package com.livingprogress.mentorme.controllers;

import com.livingprogress.mentorme.BaseTest;
import com.livingprogress.mentorme.entities.Task;
import com.livingprogress.mentorme.entities.IdentifiableEntity;
import com.livingprogress.mentorme.entities.SearchResult;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Comparator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The test cases for <code>TaskController</code>
 */
public class TaskControllerTest extends BaseTest {
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
        sample = readFile("task1.json");
        demo = readFile("demo-task.json");
        entities = readFile("tasks.json");
    }

    /**
     * Test get method.
     *
     * @throws Exception throws if any error happens.
     */
    @Test
    public void get() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/1")
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
        Task demoEntity = objectMapper.readValue(demo, Task.class);
        checkEntities(demoEntity.getUsefulLinks());
        checkEntity(demoEntity.getCustomData());
        // create without documents
        String res = mockMvc.perform(MockMvcRequestBuilders.post("/tasks")
                                                           .params(getTaskParams(demoEntity))
                                                           .contentType(MediaType.MULTIPART_FORM_DATA))
                            .andExpect(status().isCreated())
                            .andExpect(jsonPath("$.id").isNumber())
                            .andExpect(jsonPath("$.documents", Matchers.hasSize(0)))
                            .andReturn()
                            .getResponse()
                            .getContentAsString();
        Task result = objectMapper.readValue(res, Task.class);
        demoEntity.setId(result.getId());
        verifyEntities(demoEntity.getUsefulLinks(), result.getUsefulLinks());
        verifyEntity(demoEntity.getCustomData(), result.getCustomData());
        assertEquals(objectMapper.writeValueAsString(demoEntity), objectMapper.writeValueAsString(result));
        verifyDocuments(0);
        // upload file
        demoEntity.setId(0);
        demoEntity.setDocuments(null);
        res = mockAuthMvc.perform(MockMvcRequestBuilders.fileUpload("/tasks")
                                                    .file(FILE1)
                                                    .file(FILE2)
                                                    .header(AUTH_HEADER_NAME, mentorToken)
                                                    .params(getTaskParams(demoEntity))
                                                    .contentType(MediaType.MULTIPART_FORM_DATA))
                     .andExpect(status().isCreated())
                     .andExpect(jsonPath("$.id").isNumber())
                     .andExpect(jsonPath("$.documents", Matchers.hasSize(2)))
                     .andReturn()
                     .getResponse()
                     .getContentAsString();
        result = objectMapper.readValue(res, Task.class);
        verifyDocuments(2);
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/" + result.getId())
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.documents", Matchers.hasSize(2)));
    }

    /**
     * Test update method.
     *
     * @throws Exception throws if any error happens.
     */
    @Test
    public void update() throws Exception {
        Task demoEntity = objectMapper.readValue(demo, Task.class);
        checkEntities(demoEntity.getUsefulLinks());
        checkEntity(demoEntity.getCustomData());
        demoEntity.setId(1);
        // update without documents
        String res = mockMvc.perform(MockMvcRequestBuilders.post("/tasks/1")
                                                           .params(getTaskParams(demoEntity))
                                                           .contentType(MediaType.MULTIPART_FORM_DATA))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.customData.mentee.id").value((int) demoEntity.getCustomData()
                                                                                                .getMentee()
                                                                                                .getId()))
                            .andExpect(jsonPath("$.customData.mentor.id").value((int) demoEntity.getCustomData()
                                                                                                .getMentor()
                                                                                                .getId()))
                            .andReturn()
                            .getResponse()
                            .getContentAsString();
        Task result = objectMapper.readValue(res, Task.class);
        verifyEntities(demoEntity.getUsefulLinks(), result.getUsefulLinks());
        verifyEntity(demoEntity.getCustomData(), result.getCustomData());
        demoEntity.getCustomData()
                  .setMentee(result.getCustomData()
                                   .getMentee());
        demoEntity.getCustomData()
                  .setMentor(result.getCustomData()
                                   .getMentor());
        assertEquals(objectMapper.writeValueAsString(demoEntity), objectMapper.writeValueAsString(result));
        // upload file
        mockAuthMvc.perform(MockMvcRequestBuilders.fileUpload("/tasks/1")
                                              .file(FILE1)
                                              .file(FILE2)
                                              .header(AUTH_HEADER_NAME, mentorToken)
                                              .params(getTaskParams(demoEntity))
                                              .contentType(MediaType.MULTIPART_FORM_DATA))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").isNumber())
               .andExpect(jsonPath("$.documents", Matchers.hasSize(2)));
        verifyDocuments(2);
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/1")
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
        mockMvc.perform(MockMvcRequestBuilders.delete("/tasks/1"))
               .andExpect(status().isOk());
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks/1"))
               .andExpect(status().isNotFound());
        mockMvc.perform(MockMvcRequestBuilders.delete("/tasks/1"))
               .andExpect(status().isNotFound());
    }

    /**
     * Test search method.
     *
     * @throws Exception throws if any error happens.
     */
    @Test
    public void search() throws Exception {
        SearchResult<Task> result = readSearchResult(entities, Task.class);
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks?sortColumn=id&sortOrder=ASC")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().json(entities));
        SearchResult<Task> result1 = getSearchResult
                ("/tasks?pageNumber=1&pageSize=2&sortColumn=id&sortOrder=ASC", Task
                        .class);
        assertEquals(result.getTotal(), result1.getTotal());
        assertEquals(getTotalPages(result.getTotal(), 2), result1.getTotalPages());
        assertArrayEquals(result.getEntities()
                                .stream()
                                .skip(2)
                                .limit(2)
                                .map(Task::getId)
                                .toArray(),
                result1.getEntities()
                       .stream()
                       .map(Task::getId)
                       .toArray());
        SearchResult<Task> result2 = getSearchResult
                ("/tasks?pageNumber=1&pageSize=2&sortColumn=id&sortOrder=DESC", Task
                        .class);
        assertEquals(result.getTotal(), result2.getTotal());
        assertEquals(getTotalPages(result.getTotal(), 2), result2.getTotalPages());
        assertArrayEquals(result.getEntities()
                                .stream()
                                .sorted(Comparator.comparing(IdentifiableEntity::getId)
                                                  .reversed())
                                .skip(2)
                                .limit(2)
                                .map(Task::getId)
                                .toArray(),
                result2.getEntities()
                       .stream()
                       .map(Task::getId)
                       .toArray());

        SearchResult<Task> result3 = getSearchResult
                ("/tasks?pageNumber=2&pageSize=2&sortColumn=description&sortOrder=DESC",
                        Task.class);
        assertEquals(result.getTotal(), result2.getTotal());
        assertEquals(getTotalPages(result.getTotal(), 2), result2.getTotalPages());
        assertArrayEquals(result.getEntities()
                                .stream()
                                .sorted(Comparator.comparing(Task::getDescription)
                                                  .reversed())
                                .skip(4)
                                .limit(2)
                                .map(Task::getId)
                                .toArray(),
                result3.getEntities()
                       .stream()
                       .map(Task::getId)
                       .toArray());
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks?goalId=5")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.total").value(1))
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.entities", Matchers.hasSize(1)))
               .andExpect(jsonPath("$.entities[0].id").value(5));
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks?description=description3")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.total").value(1))
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.entities", Matchers.hasSize(1)))
               .andExpect(jsonPath("$.entities[0].id").value(3));
        mockMvc.perform(MockMvcRequestBuilders.get("/tasks?custom=true")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.total").value(4))
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.entities", Matchers.hasSize(4)));
        mockMvc.perform(MockMvcRequestBuilders.get
                ("/tasks?pageNumber=0&pageSize=2&sortColumn=description&sortOrder=DESC&goalId=1" +
                        "&description=description1&custom=true")
                                              .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.total").value(1))
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.entities", Matchers.hasSize(1)))
               .andExpect(jsonPath("$.entities[0].id").value(1));
    }
}
