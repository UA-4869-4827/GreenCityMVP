package greencity.controller;

import greencity.service.LanguageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LanguageController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {
        LanguageControllerTest.TestConfig.class,
        LanguageController.class
})
public class LanguageControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    LanguageService languageService;

    @Test
    void getAllLanguageCodes_returnsLanguageCodes() throws Exception {
        List<String> expectedLanguageCodes = List.of("en", "ua", "fr");

        when(languageService.findAllLanguageCodes()).thenReturn(expectedLanguageCodes);

        mockMvc.perform(get("/language"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("en"))
                .andExpect(jsonPath("$[1]").value("ua"))
                .andExpect(jsonPath("$[2]").value("fr"));

        verify(languageService).findAllLanguageCodes();
    }
}
