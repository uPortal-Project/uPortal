/**
 * 
 */
package org.jasig.portal.portlet.container.cache;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
/**
 * Tests for {@link LimitedBufferStringWriter}.
 * 
 * @author Nicholas Blair
 *
 */
public class LimitedBufferStringWriterTest {

	@Test
	public void testControl() throws IOException {
		final String content = "<p>Simple content</p>";
		LimitedBufferStringWriter writer = new LimitedBufferStringWriter(content.length());
		writer.write(content);

		Assert.assertFalse(writer.isLimitExceeded());
		Assert.assertEquals(content, writer.getBuffer().toString());
	}
	
	@Test
	public void testContentExceedsThreshold() throws IOException {
		final String content = "<p>Simple content</p>";
		LimitedBufferStringWriter writer = new LimitedBufferStringWriter(content.length() - 1);
		writer.write(content);

		Assert.assertTrue(writer.isLimitExceeded());
		Assert.assertEquals("", writer.getBuffer().toString());
		// try to write more and see no results
		writer.write("a");
		Assert.assertEquals("", writer.getBuffer().toString());
	}
	
	@Test
	public void testContentExceedsClearBuffer() throws IOException {
		final String content = "<p>Simple content</p>";
		LimitedBufferStringWriter writer = new LimitedBufferStringWriter(content.length() - 1);
		// write the first few chars
		writer.write(content.substring(0, 5));
		// verify content successfully buffered
		Assert.assertFalse(writer.isLimitExceeded());
		Assert.assertEquals(content.substring(0,5), writer.getBuffer().toString());

		// now write the remainder 
		writer.write(content.substring(5, content.length()));
		
		Assert.assertTrue(writer.isLimitExceeded());
		Assert.assertEquals("", writer.getBuffer().toString());
		// try to write more and see no results
		writer.write("a");
		Assert.assertEquals("", writer.getBuffer().toString());
	}
}
