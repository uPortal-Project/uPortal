Heads up when editing fragment layout files:
============================================

These are considerations both for uPortal developers editing the uPortal product out of the box entities and (more so?) for uPortal adopters who start from these out of the box entities and continue to update them to local portal needs.

File names are not controlling
------------------------------

* While it's a great practice to name fragment-layout.xml files to reflect what fragment layout they're defining, uPortal doesn't use the file name to e.g. imply the fragment owner user whose layout the file defines.  Yet.
* Rather, the username whose layout is being defined is specified as a `username` attribute on the `<layout>` element.

Node ID uniqueness constraints
------------------------------

* XML node `ID` attribute uniqueness must be observed within a given `*-fragment-layout.xml` file.
* Furthermore, **the integer portion of** node `ID`s must be unique within a given `*-fragment-layout.xml` file.

Considerations in re-using node IDs
-----------------------------------

* End users may have stored directives describing their changes to their experience of fragment layouts, referencing fragment layout nodes by `ID`.
* If you re-use a node `ID` in a changed version of a fragment-layout, prior directives will try to apply to the changed node.
* In practice this mostly means don't re-use node IDs.
* To facilitate avoiding re-use, it's a good idea to note previously used node IDs in a comment in the particular `*-fragment-layout.xml` file when removing them.
