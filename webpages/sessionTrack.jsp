<%@ page import="java.lang.reflect.*" %>
<%@ page import="org.jasig.portal.*" %>
<%@ page isThreadSafe="false" %>

<Html>
<head>
<style type="text/css">
font { font-size: 8 pt }
td {font-size: 8 pt }

</style>
</head>
<body>
<div>

<%!

// The memory threshold under which we knock-off Guest users
double memThreshold_H = 30.0 ;

// Log files on regi2:
 File sessionInfoLogFile = new File("/ut01/app/portal/prod/UBCbase15/logs/sessionInfo.log");

// Log files on sumatra:
// File sessionInfoLogFile = new File("/ut01/home/its/vjoshi/portal/devl/UBCbase15/logs/sessionInfo.log");


static Vector reports = new Vector();
static Map portalSessions = new HashMap();
static double totalMemory;

static int lastLoggedHour = -1;
Date portalULast; 

private class Report {

	int guestSessionCount;
	int portalUserSessionCount;
	double mcount;
	Date date;

	Report() {
		guestSessionCount = 0;
		portalUserSessionCount = 0;
		mcount = getMemory(true);
		date = Calendar.getInstance().getTime();
	}
} 

/** This method prints out the little graphs for the different samples.
	It uses #s for every 10 counts and .s (periods) for every remaining 1.
*/

private String printGraph(int count, String bgcolor) {
	int hashCount = count/10;

	if(count == 0) {
		//nothing to graph 
		return "";
	} else if(hashCount == 0) {
		// too small, return a dot
		return ".";
	} 
	
	StringBuffer sbuf = new StringBuffer();

	sbuf.append("<font style=\"background:" + bgcolor + "\" color=\"" + bgcolor + "\">");	

	for(int h = 0; h < hashCount; h++) {
		sbuf.append("#");
	}
	sbuf.append("</font>");
	// return our newly created graph
	return sbuf.toString();
}

private double getMemory ( boolean askingForFreeMem ) {
	// the argument specifies whether the request is for free Memory or for Total Memory
	long mem; 
	if ( askingForFreeMem == true ) {
		mem = Math.abs(Runtime.getRuntime().freeMemory());
	} else {
		mem = Math.abs(Runtime.getRuntime().totalMemory());
	}
	// the following converts the memory (in Bytes) into MB with 2-decimal precision   
	double memInMB = ( Math.floor( (new Long (mem)).doubleValue() / (1024.0 * 10.24) ) )/100.0 ;
	return (memInMB);
}

private void printResults (JspWriter out) {
   try {
	out.println("<table border=1 cellspacing=0 cellpadding=0><tr><td>Reading</td><td><Center>Date</Center></td><td>Graph</td><td><Center>Guest Sessions<br>Portal Sessions<br></Center></td><td>Total Sessions</td><td>Free Memory (MB)</td></tr>");

	// now we can print our bad looking little graph
	int numReports = reports.size();

	for(int i = 0; i < numReports; i++) {
		// put this in a try block to protect against bad access
		Report thisRep = (Report) reports.elementAt(i);

		out.println("<tr><td><Center>" + i + "</Center></td>");

		int totalCount = thisRep.guestSessionCount + thisRep.portalUserSessionCount; 
		// start our table row
		out.println("<td>" + thisRep.date + "</td>");
		out.println("<td>");
		// now we do a simple graph using # and .  symbols
		out.println(printGraph(thisRep.guestSessionCount, "#990000") + "<br>");
		out.println(printGraph(thisRep.portalUserSessionCount, "#009900") + "<br>");
		out.println("</td><td><Center>" + thisRep.guestSessionCount+ "<br>" + thisRep.portalUserSessionCount + "<br></Center></td>");
		out.println("<td><Center>" + totalCount + "</Center></td>");
		out.println("<td><Center>" + thisRep.mcount + "</Center></td></tr>");
	}

	out.println("</table>");
   } catch (IOException e) {
	// do nothing on errors but continue
   }
}

