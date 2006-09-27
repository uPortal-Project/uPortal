<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page session="false" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.*" %>
<%@ page import="org.jasig.portal.ChannelManager" %>
<%@ page import="org.jasig.portal.GuestUserInstance" %>
<%@ page import="org.jasig.portal.PortalSessionManager" %>
<%@ page import="org.jasig.portal.RDBMServices" %>
<%@ page import="org.jasig.portal.UserInstance" %>
<%@ page import="org.jasig.portal.services.Authentication" %>
<%@ page import="org.jasig.portal.utils.MovingAverage" %>
<%@ page import="org.jasig.portal.utils.MovingAverageSample" %>
<%!
  // Period before page is refreshed in seconds
  static final int refreshPeriod = 60;

  // String formatters to make things pretty
  static final NumberFormat numFormatter = NumberFormat.getNumberInstance();
  static final DateFormat dateFormatter = DateFormat.getDateTimeInstance();

  public static long getTotalUserCount() {
    return UserInstance.userSessions.longValue();
  }

  public static long getTotalGuestCount() {
    return GuestUserInstance.guestSessions.longValue();
  }

  public static long getTotalSessionCount() {
    return getTotalUserCount() + getTotalGuestCount();
  }

  public static long getTotalMemory() {
    return Runtime.getRuntime().totalMemory();
  }

  public static long getFreeMemory() {
    return Runtime.getRuntime().freeMemory();
  }

  public static long getUsedMemory() {
    return getTotalMemory() - getFreeMemory();
  }

  public static long getAvgMemoryPerUser() {
    long averageMemoryPerUser = 0;
    if(getTotalSessionCount() > 0) {
      averageMemoryPerUser = getUsedMemory() / getTotalSessionCount();
    }
    return averageMemoryPerUser;
  }

  public static Date getCurrentDate() {
    return new Date();
  }

  public ThreadGroup getTopThreadGroup() {
	  ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
	  ThreadGroup parentThread;

	  while ((parentThread = threadGroup.getParent()) != null) {
	     threadGroup = parentThread;
	  }

	  return threadGroup;
  }

  String extraPars = null;
%>

<%
    Iterator pars = request.getParameterMap().entrySet().iterator();

    StringBuffer sb = new StringBuffer();
    String sep = "";
      while (pars.hasNext()) {
        Map.Entry entry = (Map.Entry)pars.next();
        String name = (String)entry.getKey();

          String[] values = (String[])entry.getValue();
          for (int i = 0; i < values.length; i++) {
            sb.append(sep + name + "=" + values[i]);
            sep = "&";
          }
      }
    if (sb.length() > 0) {
       extraPars = sb.toString();
    }
%>

