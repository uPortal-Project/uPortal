/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import junit.framework.TestCase;

/**
 * Testcase for ExceptionHelper.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 */
public class ExceptionHelperTest extends TestCase {

    /**
     * A trace which requires no trimming.
     */
    private String reallyBasicTrace = "Programming error\n" +
    "   org.jasig.portal.PortalException: java.lang.RuntimeException: java.lang.NullPointerException [based on exception: java.lang.NullPointerException]\n" +
    "   at org.jasig.portal.UserInstance.renderState(UserInstance.java:600)\n" +
    "   at org.jasig.portal.UserInstance.writeContent(UserInstance.java:174)\n" +
    "   at org.jasig.portal.PortalSessionManager.doGet(PortalSessionManager.java:234)\n";
    
    /**
     * An example raw stack trace which has only one throwable.
     */
    private String basicTrace = "Programming error\n" +
            "   org.jasig.portal.PortalException: java.lang.RuntimeException: java.lang.NullPointerException [based on exception: java.lang.NullPointerException]\n" +
            "   at org.jasig.portal.UserInstance.renderState(UserInstance.java:600)\n" +
            "   at org.jasig.portal.UserInstance.writeContent(UserInstance.java:174)\n" +
            "   at org.jasig.portal.PortalSessionManager.doGet(PortalSessionManager.java:234)\n" +
            "   at javax.servlet.http.HttpServlet.service(HttpServlet.java:697)\n" +
            "   at javax.servlet.http.HttpServlet.service(HttpServlet.java:810)\n" +
            "   at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:237)\n" +
            "   at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:157)\n" +
            "   at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:214)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:104)\n" +
            "   at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:520)\n" +
            "   at org.apache.catalina.core.StandardContextValve.invokeInternal(StandardContextValve.java:198)\n" +
            "   at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:152)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:104)\n" +
            "   at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:520)\n" +
            "   at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:137)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:104)\n" +
            "   at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:117)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:102)\n" +
            "   at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:520)\n" +
            "   at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:109)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:104)\n" +
            "   at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:520)\n" +
            "   at org.apache.catalina.core.ContainerBase.invoke(ContainerBase.java:929)\n" +
            "   at org.apache.coyote.tomcat5.CoyoteAdapter.service(CoyoteAdapter.java:160)\n" +
            "   at org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:793)\n" +
            "   at org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler.processConnection(Http11Protocol.java:702)\n" +
            "   at org.apache.tomcat.util.net.TcpWorkerThread.runIt(PoolTcpEndpoint.java:571)\n" +
            "   at org.apache.tomcat.util.threads.ThreadPool$ControlRunnable.run(ThreadPool.java:644)\n" +
            "   at java.lang.Thread.run(Thread.java:534)\n";
    
    /**
     * Excpected result of trimming the basic trace.
     */
    private String basicTraceTrimmed = "Programming error\n" +
            "   org.jasig.portal.PortalException: java.lang.RuntimeException: java.lang.NullPointerException [based on exception: java.lang.NullPointerException]\n" +
            "   at org.jasig.portal.UserInstance.renderState(UserInstance.java:600)\n" +
            "   at org.jasig.portal.UserInstance.writeContent(UserInstance.java:174)\n" +
            "   at org.jasig.portal.PortalSessionManager.doGet(PortalSessionManager.java:234)\n";
    
