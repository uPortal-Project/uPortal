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

package org.jasig.portal.tools.dbloader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.utils.XSLT;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * <p>A tool to set up a uPortal database. This tool was created so that uPortal
 * developers would only have to maintain a single set of xml documents to define
 * the uPortal database schema and data.  Previously it was necessary to maintain
 * different scripts for each database we wanted to support.</p>
 *
 * <p>DbLoader reads the generic types that are specified in tables.xml and
 * tries to map them to local types by querying the database metadata via methods
 * implemented by the JDBC driver.  Fallback mappings can be supplied in
 * dbloader.xml for cases where the JDBC driver is not able to determine the
 * appropriate mapping.  Such cases will be reported to standard out.</p>
 *
 * <p>An xsl transformation is used to produce the DROP TABLE and CREATE TABLE
 * SQL statements. These statements can be altered by modifying tables.xsl</p>
 *
 * <p>Generic data types (as defined in java.sql.Types) which may be specified
 * in tables.xml include:
 * <code>BIT, TINYINT, SMALLINT, INTEGER, BIGINT, FLOAT, REAL, DOUBLE,
 * NUMERIC, DECIMAL, CHAR, VARCHAR, LONGVARCHAR, DATE, TIME, TIMESTAMP,
 * BINARY, VARBINARY, LONGVARBINARY, NULL, OTHER, JAVA_OBJECT, DISTINCT,
 * STRUCT, ARRAY, BLOB, CLOB, REF, DATALINK, BOOLEAN</code>
 *
 * <p><strong>WARNING: YOU MAY WANT TO MAKE A BACKUP OF YOUR DATABASE BEFORE RUNNING DbLoader</strong></p>
 *
 * <p>DbLoader will perform the following steps:
 * <ol>
 * <li>Read configurable properties from dbloader.xml</li>
 * <li>Get database connection from RDBMServices
 *     (reads JDBC database settings from rdbm.properties).</li>
 * <li>Read tables.xml and issue corresponding DROP TABLE and CREATE TABLE SQL statements.</li>
 * <li>Read data.xml and issue corresponding INSERT/UPDATE/DELETE SQL statements.</li>
 * </ol>
 * </p>
 *
 * @author Ken Weiner, kweiner@unicon.net
 * @author Mark Boyd, mboyd@sungardsct.com
 * @version $Revision$
 * @see java.sql.Types
 * @since uPortal 2.0
 */
public class DbLoader
{
    private Configuration config = null;
    private static final Log log = LogFactory.getLog(DbLoader.class);

  public DbLoader(Configuration c)
  {
      this.config = c;
  }
  
  /**
   * Creates a default DbLoader with no configuration object installed. Before
   * DbLoader can work it must have a configuration object set.
   *
   */
  public DbLoader()
  {
  }
  
  /**
   * Set the configuration object to govern DbLoader's behavior.
   * @param c
   */
  public void setConfig(Configuration c)
  {
      this.config = c;
  }
  
  public static void main(String[] args)
  {
    Configuration config = new Configuration();
    try
    {
        // read dbloader.xml properties
        loadConfiguration(config);
        // read command line arguements to override properties in dbloader.xml
        readOverrides(config, args);
        
        // create the script file if indicated
        if (config.getCreateScript())
          initScript(config);

        // instantiate loader and run
        DbLoader loader = new DbLoader(config);
        loader.process();
    }
    catch (Exception e)
    {
        e.printStackTrace(config.getLog());
    }
    finally
        // call local exit method to clean up.  This does not actually
        // do a system.exit() allowing a stack trace to the console in
        // the case of a run time error.
        {
        exit(config);
    }
    config.getLog().flush();
    
    if (config.getScriptWriter() != null)
        config.getScriptWriter().flush();
  }

