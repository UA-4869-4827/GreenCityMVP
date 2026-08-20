package greencity.validator;

import greencity.annotations.ValidLanguage;
import greencity.service.LanguageService;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LanguageValidatorTest {
    @Mock
    private LanguageService languageService;

    @Mock
    private ValidLanguage validLanguage;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @InjectMocks
    private LanguageValidator validator;

    @BeforeEach
    public void setUp() {
        List<String> languageCodes = List.of("en", "ua", "fr");

        when(languageService.findAllLanguageCodes()).thenReturn(languageCodes);

        validator.initialize(validLanguage);
    }

    @Test
    public void isValid_returnsTrue_whenLanguageCodeExists() {
        Locale testingLocale = new Locale("en", "US");

        assertTrue(validator.isValid(testingLocale, constraintValidatorContext));
    }

    @Test
    public void isValid_returnsFalse_whenLanguageCodeDoesNotExist() {
        Locale testingLocale = new Locale("pl", "PL");

        assertFalse(validator.isValid(testingLocale, constraintValidatorContext));
    }

    @Test
    public void isValid_throwsNullPointerException_whenLanguageCodeIsNull() {
        assertThrowsExactly(NullPointerException.class,
                () -> validator.isValid(null, constraintValidatorContext));
    }

    @Test
    public void isValid_returnsFalse_whenLanguageCodeIsEmpty() {
        Locale testingLocale = new Locale("", "");
        assertFalse(validator.isValid(testingLocale, constraintValidatorContext));
    }

    @Test
    void initialize_loadsLanguageCodesFromService() {
        verify(languageService).findAllLanguageCodes();
    }
}
