/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.utils;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Generate random tokens. The default character set is the numbers 2-9 and lower case letters a-z
 * except i, l and o. This is intended to create a set that avoids numbers and letters which can
 * easily be confused with others depending on the font in use.
 *
 */
public final class RandomTokenGenerator {
    private static final char[] DEFAULT_TOKEN_CHARS =
            new char[] {
                '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b',
                'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n',
                'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
                'z'
            };

    /** The recommended generator, uses its own instance of {@link SecureRandom} */
    public static final RandomTokenGenerator INSTANCE =
            new RandomTokenGenerator(new SecureRandom());

    private final Random random;
    private final char[] tokenChars;
    private final double bitsPerChar;

    /** @param random User defined random source */
    public RandomTokenGenerator(Random random) {
        this(random, DEFAULT_TOKEN_CHARS);
    }

    /**
     * @param random User defined random source
     * @param tokenChars User defined character set to generate tokens from
     */
    public RandomTokenGenerator(Random random, char[] tokenChars) {
        this.random = random;
        this.tokenChars = tokenChars.clone();
        this.bitsPerChar = Math.log(tokenChars.length) / Math.log(2);
    }

    /**
     * @param tokenChars User defined character set to generate tokens from, performance will be
     *     better if the length of the array is a power of 2
     */
    public RandomTokenGenerator(char[] tokenChars) {
        this(new SecureRandom(), tokenChars);
    }

    /** Generate a random token of the specified length */
    public String generateRandomToken(int length) {
        final char[] token = new char[length];

        for (int i = 0; i < length; i++) {
            final int tokenIndex = random.nextInt(this.tokenChars.length);
            token[i] = tokenChars[tokenIndex];
        }

        return new String(token);
    }

    /** Determine the token length required to assure the specified number of bits of randomness */
    public int getLengthRequiredForBits(int bits) {
        final double length = (double) bits / bitsPerChar;
        return (int) Math.ceil(length);
    }

    /**
     * Determine the number of bits of randomness a token of the specified length would be generated
     * from
     */
    public double getNumberOfRandomBits(int length) {
        final double bitsPerToken = bitsPerChar * (double) length;
        return bitsPerToken;
    }
}
