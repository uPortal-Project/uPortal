package org.jasig.portal.channels.sqlquery;

import java.util.Map;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.RDBMServices;
import org.jasig.portal.channels.CAbstractXslt;
import org.jasig.portal.properties.PropertiesManager;
import org.w3c.dom.Document;

/**
 * This channel executes a (configurable) SQL query against a (configurable)
 * DataSource accessed via JNDI, translates the ResultSet into XML
 * like that in data.xml, and feeds that XML to a (configurable) XSLT.
 * 
 * This channel is useful for exposing dashboard components with relatively
 * low usage.  It does not presently implement caching and so is not suitable
 * for high volume use.
 * 
 * This channel is eminently useful for simple administrative queries.
 * 
 * Potentially useful future enhancements of this channel might include an
 * an ability to bind user attributes to parameters of the query.
 */
public class CSqlQuery extends CAbstractXslt {

    /**
     * The JNDI name of the DataSource against which this channel will
     * execute the SQL query is specified as a channel parameter named 
     * "dataSource".  This parameter is optional, defaulting to the uPortal 
     * DataSource (PortalDb).
     */
    public static final String DATASOURCE_JNDI_NAME_PARAM_NAME = "dataSource";
    
    /**
     * The SQL query this channel will execute is specified as a channel
     * parameter named "sql".  This parameter is required.
     */
    public static final String SQL_QUERY_PARAM_NAME = "sql";
    
    /**
     * The URI of the XSLT this channel should use to render the XML
     * is specified as a channel parameter named "xsltUri".  This parameter
     * is optional.
     */
    public static final String XSLT_URI_PARAM_NAME = "xsltUri";
    
    private String xsltUri = "CSqlQuery/sqlquery.xsl";
    
    SqlToXml sqlToXml;
    
    protected final void staticDataSet() {
        ChannelStaticData csd = getStaticData();
        String sqlQuery = csd.getParameter(SQL_QUERY_PARAM_NAME);
        String datasourceJndiName = csd.getParameter(DATASOURCE_JNDI_NAME_PARAM_NAME);
        
        if (datasourceJndiName == null) {
            datasourceJndiName = PropertiesManager.getProperty("org.jasig.portal.RDBMServices.PortalDatasourceJndiName", "PortalDb");
        }
        
        DataSource ds = RDBMServices.getDataSource(datasourceJndiName);
        
        sqlToXml = new SqlToXml(ds, sqlQuery);
        
        this.xsltUri = csd.getParameter(XSLT_URI_PARAM_NAME);
        
        
    
    }
    
    protected Document getXml() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        this.sqlToXml.populateDomFromSqlQuery(doc);
        
        return doc;
    }

    protected String getXsltUri() throws Exception {
        return this.xsltUri;
    }

    protected Map getStylesheetParams() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    public void receiveEvent(PortalEvent ev) {
        // does not currently handle events
    }

}
