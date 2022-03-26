package edu.cscc.demo.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cscc.demo.data.AuthorRepository;
import edu.cscc.demo.domain.Author;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthorControllerTests {

    @MockBean
    private AuthorRepository mockRepository;

    private static final String RESOURCE_URI = "/api/authors";
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Author testAuthor =
            new Author(0L, "John", "Doe", "john@doe.com");
    private static final Author savedAuthor =
            new Author(1L, "Jane", "Doe", "jane@doe.com");

    @Test
    @DisplayName("T01 - POST accepts and returns author representation")
    public void test_01(@Autowired MockMvc mockMvc)
            throws Exception {
        when(mockRepository.save(refEq(testAuthor))).thenReturn(savedAuthor);
        MvcResult result = mockMvc.perform(post(RESOURCE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(testAuthor)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath(
                        "$.id").value(savedAuthor.getId()))
                .andExpect(jsonPath(
                        "$.firstName").value(savedAuthor.getFirstName()))
                .andExpect(jsonPath(
                        "$.lastName").value(savedAuthor.getLastName()))
                .andExpect(
                        jsonPath("$.emailAddress").value(savedAuthor.getEmailAddress()))
                .andReturn();
        MockHttpServletResponse mockResponse = result.getResponse();
        assertEquals(String.format(
                        "http://localhost/api/authors/%d", savedAuthor.getId()),
                mockResponse.getHeader("Location"));
        verify(mockRepository, times(1)).save(refEq(testAuthor));
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T02 - When no articles exist, GET returns an empty list")
    public void test_02(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.findAll()).thenReturn(new ArrayList<>());
        mockMvc.perform(get(RESOURCE_URI))
                .andExpect(jsonPath("$.length()").value(0))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(mockRepository, times(1)).findAll();
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T03 - When one article exists, GET returns a list with it")
    public void test_03(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.findAll()).
                thenReturn(Collections.singletonList(savedAuthor));
        mockMvc.perform(get(RESOURCE_URI))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(
                        "$.[0].id").value(savedAuthor.getId()))
                .andExpect(jsonPath(
                        "$.[0].firstName").value(savedAuthor.getFirstName()))
                .andExpect(jsonPath(
                        "$.[0].lastName").value(savedAuthor.getLastName()))
                .andExpect(
                        jsonPath("$.[0].emailAddress").value(savedAuthor.getEmailAddress()))
                .andExpect(status().isOk());
        verify(mockRepository, times(1)).findAll();
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T04 - Requested article does not exist so GET returns 404")
    public void test_04(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.findById(anyLong()))
                .thenReturn(Optional.empty());
        mockMvc.perform(get(RESOURCE_URI + "/1"))
                .andExpect(status().isNotFound());
        verify(mockRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T05 - Requested article exists so GET returns it in a list")
    public void test_05(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.findById(anyLong()))
                .thenReturn(Optional.of(savedAuthor));
        mockMvc.perform(get(RESOURCE_URI + "/1"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(
                        "$.[0].id").value(savedAuthor.getId()))
                .andExpect(jsonPath(
                        "$.[0].firstName").value(savedAuthor.getFirstName()))
                .andExpect(jsonPath(
                        "$.[0].lastName").value(savedAuthor.getLastName()))
                .andExpect(
                        jsonPath("$.[0].emailAddress").value(savedAuthor.getEmailAddress()))
                .andExpect(status().isOk());
        verify(mockRepository, times(1)).findById(anyLong());
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T06 - Article to be updated does not exist so PUT returns 404")
    public void test_06(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.existsById(10L)).thenReturn(false);
        mockMvc.perform(put(RESOURCE_URI + "/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new Author(10L, "firstName", "lastName", "emailAddress"))))
                .andExpect(status().isNotFound());
        verify(mockRepository, never()).save(any(Author.class));
        verify(mockRepository, times(1)).existsById(10L);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T07 -  Article to be updated exists so PUT saves new copy")
    public void test_07(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.existsById(10L)).thenReturn(true);
        mockMvc.perform(put(RESOURCE_URI + "/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new Author(10L, "firstName", "lastName", "emailAddress"))))
                .andExpect(status().isNoContent());
        verify(mockRepository, times(1)).save(any(Author.class));
        verify(mockRepository, times(1)).existsById(10L);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T08 - ID in PUT URL not equal to one in request body")
    public void test_08(@Autowired MockMvc mockMvc) throws Exception {
        mockMvc.perform(put(RESOURCE_URI + "/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new Author(10L, "firstName", "lastName", "emailAddress"))))
                .andExpect(status().isConflict());
        verify(mockRepository, never()).save(any(Author.class));
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T09 - Article to be removed does not exist so DELETE returns 404")
    public void test_09(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.findById(1L))
                .thenReturn(Optional.empty());
        mockMvc.perform(delete(RESOURCE_URI + "/1"))
                .andExpect(status().isNotFound());
        verify(mockRepository, never()).delete(any(Author.class));
        verify(mockRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T10 - Article to be removed exists so DELETE deletes it")
    public void test_10(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.findById(1L))
                .thenReturn(Optional.of(savedAuthor));
        mockMvc.perform(delete(RESOURCE_URI + "/1"))
                .andExpect(status().isNoContent());
        verify(mockRepository, times(1)).delete(refEq(savedAuthor));
        verify(mockRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T11 - POST returns 400 if all required properties are not set")
    public void test_11(@Autowired MockMvc mockMvc) throws Exception {
        mockMvc.perform(post(RESOURCE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new Author())))
                .andExpect(status().isBadRequest());
        verify(mockRepository, never()).save(any(Author.class));
        verifyNoMoreInteractions(mockRepository);
    }

    @Test
    @DisplayName("T12 - Field errors present for each invalid property")
    public void test_12(@Autowired MockMvc mockMvc) throws Exception {
        mockMvc.perform(post(RESOURCE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new Author())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.lastName")
                        .value("must not be null"))
                .andExpect(jsonPath("$.fieldErrors.firstName")
                        .value("must not be null"))
                .andExpect(jsonPath("$.fieldErrors.emailAddress")
                        .value("must not be null"));

        mockMvc.perform(post(RESOURCE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                new Author(0L, "", "", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.lastName").value(
                        "Last name should be between 1 and 80 characters"))
                .andExpect(jsonPath("$.fieldErrors.firstName").value(
                        "First name should be between 1 and 80 characters"));
        verify(mockRepository, never()).save(any(Author.class));
    }

    @Test
    @DisplayName("T13 - Get requests have proper CORS headers")
    public void test_13(@Autowired MockMvc mockMvc) throws Exception {
        when(mockRepository.findAll()).
                thenReturn(Collections.singletonList(savedAuthor));
        mockMvc.perform(get(RESOURCE_URI))
                .andExpect(status().isOk()).andExpect(
                        header().stringValues(HttpHeaders.VARY,
                                hasItems("Origin", "Access-Control-Request-Method",
                                        "Access-Control-Request-Headers")));
    }

}