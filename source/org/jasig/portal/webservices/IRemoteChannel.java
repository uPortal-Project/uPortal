package org.jasig.portal.webservices;

import java.util.Map;
import javax.servlet.http.Cookie;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public interface IRemoteChannel {

  /**
   * Authenticates user and establishes a session.
   * @param username the user name of the user
   * @param password the user's password
   * @throws Exception if there was a problem trying to authenticate
   */
  public void authenticate(String username, String password) throws Exception;


  /**
   * Unauthenticates a user, killing the session.
   * @throws Exception if there was a problem trying to logout
   */
  public void logout() throws Exception;


  /**
   * Establishes a channel instance which the webservice client will communicate with.
   * @param fname an identifier for the channel unique within a particular portal implementation
   * @return instanceId an identifier for the newly-created channel instance
   * @throws Exception if the channel cannot be located
   */
  public String instantiateChannel(String fname) throws Exception;


  /**
   * Asks the channel to render content and return it as a String.
   * The content will be well-formed XML which the client must serialize.
   * @param instanceId an identifier for the channel instance returned by instantiateChannel()
   * @param headers a Map of headers (name/value pairs).
            One of the headers must be a "user-agent".
   * @param cookies an array of javax.servlet.http.Cookie objects.
            Can be null if there are no cookies to send.
   * @param parameters a Map of request parameter name/value pairs.
            Can be null if there are no request parameters.
   * @param baseActionURL a String representing the base action URL to which
            channels will append '?' and a set of name/value pairs delimited by '&'.
   * @return xml an XML element representing the channel's output
   * @throws Throwable if the channel cannot respond with the expected rendering
   */
  public Element renderChannel(String instanceId, Map headers, Cookie[] cookies,
                               Map parameters, String baseActionURL) throws Throwable;

  /**
   * Indicates to the portal that the web services client is finished
   * talking to the channel instance.
   * @param instanceId an identifier for the channel instance returned by instantiateChannel()
   * @throws Exception if the channel cannot be freed
   */
  public void freeChannel(String instanceId) throws Exception;

}