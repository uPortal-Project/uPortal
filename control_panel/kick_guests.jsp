<!DOCTYPE html PUBLIC "-//w3c//dtd html 4.0 transitional//en">

<html>
  <head>
    <meta http-equiv="Content-Type" content=
    "text/html; charset=iso-8859-1">
    <meta name="GENERATOR" content=
    "Mozilla/4.76 [en] (X11; U; Linux 2.2.19-4.1mdk i686) [Netscape]">

    <title>Kick Guests: <%=request.getServerName() %>:<%=request.getServerPort()%></title>
  </head>

  <body bgcolor="#FFFFFF">
    <p>&nbsp;</p>

    <table width="100%">
      <tr>
        <td>Control Panel: Kick Guests</td>

        <td><a href="index.jsp">[Stats]</a> <a href=
        "kick_users.jsp">[Kick Users]</a> </td>
      </tr>
    </table>
    <br>

    <p>&nbsp;</p>

    <form action="kick_guests_done.jsp" method="GET">
      <table>
        <tr>
          <td>This is all or nothing.&nbsp; Clicking the button
          below will invalidate ALL&nbsp;guest user
          sessions.&nbsp; Only do this if your server is about to
          crash.</td>

          <td>
          </td>
        </tr>

        <tr>
          <td>
          </td>

          <td>
          </td>
        </tr>

        <tr>
          <td>Type the word "YES" in caps here:&nbsp; <input name="requiredYes"
          size="3"></td>

          <td>
          </td>
        </tr>

        <tr>
          <td>
          </td>

          <td>
          </td>
        </tr>

        <tr>
          <td><input type="submit" value="KICK GUESTS"></td>

          <td>
          </td>
        </tr>
      </table>
    </form>
  </body>
</html>


