# Setup a fragment layout by modifying fragment-layout XML files

Source: https://wiki.jasig.org/display/UPM43/Setup+a+fragment+layout+by+modifying+fragment-layout+XML+files

This page is a work in progress.... It's in pretty good shape at this point
though.

You can create a new fragment layout by modify creating a new fragment-layout
XML file, or alter an existing fragment layout by modifying an existing
fragment-layout XML file.  When done, you import the fragment using one of the
data import approaches (see Importing and Exporting data).

The fragment-layout XML files are a hierarchy of folders and channels with some
additional attributes and elements to help define the page structure.  An
example is:

Example fragment layout file

<?xml version="1.0" encoding="UTF-8"?>
<layout xmlns:dlm="http://www.uportal.org/layout/dlm" script="classpath://org/jasig/portal/io/import-layout_v3-2.crn"
  username="welcome-lo" >
  <folder ID="s1" hidden="false" immutable="false" name="Root folder" type="root" unremovable="true">
    <!--
     | Hidden folders do not propagate to regular users, and fragment owner
     | accounts don't receive (other) fragments at all;  Fragment owners must
     | have their own copies of the minimal portlets required to view and manage
     | their own layouts.
     +-->
    <folder ID="s2" hidden="true" immutable="true" name="Page Top folder" type="page-top" unremovable="true">
      <channel fname="dynamic-respondr-skin" unremovable="false" hidden="false" immutable="false" ID="n3"/>
      <channel fname="fragment-admin-exit" unremovable="false" hidden="false" immutable="false" ID="n4"/>
    </folder>
    <folder ID="s5" hidden="true" immutable="true" name="Customize folder" type="customize" unremovable="true">
      <channel fname="personalization-gallery" unremovable="false" hidden="false" immutable="false" ID="n6"/>
    </folder>
    <folder ID="s7" dlm:deleteAllowed="false" dlm:editAllowed="false" dlm:moveAllowed="false" hidden="false" immutable="false" name="Welcome" type="regular" unremovable="false">
      <structure-attribute>
          <name>externalId</name>
          <value>welcome</value>
      </structure-attribute>
      <folder ID="s8" hidden="false" immutable="false" name="Column" type="regular" unremovable="false">
        <structure-attribute>
          <name>width</name>
          <value>60%</value>
        </structure-attribute>
        <channel fname="email-preview-demo" unremovable="false" hidden="false" immutable="false" ID="n9" dlm:moveAllowed="false" dlm:deleteAllowed="false"/>
        <channel fname="weather" unremovable="false" hidden="false" immutable="false" ID="n10"/>
        <channel fname="pbookmarks" unremovable="false" hidden="false" immutable="false" ID="n11" dlm:moveAllowed="false" dlm:deleteAllowed="false"/>
      </folder>
      <folder ID="s12" hidden="false" immutable="false" name="Column" type="regular" unremovable="false">
        <structure-attribute>
          <name>width</name>
          <value>40%</value>
        </structure-attribute>
        <channel fname="calendar" unremovable="false" hidden="false" immutable="false" ID="n13"/>
      </folder>
    </folder>
  </folder>
</layout>

The fragment-layout XML file has the following, hierarchical structure:

A <layout> node;  this is the root node of the document and contains a few
important attributes

A <folder type="root"> node that is the only direct child of <layout>;
ll layout contents are descendants of this element
Additional <folder> elements of various types including regular, page-top,
pre-header, customize, etc.;  regular folders define the tab/column contents of
the page;  non-regular folders place content into Regions
<folder> elements representing Regions contain <channel>
elements directly
<folder> elements that represent tabs (type="regular") contain <folder
name="Column" type="regular"> elements
<channel> elements that identify portlets by fname and thereby bring them into
the layout
Optional <structure-attribute> nodes


Hidden Folders

Fragment layouts typically contain hidden folders (hidden="true") with
administrative portlets required to allow the Manage Layouts Admin UI to
function properly when editing a fragment layout. Hidden folders pass their
contents to the fragment owner account only – not to audience members of the
fragment.

Layout node

Root node of the XML file.

xmlns:dlm

Namespace identifying the schema to parse the XML file. Must be
http://www.uportal.org/layout/dlm.	Y

script

Location of the script to process the XML file	Y
username	Name of the fragment owner that can manage the fragment. When
managing the fragment using the admin UI, this is the local account that you
impersonate to manage the fragment layout. By convention it is
"fragmentOwnerName-lo" where fragmentOwnerName is a name that semantically
conveys what the content is (perhaps the tab name) and lo stands for layout
owner (ex: "welcome-lo") . The fragment owner account must exist.	Y
Folders (including column folders)

Elements of folder hold other folders or lists of portlets.  Folders are
inherently just containers of items, but the theme XSL files use the attribute
values and the nesting levels to determine how to render the page to the user.
Folders have different options available depending upon location.

The following are the general attributes for folders.  Specific folder
subsections clarify usage.
ID

ID value. By convention folders are "s#" and channels are "n#" where # is a
numeric value. The letter designations are arbitrary. The numeric portion must
be a unique numeric value (unique among all folders and channels) within the XML
file; e.g. you cannot have both an "s1" and an "n1" in the same XML file as the
"s" and "n" are not stored to the database.

User customizations to a page are represented in the database by "remove ID x"
so once a layout is used in production, you must not change the existing ID
values or re-use ID values or uPortal will not be able to properly render a
user's page that has modified their layout.

After modifying a layout with the Admin UI you should export the fragment-layout
and store it in your source code repository so you can recreate the layout in
other environments with ant initdb or ant initportal.
	 	Y
hidden

Set to false for folders that are displayed to users.

Set to true for folders that are not displayed to users, but are displayed when
you do select Manage DLM Fragments in the Admin UI.