    /**
     * An example raw stack trace which has two throwables in the chain and 
     * only the first throwable need be trimmed.
     */
    private String rawCauseTrace = "Programming error\n" +
            "   org.jasig.portal.PortalException: java.lang.RuntimeException: java.lang.NullPointerException [based on exception: java.lang.NullPointerException]\n" +
            "   at org.jasig.portal.UserInstance.renderState(UserInstance.java:600)\n" +
            "   at org.jasig.portal.UserInstance.writeContent(UserInstance.java:174)\n" +
            "   at org.jasig.portal.PortalSessionManager.doGet(PortalSessionManager.java:234)\n" +
            "   at javax.servlet.http.HttpServlet.service(HttpServlet.java:697)\n" +
            "   at javax.servlet.http.HttpServlet.service(HttpServlet.java:810)\n" +
            "   at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:237)\n" +
            "   at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:157)\n" +
            "   at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:214)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:104)\n" +
            "   at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:520)\n" +
            "   at org.apache.catalina.core.StandardContextValve.invokeInternal(StandardContextValve.java:198)\n" +
            "   at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:152)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:104)\n" +
            "   at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:520)\n" +
            "   at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:137)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:104)\n" +
            "   at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:117)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:102)\n" +
            "   at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:520)\n" +
            "   at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:109)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:104)\n" +
            "   at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:520)\n" +
            "   at org.apache.catalina.core.ContainerBase.invoke(ContainerBase.java:929)\n" +
            "   at org.apache.coyote.tomcat5.CoyoteAdapter.service(CoyoteAdapter.java:160)\n" +
            "   at org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:793)\n" +
            "   at org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler.processConnection(Http11Protocol.java:702)\n" +
            "   at org.apache.tomcat.util.net.TcpWorkerThread.runIt(PoolTcpEndpoint.java:571)\n" +
            "   at org.apache.tomcat.util.threads.ThreadPool$ControlRunnable.run(ThreadPool.java:644)\n" +
            "   at java.lang.Thread.run(Thread.java:534)\n" +
            "Caused by: java.lang.RuntimeException: java.lang.NullPointerException\n" +
            "   at org.apache.xalan.transformer.TransformerImpl.run(TransformerImpl.java:3407)\n" +
            "   at org.apache.xalan.transformer.TransformerHandlerImpl.endDocument(TransformerHandlerImpl.java:433)\n" +
            "   at org.jasig.portal.utils.SAX2FilterImpl.endDocument(SAX2FilterImpl.java:654)\n" +
            "   at org.jasig.portal.utils.SAX2BufferImpl.outputBuffer(SAX2BufferImpl.java:181)\n" +
            "   at org.jasig.portal.UserInstance.renderState(UserInstance.java:557)\n";
    
