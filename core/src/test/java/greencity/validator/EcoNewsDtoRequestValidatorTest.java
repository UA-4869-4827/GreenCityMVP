package greencity.validator;

import greencity.dto.econews.AddEcoNewsDtoRequest;
import greencity.exception.exceptions.WrongCountOfTagsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static greencity.ModelUtils.getAddEcoNewsDtoRequest;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EcoNewsDtoRequestValidatorTest {
    private final EcoNewsDtoRequestValidator validator = new EcoNewsDtoRequestValidator();

    @Test
    void isValid_returnsTrue_whenSourceIsValid() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();

        request.setSource("https://eco-lavca.ua/");

        assertTrue(validator.isValid(request, null));
    }

    @Test
    void isValid_returnsTrue_whenSourceIsNullAndCorrectNumberOfTags() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();

        request.setSource(null);
        request.setTags(List.of("news", "eco"));

        assertTrue(validator.isValid(request, null));
    }

    @Test
    void isValid_returnsTrue_whenSourceIsEmptyAndCorrectNumberOfTags() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();

        request.setSource("");
        request.setTags(List.of("news", "eco"));

        assertTrue(validator.isValid(request, null));
    }

    @Test
    void isValid_returnsTrue_whenSourceIsIgnoredAndCorrectNumberOfTags() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();

        request.setTags(List.of("news", "eco"));

        assertTrue(validator.isValid(request, null));
    }

    @Test
    void isValid_throwsWrongCountOfTagsException_whenSourceIsValidAndEmptyTagsList() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();

        request.setSource("https://eco-lavca.ua/");
        request.setTags(new ArrayList<>());

        assertThrowsExactly(WrongCountOfTagsException.class, () -> {
            validator.isValid(request, null);
        });
    }

    @Test
    void isValid_throwsWrongCountOfTagsException_whenSourceIsValidAndTagsListSizeExceedsMaxSize() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();

        request.setSource("https://eco-lavca.ua/");
        request.setTags(List.of("news", "eco", "tag", "custom"));

        assertThrowsExactly(WrongCountOfTagsException.class, () -> {
            validator.isValid(request, null);
        });
    }

    @Test
    void isValid_returnsTrue_whenSourceIsValidAndTagsListSizeIsMaxAllowedSize() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();

        request.setSource("https://eco-lavca.ua/");
        request.setTags(List.of("news", "eco", "tag"));

        assertTrue(validator.isValid(request, null));
    }
}
