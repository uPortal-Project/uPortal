package org.apache.xml.serialize;

import java.io.*;

public interface CharacterCachingWriter {

    public boolean startCaching();
    public boolean stopCaching();
    public String getCache(String encoding) throws UnsupportedEncodingException, IOException;

}
