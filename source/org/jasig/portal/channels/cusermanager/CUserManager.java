/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.channels.cusermanager;

import java.util.Enumeration;
import java.text.MessageFormat;

import org.jasig.portal.IChannel;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPermission;
import org.jasig.portal.security.provider.PersonImpl;
//import edu.cornell.uportal.channels.cusermanager.provider.PersonImpl;

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.jasig.portal.IPermissible;
import org.jasig.portal.AuthorizationException;

import org.jasig.portal.utils.XSLT;
import org.jasig.portal.utils.DocumentFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.ContentHandler;

/**
 * @author smb1@cornell.edu
 * @version $Revision$ $Date$
 */
public class CUserManager extends CUserManagerPermissions implements IChannel, IPermissible {

  private IDataHandler datasource;

  private String mode = Constants.MODEDISPLAY;

  private ChannelStaticData channelStaticData;
  private ChannelRuntimeData channelRuntimeData;

  private boolean managerMode = false;
  private boolean passwordChangeMode = true;

  private PortalEvent lastEvent;

  /** for pwd chng mode, we want to cache the user's info because
   *  there will be many more of these than admin channels
   */
  private Document personalDocument = null;

  /**
   */
  public CUserManager() {
  }// CUserManager


  public ChannelRuntimeProperties getRuntimeProperties() {
    return new ChannelRuntimeProperties();
  }// getRuntimeProperties


  public void receiveEvent(PortalEvent ev) {

    this.lastEvent = ev;

  }// receiveEvent


  public void setStaticData(ChannelStaticData sd) {
    channelStaticData = sd;

// Ignore since 2.0 (2003.04.21)
//    if( CSD.getParameter( Constants.CHNPARAMNOTMGR ) != null )
//      ManagerMode = false;

    // let determine the user's rights
    try{

      IPermission[] perms = channelStaticData.getAuthorizationPrincipal().getAllPermissions(
        Constants.PERMISSION_OWNERTOKEN, null, Constants.PERMISSION_OWNERTARGET );

      for( int i = 0; i < perms.length; i++ ) {

         if( perms[ i ].getActivity().equals( Constants.PERMISSION_MNGRRIGHT )
            && perms[ i ].getType().equals( IPermission.PERMISSION_TYPE_GRANT ))
          managerMode = true;

         if( perms[ i ].getActivity().equals( Constants.PERMISSION_PWDCHNGRIGHT )
            && perms[ i ].getType().equals( IPermission.PERMISSION_TYPE_DENY ))
          passwordChangeMode = false;

      }// for
    }catch( AuthorizationException ae ){
        log.error(ae,ae);
    }

  }// setStaticData


  public void setRuntimeData(ChannelRuntimeData rd) {
    channelRuntimeData = rd;
  }// setRuntimeData


