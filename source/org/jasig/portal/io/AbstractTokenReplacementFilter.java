/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.io;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang.Validate;

/**
 * Abstract token replacement filter. Wraps a Writer and can replace tokens as described
 * by the configured prefix and suffix with the value provided by the sub-class for the
 * parsed token.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractTokenReplacementFilter extends Writer {
    private final StringBuilder prefixBuffer;
    private final StringBuilder tokenBuffer;
    private final StringBuilder suffixBuffer;
    
    private final char[] resetBuffer;
    
    private final Writer wrappedWriter;
    private final String prefix;
    private final int maxTokenLength;
    private final String suffix;

    /**
     * @param wrappedWriter Writer to delegate writing to.
     */
    public AbstractTokenReplacementFilter(Writer wrappedWriter, String prefix, int maxTokenLength, String suffix) {
        Validate.notNull(wrappedWriter, "wrappedWriter can not be null");
        Validate.notNull(prefix, "Replacement prefix can not be null");
        Validate.notNull(suffix, "Replacement suffix can not be null");
        
        if (prefix.length() <= 0) {
            throw new IllegalArgumentException("prefix must be at least one character");
        }
        if (maxTokenLength <= 0) {
            throw new IllegalArgumentException("maxTokenLength must be greater than zero (was " + maxTokenLength + ")");
        }
        if (suffix.length() <= 0) {
            throw new IllegalArgumentException("suffix must be at least one character");
        }
        
        this.wrappedWriter = wrappedWriter;
        this.prefix = prefix;
        this.maxTokenLength = maxTokenLength;
        this.suffix = suffix;
        
        this.prefixBuffer = new StringBuilder(this.prefix.length());
        this.tokenBuffer = new StringBuilder(this.maxTokenLength);
        this.suffixBuffer = new StringBuilder(this.suffix.length());
        
        this.resetBuffer = new char[this.prefix.length() + this.maxTokenLength + this.suffix.length()];
    }
    
    /**
     * Called when a token is found and needs to be replaced.
     * 
     * @param token The found token (text between prefix and suffix)
     * @return The string to replace the prefix + token + suffix with.
     */
    protected abstract String replaceToken(String token);
    
    /* (non-Javadoc)
     * @see java.io.Writer#close()
     */
    @Override
    public final void close() throws IOException {
        if (this.prefixBuffer.length() > 0) {
            this.wrappedWriter.append(this.prefixBuffer);
        }
        
        if (this.tokenBuffer.length() > 0) {
            this.wrappedWriter.append(this.tokenBuffer);
        }
        
        if (this.suffixBuffer.length() > 0) {
            this.wrappedWriter.append(this.suffixBuffer);
        }
        
        this.wrappedWriter.close();
    }

    /* (non-Javadoc)
     * @see java.io.Writer#flush()
     */
    @Override
    public final void flush() throws IOException {
        this.wrappedWriter.flush();
    }
    
    /* (non-Javadoc)
     * @see java.io.Writer#write(char[], int, int)
     */
    @Override
    public final void write(char[] cbuf, int off, int len) throws IOException {
        for (int charIndex = off; charIndex < (off + len); charIndex++) {
            this.write(cbuf[charIndex]);
        }
    }

    /* (non-Javadoc)
     * @see java.io.Writer#write(int)
     */
    @Override
    public final void write(int c) throws IOException {
        final char writerChar = (char)c;
        
        final int prefixBufferLength = this.prefixBuffer.length();
        //Prefix buffer is full, must be looking for token or suffix
        if (this.prefix.length() == prefixBufferLength) {
            
            final int suffixBufferLength = this.suffixBuffer.length();
            //No token chars, there must always be at least one
            if (this.tokenBuffer.length() == 0) {
                this.tokenBuffer.append(writerChar);
            }
            //Something in the token buffer, check against suffix
            else {
                //Found a suffix character
                if (this.suffix.charAt(suffixBufferLength) == writerChar) {
                    this.suffixBuffer.append(writerChar);
                    
                    //If the suffix buffer is full do the title replacement and clear the buffers.
                    if (this.suffix.length() == this.suffixBuffer.length()) {
                        final String token = this.tokenBuffer.toString();
                        final String tokenReplacement = this.replaceToken(token);
                        this.wrappedWriter.write(tokenReplacement);
                        
                        this.prefixBuffer.delete(0, this.prefixBuffer.length());
                        this.tokenBuffer.delete(0, this.tokenBuffer.length());
                        this.suffixBuffer.delete(0, this.suffixBuffer.length());
                    }
                }
                //Not a suffix character
                else {
                    //token is too long, must not be a token, re-parse all but the first char of the prefix buffer
                    final int tokenBufferLength = this.tokenBuffer.length();
                    if (this.maxTokenLength == tokenBufferLength) {
                        //Write the first char from the prefix out to the writer
                        final char deadChar = this.prefixBuffer.charAt(0);
                        this.wrappedWriter.write(deadChar);
                        
                        //Copy the rest of the prefix buffer so it can be parsed from the 2nd char on with a clean buffer
                        this.prefixBuffer.getChars(1, prefixBufferLength, this.resetBuffer, 0);
                        this.prefixBuffer.delete(0, prefixBufferLength);
                        
                        this.tokenBuffer.getChars(0, tokenBufferLength, this.resetBuffer, prefixBufferLength - 1);
                        this.tokenBuffer.delete(0, tokenBufferLength);
                        
                        this.resetBuffer[tokenBufferLength + prefixBufferLength - 1] = writerChar;
                        this.write(this.resetBuffer, 0, tokenBufferLength + prefixBufferLength);
                    }
                    //Chars in the suffix buffer, need to re-parse all be the first.
                    else  if (suffixBufferLength > 0) {
                        //Write the first char from the suffix to the token buffer
                        final char tokenChar = this.suffixBuffer.charAt(0);
                        this.tokenBuffer.append(tokenChar);
                        
                        //Copy the rest of the suffix buffer so it can be parsed from the 2nd char on with a clean buffer
                        this.suffixBuffer.getChars(1, suffixBufferLength, this.resetBuffer, 0);
                        this.suffixBuffer.delete(0, suffixBufferLength);
                        
                        this.resetBuffer[suffixBufferLength - 1] = writerChar;
                        this.write(this.resetBuffer, 0, suffixBufferLength);
                    }
                    //Nothing in the suffix buffer, must be part of the token
                    else {
                        this.tokenBuffer.append(writerChar);
                    }
                }
            }
        }
        //Found a prefix char
        else if (this.prefix.charAt(prefixBufferLength) == writerChar) {
            this.prefixBuffer.append(writerChar);
        }
        //Char doesn't match anything
        else {
            //Chars in the prefix buffer need to re-parse all but the first
            if (prefixBufferLength > 0) {
                //Write the first char from the prefix out to the writer
                final char deadChar = this.prefixBuffer.charAt(0);
                this.wrappedWriter.write(deadChar);
                
                //Copy the rest of the prefix buffer so it can be parsed from the 2nd char on with a clean buffer
                this.prefixBuffer.getChars(1, prefixBufferLength, this.resetBuffer, 0);
                this.prefixBuffer.delete(0, prefixBufferLength);
                
                this.resetBuffer[prefixBufferLength - 1] = writerChar;
                this.write(this.resetBuffer, 0, prefixBufferLength);
            }
            //Nothing in the prefix buffer, must be a normal char
            else {
                this.wrappedWriter.write(writerChar);
            }
        }
    }
}
