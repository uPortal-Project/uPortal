package org.apache.xml.serialize;

import java.io.*;

public interface CharacterCachingWriter {

    public boolean startCaching() throws IOException;
    public boolean stopCaching();
    public String getCache(String encoding) throws UnsupportedEncodingException, IOException;
    public void flush() throws IOException;

}
