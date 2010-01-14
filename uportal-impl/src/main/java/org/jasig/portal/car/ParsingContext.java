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

package org.jasig.portal.car;

import java.util.jar.JarFile;

/**
 * A class for holding parsing context information like the current
 * path within the XML being parsed and the jarfile whose deployment
 * descriptor is being parsed.
 *
 * @author Mark Boyd  {@link <a href="mailto:mark.boyd@engineer.com">mark.boyd@engineer.com</a>}
 * @version $Revision$
 */
public class ParsingContext
{
    private JarFile jarFile;
    private Path parsingPath = new Path();

    public ParsingContext(JarFile jarfile)
    {
        this.jarFile = jarfile;    
    }
    
    public Path getPath()
    {
        return parsingPath;
    }
    
    public JarFile getJarFile()
    {
        return jarFile;
    }
}
