/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */


package org.jasig.portal.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

public class PropsMatcher {
    Vector patterns;
    Vector ids;
    PatternMatcher matcher;
    
    public PropsMatcher(InputStream is) throws IOException {
        PatternCompiler compiler=new Perl5Compiler();
        matcher=new Perl5Matcher();

        // read in a properties file
        Vector v=readPropertiesVector(is);
        
        patterns=new Vector();
        ids=new Vector(v.size());

        // prepare separate vectors of compiled patterns
        // and mapped ids
        for(int i=0;i<v.size();i++) {
            String[] temp=(String[]) v.elementAt(i);

            // compile a pattern
            try {
                Pattern p=compiler.compile(temp[0]);
                patterns.addElement(p);
                // save the id
                ids.addElement(temp[1]);
            } catch (MalformedPatternException mpe) {
                // omit entry, give warning*
                System.out.println("PropsMatcher::PropsMatcher() : invalid pattern: "+temp[0]);
                System.out.println("PropsMatcher::PropsMatcher() : "+mpe.getMessage());
            }
            

        }        
    }
    
    public String match(String input) {
        // sequentially try to match to every pattern
        for(int i=0;i<patterns.size();i++) {
            if(matcher.matches(input,(Pattern) patterns.elementAt(i))) {
                return (String) ids.elementAt(i);
            }
        }
        return null;
    }
    

    private Vector readPropertiesVector(InputStream inputStream) throws IOException {
        Vector v=new Vector(10);
        BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
        String currentLine, Key = null;
        StringTokenizer currentTokens;
        while ((currentLine = input.readLine()) != null) {
            currentTokens = new StringTokenizer(currentLine, "=\t\r\n");
            if (currentTokens.hasMoreTokens()) {
                Key = currentTokens.nextToken().trim();
            }
            if ((Key != null) && !Key.startsWith("#") && currentTokens.hasMoreTokens()) {
                String temp[] = new String[2];
                temp[0] = Key;
                temp[1] = currentTokens.nextToken().trim();
                v.addElement(temp);
            }
        }
        return v;
    }
}

