package greencity.validator;

import greencity.constant.ValidationConstants;
import greencity.dto.econews.AddEcoNewsDtoRequest;
import greencity.exception.exceptions.InvalidURLException;
import greencity.exception.exceptions.WrongCountOfTagsException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static greencity.ModelUtils.getAddEcoNewsDtoRequest;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EcoNewsDtoRequestValidatorTest {
    private final EcoNewsDtoRequestValidator validator = new EcoNewsDtoRequestValidator();

    @Test
    void isValid_returnsTrue_whenSourceIsValidAndTagsAreValid() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();

        request.setSource("https://eco-lavca.ua/");
        request.setTags(createTags(2));

        assertTrue(validator.isValid(request, null));
    }

    @Test
    void isValid_returnsTrue_whenSourceIsNullAndTagsAreValid() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();

        request.setSource(null);
        request.setTags(createTags(ValidationConstants.MAX_AMOUNT_OF_TAGS));

        assertTrue(validator.isValid(request, null));
    }

    @Test
    void isValid_returnsTrue_whenSourceIsEmptyAndTagsAreValid() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();

        request.setSource("");
        request.setTags(createTags(ValidationConstants.MAX_AMOUNT_OF_TAGS));

        assertTrue(validator.isValid(request, null));
    }

    @Test
    void isValid_throwsInvalidURLException_whenSourceIsInvalid() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();

        request.setSource("invalid-url");
        request.setTags(createTags(ValidationConstants.MAX_AMOUNT_OF_TAGS));

        assertThrowsExactly(InvalidURLException.class, () ->
                validator.isValid(request, null));
    }

    @Test
    void isValid_throwsWrongCountOfTagsException_whenTagsListIsEmpty() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();

        request.setSource("https://eco-lavca.ua/");
        request.setTags(List.of());

        assertThrowsExactly(WrongCountOfTagsException.class, () ->
                validator.isValid(request, null));
    }

    @Test
    void isValid_throwsNullPointerException_whenTagsListIsNull() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();

        request.setSource("https://eco-lavca.ua/");
        request.setTags(null);

        assertThrowsExactly(NullPointerException.class, () ->
                validator.isValid(request, null));
    }

    @Test
    void isValid_throwsWrongCountOfTagsException_whenTagsListSizeExceedsMaxSize() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();

        request.setSource("https://eco-lavca.ua/");
        request.setTags(createTags(ValidationConstants.MAX_AMOUNT_OF_TAGS + 1));

        assertThrowsExactly(WrongCountOfTagsException.class, () ->
                validator.isValid(request, null));
    }

    @Test
    void isValid_returnsTrue_whenTagsListSizeIsMaxAllowedSize() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();

        request.setSource("https://eco-lavca.ua/");
        request.setTags(createTags(ValidationConstants.MAX_AMOUNT_OF_TAGS));

        assertTrue(validator.isValid(request, null));
    }

    @Test
    void initialize_doesNotThrowException() {
        assertDoesNotThrow(() -> validator.initialize(null));
    }

    private List<String> createTags(int amountOfTags) {
        List<String> tags = new ArrayList<>();
        for (int i = 1; i <= amountOfTags; i++) {
            tags.add("tag" + i);
        }

        return tags;
    }
}