  public void renderXML(ContentHandler out) throws PortalException {

    // first, be sure they are allowed to be here
    if( !managerMode && !passwordChangeMode )
      throw new AuthorizationException(  MessageFormat.format(
        Constants.ERRMSG_NORIGHTS, new Object[]
          { (String)channelStaticData.getPerson().getAttribute( IPerson.USERNAME ) } ));

    try{

      String message_to_user_about_action = "";  // these always start blank
      Document doc = null;
      IPerson[] people = null;
      mode = Constants.MODEDISPLAY;

      // now, b4 we get going, there may have been an event to deal with
      if( channelRuntimeData.getParameter( Constants.FORMACTION ) == null && lastEvent != null ) {

        if( lastEvent.getEventNumber() == PortalEvent.ABOUT_BUTTON_EVENT )
          channelRuntimeData.setParameter( Constants.FORMACTION, "10" );

        if( lastEvent.getEventNumber() == PortalEvent.HELP_BUTTON_EVENT )
          channelRuntimeData.setParameter( Constants.FORMACTION, "11" );

        lastEvent = null; // don't need that anymore
      }// if, null & !null

      // see if we have form data to process
      if( channelRuntimeData.getParameter( Constants.FORMACTION ) != null ){

        log.debug("form.action=" + channelRuntimeData.getParameter( Constants.FORMACTION ));

        switch( Integer.parseInt( channelRuntimeData.getParameter( Constants.FORMACTION ))) {

            case 1: { // update

              getDataSource().setUserInformation( crd2persion( channelRuntimeData ));
              message_to_user_about_action = Constants.MSG_SAVED;

              break;
            }// 1

            case 2: {  // choose

              mode = Constants.MODEDISPLAY;
              break;
            }// 2

            case 3: {  // choose

              people = getDataSource().getAllUsers();
              mode = Constants.MODECHOOSE;

              break;
            }// 3

            case 4: {  // search

              if( channelRuntimeData.getParameter( Constants.FORMSRCHSTR ) == null ) {
                people = new PersonImpl[0];
                mode = Constants.MODESEARCH;
               }else{

                // if they did not enter a src str, display mode
                people = getDataSource().getAllUsersLike(
                          channelRuntimeData.getParameter( Constants.FORMSRCHSTR ));

                if( people.length == 1 ) {

                   // this will cause a second lookup but the user experience will benifit
                   mode = Constants.MODEDISPLAY;

                   channelRuntimeData.setParameter( Constants.UNFIELD,
                      (String)people[0].getAttribute( Constants.UNFIELD ));

                 }else
                   mode = Constants.MODECHOOSE;  // they will need to select to narrow
              }// if

              break;
            }// 4

            case 5: {  // prepare add new user

              channelRuntimeData.setParameter( Constants.UNFIELD,(String)
                       channelStaticData.getPerson().getAttribute( Constants.ATTRUSERNAME ));
              mode = Constants.MODEADD;

              break;
            }// 5

            case 6: {  // add new user

              try{ getDataSource().addUser( crd2persion( channelRuntimeData ));
              }catch( Exception adde ) {

                if( adde.getMessage().indexOf( Constants.ALREADY_EXISTS ) > -1 )
                   message_to_user_about_action = adde.getMessage();
                  else
                   throw adde;

               }// catch

              mode = Constants.MODEDISPLAY;

              break;
            }// 6

            case 7: {  // prepare password chng

              people = new IPerson[] { getDataSource().getUser(
                          channelRuntimeData.getParameter( Constants.UNFIELD )) };

              mode = Constants.MODEPWDCHNG;

              break;
            }// 7

            case 8: {  // password chng

              message_to_user_about_action = Constants.MSG_PWD_SAVED;

              try{
               getDataSource().setUserPassword( crd2persion( channelRuntimeData ),
                 ( managerMode?null: channelRuntimeData.getParameter( Constants.PWDFIELD )));
              }catch( Exception pwdchng ) {

                if( pwdchng.getMessage()
                       .equals( Constants.ERRMSG_PWDNOTMATACHED ))
                   message_to_user_about_action = pwdchng.getMessage();
                  else
                   throw pwdchng;

               }// catch

              mode = Constants.MODEDISPLAY;

              personalDocument = null;

              break;
            }// 8

            case 9: {  // delete user

              getDataSource().removeUser( crd2persion( channelRuntimeData ));

              mode = Constants.MODEDISPLAY;

              personalDocument = null;

              break;
            }// 9

            case 10: {  // about

              mode = Constants.MODEABOUT;

              break;
            }// 10

            case 11: {  // help

              mode = Constants.MODEHELP;

              break;
            }// 11

            default: {
              mode = Constants.MODEDISPLAY;
              channelRuntimeData.remove( Constants.UNFIELD );
            }// default

        }// switch
     }// if

     if( !managerMode && personalDocument == null
              && !mode.equals(Constants.MODEABOUT) && !mode.equals(Constants.MODEHELP) ) // always override
       mode = Constants.MODEDISPLAY;  // force a read

     // look up the person we are supposed to display
     if( mode.equals( Constants.MODEDISPLAY ) || mode.equals( Constants.MODEADD ))
        people = new IPerson[] { getDataSource().getUser(
          ( channelRuntimeData.getParameter( Constants.FORMCHOSEN ) == null?
            ( channelRuntimeData.getParameter( Constants.UNFIELD ) == null?
             (String)channelStaticData.getPerson().getAttribute( Constants.ATTRUSERNAME )
               : channelRuntimeData.getParameter( Constants.UNFIELD ))
                 : channelRuntimeData.getParameter( Constants.FORMCHOSEN ) )) };


     if( !managerMode && !mode.equals(Constants.MODEABOUT) && !mode.equals(Constants.MODEHELP) ) // always override
       mode = Constants.MODEPWDCHNG;

     if( (managerMode || ( !managerMode && personalDocument == null ))
             && !mode.equals(Constants.MODEABOUT) && !mode.equals(Constants.MODEHELP) ) {
       doc = DocumentFactory.getNewDocument();

       // fill in info about the user
       Element outtermost = doc.createElement( "people" );
       Element person;

       for( int i = 0; i < people.length; i++ ) {

          person = doc.createElement( "person" );
          Element attr = null;

          String worker = null;
          Enumeration E = people[ i ].getAttributeNames();
          while( E.hasMoreElements()) {

            worker = (String)E.nextElement();

              attr = doc.createElement( worker );
              attr.appendChild( doc.createTextNode(
                     (String)people[ i ].getAttribute( worker )));

              person.appendChild( attr );
          }// while

          outtermost.appendChild( person );
       }// for

       doc.appendChild( outtermost );
       // end - fill in info about the user

       if( !managerMode )
         personalDocument = doc;

      }else{

       doc = personalDocument;
     }// if

     // Create a new XSLT styling engine
     XSLT xslt = XSLT.getTransformer( this, channelRuntimeData.getLocales());
//     XSLT xslt = new XSLT( this );

     // we could have a blank document, help and about
     if( doc == null ) {
      doc = DocumentFactory.getNewDocument();
      doc.appendChild( doc.createElement( mode )); // null;
     }// doc null

     // pass the result XML to the styling engine.
     xslt.setXML( doc );

     // specify the stylesheet selector
     xslt.setXSL( Constants.SSLFILE, mode, channelRuntimeData.getBrowserInfo());

     // set parameters that the stylesheet needs.
     xslt.setStylesheetParameter( Constants.BASEACTION, channelRuntimeData.getBaseActionURL());
     xslt.setStylesheetParameter( Constants.MODE, mode );
     xslt.setStylesheetParameter(
                  Constants.DISPLAYMESSAGE, message_to_user_about_action );

     String MM = (!managerMode?"yes":"no");
     xslt.setStylesheetParameter( Constants.MODEUSRPWDCHNG, MM );

  /** If I write the above as shown below it does not work.  Wasted a .5hr on that! */
//     xslt.setStylesheetParameter( Constants.MODEUSRPWDCHNG, (!ManagerMode?"yes":"no"));

     // set the output Handler for the output.
     xslt.setTarget( out );

     // do the deed
     xslt.transform();

   }catch( Exception e ){
      log.error(e,e);

      throw new PortalException(
                   (e.getMessage()!=null?e.getMessage():e.toString()));
    }// catch
  }// renderXML

  private IDataHandler getDataSource() throws Exception {

    if( datasource == null )
      datasource = (IDataHandler)
       Class.forName(
         (
           channelStaticData.getParameter( Constants.CHNPARAMDATAHANDLER ) == null?
             Constants.DEFAULTDATAHANDLER
             :
             channelStaticData.getParameter( Constants.CHNPARAMDATAHANDLER )
         )
        ).newInstance();

    return datasource;
  }// getDataSource

  private IPerson crd2persion( ChannelRuntimeData CRD ) throws Exception {

      String worker = null;
      IPerson newborn = new PersonImpl();
      Enumeration E = CRD.getParameterNames();
      while( E.hasMoreElements()){
         worker = (String)E.nextElement();
         if( !worker.equals( Constants.FORMACTION ))
             newborn.setAttribute( worker,
              ( CRD.getParameter( worker ) == null?
                 "" : CRD.getParameter( worker ) ));

      }// while

      return newborn;
  }// crd2persion

}// eoc
