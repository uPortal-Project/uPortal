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

### Examples

This example illustrates non-unique integer IDs that will result in an error because the integer portion of the ID of both folder node and channel node are '6':

```
    <folder ID="s6" hidden="false" immutable="true" name="Footer Second"
type="footer-second" unremovable="true">
      <channel fname="page-bottom" unremovable="true" hidden="false"
immutable="false" ID="n6"/>
    </folder>
```

Tweaking that so that the integer portion of the `ID`s differ solves the ID collision problem:

```
    <folder ID="s6" hidden="false" immutable="true" name="Footer Second"
type="footer-second" unremovable="true">
      <channel fname="page-bottom" unremovable="true" hidden="false"
immutable="false" ID="n8"/>
    </folder>
```

Note that while these examples just show little snippets of what might be in fragment-layout.xml files, the identifier uniqueness constraints must be respected throughout each of those files individually, regardless of parent-child relationships of nodes within the file.

However, before you get too excited about changing node IDs to resolve conflicts, be sure to read the rest of this README. :)

Considerations in changing and re-using node IDs
------------------------------------------------

* End users may have stored directives describing their changes to their experience of fragment layouts, referencing fragment layout nodes by `ID`.
* If you re-use a node `ID` in a changed version of a fragment-layout, prior directives will try to apply to the changed node.
* In practice this mostly means don't re-use node IDs.
* To facilitate avoiding re-use, it's a good idea to note previously used node IDs in a comment in the particular `*-fragment-layout.xml` file when removing them.