<html>
  <head>
    <title>uPortal Monitor</title>
    <meta HTTP-EQUIV="Refresh" Content="<%= refreshPeriod %>; URL=">
    <style type="text/css">
    body {
        background-color: #fff;
        font-family: verdana, geneva, helvetica, arial, sans-serif;
        }
    td {
        vertical-align: top;
        font-family: verdana, geneva, helvetica, arial, sans-serif;
        }
    table {
        font-family: verdana, geneva, helvetica, arial, sans-serif;
        }
    .uportal-channel-text{
        /* html code doesn't allow relative font-sizing due to incorrect nesting */
        color: #000;
        font-size: 11px;
    }
    .uportal-background-med{
        background-color: #ccc;
    }
    </style>
  </head>

  <body>
    <span class="uportal-channel-title">uPortal Monitor</span><br/>
    <br/>
    <span class="uportal-channel-text">Server: <%=request.getServerName() %>:<%=request.getServerPort()%></span><br/>
    <span class="uportal-channel-text">Started at: <%= dateFormatter.format(PortalSessionManager.STARTED_AT) %></span><br/>
   <span class="uportal-channel-text">Refresh period: <%= numFormatter.format(refreshPeriod) %> seconds</span><br/>
    <span class="uportal-channel-text">Last refresh at: <%= dateFormatter.format(getCurrentDate()) %></span><br/>
    <br/>
    <table border="0" cellpadding="2" cellspacing="3" cols="3">

      <!-- Sessions ------------------------------------------ -->
      <tr class="uportal-text">
        <td colspan="2" class="uportal-background-med">
          <span class="uportal-channel-strong">Sessions</span>
        </td>
      </tr>

      <tr class="uportal-text">
        <td>User sessions</td>
        <td align="right"><%= numFormatter.format(getTotalUserCount()) %></td>
      </tr>

      <tr class="uportal-text">
        <td>Guest sessions</td>
        <td align="right"><%= numFormatter.format(getTotalGuestCount()) %></td>
      </tr>

      <tr class="uportal-text">
        <td>Total sessions</td>
        <td align="right"><%= numFormatter.format(getTotalSessionCount()) %></td>
      </tr>

       <!-- Performance ----------------------------- -->
       <tr class="uportal-text">
        <td colspan="2" class="uportal-background-med">
          <span class="uportal-channel-strong">Performance</span>
        </td>
      </tr>

      <tr class="uportal-text">
       <td colspan="2">
       <table width="100%" border="0" cellpadding="3" cellspacing="3" cols="8">
        <tr><th><%= "@"+MovingAverage.SAMPLESIZE %></th><th></th><th align="right">Total</th><th align="right">Min</th><th align="right">Avg</th><th align="right">Max</th><th align="right">Last</th><th align="right">&gt;Max</th></tr>
        <tr><td>Render</td><td>&nbsp;</td>
          <% MovingAverageSample render = UserInstance.lastRender; %>
          <td align="right"><%= numFormatter.format(render.totalSamples) %></td>
          <td align="right"><%= numFormatter.format(render.min) %></td>
          <td align="right"><%= numFormatter.format(render.average)%></td>
          <td align="right"><%= numFormatter.format(render.max) %></td>
          <td align="right"><%= numFormatter.format(render.lastSample) %></td>
          <td align="right"><%= numFormatter.format(render.highMax) %></td>
        </tr>
        <tr><td>Authentication</td><td>&nbsp;</td>
          <% MovingAverageSample auth = Authentication.lastAuthentication; %>
          <td align="right"><%= numFormatter.format(auth.totalSamples) %></td>
          <td align="right"><%= numFormatter.format(auth.min) %></td>
          <td align="right"><%= numFormatter.format(auth.average)%></td>
          <td align="right"><%= numFormatter.format(auth.max) %></td>
          <td align="right"><%= numFormatter.format(auth.lastSample) %></td>
          <td align="right"><%= numFormatter.format(auth.highMax) %></td>
        </tr>
        <tr><td>Database connection</td><td>&nbsp;</td>
           <% MovingAverageSample db = RDBMServices.lastDatabase; %>
          <td align="right"><%= numFormatter.format(db.totalSamples) %></td>
          <td align="right"><%= numFormatter.format(db.min) %></td>
          <td align="right"><%= numFormatter.format(db.average)%></td>
          <td align="right"><%= numFormatter.format(db.max) %></td>
          <td align="right"><%= numFormatter.format(db.lastSample) %></td>
          <td align="right"><%= numFormatter.format(db.highMax) %></td>
        </tr>
      </table>
      <!-- Memory -------------------------------------------- -->
      <tr class="uportal-text">
        <td colspan="2" class="uportal-background-med">
          <span class="uportal-channel-strong">Memory (bytes)</span>
        </td>
      </tr>

      <tr class="uportal-text">
        <td>Free memory</td>
        <td align="right"><%= numFormatter.format(getFreeMemory()) %></td>
      </tr>

      <tr class="uportal-text">
        <td>Used memory</td>
        <td align="right"><%= numFormatter.format(getUsedMemory()) %></td>
      </tr>

      <tr class="uportal-text">
        <td>Total memory</td>
        <td align="right"><%= numFormatter.format(getTotalMemory()) %></td>
      </tr>

      <tr class="uportal-text">
        <td>Used memory/session</td>
        <td align="right"> <%= numFormatter.format(getAvgMemoryPerUser()) %></td>
      </tr>

       <!-- Threads ------------------------------------------ -->
       <tr class="uportal-text">
        <td colspan="2" class="uportal-background-med">
          <span class="uportal-channel-strong">Threads</span>
        </td>
      </tr>
      <tr class="uportal-text">
        <td>ChannelRenderer: Active Worker threads</td>
        <td align="right"> <%= numFormatter.format(ChannelManager.activeRenderers.get()) %></td>
      </tr>
      <tr class="uportal-text">
        <td>ChannelRenderer: Max active</td>
        <td align="right"> <%= numFormatter.format(ChannelManager.maxRenderThreads.get()) %></td>
      </tr>

      <tr class="uportal-text">
        <td>uPortal Active threads</td>
        <td align="right"> <%= numFormatter.format(PortalSessionManager.getThreadGroup().activeCount()) %></td>
      </tr>
      <tr class="uportal-text">
        <td>Active threads</td>
        <td align="right"> <%= numFormatter.format(Thread.activeCount()) %></td>
      </tr>
      <tr class="uportal-text">
        <td>Total threads</td>
        <td align="right"> <%= numFormatter.format(getTopThreadGroup().activeCount()) %></td>
      </tr>


       <!-- Database connections ----------------------------- -->
       <tr class="uportal-text">
        <td colspan="2" class="uportal-background-med">
          <span class="uportal-channel-strong">Database connections</span>
        </td>
      </tr>

      <tr>
       <td colspan="3">
       <table border="0" cellpadding="2" cellspacing="3" cols="3">
        <tr class="uportal-text">
          <th class="uportal-text" align="left">Type</th>
          <th class="uportal-text" align="right">Active</th>
          <th class="uportal-text" align="right">Maximum</th>
        <tr>
        <tr class="uportal-text">
          <td>Connections</td>
          <td align="right"> <%= numFormatter.format(RDBMServices.getActiveConnectionCount()) %></td>
          <td align="right"> <%= numFormatter.format(RDBMServices.getMaxConnectionCount()) %></td>
        </tr>
        </table>
       </td>
      </tr>

    </table>

  </body>
</html>