Certain channels must exist for the fragment owner to edit fragments. These
channels include dynamic-respondr-skin (for Respondr them), fragment-admin-exit
(allows exiting Manage DLM Fragments user impersonation), and
personalization-gallery (allows using Customize to find and add portlets to the
page).
	 	Y
immutable	obsolete parameter from uPortal 2.x days	 	?	false
name	Name of the folder. If a folder is a tab, this is the name of the tab.
Columns by convention have a name of Column or perhaps Column1, Column2. Users
do not see column names.	 	Y
type

Type of the folder. Due to historic reasons, this generally indicates the
placement of the contents of the folder on the page. Options are:

    regular - content placed in a tab or column (depends upon level in the
    hierarchy)
    region name - name of a region (see Respondr Regions Feature)

	 	Y
unremovable	Obsolete parameter from earlier uPortal releases. Do not use.	 	N	false
dlm:addChildAllowed	For a tab folder or column folder, determines whether a user who has permissions to customize their layout can add children (columns for tabs, portlets for columns) to the folder.	hasColumnAddChildAllowed	N	true
dlm:deleteAllowed	For a tab folder or column folder, determines whether a user who has permissions to customize their layout can remove the item (tab for tab, column for column) to the folder.	deleteAllowed	N	true
dlm:editAllowed	For a tab folder, determines whether a user who has permissions to customize their layout can rename the folder (practically only useful on a tab to rename the tab)	editAllowed	N	true
dlm:moveAllowed	For a tab folder or column folder, determines whether a user who has permissions to customize their layout can move the tab or column on the page	moveAllowed	N	true
Channels

Elements of type channel are publications of portlets on the page.  A channel may appear multiple times on the page, though it is generally not recommended.
ID

ID value. By convention folders are "s#" and channels are "n#" where # is a numeric value. The letter designations are arbitrary. The numeric portion must be a unique numeric value (unique among all folders and channels) within the XML file; e.g. you cannot have both an "s1" and an "n1" in the same XML file as the "s" and "n" are not stored to the database.

User customizations to a page are represented in the database by "remove ID x" so once a layout is used in production, you must not change the existing ID values or re-use ID values or uPortal will not be able to properly render a user's page that has modified their layout.

After modifying a layout with the Admin UI you should export the fragment-layout and store it in your source code repository so you can recreate the layout in other environments with ant initdb or ant initportal.
	Y
fname	Unique name of the portlet created when publishing the portlet.	Y
hidden

obsolete parameter from uPortal 2.x days. Always set to false
	?
immutable	obsolete parameter from uPortal 2.x days. Always set to false	?	false
unremovable	Obsolete parameter from earlier uPortal releases. Do not use.	N	false
dlm:deleteAllowed	Determines whether a user who has permissions to customize their layout can delete this portlet	N	true
dlm:moveAllowed	Determines whether a user who has permissions to customize their layout can move this portlet.	N	true
Structure-attribute

Child elements of a structure-attribute element vary depending upon where this
is at in the hierarchy.  The structure XSLT transform doesn't care whether the structure-attributes apply to a tab or column, but the theme XSLT transform does
as it acts upon the value when present at a specific level in the folder
hierarchy.  Structure attributes have the form:

<structure-attribute>
   <name>width</name>
   <value>40%</value>
</structure-attribute>

For a structure attribute name to be valid, it must be defined in the structure
stylesheet definition file.  See https://github.com/uPortal-project/uPortal/blob/uportal-4.2.1/uportal-war/src/main/data/required_entities/stylesheet-descriptor/DLMTabsColumns.stylesheet-descriptor.xml#L49-L67 as an
example.

New with uPortal 4.3:  The value field can be a Spring Expression Langugage
(SpEL) expression (see http://docs.spring.io/spring/docs/4.0.5.RELEASE/spring-framework-reference/html/expressions.html) of the form ${expression} where the expression is of the form:

    ${request.method}. Access to Http Request. Example: ${request.contextPath}
    ${person.method}. Access to Person object. Example: ${person.attributeMap['collegge']}
    ${@bean.method}. Access to arbitrary beans by name in the root application context. Example: ${@PortalDb.class.toString()?:unknown}

Child of tab folder:
external-id	URL-safe-value

Indicates folder has an external ID name. Allows creating an URL of the form
/f/URL-safe-value (see  Consistent Portal URLs ("Deep Linking").
tabGroup	tab group name	Used when the optional feature of tab groups is
enabled. See TabGroups ('Super-tabs') Feature for more information.

Child of a column folder:
width	percentage

Defines the width of the column in percentage; e.g. 50%. The column widths in a
tab/row should add up to 100

Additional References

Overview

Don't forget that in addition to creating the *-fragment-layout.xml file (like https://github.com/uPortal-project/uPortal/blob/uportal-4.2.0/uportal-war/src/main/data/quickstart_entities/fragment-layout/academics-lo.fragment-layout.xml), you also need to:

    Create a corresponding *-fragment-definition.xml file (see https://github.com/uPortal-project/uPortal/blob/uportal-4.2.0/uportal-war/src/main/data/quickstart_entities/fragment-definition/academics-lo.fragment-definition.xml for an example)
    Create a user corresponding to the username attribute in your fragment layout XML file (see https://github.com/uPortal-project/uPortal/blob/uportal-4.2.0/uportal-war/src/main/data/quickstart_entities/user/academics-lo.user.xml for an example)
    Add the user to either the 'Fragment Owners' group or 'Subscribable Fragments' group. (see https://github.com/uPortal-project/uPortal/blob/uportal-4.2.0/uportal-war/src/main/data/quickstart_entities/group_membership/Fragment_Owners.group-membership.xml for an example).
