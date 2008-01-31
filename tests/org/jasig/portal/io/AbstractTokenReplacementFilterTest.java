/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.io;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AbstractTokenReplacementFilterTest extends TestCase {
    public static final String TOKEN_PREFIX = "UP:CHANNEL_TITLE-[{";
    public static final int MAX_TOKEN_LENGTH = 32;
    public static final String TOKEN_SUFFIX = "}]";
    
    public void testWholeRewriting() throws Exception {
        //normal test
        this.testRewriting(
                "<img src=\"foo\" alt=\"Channel '12' Title\"/>", 
                "<img src=\"foo\" alt=\"" + TOKEN_PREFIX + "12" + TOKEN_SUFFIX + "\"/>");
        
        //start of string
        this.testRewriting(
                "Channel '12' Title\"/>", 
                TOKEN_PREFIX + "12" + TOKEN_SUFFIX + "\"/>");
        
        //end of string
        this.testRewriting(
                "<img src=\"foo\" alt=\"Channel '12' Title", 
                "<img src=\"foo\" alt=\"" + TOKEN_PREFIX + "12" + TOKEN_SUFFIX);

        //partial prefix
        this.testRewriting(
                "<img src=\"UP:U\" alt=\"Channel '12' Title\"/>", 
                "<img src=\"UP:U\" alt=\"" + TOKEN_PREFIX + "12" + TOKEN_SUFFIX + "\"/>");

        //partial suffix
        this.testRewriting(
                "<img src=\"foo\" alt=\"Channel '1}2' Title\"/>", 
                "<img src=\"foo\" alt=\"" + TOKEN_PREFIX + "1}2" + TOKEN_SUFFIX + "\"/>");
        
        //no channel id
        this.testRewriting(
                "<img src=\"foo\" alt=\"" + TOKEN_PREFIX + TOKEN_SUFFIX + "\"/>", 
                "<img src=\"foo\" alt=\"" + TOKEN_PREFIX + TOKEN_SUFFIX + "\"/>");

        //max length channel id
        this.testRewriting(
                "<img src=\"foo\" alt=\"Channel '12345678901234567890123456789012' Title\"/>", 
                "<img src=\"foo\" alt=\"" + TOKEN_PREFIX + "12345678901234567890123456789012" + TOKEN_SUFFIX + "\"/>");
        
        //too-long channel id
        this.testRewriting(
                "<img src=\"foo\" alt=\"" + TOKEN_PREFIX + "123456789012345678901234567890123" + TOKEN_SUFFIX + "\"/>", 
                "<img src=\"foo\" alt=\"" + TOKEN_PREFIX + "123456789012345678901234567890123" + TOKEN_SUFFIX + "\"/>");

        //nested too-long channel id
        this.testRewriting(
                "<img src=\"foo\" alt=\"" + TOKEN_PREFIX + "\"/> Channel \'8901234567890123\' Title", 
                "<img src=\"foo\" alt=\"" + TOKEN_PREFIX + "\"/> " + TOKEN_PREFIX + "8901234567890123" + TOKEN_SUFFIX + "");
    }
    
    public void testChunkedRewriting() throws Exception {
        //test chunked in ignored string
        this.testRewriting(
                "<img src=\"foo\" alt=\"Channel '12' Title\"/>", 
                "<img src=\"foo\"", " alt=\"" + TOKEN_PREFIX + "12" + TOKEN_SUFFIX + "\"/>");
        
        //test chunked prefix
        this.testRewriting(
                "<img src=\"foo\" alt=\"Channel '12' Title\"/>", 
                "<img src=\"foo\" alt=\"UP:CH", "ANNEL_TITLE-[{12" + TOKEN_SUFFIX + "\"/>");
     
        //test chunked token
        this.testRewriting(
                "<img src=\"foo\" alt=\"Channel '12' Title\"/>", 
                "<img src=\"foo\" alt=\"" + TOKEN_PREFIX + "1", "2" + TOKEN_SUFFIX + "\"/>");

        //test chunked suffix
        this.testRewriting(
                "<img src=\"foo\" alt=\"Channel '12' Title\"/>", 
                "<img src=\"foo\" alt=\"" + TOKEN_PREFIX + "12}", "]\"/>");
        
        //test lots of chunks
        this.testRewriting(
                "<img src=\"foo\" alt=\"Channel '12' Title\"/>", 
                "<img src=\"foo\"", " alt=\"UP:CH", "ANNEL_TITLE-[{12}", "]\"", "/>");
        
    }
    
    public void testFullFile() throws Exception {
        final InputStream inputFileStream = this.getClass().getResourceAsStream("/org/jasig/portal/io/uportal.html");
        
        final StringWriter writer = new StringWriter();
        final AbstractTokenReplacementFilter filter = new AbstractTokenReplacementFilter(writer, TOKEN_PREFIX, MAX_TOKEN_LENGTH, TOKEN_SUFFIX) {
            @Override
            protected String replaceToken(String token) {
                return "Channel '" + token + "' Title";
            }
        };
        
        IOUtils.copy(inputFileStream, filter);
        
        filter.flush();
        filter.close();
        
        final InputStream expectedFileStream = this.getClass().getResourceAsStream("/org/jasig/portal/io/uportal_filtered.html");
        final String expected = IOUtils.toString(expectedFileStream);
        
        final StringBuffer filteredString = writer.getBuffer();

        assertEquals(expected, filteredString.toString());
    }
    
    private void testRewriting(String expected, String... toFilter) throws Exception {
        final StringWriter writer = new StringWriter();
        final AbstractTokenReplacementFilter filter = new AbstractTokenReplacementFilter(writer, TOKEN_PREFIX, MAX_TOKEN_LENGTH, TOKEN_SUFFIX) {
            @Override
            protected String replaceToken(String token) {
                return "Channel '" + token + "' Title";
            }
        };
        
        for (final String chunk : toFilter) {
            filter.write(chunk);
        }
        
        filter.flush();
        filter.close();
        
        final StringBuffer filteredString = writer.getBuffer();
        
        System.out.println("I|" + Arrays.asList(toFilter) + "|");
        System.out.println("O|" + filteredString + "|");
        System.out.println("E|" + expected + "|");
        System.out.println();
        
        assertEquals(expected, filteredString.toString());
    }
}
