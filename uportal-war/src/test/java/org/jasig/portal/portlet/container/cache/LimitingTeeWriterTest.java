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
/**
 * 
 */
package org.jasig.portal.portlet.container.cache;

import java.io.IOException;

import org.apache.commons.io.output.NullWriter;
import org.apache.commons.io.output.StringBuilderWriter;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Function;
/**
 * Tests for {@link LimitingTeeWriter}.
 * 
 * @author Nicholas Blair
 *
 */
public class LimitingTeeWriterTest {

	@Test
	public void testControl() throws IOException {
		final String content = "<p>Simple content</p>";
		StringBuilderWriter stringWriter = new StringBuilderWriter();
		LimitingTeeWriter writer = new LimitingTeeWriter(content.length(), NullWriter.NULL_WRITER, stringWriter);
		writer.write(content);

		Assert.assertFalse(writer.isLimitReached());
		Assert.assertEquals(content, stringWriter.toString());
	}
	
	@Test
	public void testContentExceedsThreshold() throws IOException {
		final String content = "<p>Simple content</p>";
		StringBuilderWriter stringWriter = new StringBuilderWriter();
        LimitingTeeWriter writer = new LimitingTeeWriter(content.length() - 1, NullWriter.NULL_WRITER, stringWriter);
		writer.write(content);

		Assert.assertTrue(writer.isLimitReached());
		Assert.assertEquals("", stringWriter.toString());
		// try to write more and see no results
		writer.write("a");
		Assert.assertEquals("", stringWriter.toString());
	}
	
	@Test
	public void testContentExceedsClearBuffer() throws IOException {
		final String content = "<p>Simple content</p>";
		final StringBuilderWriter stringWriter = new StringBuilderWriter();
        LimitingTeeWriter writer = new LimitingTeeWriter(content.length() - 1, NullWriter.NULL_WRITER, stringWriter, 
                new Function<LimitingTeeWriter, Object>() {
                    @Override
                    public Object apply(LimitingTeeWriter input) {
                        final StringBuilder builder = stringWriter.getBuilder();
                        builder.delete(0, builder.length());
                        return null;
                    }
        });
		// write the first few chars
		writer.write(content.substring(0, 5));
		// verify content successfully buffered
		Assert.assertFalse(writer.isLimitReached());
		Assert.assertEquals(content.substring(0,5), stringWriter.toString());

		// now write the remainder 
		writer.write(content.substring(5, content.length()));
		
		Assert.assertTrue(writer.isLimitReached());
		Assert.assertEquals("", stringWriter.toString());
		// try to write more and see no results
		writer.write("a");
		Assert.assertEquals("", stringWriter.toString());
	}
    
    @Test
    public void testContentExceedsResetBuffer() throws IOException {
        final String content = "<p>Simple content</p>";
        StringBuilderWriter stringWriter = new StringBuilderWriter();
        LimitingTeeWriter writer = new LimitingTeeWriter(content.length() - 1, NullWriter.NULL_WRITER, stringWriter);
        writer.write(content);

        Assert.assertTrue(writer.isLimitReached());
        Assert.assertEquals("", stringWriter.toString());
        
        writer.resetByteCount();
        
        // try to write more and see results
        writer.write("a");
        Assert.assertEquals("a", stringWriter.toString());
    }
}
