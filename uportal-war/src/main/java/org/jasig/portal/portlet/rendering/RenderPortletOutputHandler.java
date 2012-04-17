package org.jasig.portal.portlet.rendering;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.output.WriterOutputStream;

/**
 * PortletOutputHandler that stores all content written to a string. Requires that
 * an encoding be specified in case the portlet writes to the output stream.
 * 
 * @author Eric Dalquist
 */
public class RenderPortletOutputHandler implements PortletOutputHandler {
    private final StringBuilderWriter writer = new StringBuilderWriter();
    private final PrintWriter printWriter = new PrintWriter(this.writer);
    private final String characterEncoding;
    private OutputStream writerOutputStream;

    private int bufferSize = Integer.MAX_VALUE;
    private String contentType;
    
    public RenderPortletOutputHandler(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }
    
    /**
     * @return The output the portlet has written, calls {@link #flushBuffer()} first
     */
    public String getOutput() {
        this.flushBuffer();
        return writer.getBuilder().toString();
    }

    /**
     * @return The content-type the portlet set
     */
    public String getContentType() {
        return contentType;
    }

    @Override
    public PrintWriter getPrintWriter() {
        return this.printWriter;
    }

    @Override
    public OutputStream getOutputStream() {
        if (this.writerOutputStream == null) {
            this.writerOutputStream = new WriterOutputStream(this.printWriter, this.characterEncoding);
        }
        
        return writerOutputStream;
    }

    @Override
    public void flushBuffer() {
        if (this.writerOutputStream != null) {
            try {
                this.writerOutputStream.flush();
            }
            catch (IOException e) {
                //No way the output stream can throw an IOE here
            }
        }
        this.printWriter.flush();
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {
        this.contentType = null;
        
        this.resetBuffer();
    }

    @Override
    public void resetBuffer() {
        this.flushBuffer();
        final StringBuilder builder = this.writer.getBuilder();
        if (builder.length() > 0) {
            builder.delete(0, builder.length());
        }
    }

    @Override
    public void setBufferSize(int size) {
        //Ignore, we can always buffer the max string length
    }

    
    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
