/*
 * put your module comment here
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal.channels.groupsmanager;


/**
 * <p>Title: uPortal</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Columbia University</p>
 * @author Don Fracapane
 * @version 2.0
 */
public class GroupsManagerException extends Exception {
   /**
    * put your documentation comment here
    */
   private Throwable cause = null;

   /**
    * put your documentation comment here
    */
   public GroupsManagerException () {
      super();
   }

   /**
    * put your documentation comment here
    * @param       String message
    */
   public GroupsManagerException (String message) {
      super(message);
   }

   /**
    * put your documentation comment here
    * @param       String message
    * @param       Throwable cause
    */
   public GroupsManagerException (String message, Throwable cause) {
      super(message);
      this.cause = cause;
   }

   /**
    * put your documentation comment here
    * @return
    */
   public Throwable getCause () {
      return  cause;
   }

   /**
    * put your documentation comment here
    */
   public void printStackTrace () {
      super.printStackTrace();
      if (cause != null) {
         System.err.println("Caused by:");
         cause.printStackTrace();
      }
   }

   /**
    * put your documentation comment here
    * @param ps
    */
   public void printStackTrace (java.io.PrintStream ps) {
      super.printStackTrace(ps);
      if (cause != null) {
         ps.println("Caused by:");
         cause.printStackTrace(ps);
      }
   }

   /**
    * put your documentation comment here
    * @param pw
    */
   public void printStackTrace (java.io.PrintWriter pw) {
      super.printStackTrace(pw);
      if (cause != null) {
         pw.println("Caused by:");
         cause.printStackTrace(pw);
      }
   }
}



