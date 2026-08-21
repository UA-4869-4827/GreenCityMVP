package greencity.controller;

import greencity.dto.PageableDto;
import greencity.dto.search.SearchNewsDto;
import greencity.dto.search.SearchResponseDto;
import greencity.dto.user.EcoNewsAuthorDto;
import greencity.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.Validator;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import static org.mockito.ArgumentMatchers.eq;


@ExtendWith(MockitoExtension.class)
public class SearchControllerTest {
    private static final String searchLink = "/search";
    private static final String searchEcoNewsLink = "/search/econews";

    @InjectMocks
    private SearchController searchController;
    @Mock
    private SearchService searchService;
    @Mock
    private Validator mockValidator;

    private MockMvc mock;

    @BeforeEach
    void setUp(){
        mock = MockMvcBuilders
                .standaloneSetup(searchController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(mockValidator)
                .build();
    }

    @Test
    void searchTest() throws Exception {
        SearchResponseDto responseDto = SearchResponseDto.builder()
                .ecoNews(List.of())
                .countOfResults(0L)
                .build();

        when(searchService.search("eco", "en"))
                .thenReturn(responseDto);

        mock.perform(get(searchLink)
                        .param("searchQuery", "eco")
                        .locale(Locale.ENGLISH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("ecoNews", empty()))
                .andExpect(jsonPath("countOfResults", is(0)));
        verify(searchService).search("eco", "en");

    }

    @Test
    void searchEcoNewsTest()throws Exception{
        EcoNewsAuthorDto ec = new EcoNewsAuthorDto(1L,"Test Author");
        SearchNewsDto sec = new SearchNewsDto(1L,
                "Eco news",
                ec,
                ZonedDateTime.of(2026,8,18,12,0,0,0, ZoneId.of("Europe/Prague")),
                List.of("eco"));
        PageableDto<SearchNewsDto> p = new PageableDto<>(List.of(sec), 1, 0, 1);
        when(searchService.searchAllNews(any(Pageable.class), eq("eco"), eq("en")))
                .thenReturn(p);
        mock.perform(get(searchEcoNewsLink)
                .param("searchQuery", "eco")
                        .param("page", "0")
                        .param("size", "5")
                .locale(Locale.ENGLISH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("page", hasSize(1)))
                .andExpect(jsonPath("totalElements", is(1)))
                .andExpect(jsonPath("currentPage", is(0)))
                .andExpect(jsonPath("totalPages", is(1)))
                .andExpect(jsonPath("page[0].id", is(1L)))
                .andExpect(jsonPath("page[0].title", is("Eco news")))
                .andExpect(jsonPath("page[0].author.id", is(1L)))
                .andExpect(jsonPath("page[0].author.name", is("Test Author")))
                .andExpect(jsonPath("page[0].tags", hasSize(1)))
                .andExpect(jsonPath("page[0].tags[0]", is("eco")));
        ArgumentCaptor<Pageable> pag = ArgumentCaptor.forClass(Pageable.class);
        verify(searchService).searchAllNews(
                pag.capture(),
                "eco",
                "en");
        assertEquals(0, pag.getValue().getPageNumber());
        assertEquals(5, pag.getValue().getPageSize());
    }
}
