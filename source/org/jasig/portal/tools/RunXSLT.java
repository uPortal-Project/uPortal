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

package org.jasig.portal.tools;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jasig.portal.PortalException;
import org.jasig.portal.utils.XSLT;

/**
 * Title:        Run XSLT
 * Description:  applies an xsl stylesheet to an xml file
 * Company:
 * @author  Susan Bramhall
 * $Revision$
 */

public class RunXSLT {

  public static void main(String[] args) {

    File xmlSourceFile = null;
    FileOutputStream ostream = null;
    File XslOutputFile = null;
    String xslUri = null;

    if (args.length < 3) {
      System.err.println("Usage \"runXSLT <xmlSource> <xslUri> <outFile>\"");
      return;
    }
            for (int i = 0; i < args.length; i++) {
              if (!args[i].startsWith("-")) {
                xmlSourceFile = new File(args[i].trim());
                System.out.println("xmlSourceFile is "+xmlSourceFile.getAbsolutePath());
                xslUri = args[++i].trim();
                System.out.println("xslUri is "+xslUri);
                XslOutputFile = new File(args[++i].trim());
                System.out.println("XslOutputFile is "+XslOutputFile.getAbsolutePath());
                }
              }
      XSLT xslt = new XSLT(RunXSLT.class);

      try {
          ostream = new FileOutputStream(XslOutputFile);
          } catch (IOException ioe) {
            System.err.println("Unable to create output file "+ XslOutputFile.getName());
            return;
            }

      try {
        xslt.setXML(xmlSourceFile);
        xslt.setXSL(xslUri);
        xslt.setTarget(ostream);
        xslt.transform();
        } catch (PortalException pe) {
          System.err.println("RunXSLT: Error on transform");
          pe.getRecordedException().printStackTrace();
          }

  }
}
