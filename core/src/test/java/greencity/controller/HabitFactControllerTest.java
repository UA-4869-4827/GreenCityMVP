package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.constant.ErrorMessage;
import greencity.converters.UserArgumentResolver;
import greencity.dto.PageableDto;
import greencity.dto.habitfact.*;
import greencity.dto.language.LanguageDTO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.dto.user.HabitIdRequestDto;
import greencity.enums.FactOfDayStatus;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.HabitFactService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;
import static greencity.ModelUtils.getPrincipal;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.Mockito.verify;
import static org.hamcrest.Matchers.*;
import java.security.Principal;
import java.util.Locale;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.http.MediaType;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class HabitFactControllerTest {
    private static final String factLink = "/facts";

    @InjectMocks
    private HabitFactController habitFactController;
    @Mock
    private HabitFactService habitFactService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private Validator validator;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private Principal principal = getPrincipal();
    private ErrorAttributes errorAttributes = new DefaultErrorAttributes();


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(habitFactController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new UserArgumentResolver(userService, modelMapper))
                .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
                .setValidator(validator)
                .build();
    }

    @Test
    void getRandomFactTest() throws Exception {
        Locale locale = new Locale("en");
        Long habitId = 1L;
        LanguageTranslationDTO resDTO = LanguageTranslationDTO.builder()
                .language(LanguageDTO.builder()
                        .id(1L)
                        .code("en")
                        .build())
                .content("random habit fact")
                .build();
        when(habitFactService.getRandomHabitFactByHabitIdAndLanguage(habitId, locale.getLanguage()))
                .thenReturn(resDTO);

        mockMvc.perform(get(factLink + "/random/{habitId}",  habitId)
                        .locale(locale))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content")
                        .value("random habit fact"));

        verify(habitFactService).getRandomHabitFactByHabitIdAndLanguage(habitId, locale.getLanguage());
    }

    @Test
    void getHabitFactOfTheDayTest() throws Exception {
        Long languageId = 1L;
        LanguageTranslationDTO resDTO = LanguageTranslationDTO.builder()
                        .language(LanguageDTO.builder()
                                .id(1L)
                                .code("en")
                                .build())
                        .content("test habit fact")
                        .build();
        when(habitFactService.getHabitFactOfTheDay(languageId))
                .thenReturn(resDTO);

        mockMvc.perform(get(factLink + "/dayFact/{languageId}", languageId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content")
                        .value("test habit fact"));

        verify(habitFactService).getHabitFactOfTheDay(languageId);
    }

    @Test
    void getAllHabitFactsTest() throws Exception {
        Locale locale = new Locale("en");
        LanguageTranslationDTO resDTO = LanguageTranslationDTO.builder()
                .language(LanguageDTO.builder()
                        .id(1L)
                        .code("en")
                        .build())
                .content("all habit fact")
                .build();
        PageableDto<LanguageTranslationDTO> presDTO = new PageableDto<>(List.of(resDTO), 1L, 0, 1);
        when(habitFactService.getAllHabitFacts(any(Pageable.class), locale.getLanguage()))
                .thenReturn(presDTO);

        mockMvc.perform(get(factLink)
                        .locale(locale))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page[0].content")
                        .value("all habit fact"));
        verify(habitFactService).getAllHabitFacts(any(Pageable.class), locale.getLanguage());

    }

    @Test
    void saveTest() throws Exception {
        HabitFactPostDto habitFactPostDto = HabitFactPostDto
                .builder()
                .translations(List.of(LanguageTranslationDTO
                        .builder()
                        .language(LanguageDTO.builder()
                                .id(1L)
                                .code("en")
                                .build())
                        .content("save habit fact")
                        .build()))
                .habit(HabitIdRequestDto
                        .builder()
                        .id(1L)
                        .build())
                .build();
        ObjectMapper realObjectMapper = new ObjectMapper();
        String json = realObjectMapper.writeValueAsString(habitFactPostDto);
        HabitFactVO saveFact =  mock(HabitFactVO.class);
        when(habitFactService.save(habitFactPostDto))
                .thenReturn(saveFact);

        HabitFactDtoResponse responseDto = HabitFactDtoResponse
                .builder()
                .id(1L)
                .build();
        when(modelMapper.map(saveFact, HabitFactDtoResponse.class))
                .thenReturn(responseDto);

        mockMvc.perform(post(factLink)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id",is(1L)));

        verify(habitFactService).save(habitFactPostDto);
        verify(modelMapper).map(saveFact, HabitFactDtoResponse.class);
    }

    @Test
    void updateTest() throws Exception {
        Long id = 1L;
        HabitFactUpdateDto updateHabit = HabitFactUpdateDto.builder()
                .translations(List.of(HabitFactTranslationUpdateDto
                        .builder()
                                .factOfDayStatus(FactOfDayStatus.USED)
                                .language(LanguageDTO.builder()
                                    .id(1L)
                                    .code("en")
                                    .build())
                                .content("update habit test")
                        .build()))
                .habit(HabitIdRequestDto
                        .builder()
                        .id(1L)
                        .build())
                .build();
        ObjectMapper realObjectMapper = new ObjectMapper();
        String json = realObjectMapper.writeValueAsString(updateHabit);
        HabitFactVO updateFact =  mock(HabitFactVO.class);
        when(habitFactService.update(updateHabit, id))
                .thenReturn(updateFact);

        HabitFactDtoResponse responseDto = HabitFactDtoResponse
                .builder()
                .id(1L)
                .build();
        when(modelMapper.map(updateFact, HabitFactDtoResponse.class))
                .thenReturn(responseDto);


        mockMvc.perform(put(factLink + "/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id",is(1L)));

        verify(habitFactService).update(updateHabit, id);
        verify(modelMapper).map(updateFact, HabitFactDtoResponse.class);
    }

    @Test
    void deleteTest() throws Exception {
        Long id = 1L;

        mockMvc.perform(delete(factLink + "/{id}", id))
                .andExpect(status().isOk());

        verify(habitFactService).delete(id);
    }

    @Test
    void deleteInvalidTest() throws Exception {
        Long invalidId = 999L;

        when(habitFactService.delete(invalidId))
                .thenThrow(new NotDeletedException(ErrorMessage.HABIT_FACT_NOT_DELETED_BY_ID));

        mockMvc.perform(delete(factLink + "/{id}", invalidId))
                .andExpect(status().isBadRequest());

        verify(habitFactService).delete(invalidId);
    }

}
