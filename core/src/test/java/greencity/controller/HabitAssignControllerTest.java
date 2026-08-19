package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.dto.habit.HabitAssignCustomPropertiesDto;
import greencity.dto.habit.UserShoppingAndCustomShoppingListsDto;
import greencity.dto.user.UserVO;
import greencity.service.HabitAssignService;
import greencity.service.UserService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Locale;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HabitAssignControllerTest {
    private static final String habitAssignLink = "/habit/assign";
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ModelMapper modelMapper;

    @Mock
    private HabitAssignService habitAssignService;

    @Mock
    private UserService userService;

    @InjectMocks
    private HabitAssignController habitAssignController;

    private final Principal principal = getPrincipal();

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        modelMapper = new ModelMapper();

        this.mockMvc = MockMvcBuilders.standaloneSetup(habitAssignController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                        new UserArgumentResolver(userService, modelMapper))
                .build();
    }

    @Test
    void testAssignDefault_whenCorrectHabitId_returnsCreatedStatusCode() throws Exception {
        UserVO userVO = getUserVO();
        Long habitId = 1L;
        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(post(habitAssignLink + "/{habitId}", habitId)
                        .principal(principal))
                .andExpect(status().isCreated());

        verify(habitAssignService).assignDefaultHabitForUser(habitId, userVO);
    }

    @Test
    void testAssignCustom_whenCorrectCustomPropertiesDto_returnsCreatedStatusCode() throws Exception {
        UserVO userVO = getUserVO();
        Long habitId = 1L;

        HabitAssignCustomPropertiesDto habitAssignCustomPropertiesDto =
                new HabitAssignCustomPropertiesDto();

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(post(habitAssignLink + "/{habitId}/custom", habitId)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(habitAssignCustomPropertiesDto)))
                .andExpect(status().isCreated());

        verify(habitAssignService).assignCustomHabitForUser(
                habitId,
                userVO,
                habitAssignCustomPropertiesDto
        );
    }

    @Test
    void testUpdateHabitAssignDuration_whenCorrectMinMaxDuration_returnsOkStatusCode() throws Exception {
        UserVO userVO = getUserVO();
        Long habitAssignId = 1L;
        Integer duration = 25;

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(put(habitAssignLink + "/{habitAssignId}/update-habit-duration", habitAssignId)
                        .principal(principal)
                        .param("duration", duration.toString()))
                .andExpect(status().isOk());

        verify(habitAssignService).updateUserHabitInfoDuration(
                habitAssignId,
                userVO.getId(),
                duration);
    }

    @Test
    void testGetHabitAssign_whenCorrectHabitAssignIdAndLocale_returnsOkStatusCode() throws Exception {
        UserVO userVO = getUserVO();
        Long habitAssignId = 1L;
        Locale locale = new Locale("en");

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(get(habitAssignLink + "/{habitAssignId}", habitAssignId)
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "en"))
                .andExpect(status().isOk());

        verify(habitAssignService).getByHabitAssignIdAndUserId(
                habitAssignId,
                userVO.getId(),
                locale.getLanguage()
        );
    }

    @Test
    void testGetCurrentUserHabitAssignsByIdAndAcquired_whenCorrectUserAndLocale_returnsOkStatusCode() throws Exception {
        UserVO userVO = getUserVO();
        Locale locale = new Locale("en");

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(get(habitAssignLink + "/allForCurrentUser")
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "en"))
                .andExpect(status().isOk());

        verify(habitAssignService).getAllHabitAssignsByUserIdAndStatusNotCancelled(
                userVO.getId(),
                locale.getLanguage()
        );
    }

    @Test
    void testGetUserShoppingAndCustomShoppingLists_whenCorrectDataProvided_returnsOkStatusCode() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        Locale locale = new Locale("en");

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(get(habitAssignLink + "/{habitAssignId}/allUserAndCustomList", habitAssignId)
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "en"))
                .andExpect(status().isOk());

        verify(habitAssignService).getUserShoppingAndCustomShoppingLists(
                userVO.getId(),
                habitAssignId,
                locale.getLanguage()
        );
    }

    @Test
    void testUpdateUserAndCustomShoppingLists_whenCorrectDataProvided_returnsOkStatusCode() throws Exception {
        Long habitAssignId = 1L;
        UserVO userVO = getUserVO();
        Locale locale = new Locale("en");

        // Создаем JSON объект для отправки
        JSONObject requestBody = new JSONObject();
        requestBody.put("userShoppingListItemDto", new JSONArray()
                .put(new JSONObject()
                        .put("id", 1)
                        .put("text", "String_1")
                        .put("status", "ACTIVE")));
        requestBody.put("customShoppingListItemDto", new JSONArray()
                .put(new JSONObject()
                        .put("id", 1)
                        .put("text", "String_1")
                        .put("status", "ACTIVE")));

        ObjectMapper mapper = new ObjectMapper();
        UserShoppingAndCustomShoppingListsDto dto = mapper.readValue(
                requestBody.toString(),
                UserShoppingAndCustomShoppingListsDto.class
        );

        when(userService.findByEmail(principal.getName())).thenReturn(userVO);

        mockMvc.perform(put("/habit/assign/{habitAssignId}/allUserAndCustomList", habitAssignId)
                        .principal(principal)
                        .header(HttpHeaders.ACCEPT_LANGUAGE, "en")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody.toString()))
                .andExpect(status().isOk());

        verify(habitAssignService).fullUpdateUserAndCustomShoppingLists(
                userVO.getId(),
                habitAssignId,
                dto,
                locale.getLanguage()
        );

    }

    //
}


