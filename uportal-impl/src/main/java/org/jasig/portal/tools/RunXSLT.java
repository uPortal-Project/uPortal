/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
      XSLT xslt = XSLT.getTransformer(RunXSLT.class);

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
          pe.printStackTrace();
          }

  }
}
