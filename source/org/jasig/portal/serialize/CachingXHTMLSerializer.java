package org.jasig.portal.serialize;


import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;


/**
 * Caching version of the XHTML serializer
 * @author Peter Kharchenko
 * @author <a href="mailto:arkin@exoffice.com">Assaf Arkin</a>
 * @see Serializer
 */
public final class CachingXHTMLSerializer
    extends XHTMLSerializer implements CachingSerializer
{

    CharacterCachingWriter cacher;
    String encoding;

    /**
     * Constructs a new serializer. The serializer cannot be used without
     * calling {@link #setOutputCharStream} or {@link #setOutputByteStream}
     * first.
     */
    public CachingXHTMLSerializer()
    {
        super(new OutputFormat( Method.XHTML, null, false ) );
    }


    /**
     * Constructs a new serializer. The serializer cannot be used without
     * calling {@link #setOutputCharStream} or {@link #setOutputByteStream}
     * first.
     */
    public CachingXHTMLSerializer( OutputFormat format )
    {
        super(format != null ? format : new OutputFormat( Method.XHTML, null, false ) );
        this.encoding=format.getEncoding();
    }


    /**
     * Constructs a new serializer that writes to the specified writer
     * using the specified output format. If <tt>format</tt> is null,
     * will use a default output format.
     *
     * @param writer The writer to use
     * @param format The output format to use, null for the default
     */
    public CachingXHTMLSerializer( Writer writer, OutputFormat format )
    {
        super(format != null ? format : new OutputFormat( Method.XHTML, null, false ) );
        CachingWriter cw=new CachingWriter(writer);
        this.cacher=cw;
        setOutputCharStream(cw);
        this.encoding=format.getEncoding();
    }

    public void setOutputCharStream( Writer writer ) {
        CachingWriter cw=new CachingWriter(writer);
        this.cacher=cw;
        super.setOutputCharStream(cw);
    }

    /**
     * Constructs a new serializer that writes to the specified output
     * stream using the specified output format. If <tt>format</tt>
     * is null, will use a default output format.
     *
     * @param output The output stream to use
     * @param format The output format to use, null for the default
     */
    public CachingXHTMLSerializer( OutputStream output, OutputFormat format )
    {
        super(format != null ? format : new OutputFormat( Method.XHTML, null, false ) );
        CachingOutputStream cos=new CachingOutputStream(output);
        this.cacher=cos;
        setOutputByteStream( cos );
        this.encoding=format.getEncoding();
    }

    public void setOutputByteStream( OutputStream output ) {
        CachingOutputStream cos=new CachingOutputStream(output);
        this.cacher=cos;
        super.setOutputByteStream(cos);
    }

    public void setOutputFormat( OutputFormat format )
    {
        super.setOutputFormat( format != null ? format : new OutputFormat( Method.XHTML, null, false ) );
        this.encoding=format.getEncoding();
    }

    // caching methods
    public boolean startCaching() throws IOException{
        _printer.flush();
        return cacher.startCaching();
    }
    public boolean stopCaching() throws IOException {
        _printer.flush();
        return cacher.stopCaching(); 
    }

    public String getCache() throws UnsupportedEncodingException, IOException {
        _printer.flush();
        return cacher.getCache(this.encoding);
    }
    
    /**
     * Allows one to print a <code>String</code> of characters directly to the output stream.
     *
     * @param text a <code>String</code> value
     */
    public void printRawCharacters(String text) throws IOException{
        content();
        _printer.printText(text);
        //        _printer.flush();
    }

    /**
     * Let the serializer know if the document has already been started.
     *
     * @param setting a <code>boolean</code> value
     */
    public void setDocumentStarted(boolean setting) {
        _started=setting;
    }

    public void flush() throws IOException {
        _printer.flush();
        cacher.flush();
    }
        
}
