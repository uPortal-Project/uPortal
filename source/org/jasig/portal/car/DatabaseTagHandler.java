/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.car;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.tools.dbloader.Configuration;
import org.jasig.portal.tools.dbloader.DbLoader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Processes the database tag looking for two element that should each 
 * contain a file path suitable for loading via classloader.findResource().
 * The "tables" tag should point to a file having the appropriate format for
 * creating tables in the portal's database using DbLoader and hence should be
 * structured identically to uPortal's default data structure definition file
 * tables.xml. The "data" tag is optional but if included should point to file 
 * having the appropriate format for loading with DbLoader the tables created 
 * by tables.xml and hence should be structured identically to uPortal's 
 * default data set file data.xml.
 * 
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
public class DatabaseTagHandler
    extends DefaultHandler
{
    private static final Log log = LogFactory.getLog(DatabaseTagHandler.class);
    private boolean CREATE_SCRPT_DFLT = false;
    private boolean POP_TBL_DFLT = true;
    private boolean CREATE_TBL_DFLT = true;
    private boolean DROP_TBL_DFLT = false;
    
    private boolean createScript;
    private boolean populateTables;
    private boolean createTables;
    private boolean dropTables;
    
    private ParsingContext ctx = null;
    private String tables = null;
    private String data = null;
    private StringBuffer chars = null;
        
    /**
     * Construct a DatabaseHandler that receives events from parsing
     * a channel archive deployment descriptor but only for any contained 
     * database elements and their children.
     * 
     * @param ctx
     */
    DatabaseTagHandler( ParsingContext ctx )
    {
        this.ctx = ctx;
        resetValues();
    }

///////////////////// Content Handler Implementations //////////////////    

    /**
     * Handle start element events.
     */
    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
        throws SAXException
    {
        this.chars = new StringBuffer();
    }

    /**
     * Handle the characters event to capture textual content for elements.
     */
    public void characters(char[] ch,
                           int start,
                           int length)
        throws SAXException
    {
        chars.append(ch, start, length);
    }

    /**
     * Handle the closing element event.
     */
    public void endElement(String namespaceURI,
                           String localName,
                           String qName)
        throws SAXException
    {
        if (qName.equals("tables"))
            this.tables = this.chars.toString();
        else if (qName.equals("data"))
            this.data = this.chars.toString();
        else if (qName.equals("drop-tables"))
            this.dropTables = getBoolean(chars.toString(), DROP_TBL_DFLT);
        else if (qName.equals("create-tables"))
            this.createTables = getBoolean(chars.toString(), CREATE_TBL_DFLT);
        else if (qName.equals("populate-tables"))
            this.populateTables = getBoolean(chars.toString(), POP_TBL_DFLT);
        else if (qName.equals("create-script"))
            this.createScript = getBoolean(chars.toString(), CREATE_SCRPT_DFLT);
        else if (qName.equals(DescriptorHandler.DATABASE_TAG_NAME)
            && ctx.getPath().equals(
                DescriptorHandler.DBDEFS))
        {
            // leaving block so run dbloader for acquired tables and data files
            loadDatabase();
                
            // reset all values for next database element if any
            resetValues();
        }
    }

    /**
     * @param string
     * @return
     */
    private boolean getBoolean(String string, boolean deflt)
    {
        if (string == null)
            return deflt;
        if (string.equals("false"))
            return false;
        if (string.equals("true"))
            return true;
        return deflt;
    }

    /**
     * 
     */
    private void resetValues()
    {
        tables = null;
        data = null;
        dropTables = DROP_TBL_DFLT;
        createTables = CREATE_TBL_DFLT;
        createScript = CREATE_SCRPT_DFLT;
        populateTables = POP_TBL_DFLT;
    }

    /**
     * Load the database using DbLoader.
     */
    private void loadDatabase()
    {
        if ( tables == null )
        {
            log.error(
                "A database declaration in a channel deployment descriptor " +
                "must contain a non-empty <tables> element to " +
                "successfully create tables for the channel and populate " +
                "them with data. Declaration ignored.");
                return;
        }
        try
        {
            ClassLoader cl = CarResources.getInstance().getClassLoader();
            URL dataURL = null;
            
            if (data != null)
            {
                dataURL = cl.getResource(data);
                
                if (dataURL == null)
                {
                    log.error(
                        "JAR file = '" + ctx.getJarFile().getName() +
                        "' unable to find specified data file '" + data + "'");
                }
            }
            URL tablesURL = cl.getResource(tables);
            
            if (tablesURL == null)
            {
                log.error(
                    "JAR file = '" + ctx.getJarFile().getName() +
                    "' unable to find specified tables file '" + tables + "'");
            }
            if ((data != null && dataURL == null) || tablesURL == null)
                return;
                
            // set up log buffers for capturing DbLoader output.
            
            Configuration config = new Configuration();
            StringWriter logBfr = new StringWriter();
            PrintWriter logWriter = new PrintWriter(logBfr);
            config.setLog(logWriter);

            StringWriter scriptLog = null;
            
            if (createScript)
            {
                scriptLog = new StringWriter();
                PrintWriter scriptWriter = new PrintWriter(scriptLog);
                config.setScriptWriter(scriptWriter);
            }
            
            // load default config
            DbLoader.loadConfiguration(config);
            
            // set overrides for car table loading
            config.setCreateTables(createTables);
            config.setDropTables(dropTables);
            config.setPopulateTables(populateTables);
            config.setTablesURL(tablesURL);
            config.setDataURL(dataURL);
            
            DbLoader loader = new DbLoader(config);
            try
            {
                loader.process();
                logWriter.flush();
                if (config.getScriptWriter() != null )
                    config.getScriptWriter().flush();
                log.info(
                    "***** Successfully processed *****\n"
                        + tablesURL.toString()
                        + " and \n"
                        + dataURL.toString()
                        + ".\nLogged Output:\n---------------------\n"
                        + logBfr.toString()
                        + (createScript
                            ? "\nDatabase Script:\n---------------------\n"
                                + scriptLog.toString()
                            : ""));
            }
            catch(Exception e)
            {            
                log.error(
                    "***** Failure during processing ***** \n"
                        + tablesURL.toString()
                        + " and \n"
                        + dataURL.toString()
                        + ".\nLogged Output:\n---------------------\n"
                        + logBfr.toString()
                        + (createScript
                            ? "\nDatabase Script:\n---------------------\n"
                                + scriptLog.toString()
                            : ""));
            }
        }
        catch(Exception e)
        {
            log.error(
                "Problem occurred while loading database from CAR.", e );
        }
    }
}
