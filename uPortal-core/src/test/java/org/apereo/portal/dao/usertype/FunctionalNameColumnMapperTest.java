package org.apereo.portal.dao.usertype;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertThrows;
import static org.springframework.test.util.AssertionErrors.assertEquals;

/**
 * Unit tests for FunctionalNameColumnMapper class.
 */
public class FunctionalNameColumnMapperTest {
    private static FunctionalNameColumnMapper mapper;

    /**
     * Sets up the test environment by creating an instance of FunctionalNameColumnMapper.
     */
    @BeforeClass
    public static void setUp() {
        mapper = new FunctionalNameColumnMapper();
    }

    /**
     * Tests the fromNonNullValue method with a valid input.
     */
    @Test
    public void testFromNonNullValueWithValidInput() {
        String input = "valid_functional_name123";
        String result = mapper.fromNonNullValue(input);
        assertEquals("Mapping from non-null value failed for valid input", input, result);
    }

    /**
     * Tests the fromNonNullValue method with an invalid input.
     * Expects IllegalArgumentException to be thrown with the correct message.
     */
    @Test
    public void testFromNonNullValueWithInvalidInput() {
        String input = "invalid functional name";
        assertThrows(
            "Value from database 'invalid functional name' does not validate against pattern: " + FunctionalNameType.VALID_FNAME_PATTERN.pattern(),
            IllegalArgumentException.class,
            () -> {
                mapper.fromNonNullValue(input);
            }
        );

    }

    /**
     * Tests the toNonNullValue method with a valid input.
     */
    @Test
    public void testToNonNullValueWithValidInput() {
        String input = "valid_functional_name123";
        String result = mapper.toNonNullValue(input);
        assertEquals("Mapping to non-null value failed for valid input", input, result);
    }

    /**
     * Tests the toNonNullValue method with an invalid input.
     * Expects IllegalArgumentException to be thrown with the correct message.
     */
    @Test
    public void testToNonNullValueWithInvalidInput() {
        String input = "invalid functional name";

        assertThrows(
            "Value being stored 'invalid functional name' does not validate against pattern: " + FunctionalNameType.VALID_FNAME_PATTERN.pattern(),
            IllegalArgumentException.class,
            () -> {
                mapper.toNonNullValue(input);
            }
        );
    }
}