  public void process()
      throws
          SQLException,
          PortalException,
          IOException,
          SAXException,
          ParserConfigurationException
    {
        try
        {
            config.setConnection(RDBMServices.getConnection());
    
          if (config.getConnection() == null)
          {
              config.getLog().println(
                  "DbLoader couldn't obtain a database connection.  " +
                  "See the portal log for details.");
              return;
          }
           long startTime = System.currentTimeMillis();

            DbUtils.logDbInfo(config);
          
            if ( config.getDataURL() == null )
                config.setDataURL(DbLoader.class.getResource(config.getDataUri()));

            // okay, start processing
            // get tablesURL and dataURL here
            if ( config.getTablesURL() == null)
              config.setTablesURL(DbLoader.class.getResource(config.getTablesUri()));
            
            config.getLog().println("Getting tables from: "+config.getTablesURL());
            config.getLog().println("Getting data from: "+config.getDataURL());

            DocumentBuilder domParser = null;

            // get a dom parser for handling tables.xml and/or indexes.xml 
            try
            {
                // Read tables.xml
                DocumentBuilderFactory dbf = null;
                dbf=DocumentBuilderFactory.newInstance();
                domParser = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException pce) {
                config.getLog().println(
                    "Unable to instantiate DOM parser. Pease check your JAXP " +
                    "configuration.");
                pce.printStackTrace(config.getLog());
                return;
            }

            // load tables.xml doc if we are creating or dropping tables or
            // we are populating tables otherwise skip.
            try
            {
                // Eventually, write and validate against a DTD
                //domParser.setFeature ("http://xml.org/sax/features/validation", true);
                //domParser.setEntityResolver(new DTDResolver("tables.dtd"));

                //tablesURL = DbLoader.class.getResource(Configuration.properties.getTablesUri());
                if (config.getCreateTables()
                    || config.getDropTables()
                    || config.getPopulateTables())
                    config.setTablesDoc(
                        domParser.parse(
                            new InputSource(config.getTablesURL().openStream())));
            }
            catch(Exception e)
            {
                config.getLog().println(
                    "Could not process tablesURL '" + config.getTablesURL() + "'");
                e.printStackTrace(config.getLog());

                return;
            }

            // Hold on to tables xml with generic types for populating tables
            if (config.getPopulateTables())
                config.setGenericTablesDoc(
                    (Document) config.getTablesDoc().cloneNode(true));

            // drop and create tables if indicated
            if (config.getCreateTables() || config.getDropTables())
            {
                // Replace all generic data types with local data types
                DomUtils.replaceDataTypes(config, config.getTablesDoc());

                // tables.xml + tables.xsl --> DROP TABLE and CREATE TABLE sql statements
                XSLT xslt = new XSLT(this);
                xslt.setXML(config.getTablesDoc());
                xslt.setXSL(config.getTablesXslUri());
                xslt.setTarget(new TableHandler(config));
                xslt.transform();
            }
            else
            {
                config.getLog().println();
                config.getLog().println("Dropping tables and Creating tables...Disabled");
                config.getLog().println();
            }
            
            // populate tables if indiicated
            // data.xml --> INSERT sql statements

            if ( config.getPopulateTables() )
            {
                config.getLog().println("Populating tables...");
                XMLReader parser = getXMLReader();
                DataHandler dataHandler = new DataHandler(config);
                parser.setContentHandler(dataHandler);
                parser.setErrorHandler(dataHandler);
                parser.parse(new InputSource(config.getDataURL().openStream()));
            }
            else
                config.getLog().println("Populating tables...disabled.");

            // cleanup and exit        
            config.getConnection().commit();
            config.getLog().println("Done!");
            long endTime = System.currentTimeMillis();
            config.getLog().println(
                "Elapsed time: " + ((endTime - startTime) / 1000f) + " seconds");
        }
        finally
        {
            RDBMServices.releaseConnection(config.getConnection());
        }          
    }
  
    public static void loadConfiguration(Configuration config)
      throws ParserConfigurationException, SAXException, IOException
    {
        PropertiesHandler handler = new PropertiesHandler(config);
        config.setPropertiesURL(
            DbLoader.class.getResource("/properties/db/dbloader.xml"));
        // Read in the dbloader properties
        XMLReader parser = getXMLReader();
        parser.setContentHandler(handler);
        parser.setErrorHandler(handler);
        handler.properties.getLog().print("Parsing " + handler.properties.getPropertiesURL() + "...");
        parser.parse(new InputSource(handler.properties.getPropertiesURL().openStream()));
    }
  
  /**
     * @param config
     */
    private static void readOverrides(Configuration config, String[] args)
        throws MalformedURLException
    {
        boolean usetable = false;
        boolean useDataUri  = false;
        boolean useDataFile  = false;

        for (int i = 0; i < args.length; i++) {
           //System.out.println("args["+i+"]: "+args[i]);
           if (!args[i].startsWith("-")) {
              if (usetable) {
                 config.setTablesUri(args[i]);
                 usetable=false;
              } else if (useDataUri) {
                 config.setDataUri(args[i]);
                 config.setDataURL(DbLoader.class.getResource(config.getDataUri()));
                 useDataUri=false;
              } else if (useDataFile) {
                  URL url = getDataFileUri(args[i]);
                 config.setDataUri(url.toString());
                 config.setDataURL(url);
                 useDataFile=false;
              }
           } else if (args[i].equals("-t")) {
              usetable = true;
           } else if (args[i].equals("-d")) {
              useDataUri= true;
           } else if (args[i].equals("-df")) {
              useDataFile= true;
           } else if (args[i].equals("-c")) {
              config.setCreateScript(true);
           } else if (args[i].equals("-nc")) {
               config.setCreateScript(false);
           } else if (args[i].equals("-D")) {
               config.setDropTables(true);
           } else if (args[i].equals("-nD")) {
               config.setDropTables(false);
           } else if (args[i].equals("-C")) {
               config.setCreateTables(true);
           } else if (args[i].equals("-nC")) {
               config.setCreateTables(false);
           } else if (args[i].equals("-P")) {
               config.setPopulateTables(true);
           } else if (args[i].equals("-nP")) {
               config.setPopulateTables(false);
           } else {
           }
        }
   }

    /**
     * @param file
     * @return
     */
    private static URL getDataFileUri(String file)
        throws IllegalArgumentException
    {
        File f = new File(file);
        if (!f.exists())
        {
            throw new IllegalArgumentException(
                "File specified '" + file + "' not found.");
        }
        URL url = null;
        try
        {
            url = f.toURL();
        }
        catch(MalformedURLException mue)
        {
            throw new IllegalArgumentException(
                "File specified '" + file + "' can not be converted to a URL for loading.");
        }
        return url;
    }

    private static XMLReader getXMLReader()
        throws SAXException, ParserConfigurationException
  {
      SAXParserFactory spf=SAXParserFactory.newInstance();
      return spf.newSAXParser().getXMLReader();
  }

  private static void initScript(Configuration config) throws java.io.IOException
  {
    String scriptFileName = config.getScriptFileName();
    File scriptFile = new File(scriptFileName);
    if (scriptFile.exists())
      scriptFile.delete();
    scriptFile.createNewFile();
    config.getLog().println("Generating script file " + scriptFile.getAbsolutePath());
    config.setScriptWriter(new PrintWriter(new BufferedWriter(new FileWriter(scriptFileName, true))));
  }

  static void exit(Configuration config)
  {
    if (config.getScriptWriter() != null)
        config.getScriptWriter().close();
  }
}
