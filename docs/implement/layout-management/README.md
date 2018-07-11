# Layout management

( Source: https://wiki.jasig.org/display/UPM43/Overview)

Layout is the structure of what the user sees: what items of content, where, in
what order, etc.

Layout management is system for configuring, administering, and executing
determining layouts for users. This sub-system is called "Distributed Layout
Management" (often, "DLM"), for historical reasons. DLM merges "distributed"
bits of layout into the layout a given user experiences.

Layout management in uPortal can

+ maintain multiple layout templates for layouts or parts of layouts
+ merge these templates into a unified experience for a given user according to
  defined rules about who should see what
+ honor or restrict end user preferences customizing these templates for the
  individual user
+ degrade gracefully according to the user's permissions, suppressing content
  for which the user lacks authorization

This means that you can

+ present different layout templates to different groups of users
+ share layout templates across groups of users
+ include in layout templates items that only some of the template's audience
  will have authorization to see
+ optionally constrain end users from messing with the layout templates as they
  experience them

[Presentation by Andrew Petro](http://www.youtube.com/watch?v=YSTONxaX8rc)

## What are fragments?

A "Fragment" is Born

The resulting layouts-to-be-pushed to end users were modified when
included in end user's layouts. For example, end user's
already had a header and footer folder containing channels used by the theme
stylesheet. These were necessary when the editor of the layout-to-be-pushed
logged in to edit the layout but were not needed by the user of the layout.
Therefore, such pieces were dropped during loading for merging into end user's
layouts. As such, the term, layout fragment, was coined to identify the
remaining layout piece that was merged into user's layouts.

Finally, to convey to the system what user accounts were to be used as fragments
an XML file (dlm.xml) was used. Put simply, it told the system what fragments
existed, what user accounts were used to provide the layout of each, and what
users would receive each fragment. This file was named dlm.xml and was read each
time that the system started up allowing the portal to then pre-load each of the
layouts of the indicated fragments and special classes used to determine which
users, the audience of the fragment, received each fragment when they logged in.


## The dlm.xml File

DLM leverages
existing, reliable pieces of the portal to produce layout
fragments to be pushed to users. Specifically, fragments are modeled using
regular portal
accounts to define what each fragment should look like. A configuration file,
dlm.xml defines fragments, their audiences,
and the special portal accounts used to set up their layouts.

The layout management system in uPortal expects dlm.xml to have a particular
structure.

The dlm.xml file is located in the properties directory. The top level element
in dlm.xml is the <managedLayoutFragments> element. It includes the namespace
declaration for DLM used to prefix all expected DLM property, fragment, and
audience elements. This element appears as follows in dlm.xml.

<managedLayoutFragments xmlns:dlm="http://org.jasig.portal.layout.dlm.config">

The only two supported child elements for managedLayoutFragments are
<dlm:property> and <dlm:fragment>. Others may be included but they will be
ignored. The <dlm:property> element has two required attributes, 'name' and
'value'. There are currently only two properties supported by DLM and if not
defined, suitable defaults will be used as explained below.

DLM Restrictions

For this, an extended version of SimpleLayoutManagement's Preferences channel is
provided and is the channel seen when using DLM and the Preferences link is
selected. For non-fragment users they see that channel in the traditional
manner. For fragment owner the preferences channel changes slightly to indicate
the name of the fragment that is being edited as can be seen in the images
below. Additionally, controls are added to the user interface to provide the
ability to restrict what users of the fragment can do to fragment elements.
Tabs

In the image below the typical controls for a tab are shown along with some DLM
specific fragment-user-actions that can be restricted. By default all check
boxes are selected and hence all actions are allowed by end users. As seen in
the image, the "Useful News" tab in the News fragment's layout restricts
movement by end users of the fragment. To modify any of these actions the
appropriate checkbox should be checked or unchecked as needed and the "Set
Actions" button pressed. Each of these actions that can be restricted for tabs
is discussed below.

Move Tab

If moving a tab is restricted by a fragment owner then users will not be able to
move any of their personally-added tabs to the left of this tab. If any tabs
exist in the user's layout that come from a fragment with lower precedence as
declared in dlm.xml then the user will also be unable to move those tabs to the
left of this tab. Tabs from a fragment with higher precedence can be moved by
the end user to the left of this tab. If any of these lower precedence tabs or
user-added tabs were moved to the left of this tab prior to it being marked as
movement restricted then those tabs will be pushed to the right of this tab when
the user next logs back in. To the end user the inability to move a lower
precedence tab including those added by the user is depicted by the lack of the
buttons allowing the user to move the tab to the left as seen in the image
below.

Edit Properties

If editing tab properties is restricted by a fragment owner then users will not
be able to rename that tab in their layout. If a user has previously renamed
that tab and the fragment owner then marks the tab to prevent editing of
properties then the next time that the user logs in that tab name change will
be discarded and the name set by the fragment owner will reappear. When such a
tab is restricted from editing the controls for renaming that tab are removed
from the user's view when selecting that tab as shown in the image below. The
"Real Entertainment" tab has editing of properties restricted as can be seen by
the missing renaming elements when viewed by the student user.

Add Columns

If adding columns is restricted by a fragment owner then users will not be able
to add columns to that tab in their layout. If a user has previously added
columns to that tab and the fragment owner then marks the tab to prevent adding
columns then the next time that the user logs in those user columns will be
discarded. (There is currently no holding place to such discarded items. Such a
holding place similar to the waste basket concept could be added if there was a
pressing needed and a channel could be created to allow a user to restore those
items to some other location in their layout.) When such a tab is restricted
from adding columns the "Add Column" buttons are removed from the user's view
when selecting that tab as shown in the image below. The "Useful News" tab has
adding columns restricted as can be seen by the missing buttons in the spaces
between and to the left and right of columns when viewed by the student user.

Delete Tab

If deleting the tab is restricted by a fragment owner then users will not be
able to delete this tab from their layout. If a user has previously deleted this
tab and the fragment owner then marks the tab to prevent deleting then the next
time that the user logs in, that tab will reappear. When such a tab is
restricted from being deleted the "Delete this tab" link is removed from the
user interface for that tab when end users are editing their layout.

Columns

The action-restricting controls for columns are nearly identical to those of
tabs as is their application.

Move Column

If moving a column is restricted by a fragment owner then users will not be able
to move any columns that they have added to the tab to the left of this column
assuming that the tab allowed them to add their own columns. If any user-added
columns have were previously added to the left of this column and the fragment
owner marked this column as movement restricted then those user added columns
will be bumped to the right of this column the next time that the user logs in.
Additionally, the buttons for moving those user added columns to the left of
this column are removed from the user interface when a user is editing that tab
in their layout.

Edit Properties

Restriction of column property editing is identical to restriction of tab
property editing except that the only property that currently exists for columns
is width. If editing column properties is restricted by a fragment owner then
users will not be able to change the column's width. They can read it but they
can't change it as shown in the image below. Note that only one of these columns
is marked as being edit restricted.

Add Channels

If adding channels is restricted by a fragment owner then users will not be able
to add channels to that column in their layout. If a user had previously added
channels to that column and the fragment owner then marked the column to prevent
adding channels then the next time that the user logs in those user added
channels will be discarded. When such a column is restricted from adding
channels, all "Add Channel" buttons are removed from the user's view of that
column.

Delete Column

If deleting the column is restricted by a fragment owner then users will not be
able to delete this column from their layout. Additionally, the user will not be
able to delete the tab containing this column. When such a column is restricted
from being deleted the "Delete this column" link is removed from the user
interface for that column and the "Delete this tab" link is removed for its
containing tab when end users are editing their layout.

Channels

The action-restricting controls for channels vary from those of columns and tabs
in that only move and delete are restricted.

Move Channel

If moving a channel is restricted by a fragment owner then users will not be
able to move any channels that they have added above this channel assuming that
the column allowed them to add their own channels. If any channels were
previously added above this channel and the fragment owner marked this channel
as movement restricted then those user added channels will be bumped below this
channel the next time that the user logs in. Additionally, the buttons for
moving those user added channels above this channel are removed from the user
interface when a user is editing the column containing that channel in their
layout.

Delete Channel

If deleting a channel is restricted by a fragment owner then users will not be
able to delete that channel from their layout. Additionally, the user will not
be able to delete the containing column and tab. If any of these were previously
removed by the user and the fragment owner then marked that channel as
undeletable then the tab and all of its columns and channels would reappear the
next time that the user logged in. When such a channel is restricted from being
deleted the delete button for the channel is removed from the user interface
when an end user is editing their layout. Additionally, the "Delete this column"
link is removed from the user interface for that channel's containing column and
the "Delete this tab" link is removed for its containing tab.

The 'defaultLayoutOwner' Property

If defined, the value of this property should be the user ID (ie: the log-in ID)
of an account whose layout should be copied for all new fragment owners. When a
fragment is defined in dlm.xml it includes an ownerID attribute. (See the
related section on defining fragments.) Since fragments are layouts for special
accounts then these accounts must have a layout. When regular users log in for
the first time and have no layout, the portal gives them a copy of a default
account's layout as declared in portal.properties. This default should not be
used for fragment owner accounts since it will result in that entire layout
being pushed to all users. The approach taken by the default dlm.xml is to use a
special account included in the portal. To change this account you can use the
following credentials.

User Id: fragmentTemplate, Password: fragmentTemplate

Warning: It is strongly recommended that you do not change this user account's
layout. It is empty except for hidden content like the header and footer folders
and their channels. The layout for this account including hidden but necessary
content is copied whenever a new fragment owner is declared in dlm.xml. The
layout for the owner account is created immediately after adding the fragment's
declaration to dlm.xml and restarting the portal. These accounts should be empty
until such time that the account can be accessed and the proper layout set up
that should be pushed for that fragment.



The 'org.jasig.portal.layout.dlm.RDBMDistributedLayoutStore.fragment_cache_refresh' Property

For performance, DLM caches the layout for each defined fragment in memory. When
layout owners modify the layout, those changes are pushed into the database and
into the in-memory cache in the server handling the session for that account. If
multiple servers are employed, the other servers will not see these changes made
by the fragment owner. This property determines how often the cache of fragment
layouts is reloaded to force such changes to appear in other servers. This does
not include instantiating new fragments declared in dlm.xml. This only forces a
reload of the fragments seen in dlm.xml when the servers were last restarted.

The 'allowExpandedContent' Property

For uPortal 3.2.5 and later, DLM was enhanced to support Expanded Content: for
fragments: these are portlets outside of tabs – typically in the header and/or
footer area – that get passed on to fragment audience members in the same way
that tabs do. The allowExpandedContent feature is disabled by default in uPortal
3.2.x, but will be enabled (by default) in the next minor release of uPortal.

DLM Expanded Content is a powerful and compelling approach to managing non-tab
content for all the same reasons that DLM fragments are better than Template
Users for managing administrator-provided tab content. Specifically, it's very
difficult to make changes to Template User content once individual users have
their own layouts in the database. For this reason, uPortal admins should
provision content to users through DLM only; individual users can still add,
move, and remove layout content for their own layouts as before.

Warning

Earlier distributions of uPortal, even distributions of uPortal 3.2, included
the same header and footer content in layouts of every variety: users, template
users, and fragment owners. This data is incompatible with the
allowExpandedContent feature because it will cause users to have the same
portlets several times.

This result would have negative effects both visually and on performance. It was
for this reason that allowExpandedContent was disabled by default in uPortal
3.2. If you want to use it, you must first make data adjustments to prevent
these issues from occuring. The easiest way is to remove all existing header and
footer portlets from DLM fragment owner layouts and from the fragmentTemplate
layout. It is vital to make sure that users don't get the same header/footer
portlet(s) from both template users and DLM fragments.
