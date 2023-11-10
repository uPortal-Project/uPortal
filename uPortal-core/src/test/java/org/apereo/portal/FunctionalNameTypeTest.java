package org.apereo.portal;

import static org.junit.Assert.*;

import org.apereo.portal.dao.usertype.FunctionalNameType;
import org.junit.Test;

/** @author snehitroda */
public class FunctionalNameTypeTest {

    @Test
    public void testValidateValidFunctionalName() {
        // Arrange
        String validFname = "my_functional-name123";

        // Act & Assert
        assertTrue(FunctionalNameType.isValid(validFname));
    }

    @Test
    public void testValidateInvalidFunctionalName() {
        // Arrange
        String invalidFname = "my functional name"; // Contains spaces

        // Act & Assert
        assertFalse(FunctionalNameType.isValid(invalidFname));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateThrowsIllegalArgumentException() {
        // Arrange
        String invalidFname = "my functional name"; // Contains spaces

        // Act
        FunctionalNameType.validate(invalidFname);
    }

    @Test
    public void testMakeValidNullInput() {
        // Arrange
        String nullFname = null;

        // Act
        String result = FunctionalNameType.makeValid(nullFname);

        // Assert
        assertEquals("_", result);
    }

    @Test
    public void testMakeValidWithSpecialCharacters() {
        // Arrange
        String fnameWithSpecialChars = "my_functional#name123";

        // Act
        String result = FunctionalNameType.makeValid(fnameWithSpecialChars);

        // Assert
        assertEquals("my_functional_name123", result);
    }
}
