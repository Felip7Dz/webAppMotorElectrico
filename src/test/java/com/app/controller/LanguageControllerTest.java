package com.app.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import com.app.constants.ViewConstants;

public class LanguageControllerTest {

    private LanguageController languageController;

    @Mock
    private CookieLocaleResolver localeResolver;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        languageController = new LanguageController();
    }

    @Test
    public void testChangeLocale() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        String language = "es"; // Example language code

        // Mock behavior of localeResolver.setLocale
        doNothing().when(localeResolver).setLocale(eq(request), eq(response), any(Locale.class));

        String viewName = languageController.changeLocale(request, response, language);

        // Verify that the controller returns the expected redirect view name
        assertEquals(ViewConstants.REDIRECT_HOME_PAGE, viewName);

    }
}
