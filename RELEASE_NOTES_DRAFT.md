uPortal 4.2.0-SNAPSHOT release notes
=====================================

(There's a Meta section at the end of the file clarifying what this file is and how it works. :) ).


**uPortal 4.2 is uPortal 4.1 except better.**

* Better **Marketplace**, something much closer to feature-complete than that shipping in uPortal 4.1.  You might even be willing to put this Marketplace in front of real users.
* Better **APIs**.  Especially JSON web service APIs.  Also improvements to Java APIs modeling users and to plugin points in the rendering pipeline and for user profile selection.
* Better experiences.  A user-facing dialog inviting session extension when session timeout expires.  Better messaging to users when portlets are taken out of service for maintenance.
* Better **tools**.  A much more attractive Portlet Manager and Permissions Manager.  Command line conveniences and a better command line build implementation.
* Better logging for better troubleshooting.

Pull requests that were merged for inclusion in this release:

* [Marketplace](https://github.com/Jasig/uPortal/pull/423) with [asynchronous](https://github.com/Jasig/uPortal/pull/448) [cache](https://github.com/Jasig/uPortal/pull/411) population, [layout adding](https://github.com/Jasig/uPortal/pull/419), [enforcement of BROWSE permission on related portlets](https://github.com/Jasig/uPortal/pull/481) and [client-side screenshot validation](https://github.com/Jasig/uPortal/pull/453).
* [A](https://github.com/Jasig/uPortal/pull/426) [much](https://github.com/Jasig/uPortal/pull/445) [nicer](https://github.com/Jasig/uPortal/pull/458) Portlet Manager, with [tooltips in the UI](https://github.com/Jasig/uPortal/pull/492) and [better](https://github.com/Jasig/uPortal/pull/451) [documentation](https://github.com/Jasig/uPortal/pull/493) to disambiguate portlet names, titles, and fnames. [Other](https://github.com/Jasig/uPortal/pull/441) administrative UIs got better too. And a new [Maintenance portlet lifecycle state](https://github.com/Jasig/uPortal/pull/397).
* [Fixed](https://github.com/Jasig/uPortal/pull/431) [Google](https://github.com/Jasig/uPortal/pull/425) [Analytics](https://github.com/Jasig/uPortal/pull/389) integration.
* A [session timeout](https://github.com/Jasig/uPortal/pull/392) [dialog](https://github.com/Jasig/uPortal/pull/424)
* [Inline](https://github.com/Jasig/uPortal/pull/446) portlet configuration
* Better search results for [Simple Content portlets](https://github.com/Jasig/uPortal/pull/440).
* [App Launcher portlet type](https://github.com/Jasig/uPortal/pull/421) with a handy [six column layout](https://github.com/Jasig/uPortal/pull/468) to place them in.
* Better [JSON APIs](https://github.com/Jasig/uPortal/pull/449) [including fnames](https://github.com/Jasig/uPortal/pull/464).  Better [IPerson](https://github.com/Jasig/uPortal/pull/460) API.
* Better handling of [access to portlets not in one’s layout](https://github.com/Jasig/uPortal/pull/480), also [for unauthenticated users](https://github.com/Jasig/uPortal/pull/485) [in transient layouts](https://github.com/Jasig/uPortal/pull/483) no less
* Filters [Respondr regions out of mUniversality](https://github.com/Jasig/uPortal/pull/428) and adds [Google Analytics](https://github.com/Jasig/uPortal/pull/376) in.
* Handy [administrative access to dynamic skin configuration](https://github.com/Jasig/uPortal/pull/471)
* [Saving a layout change as one AJAX call rather than two](https://github.com/Jasig/uPortal/pull/495)
* [Updated PostgreSQL dependency](https://github.com/Jasig/uPortal/pull/489)
* A SmartLdap group store that’s [less weird](https://github.com/Jasig/uPortal/pull/443).  Still smart.
* Use of [CSS animation rather than jQuery animation](https://github.com/Jasig/uPortal/pull/476), with the added bonus of working.  [Better `showchrome = false` styling](https://github.com/Jasig/uPortal/pull/469), again with the workings.
* [Sticky profile](https://github.com/Jasig/uPortal/pull/450) selections, in [transactions](https://github.com/Jasig/uPortal/pull/459), with a fancy [createOrUpdate DAO API](https://github.com/Jasig/uPortal/pull/479),  but [not for the guest user](https://github.com/Jasig/uPortal/pull/482), and [with](https://github.com/Jasig/uPortal/pull/454) [graceful](https://github.com/Jasig/uPortal/pull/455) [failure](https://github.com/Jasig/uPortal/pull/456) and [logging](https://github.com/Jasig/uPortal/pull/437).
* [MarketplaceEntry](https://github.com/Jasig/uPortal/pull/488),  [MarketplacePortletDefinition](https://github.com/Jasig/uPortal/pull/487), and [PortletDefinitionImpl](https://github.com/Jasig/uPortal/pull/474) nicities, with [keywords](https://github.com/Jasig/uPortal/pull/486), [launching better URLS](https://github.com/Jasig/uPortal/pull/463) [to specified target windows](https://github.com/Jasig/uPortal/pull/470).
* Better [portlet failure logging](https://github.com/Jasig/uPortal/pull/442) and [logging on DLM fragment audience determination](https://github.com/Jasig/uPortal/pull/402).
* Better tools for [schema update generation](https://github.com/Jasig/uPortal/pull/417) and [deploying XSLT and LESS files](https://github.com/Jasig/uPortal/pull/409), and you can even run them under [`Ant 1.9.3+`](https://github.com/Jasig/uPortal/pull/386)
* Rendering [pipeline](https://github.com/Jasig/uPortal/pull/432) [tricks](https://github.com/Jasig/uPortal/pull/435) [to](https://github.com/Jasig/uPortal/pull/434) [conditionally](https://github.com/Jasig/uPortal/pull/433) terminate in a [redirect](https://github.com/Jasig/uPortal/pull/436).
* Better [Groovy compilation](https://github.com/Jasig/uPortal/pull/475) implementation in a [build process without focus stealing](https://github.com/Jasig/uPortal/pull/472)
* [Unit testing that respondr.xsl compiles](https://github.com/Jasig/uPortal/pull/484), now that everyone is adopting it.
* Updated [guidance for contributing](https://github.com/Jasig/uPortal/pull/461).  You should.




--------------------------------------------------------------------------------------

Meta
====

What is this file ?
-------------------

This file accumulates release notes for a uPortal release as we work towards that release.
Specifically, this file is accumulating release notes for the uPortal 4.2.0 release.

How does this file relate to releaseNotes.html ?
------------------------------------------------

**This file is primarily a collaboration tool for uPortal developers.**
It provides a place to collaborate on drafting the release notes in the same way, in the same
commits and pull requests, as collaborating on the software that will be, notably, released.

This file seeds the GitHub release notes upon product release.

The `releaseNotes.html` file links to (a page that links to) those release notes.

What should uPortal adopters do with this file?
-----------------------------------------------

uPortal adopters don't need to worry about this file.

Feel free to read it, of course, and it may be helpful in understanding the changes in flight

What should developers do with this file?
-----------------------------------------

See also `CONTRIBUTING.md`.

Commits (and thus, Pull Requests) that make notable changes to uPortal
should also update this release notes file
so that the human-readable release notes are collaboratively gathered.

This reduces the deferred effort in the way of cutting a uPortal release.


What should the release engineer do with this file?
---------------------------------------------------

The release engineer should:

1. Use the content below as a starting point for writing the release notes for posting with the
release in GitHub.
2. Remove this file from the codebase just before running the `mvn release:prepare` step.  This
is important so that a "DRAFT" release notes file is not included in the release tag and release
artifacts, which could confuse adopters.  Pointing them at the GitHub-hosted version of the
release notes rather than a Markdown file in the release itself gives the project a way to update
 the release notes after the release.
3. Add a fresh copy of this file, with the title updated, just after the
`mvn release:perform` step to set the stage for again continually gathering release notes content
 for the next release.  If the release process resulted in a new active branch, also seed that
 branch with a suitable version of this file.


Can't we just use the release notes generated by the issue tracker?
-------------------------------------------------------------------

No.  Issue trackers have their place, of course, but they're primarily a tool for coordinating
development and documenting defects.

Each release also needs an articulate, human-readble document that is **primarily a tool for
summarizing what's important about the release**.  You can't automate that.  But you can use
effective tooling (hello, Git! Markdown!) to collaborate on it rather than waiting until the
release to do it.





