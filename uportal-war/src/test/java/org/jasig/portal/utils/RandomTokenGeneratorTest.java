/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.utils;

import static junit.framework.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

/**
 * Test generation of random tokens
 * 
 * @author Eric Dalquist
 */
public class RandomTokenGeneratorTest {
    @Test
    public void testTokenGeneration() {
        final RandomTokenGenerator randomTokenGenerator = new RandomTokenGenerator(new Random(0));
        
        int length = 0;
        String token = randomTokenGenerator.generateRandomToken(++length);
        double bits = randomTokenGenerator.getNumberOfRandomBits(length);
        int reqLength = randomTokenGenerator.getLengthRequiredForBits((int)Math.floor(bits));
        assertEquals("4", token);
        assertEquals("4.954196310386876", Double.toString(bits)); //easier to copy/paste from junit if comparing doubles as strings
        assertEquals(length, reqLength);
        
        token = randomTokenGenerator.generateRandomToken(++length);
        bits = randomTokenGenerator.getNumberOfRandomBits(length);
        reqLength = randomTokenGenerator.getLengthRequiredForBits((int)Math.floor(bits));
        assertEquals("4p", token);
        assertEquals("9.908392620773752", Double.toString(bits));
        assertEquals(length, reqLength);
        
        token = randomTokenGenerator.generateRandomToken(++length);
        bits = randomTokenGenerator.getNumberOfRandomBits(length);
        reqLength = randomTokenGenerator.getLengthRequiredForBits((int)Math.floor(bits));
        assertEquals("df9", token);
        assertEquals("14.862588931160627", Double.toString(bits));
        assertEquals(length, reqLength);
        
        token = randomTokenGenerator.generateRandomToken(++length);
        bits = randomTokenGenerator.getNumberOfRandomBits(length);
        reqLength = randomTokenGenerator.getLengthRequiredForBits((int)Math.floor(bits));
        assertEquals("379h", token);
        assertEquals("19.816785241547503", Double.toString(bits));
        assertEquals(length, reqLength);
        
        token = randomTokenGenerator.generateRandomToken(++length);
        bits = randomTokenGenerator.getNumberOfRandomBits(length);
        reqLength = randomTokenGenerator.getLengthRequiredForBits((int)Math.floor(bits));
        assertEquals("kjwar", token);
        assertEquals("24.77098155193438", Double.toString(bits));
        assertEquals(length, reqLength);
        
        token = randomTokenGenerator.generateRandomToken(++length);
        bits = randomTokenGenerator.getNumberOfRandomBits(length);
        reqLength = randomTokenGenerator.getLengthRequiredForBits((int)Math.floor(bits));
        assertEquals("5arzr6", token);
        assertEquals("29.725177862321253", Double.toString(bits));
        assertEquals(length, reqLength);
        
        token = randomTokenGenerator.generateRandomToken(++length);
        bits = randomTokenGenerator.getNumberOfRandomBits(length);
        reqLength = randomTokenGenerator.getLengthRequiredForBits((int)Math.floor(bits));
        assertEquals("88kypfr", token);
        assertEquals("34.67937417270813", Double.toString(bits));
        assertEquals(length, reqLength);
        assertEquals(length, reqLength);
        
        token = randomTokenGenerator.generateRandomToken(++length);
        bits = randomTokenGenerator.getNumberOfRandomBits(length);
        reqLength = randomTokenGenerator.getLengthRequiredForBits((int)Math.floor(bits));
        assertEquals("hg7kujrz", token);
        assertEquals("39.633570483095006", Double.toString(bits));
        assertEquals(length, reqLength);
    }

    @Test
    public void testTokenLengthMath31() {
        final RandomTokenGenerator randomTokenGenerator = new RandomTokenGenerator(new Random(0));
        
        for (int length = 0; length < 10000; length++) {
            final String token = randomTokenGenerator.generateRandomToken(++length);
            final double bits = randomTokenGenerator.getNumberOfRandomBits(length);
            final int reqLengthMin = randomTokenGenerator.getLengthRequiredForBits((int)Math.floor(bits));
            final int reqLengthMax = randomTokenGenerator.getLengthRequiredForBits((int)Math.ceil(bits));
            assertEquals(length, token.length());
            assertEquals(length, reqLengthMin);
            assertEquals(length + 1, reqLengthMax);
        }
    }

    @Test
    public void testTokenLengthMath32() {
        final RandomTokenGenerator randomTokenGenerator = new RandomTokenGenerator(new Random(0),
                new char[] {
                    '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b',
                    'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 
                    'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
                    'z', '_'
        });
        
        for (int length = 0; length < 10000; length++) {
            final String token = randomTokenGenerator.generateRandomToken(++length);
            final double bits = randomTokenGenerator.getNumberOfRandomBits(length);
            final int reqLengthMin = randomTokenGenerator.getLengthRequiredForBits((int)Math.floor(bits));
            final int reqLengthMax = randomTokenGenerator.getLengthRequiredForBits((int)Math.ceil(bits));
            assertEquals(length, token.length());
            assertEquals(length, reqLengthMin);
            assertEquals(length, reqLengthMax);
        }
    }
}
