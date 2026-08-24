package greencity.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageValidatorTest {
    @Mock
    private MultipartFile multipartFile;

    @Mock
    private ConstraintValidatorContext context;

    private final ImageValidator validator = new ImageValidator();

    @Test
    public void isValid_returnsTrue_whenImageIsNull() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    public void isValid_returnsTrue_whenImageTypeIsPng() {
        when(multipartFile.getContentType()).thenReturn("image/png");
        assertTrue(validator.isValid(multipartFile, context));
    }

    @Test
    public void isValid_returnsTrue_whenImageTypeIsJpeg() {
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        assertTrue(validator.isValid(multipartFile, context));
    }

    @Test
    public void isValid_returnsTrue_whenImageTypeIsJpg() {
        when(multipartFile.getContentType()).thenReturn("image/jpg");
        assertTrue(validator.isValid(multipartFile, context));
    }

    @Test
    public void isValid_returnsFalse_whenImageTypeIsWrong() {
        when(multipartFile.getContentType()).thenReturn("image/gif");
        assertFalse(validator.isValid(multipartFile, context));
    }

    @Test
    public void isValid_returnsFalse_whenContentTypeIsNotAllowed() {
        when(multipartFile.getContentType()).thenReturn("content/jpg");
        assertFalse(validator.isValid(multipartFile, context));
    }

    @Test
    public void isValid_returnsFalse_whenContentTypeAndTypeNotAllowed() {
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        assertFalse(validator.isValid(multipartFile, context));
    }

    @Test
    public void isValid_returnsFalse_whenContentTypeIsNull() {
        when(multipartFile.getContentType()).thenReturn(null);
        assertFalse(validator.isValid(multipartFile, context));
    }
}