private void logSessionInfo (Report rep) {
   try {
	PrintWriter pW = new PrintWriter (new BufferedOutputStream (new FileOutputStream(sessionInfoLogFile.getPath(), true)), true );
	pW.print("------------------------------------------------------------------------- \n");
	pW.print ("Logging Session Info :  \n");
	String sMemUsed = String.valueOf(totalMemory - rep.mcount);
	String memUsed = sMemUsed.substring(0,sMemUsed.indexOf('.')+3);
	pW.print("  Guest    Bare     Webmail     Portal     Mem used (MB)     Time of log \n");
	pW.print("** " + rep.guestSessionCount + "        " + "        " + rep.portalUserSessionCount + "             " + memUsed + "           " + rep.date + "\n");
	int realSessions = rep.portalUserSessionCount + rep.guestSessionCount; 
	pW.print("# of Real Sessions:  " + realSessions );
	pW.println("\n");
   } catch (IOException e) {}
}

%>


<jsp:scriptlet>

totalMemory = getMemory(false);

// We have to use a deprecated method here. There isn't any method in
// Java 1.3 to replace this one

// grab a session context to work with
HttpSessionContext ctx = session.getSessionContext();

// get the Session IDs from the session context and count them
Enumeration ids = ctx.getIds();
int numberOfSessions = 0;
while (ids.hasMoreElements()) {
	ids.nextElement();
	numberOfSessions++;
}

// get the Set of all Portal session Ids from the SessionManager
Set portalSessIDs = new HashSet ();
Enumeration idsSeenEnum = SessionManager.getSessionIDs("PORTAL");
if ( idsSeenEnum != null ) {
	while (idsSeenEnum.hasMoreElements()) {
	     String sId = (String)idsSeenEnum.nextElement() ;
	     portalSessIDs.add(sId);
	}
}


Report rep = new Report();
rep.portalUserSessionCount = portalSessIDs.size(); 
rep.guestSessionCount = numberOfSessions - rep.portalUserSessionCount;

// Log the current session info to the log file every hour
Calendar cal = new GregorianCalendar(); 
int hr = cal.get(Calendar.HOUR_OF_DAY);
if ( hr != lastLoggedHour) {
	logSessionInfo(rep);
        lastLoggedHour = hr;
}


Date oldestPortalUCreation = new Date ();

//Get the Creation time for the oldest Portal Session 
Iterator crTimeItr = portalSessIDs.iterator();
while (crTimeItr.hasNext()) {
	String sId = (String) crTimeItr.next();
	Date curDt = SessionManager.getCreationTime(sId);
	if (oldestPortalUCreation.after(curDt)) {
		oldestPortalUCreation = curDt;
	}
}

// protect our vector while we modify its contents
synchronized (reports) {

// now we add this to our report Vectors
// get the two variables for this sample
if(reports.size() > 0) {

	Report prevRep  = (Report) reports.elementAt(reports.size()-1);
	int gcount = prevRep.guestSessionCount;
	int ucount = prevRep.portalUserSessionCount;

	if(rep.guestSessionCount != gcount || rep.portalUserSessionCount != ucount ) {
		reports.addElement(rep);
	}
} else { // that is: if the reports vector is empty 
	 // add the report instance to the vector 
	reports.addElement(rep);
}


// now we trim the Vector real quick to keep it under 20 samples
if(reports.size() > 20) {
	int diff = Math.abs(reports.size() - 20);
	for(int i = 0; i < diff; i++) 
		reports.remove(i);
}

} // end synchronized	

// Now print out the results stored in our Vector Results
printResults(out);

</jsp:scriptlet>

<hr>
<B>Total Available Memory is :  <%= totalMemory %>  MB </B> <br><br>
<B> Creation times: </B><br>
<B>oldest Portal session : &nbsp &nbsp &nbsp  <%= oldestPortalUCreation %> </B> <br>

</body>
</html> 