    private String trimmedCauseStackTrace = "Programming error\n" +
    "   org.jasig.portal.PortalException: java.lang.RuntimeException: java.lang.NullPointerException [based on exception: java.lang.NullPointerException]\n" +
    "   at org.jasig.portal.UserInstance.renderState(UserInstance.java:600)\n" +
    "   at org.jasig.portal.UserInstance.writeContent(UserInstance.java:174)\n" +
    "   at org.jasig.portal.PortalSessionManager.doGet(PortalSessionManager.java:234)\n" +
    "Caused by: java.lang.RuntimeException: java.lang.NullPointerException\n" +
    "   at org.apache.xalan.transformer.TransformerImpl.run(TransformerImpl.java:3407)\n" +
    "   at org.apache.xalan.transformer.TransformerHandlerImpl.endDocument(TransformerHandlerImpl.java:433)\n" +
    "   at org.jasig.portal.utils.SAX2FilterImpl.endDocument(SAX2FilterImpl.java:654)\n" +
    "   at org.jasig.portal.utils.SAX2BufferImpl.outputBuffer(SAX2BufferImpl.java:181)\n" +
    "   at org.jasig.portal.UserInstance.renderState(UserInstance.java:557)\n";
    
    
    /**
     * An example raw stack trace which has two throwables in the chain and 
     * both need to be trimmed
     */
    private String complexCauseTrace = "Programming error\n" +
            "   org.jasig.portal.PortalException: java.lang.RuntimeException: java.lang.NullPointerException [based on exception: java.lang.NullPointerException]\n" +
            "   at org.jasig.portal.UserInstance.renderState(UserInstance.java:600)\n" +
            "   at org.jasig.portal.UserInstance.writeContent(UserInstance.java:174)\n" +
            "   at org.jasig.portal.PortalSessionManager.doGet(PortalSessionManager.java:234)\n" +
            "   at javax.servlet.http.HttpServlet.service(HttpServlet.java:697)\n" +
            "   at javax.servlet.http.HttpServlet.service(HttpServlet.java:810)\n" +
            "   at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:237)\n" +
            "   at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:157)\n" +
            "   at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:214)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:104)\n" +
            "   at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:520)\n" +
            "   at org.apache.catalina.core.StandardContextValve.invokeInternal(StandardContextValve.java:198)\n" +
            "   at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:152)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:104)\n" +
            "   at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:520)\n" +
            "   at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:137)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:104)\n" +
            "   at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:117)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:102)\n" +
            "   at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:520)\n" +
            "   at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:109)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:104)\n" +
            "   at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:520)\n" +
            "   at org.apache.catalina.core.ContainerBase.invoke(ContainerBase.java:929)\n" +
            "   at org.apache.coyote.tomcat5.CoyoteAdapter.service(CoyoteAdapter.java:160)\n" +
            "   at org.apache.coyote.http11.Http11Processor.process(Http11Processor.java:793)\n" +
            "   at org.apache.coyote.http11.Http11Protocol$Http11ConnectionHandler.processConnection(Http11Protocol.java:702)\n" +
            "   at org.apache.tomcat.util.net.TcpWorkerThread.runIt(PoolTcpEndpoint.java:571)\n" +
            "   at org.apache.tomcat.util.threads.ThreadPool$ControlRunnable.run(ThreadPool.java:644)\n" +
            "   at java.lang.Thread.run(Thread.java:534)\n" +
            "Caused by: java.lang.RuntimeException: java.lang.NullPointerException\n" +
            "   at org.apache.xalan.transformer.TransformerImpl.run(TransformerImpl.java:3407)\n" +
            "   at org.apache.xalan.transformer.TransformerHandlerImpl.endDocument(TransformerHandlerImpl.java:433)\n" +
            "   at org.jasig.portal.utils.SAX2FilterImpl.endDocument(SAX2FilterImpl.java:654)\n" +
            "   at org.jasig.portal.utils.SAX2BufferImpl.outputBuffer(SAX2BufferImpl.java:181)\n" +
            "   at org.jasig.portal.UserInstance.renderState(UserInstance.java:557)\n" +
            "   at javax.servlet.http.HttpServlet.service(HttpServlet.java:697)\n" +
            "   at javax.servlet.http.HttpServlet.service(HttpServlet.java:810)\n" +
            "   at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:237)\n" +
            "   at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:157)\n" +
            "   at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:214)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:104)\n" +
            "   at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:520)\n" +
            "   at org.apache.catalina.core.StandardContextValve.invokeInternal(StandardContextValve.java:198)\n" +
            "   at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:152)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:104)\n" +
            "   at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:520)\n" +
            "   at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:137)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:104)\n" +
            "   at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:117)\n" +
            "   at org.apache.catalina.core.StandardValveContext.invokeNext(StandardValveContext.java:102)\n" +
            "   at org.apache.catalina.core.StandardPipeline.invoke(StandardPipeline.java:520)\n" +
            "   at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:109)\n";
    
    /**
     * Test ability to refrain from trimming stack trace where
     * no trimming is necessary.
     */
    public void testNoTrim(){
        assertEquals(this.reallyBasicTrace, 
                ExceptionHelper.trimStackTrace(this.reallyBasicTrace));
    }
    
    /**
     * Test ability to trim a basic stack trace.
     */
    public void testTrimStackTrace(){
        assertEquals(this.basicTraceTrimmed, 
                ExceptionHelper.trimStackTrace(this.basicTrace));
    }
    
    /**
     * Test that a Cause after trimming is maintained.
     */
    public void testCauseTrace() {
       assertEquals(this.trimmedCauseStackTrace, 
               ExceptionHelper.trimStackTrace(this.rawCauseTrace));
    }
    
    /**
     * Test that causes are also properly trimmed.
     */
    public void testTrimCauseTrace() {
        assertEquals(this.trimmedCauseStackTrace, 
                ExceptionHelper.trimStackTrace(this.complexCauseTrace));
    }

}