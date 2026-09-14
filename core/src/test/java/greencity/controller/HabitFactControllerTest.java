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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.Mockito.verify;
import java.util.Locale;
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
    LanguageTranslationDTO langDTO;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;
    private final ErrorAttributes errorAttributes = new DefaultErrorAttributes();


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(habitFactController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
                .setValidator(validator)
                .build();
    }

    @Test
    void getRandomFactTest() throws Exception {
        Locale locale = new Locale("en");
        Long habitId = 1L;
        when(habitFactService.getRandomHabitFactByHabitIdAndLanguage(habitId, locale.getLanguage()))
                .thenReturn(langDTO);

        mockMvc.perform(get(factLink + "/random/{habitId}",  habitId)
                        .locale(locale))
                .andExpect(status().isOk());

        verify(habitFactService).getRandomHabitFactByHabitIdAndLanguage(habitId, locale.getLanguage());
    }

    @Test
    void getHabitFactOfTheDayTest() throws Exception {
        Long languageId = 1L;
        when(habitFactService.getHabitFactOfTheDay(languageId))
                .thenReturn(langDTO);

        mockMvc.perform(get(factLink + "/dayFact/{languageId}", languageId))
                .andExpect(status().isOk());

        verify(habitFactService).getHabitFactOfTheDay(languageId);
    }

    @Test
    void getAllHabitFactsTest() throws Exception {
        Locale locale = new Locale("en");
        PageableDto<LanguageTranslationDTO> presDTO = new PageableDto<>(List.of(langDTO), 1L, 0, 1);

        when(habitFactService.getAllHabitFacts(
                any(Pageable.class),
                eq(locale.getLanguage())))
                .thenReturn(presDTO);

        mockMvc.perform(get(factLink)
                        .locale(locale))
                .andExpect(status().isOk());
        verify(habitFactService).getAllHabitFacts(
                any(Pageable.class),
                eq(locale.getLanguage()));

    }

    @Test
    void saveTest() throws Exception {
        HabitFactPostDto habitFactPostDto = HabitFactPostDto
                .builder()
                .translations(List.of(langDTO))
                .habit(HabitIdRequestDto
                        .builder()
                        .id(1L)
                        .build())
                .build();

        String json = objectMapper.writeValueAsString(habitFactPostDto);
        HabitFactVO saveFact =  mock(HabitFactVO.class);
        HabitFactDtoResponse responseDto = HabitFactDtoResponse
                .builder()
                .id(1L)
                .build();

        when(habitFactService.save(any(HabitFactPostDto.class)))
                .thenReturn(saveFact);

        when(modelMapper.map(saveFact, HabitFactDtoResponse.class))
                .thenReturn(responseDto);

        mockMvc.perform(post(factLink)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(habitFactService).save(any(HabitFactPostDto.class));
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
        String json = objectMapper.writeValueAsString(updateHabit);
        HabitFactVO updateFact =  mock(HabitFactVO.class);
        HabitFactPostDto postDto = HabitFactPostDto.builder()
                .translations(List.of(langDTO))
                .habit(HabitIdRequestDto.builder()
                        .id(1L)
                        .build())
                .build();

        when(habitFactService.update(
                any(HabitFactUpdateDto.class),
                        eq(id)))
                .thenReturn(updateFact);

        when(modelMapper.map(updateFact, HabitFactPostDto.class))
                .thenReturn(postDto);


        mockMvc.perform(put(factLink + "/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        verify(habitFactService).update(any(HabitFactUpdateDto.class), eq(id));
        verify(modelMapper).map(updateFact, HabitFactPostDto.class);
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
