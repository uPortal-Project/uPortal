Intention: document the web services available in uPortal.

# Web Services exposed by uPortal

TODO.


## User layouts

`/uPortal/api/v4-3/dlm/layout.json`

Included examples:

`example_guest_layout.json` : `layout.json` as experienced un-authenticated in default uPortal.

`example_student_layout.json` : `layout.json` as experienced logged in as `student` in default uPortal.

Structure:

`layout.json` returns a JavaScript map with these keys:

+ `user` : username. Example: "student" .
+ `authenticated` : "true" if logged in, "false" if not.
+ `hostname` : Example: `"localhost"`
+ `fragmentAdmin` : ?
+ `locale` : Example: `"en-US"`


+ `layout` : a map, with

+ `layout.globals` : a map, with


+ `layout.globals.userLayoutRoot` : Example: "root"
+ `layout.globals.hasFavorites` : Example: `"true"`
+ `layout.globals.activeTabGroup` : Example: `"DEFAULT_TABGROUP"`
+ `layout.globals.tabsInTabGroup` : Example: `"1"`
+ `layout.globals.userImpersonation` : ?


+ `layout.regions`: an array of regions , where regions are maps

+ `layout.regions[n].name` : Name of region. Example: `"header-right"`
+ `layout.regions[n].content` : Array of maps representing content.


+ `layout.navigation`: a map representing the user layout, with

+ `layout.navigation.allowAddTab` : true iff the user may add a tab
+ `layout.navigation.tabGroupsList` : represents the active and available tab groups
+ `layout.navigation.tabs` : array of maps representing tabs in the layout
+ `layout.navigation.tabs[n].content` : array of maps representing content within the tab, typically columns.
+ `layout.navigation.tabs[n].content[n].fname` : When a map in `content` is of `._objectType` `portlet`, it has an `fname` key with the `fname` of the portlet as its value.


+ `layout.favorites` : array of maps representing the user's favorite content.
+ `layout.favoriteGroups` : array of maps representing the user's favorite grouped content.

TODO: Document more of the `layout.json` API.
