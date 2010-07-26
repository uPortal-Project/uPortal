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

