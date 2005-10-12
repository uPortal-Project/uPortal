/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.channels.cusermanager;

/**
 * @author smb1@cornell.edu
 * @version $Revision$ $Date$
 */
public interface Constants {

  /** Value = "3.0" */
  public static final String VERSION = "3.0";

  /** Value = "already exists" */
  public static final String ALREADY_EXISTS = "already exists";

  /** Value = "org.jasig.portal.channels.cusermanager.CUserManager" */
  public static final String PERMISSION_OWNERTOKEN = "org.jasig.portal.channels.cusermanager.CUserManager";

  /** Value = "CUserManager" */
  public static final String PERMISSION_OWNERNAME = "CUserManager";

  /** Value = "Account Manager" */
  public static final String PERMISSION_OWNERTARGET = "Account Manager";

  /** Value = "acctmgr" */
  public static final String PERMISSION_MNGRRIGHT = "acctmgr";

  /** Value = "Manage User Accounts" */
  public static final String PERMISSION_MNGRRIGHTDESC = "Manage User Accounts";

  /** Value = "pwdchng" */
  public static final String PERMISSION_PWDCHNGRIGHT = "pwdchng";

  /** Value = "Change Current Password" */
  public static final String PERMISSION_PWDCHNGRIGHTDESC = "Change Current Password";

  /** Value = "org.jasig.portal.channels.cusermanager.provider.DefaultDataHandlerImpl" */
  public static final String DEFAULTDATAHANDLER
     = "org.jasig.portal.channels.cusermanager.provider.DefaultDataHandlerImpl";

  /** Value = "IDataHandler.class" */
  public static final String CHNPARAMDATAHANDLER = "IDataHandler.class";

  /** Value = "act.as.user.password.change.only" */
  public static final String CHNPARAMNOTMGR = "act.as.user.password.change.only";

  /** Value = "(MD5)" */
  public static final String DEFAULTMD5PREFIX = "(MD5)";

  /** Value = "default" */
  public static final String DEFAULTSTYLESHEET = "default";

  /** Value = "baseActionURL" */
  public static final String BASEACTION = "baseActionURL";

  /** Value = "CUserManager.ssl" */
  public static final String SSLFILE = "CUserManager.ssl";

  /**
   *  Any messages to be displayed to the user are sent
   *  to the stylesheets as this named variable.
   *   Value = "message"
   */
  public static final String DISPLAYMESSAGE = "message";

  /** Value = "mode" */
  public static final String MODE        = "mode";

  /** Value = "display" */
  public static final String MODEDISPLAY = "display";

  /** Value = "pwdchng" */
  public static final String MODEPWDCHNG = "pwdchng";

  /** Value = "search" */
  public static final String MODESEARCH  = "search";

  /** Value = "choose" */
  public static final String MODECHOOSE  = "choose";

  /** Value = "add" */
  public static final String MODEADD     = "add";

  /** Value = "about" */
  public static final String MODEABOUT     = "about";

  /** Value = "help" */
  public static final String MODEHELP     = "help";

  /** Value = "User-Pwd-Only-Mode" */
  public static final String MODEUSRPWDCHNG = "User-Pwd-Only-Mode";

  /** Value = "pswd" */
  public static final String PWDFIELD = "pswd";

  /** Value = "encrptd_pswd" */
  public static final String ENCRYPTPWDFIELD = "encrptd_pswd";

  /** Value = "user_name" */
  public static final String UNFIELD = "user_name";

  /** Value = "username" */
  public static final String ATTRUSERNAME = "username";

  /** Value = "*LCK*" */
  public static final String ACCOUNTLOCK = "*LCK*";

  /** Value = "Acc.Is.Locked" */
  public static final String ACCOUNTLOCKACKNOWLEDGE = "Acc.Is.Locked";

  /** Value = "form_action" */
  public static final String FORMACTION  = "form_action";

  /** Value = "chosen" */
  public static final String FORMCHOSEN  = "chosen";

  /** Value = "search-str" */
  public static final String FORMSRCHSTR = "search-str";

  /** Value = "proposed-password" */
  public static final String FORMNEWPWD = "proposed-password";

  /** Value = "NULLIFY.THIS.USER" */
  public static final String NULLIFYUSER = "NULLIFY.THIS.USER";

  /** Value = "User \"{0}\" can perform no action within this channel!" */
  public static final String ERRMSG_NORIGHTS
                = "User \"{0}\" can perform no action within this channel!";

  /** Value = "Password NOT changed!\r\nCurrent Password does
                   not match stored password."
  */
  public static final String ERRMSG_PWDNOTMATACHED =
                   "Password NOT changed!\r\nCurrent Password does "
                           + " not match stored password.";

  /** Value = "Username \"{0}\" already exists!" */
  public static final String USER_EXISTS
                          = "Username \"{0}\" " + Constants.ALREADY_EXISTS + "!";

  /** Value of date mask = "yyyy-MM-dd hh:mm:ss" */
  public static final java.text.SimpleDateFormat SDF
                = new java.text.SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

  /** Value = "Saved." */
  public static final String MSG_SAVED = "Saved.";

  /** Value = "Password changed." */
  public static final String MSG_PWD_SAVED = "Password changed.";

}// eoi