The package `org.jasig.portal.web.servlet` and sub-packages will be component-scanned by the "api" Spring Web MVC
context in uPortal.

In practice that means that e.g. `@Controller`s will be wired up to currently answer at /api, as in a Controller like

    @Controller
    @RequestMapping("/marketplace/**")
    public class MarketplaceServletController {
    ...

will answer at something like

    https://my.awesome.edu/portal/api/marketplace/

This is kind of silly in that really this package should wire into a separate Spring Web MVC context from the api
context, and have a path more like /web/... , but, baby steps.
