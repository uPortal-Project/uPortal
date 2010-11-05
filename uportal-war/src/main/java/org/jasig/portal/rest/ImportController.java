package org.jasig.portal.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.danann.cernunnos.ReturnValueImpl;
import org.danann.cernunnos.Task;
import org.danann.cernunnos.TaskRequest;
import org.danann.cernunnos.TaskResponse;
import org.danann.cernunnos.runtime.RuntimeRequestResponse;
import org.danann.cernunnos.runtime.ScriptRunner;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ImportController {

    final Log log = LogFactory.getLog(getClass());
    
    private DataSource portalDb;
    
    @Required
    @Resource(name="PortalDb")
    public void setPortalDb(DataSource portalDb) {
        this.portalDb = portalDb;
    }
    
    private ApplicationContext applicationContext;
    
    @Autowired(required = true)
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    private JpaTransactionManager transactionManager;
    
    @Required
    @Resource(name="transactionManager")
    public void setTransactionManager(JpaTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    private Map<String, Task> exportTasks;
    
    @Required
    @Resource(name="exportTasks")
    public void setExportTasks(Map<String, Task> exportTasks) {
        this.exportTasks = exportTasks;
    }
    
    @RequestMapping(value="/import", method = RequestMethod.POST)
    public void importXml(@RequestParam("entityFile") MultipartFile entityFile, HttpServletResponse response) throws DocumentException, IOException {
        log.info("successfully reached import servlet");
        Document doc = new org.dom4j.io.SAXReader().read(entityFile.getInputStream());
        System.out.println("successfully parsed uploaded document\n" + doc.asXML());

        // build the task request
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put("PORTAL_CONTEXT", applicationContext);
        attrs.put("SqlAttributes.DATA_SOURCE", portalDb);
        attrs.put("SqlAttributes.TRANSACTION_MANAGER", transactionManager);
        attrs.put("Attributes.NODE", doc.getRootElement());        
        TaskRequest taskRequest = new RuntimeRequestResponse(attrs);

        // determine the full path to the relevant crn import script
        String scriptName = doc.getRootElement().attributeValue("script");
        String scriptLocation = this.getClass().getClassLoader().getResource(scriptName.substring("classpath:/".length())).toExternalForm();

        // execute the script
        System.out.println("Running task for " + scriptLocation);
        ScriptRunner runner = new ScriptRunner();
        System.out.println(runner.evaluate(scriptLocation, taskRequest));
        
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @RequestMapping(value="/export/channel/{channelId}", method = RequestMethod.GET)
    public void exportChannel(@PathVariable("channelId") String channelId,
            HttpServletRequest request, HttpServletResponse response)
            throws DocumentException, IOException {        
        
        Task task = exportTasks.get("channel");
        Map<String, Object> attributes = Collections
                .<String, Object> singletonMap("FNAME", channelId);
        
        exportEntityAsXml(task, attributes, request, response);
    }
    
    @RequestMapping(value="/export/layout/{userId}", method = RequestMethod.GET)
    public void exportLayout(@PathVariable("userId") String userId,
            HttpServletRequest request, HttpServletResponse response)
            throws DocumentException, IOException {        
        
        Task task = exportTasks.get("layout");
        Map<String, Object> attributes = Collections
                .<String, Object> singletonMap("USER_NAME", userId);
        
        exportEntityAsXml(task, attributes, request, response);
    }
    
    @RequestMapping(value="/export/profile/{userId}", method = RequestMethod.GET)
    public void exportProfile(@PathVariable("userId") String userId,
            HttpServletRequest request, HttpServletResponse response)
            throws DocumentException, IOException {        
        
        Task task = exportTasks.get("profile");
        Map<String, Object> attributes = Collections
                .<String, Object> singletonMap("USER_NAME", userId);
        
        exportEntityAsXml(task, attributes, request, response);
    }
    
    @RequestMapping(value="/export/channelType/{channelTypeId}", method = RequestMethod.GET)
    public void exportChannelType(@PathVariable("channelTypeId") String channelTypeId,
            HttpServletRequest request, HttpServletResponse response)
            throws DocumentException, IOException {        
        
        String scriptLocation = this.getClass().getClassLoader().getResource(
            "/org/jasig/portal/io/export-channel-type.crn").toExternalForm();
        ScriptRunner runner = new ScriptRunner();
        Task task = runner.compileTask(scriptLocation);
        
        Map<String, Object> attributes = Collections
                .<String, Object> singletonMap("NAME", channelTypeId);
        
        exportEntityAsXml(task, attributes, request, response);
    }
    
    @RequestMapping(value="/export/group/{groupId}", method = RequestMethod.GET)
    public void exportGroup(@PathVariable("groupId") String groupId,
            HttpServletRequest request, HttpServletResponse response)
            throws DocumentException, IOException {        
        
        Task task = exportTasks.get("group");
        Map<String, Object> attributes = Collections
                .<String, Object> singletonMap("GROUP_NAME", groupId);
        
        exportEntityAsXml(task, attributes, request, response);
    }
        
    @RequestMapping(value="/export/user/{userId}", method = RequestMethod.GET)
    public void exportUser(@PathVariable("userId") String userId,
            HttpServletRequest request, HttpServletResponse response)
            throws DocumentException, IOException {        
        
        String scriptLocation = this.getClass().getClassLoader().getResource(
                "/org/jasig/portal/io/export-user.crn").toExternalForm();
        ScriptRunner runner = new ScriptRunner();
        Task task = runner.compileTask(scriptLocation);
        
        Map<String, Object> attributes = Collections
                .<String, Object> singletonMap("USER_NAME", userId);
        
        exportEntityAsXml(task, attributes, request, response);
    }
    
    @RequestMapping(value="/export/theme/{themeId}", method = RequestMethod.GET)
    public void exportTheme(@PathVariable("themeId") String themeId,
            HttpServletRequest request, HttpServletResponse response)
            throws DocumentException, IOException {        
        
        Task task = exportTasks.get("theme");
        Map<String, Object> attributes = Collections
                .<String, Object> singletonMap("NAME", themeId);
        
        exportEntityAsXml(task, attributes, request, response);
    }
    
    @RequestMapping(value="/export/structure/{structureId}", method = RequestMethod.GET)
    public void exportStructure(@PathVariable("structureId") String channelId,
            HttpServletRequest request, HttpServletResponse response)
            throws DocumentException, IOException {        
        
        Task task = exportTasks.get("structure");
        Map<String, Object> attributes = Collections
                .<String, Object> singletonMap("NAME", channelId);
        
        exportEntityAsXml(task, attributes, request, response);
    }

    @RequestMapping(value="/export/entityType/{entityTypeId}", method = RequestMethod.GET)
    public void exportEntityType(@PathVariable("entityTypeId") String channelId,
            HttpServletRequest request, HttpServletResponse response)
            throws DocumentException, IOException {        
        
        Task task = exportTasks.get("entity-type");
        Map<String, Object> attributes = Collections
                .<String, Object> singletonMap("NAME", channelId);
        
        exportEntityAsXml(task, attributes, request, response);
    }
    
    protected TaskRequest getExportTaskRequest(Map<String, Object> additionalAttributes) {
        Map<String, Object> attrs = new HashMap<String, Object>();
        attrs.put("PORTAL_CONTEXT", applicationContext);
        attrs.put("SqlAttributes.DATA_SOURCE", portalDb);
        attrs.put("SqlAttributes.TRANSACTION_MANAGER", transactionManager);
        
        if (additionalAttributes != null) {
            attrs.putAll(additionalAttributes);
        }
        
        RuntimeRequestResponse taskRequest = new RuntimeRequestResponse(attrs);
        return taskRequest;
    }
    
    protected void exportEntityAsXml(Task task, Map<String, Object> attributes,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        TaskRequest taskRequest = getExportTaskRequest(attributes);

        ReturnValueImpl rslt = new ReturnValueImpl();
        TaskResponse taskResponse = new RuntimeRequestResponse(Collections
                .<String, Object> singletonMap("Attributes.RETURN_VALUE", rslt));
        
        task.perform(taskRequest, taskResponse);
        
        Node node = (Node) rslt.getValue();
        
        org.dom4j.io.OutputFormat fmt = new org.dom4j.io.OutputFormat("    ", true);
        PrintWriter writer = response.getWriter();
        XMLWriter xmlWriter = new XMLWriter(writer, fmt);;
        xmlWriter.write(node);
        writer.flush();
        xmlWriter.close();

    }
    
}
