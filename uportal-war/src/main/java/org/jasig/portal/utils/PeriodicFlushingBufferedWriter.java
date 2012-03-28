package org.jasig.portal.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Buffered Writer that flushes its buffer when any write/append method is called after a certain time has passed 
 * 
 * @author Eric Dalquist
 */
public class PeriodicFlushingBufferedWriter extends BufferedWriter {
    private final long period;
    private long lastFlush = System.currentTimeMillis();

    public PeriodicFlushingBufferedWriter(long period, Writer out, int sz) {
        super(out, sz);
        this.period = period;
    }

    public PeriodicFlushingBufferedWriter(long period, Writer out) {
        super(out);
        this.period = period;
    }
    
    private void checkForFlush() throws IOException {
        final long now = System.currentTimeMillis();
        if (lastFlush <= (now + period)) {
            lastFlush = now;
            super.flush();
        }
    }

    @Override
    public void write(int c) throws IOException {
        super.write(c);
        checkForFlush();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        super.write(cbuf, off, len);
        checkForFlush();
    }

    @Override
    public void write(String s, int off, int len) throws IOException {
        super.write(s, off, len);
        checkForFlush();
    }

    @Override
    public void newLine() throws IOException {
        super.newLine();
        checkForFlush();
    }

    @Override
    public void write(char[] cbuf) throws IOException {
        super.write(cbuf);
        checkForFlush();
    }

    @Override
    public void write(String str) throws IOException {
        super.write(str);
        checkForFlush();
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        final Writer w = super.append(csq);
        checkForFlush();
        return w;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
        final Writer w = super.append(csq, start, end);
        checkForFlush();
        return w;
    }

    @Override
    public Writer append(char c) throws IOException {
        final Writer w = super.append(c);
        checkForFlush();
        return w;
    }
}
