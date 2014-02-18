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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Waymire (chris@waymire)
 */
public class  FileUtil {
    private static final Log log = LogFactory.getLog(FileUtil.class);
    private static final List<String> locks = Collections.synchronizedList(new ArrayList<String>());

    public static void write(String file,String content) throws IOException
    {
        write(file,content.getBytes());
    }

    public static void write(String file,byte[] content) throws IOException
    {
        while(locks.contains(file))
        {
            try { Thread.sleep(100); } catch (InterruptedException ie) { }
        }
        locks.add(file);
        try
        {
            write(file);
            InputStream in = new ByteArrayInputStream(content);
            FileOutputStream out =  new FileOutputStream(file);
            IOUtils.copy(in, out);
            in. close();
            out.close();
        } catch(Exception exception) {
            log.error("Unable to write file '" + file + "' to disk.",exception);
        } finally {
            locks.remove(file);
        }
    }


    public static void write(String path) throws IOException
    {
        File f = new File(path);
        if(!f.exists())
        {
            if(f.isDirectory())
            {
                f.mkdirs();
            } else {
                f.getParentFile().mkdirs();
                f.createNewFile();
        }   }
    }

    public static byte[] read(URL url) throws IOException
    {
        if(url == null) return null;
        File file = new File(url.getFile());
        return read(file);
    }

    public static byte[] read(String path) throws IOException
    {
        if(StringUtils.isEmpty(path)) return null;
        File file = new File(path);
        return read(file);
    }

    public static byte[] read(File file) throws IOException
    {
        if(file == null || !file.exists()) return null;
        if(locks.contains(file.getAbsolutePath()))
        {
            try { Thread.sleep(100); } catch (InterruptedException ie) { }
        }

        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            int available = input.available();
            FileChannel channel = input.getChannel();
            ByteBuffer bytes = ByteBuffer.allocate(available);
            channel.read(bytes);
            bytes.flip();
            return bytes.array();
        } finally {
            if (input != null) {
                input.close();
            }
        }

    }

    public static boolean fileExists(String path) {
        File file = new File(path);
        return file.exists();
    }

}
