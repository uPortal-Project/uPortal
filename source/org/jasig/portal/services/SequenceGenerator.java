package org.jasig.portal.services;

import org.jasig.portal.ISequenceGenerator;
import org.jasig.portal.ISequenceGeneratorFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.properties.PropertiesManager;

/**
 * @author Dan Ellentuck 
 * @version $Revision$
 */
public class SequenceGenerator 
{
    protected ISequenceGenerator m_sequenceGenerator = null;
    private static SequenceGenerator m_instance;
    protected static String s_factoryName = PropertiesManager.getProperty("org.jasig.portal.SequenceGeneratorFactory.implementation");
    protected static ISequenceGeneratorFactory m_Factory = null;
    static 
    {
        // Look for our factory and instantiate an instance.
        if (s_factoryName == null) 
        {
  	        LogService.log(LogService.ERROR, new PortalException
  			("Sequence Provider not specified or incorrect in portal.properties"));
        } 
        else 
        {
            try 
            {
                m_Factory = (ISequenceGeneratorFactory)Class.forName(s_factoryName).newInstance();
            } 
            catch (Exception e) 
            {
                LogService.log(LogService.ERROR, new PortalException
                ("Failed to instantiate " + s_factoryName));
            }
        }
    }	

    /**
     */
    public SequenceGenerator() 
    {
    	m_sequenceGenerator = m_Factory.getSequenceGenerator();	
    }
    
    /**
     * @param name String
     */
    public void createCounter(String name) throws Exception
    {
    	m_sequenceGenerator.createCounter(name);
    }
    
    /**
     * @return String
     */
    public String getNext() throws Exception
    {
    	return m_sequenceGenerator.getNext();
    }
    
    /**
     * @param name String
     * @return String
     */
    public String getNext(String name) throws Exception
    {
        return m_sequenceGenerator.getNext(name);
    }
    
    /**
     * @return int
     */
    public int getNextInt() throws Exception
    {
        return m_sequenceGenerator.getNextInt();
    }
    
    /**
     * @param name String
     * @return int
     */
    public int getNextInt(String name) throws Exception
    {
        return m_sequenceGenerator.getNextInt(name);
    }
    
    /**
     * @return SequenceGenerator
     */
    public final static synchronized SequenceGenerator instance() 
    {
    	if ( m_instance == null )
    		{ m_instance = new SequenceGenerator(); }
    	return m_instance;
    }
    
    /**
     * @param name java.lang.String
     * @param newValue int
     */
    public void setCounter(String name, int newValue) throws Exception
    {
    	m_sequenceGenerator.setCounter(name, newValue);
    }

}
