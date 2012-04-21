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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;

import com.google.common.base.Function;
/**
 * Tests for {@link LimitingTeeOutputStream}.
 * 
 * @author Nicholas Blair
 *
 */
public class LimitingTeeOutputStreamTest {

	@Test
	public void testControl() throws IOException {
		final byte[] content = "<p>Simple content</p>".getBytes();
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		LimitingTeeOutputStream stream = new LimitingTeeOutputStream(content.length, NullOutputStream.NULL_OUTPUT_STREAM, byteStream);
		stream.write(content);

		assertFalse(stream.isLimitReached());
		assertArrayEquals(content, byteStream.toByteArray());
	}
	
	@Test
	public void testContentExceedsThreshold() throws IOException {
		final byte[] content = "<p>Simple content</p>".getBytes();
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        LimitingTeeOutputStream stream = new LimitingTeeOutputStream(content.length - 1, NullOutputStream.NULL_OUTPUT_STREAM, byteStream);
		stream.write(content);

		assertTrue(stream.isLimitReached());
		assertArrayEquals(new byte[0], byteStream.toByteArray());
		// try to write more and see no results
		stream.write("a".getBytes());
		assertArrayEquals(new byte[0], byteStream.toByteArray());
	}
	
	@Test
	public void testContentExceedsClearBuffer() throws IOException {
		final byte[] content = "<p>Simple content</p>".getBytes();
		final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        LimitingTeeOutputStream stream = new LimitingTeeOutputStream(content.length - 1, NullOutputStream.NULL_OUTPUT_STREAM, byteStream, 
                new Function<LimitingTeeOutputStream, Object>() {
                    @Override
                    public Object apply(LimitingTeeOutputStream input) {
                        byteStream.reset();
                        return null;
                    }
        });
		// write the first few chars
		stream.write(content, 0, 5);
		// verify content successfully buffered
		assertFalse(stream.isLimitReached());
		final byte[] subContent = Arrays.copyOf(content, 5);
		assertArrayEquals(subContent, byteStream.toByteArray());

		// now write the remainder 
		stream.write(content, 5, content.length);
		
		assertTrue(stream.isLimitReached());
		assertArrayEquals(new byte[0], byteStream.toByteArray());
		// try to write more and see no results
		stream.write("a".getBytes());
		assertArrayEquals(new byte[0], byteStream.toByteArray());
	}
    
    @Test
    public void testContentExceedsResetBuffer() throws IOException {
        final byte[] content = "<p>Simple content</p>".getBytes();
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        LimitingTeeOutputStream stream = new LimitingTeeOutputStream(content.length - 1, NullOutputStream.NULL_OUTPUT_STREAM, byteStream);
        stream.write(content);

        assertTrue(stream.isLimitReached());
        assertArrayEquals(new byte[0], byteStream.toByteArray());
        
        stream.resetByteCount();
        
        // try to write more and see results
        stream.write("a".getBytes());
        assertArrayEquals("a".getBytes(), byteStream.toByteArray());
    }
}
