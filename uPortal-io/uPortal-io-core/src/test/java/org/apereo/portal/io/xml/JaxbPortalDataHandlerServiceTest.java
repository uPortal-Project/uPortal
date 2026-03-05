package org.apereo.portal.io.xml;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.tika.mime.MediaType;
import org.junit.Test;

public class JaxbPortalDataHandlerServiceTest {

    private final JaxbPortalDataHandlerService service = new JaxbPortalDataHandlerService();

    private MediaType invokeGetMediaType(byte[] data, String filename) throws Exception {
        try (final BufferedInputStream stream =
                new BufferedInputStream(new ByteArrayInputStream(data))) {
            stream.mark(data.length);
            return service.getMediaType(stream, filename);
        }
    }

    @Test
    public void testGetMediaTypeXml() throws Exception {
        final byte[] xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>"
                        .getBytes(StandardCharsets.UTF_8);
        assertEquals(MediaType.APPLICATION_XML, invokeGetMediaType(xml, "test.xml"));
    }

    @Test
    public void testGetMediaTypeZip() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("test.xml"));
            zos.write("<root/>".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        assertEquals(MediaType.APPLICATION_ZIP, invokeGetMediaType(baos.toByteArray(), "test.zip"));
    }

    @Test
    public void testGetMediaTypeJar() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (JarArchiveOutputStream jos = new JarArchiveOutputStream(baos)) {
            final byte[] content = "Manifest-Version: 1.0\n".getBytes(StandardCharsets.UTF_8);
            final JarArchiveEntry entry = new JarArchiveEntry("META-INF/MANIFEST.MF");
            entry.setSize(content.length);
            jos.putArchiveEntry(entry);
            jos.write(content);
            jos.closeArchiveEntry();
        }
        assertEquals(
                MediaType.application("java-archive"),
                invokeGetMediaType(baos.toByteArray(), "test.jar"));
    }

    @Test
    public void testGetMediaTypeIoExceptionFallsBackToApplicationXml() throws Exception {
        final InputStream throwing =
                new InputStream() {
                    @Override
                    public int read() throws IOException {
                        throw new IOException("simulated read failure");
                    }
                };
        try (final BufferedInputStream stream = new BufferedInputStream(throwing)) {
            stream.mark(1);
            assertEquals(MediaType.APPLICATION_XML, service.getMediaType(stream, "test.xml"));
        }
    }
}
