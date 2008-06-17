/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
