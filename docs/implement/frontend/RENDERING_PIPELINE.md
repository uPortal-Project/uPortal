# Configuring the uPortal Rendering Pipeline

uPortal implements rendering of complete pages using a _pipeline_:  a nested structure of discrete,
pluggable elements that each implement the same Java interface.  The word "pipeline" is suitable
because it invokes the concepts of _movement_ and _throughput_, but in software this design is also
known as the [Decorator Pattern][].

The Java interface at the center of the uPortal Rendering Pipeline is `IPortalRenderingPipeline`.
Instances of `IPortalRenderingPipeline` are Spring-managed beans.  The primary rendering pipline
bean is assigned an id (in Spring) of `portalRenderingPipeline`.  Components in other parts of the
portal (outside the Rendering Pipeline) use this bean (exclusively) to interact with rendering in
the portal.

The `IPortalRenderingPipeline` interface defines only one method:

``` java
public void renderState(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException;
```

## uPortal Standard Pipeline

The "standard" rendering pipeline is the one that comes with uPortal out-of-the-box:  by default,
uPortal 5 uses the same rendering pipeline configuration as uPortal 4.3 -- based on an instance of
`DynamicRenderingPipeline` that contains a number of _components_.

The components within `DynamicRenderingPipeline` also each implement a single interface:
`CharacterPipelineComponent`, which itself extends
`PipelineComponent<CharacterEventReader, CharacterEvent>`.  (The standard rendering pipeline is
hard-wired for XML/XSLT.)  Each component implements a discrete step in the rendering of a page
request.

The standard pipeline includes (as of this writing) the following components (steps):

1.  `analyticsIncorporationComponent`
2.  `portletRenderingIncorporationComponent`
3.  `portletRenderingInitiationCharacterComponent`
4.  `themeCachingComponent`
5.  `postSerializerLogger`
6.  `staxSerializingComponent`
7.  `postThemeTransformLogger`
8.  `themeTransformComponent`
9.  `preThemeTransformLogger`
10. `themeAttributeIncorporationComponent`
11. `portletRenderingInitiationComponent`
12. `structureCachingComponent`
13. `postStructureTransformLogger`
14. `structureTransformComponent`
15. `preStructureTransformLogger`
16. `structureAttributeIncorporationComponent`
17. `portletWindowAttributeIncorporationComponent`
18. `dashboardWindowStateSettingsStAXComponent`
19. `postUserLayoutStoreLogger`
20. `userLayoutStoreComponent`

The order of processing for these pipeline components is essentially _backwards_:  bottom to top.

## Using `RenderingPipelineBranchPoint` Beans

uPortal adopters may configure the Rendering Pipeline to suit their needs.  Most common use cases
can be satisfied using `RenderingPipelineBranchPoint` beans.  Rendering branch points are Java
objects (Spring-managed beans) that tell some (or all) HTTP requests to follow a different path.
Rendering branch points follow the standard uPortal 5 configuration strategy for Spring-managed
beans:  if you supply a properly-configured bean of the correct type (_viz._
`RenderingPipelineBranchPoint`) to the Spring Application Context, uPortal will _discover_ it and
_do the right thing_.  (uPortal will provide it as a dependency to the components that know what
to do with it.)

uPortal evaluates `RenderingPipelineBranchPoint` beans, if present, in the specified order.  If a
branch indicates that it _should_ be followed, it _will_ be followed, and no further branches will
be tested.  If no branch is followed, the standard rendering pipeline will be used.

`RenderingPipelineBranchPoint` beans accept the following configuration settings:

| Property | Type | Required? | Notes |
| -------- | ---- |:---------:| ----- |
| `order` | `int` | N* | Defines the sequence of branch points when more than one are present (in which case `order` is required).  Branches with lower `order` values come before higher values. |
| `predicate` | `java.util.function.Predicate<HttpServletRequest>` | Y | If the `predicate` returns `true`, the branch will be followed; otherwise the next branch will be tested. |
| `alternatePipe` | `IPortalRenderingPipeline` | Y | The rendering path that will be followed if the `predicate` returns `true`. |

### Examples

The following examples illustrate some typical uses for `RenderingPipelineBranchPoint` beans.  Each
of these examples can be configured in
`uPortal-start/overlays/uPortal/src/main/resources/properties/contextOverrides/overridesContext.xml`.

#### Example 1:  Redirect Unauthenticated Users to CAS/Shibboleth

This example illustrates a commonly-requested feature:  disallow unauthenticated access to the
portal.

``` xml
<bean id="guestUserBranchPoint" class="org.apereo.portal.rendering.RenderingPipelineBranchPoint">
    <property name="predicate">
        <bean class="org.apereo.portal.rendering.predicates.GuestUserPredicate" />
    </property>
    <property name="alternatePipe">
        <bean class="org.apereo.portal.rendering.RedirectRenderingPipelineTerminator">
            <property name="redirectTo" value="${org.apereo.portal.channels.CLogin.CasLoginUrl}" />
        </bean>
    </property>
</bean>
```

#### Example 2:  Integrate uPortal-home

This example illustrates required uPortal Rendering Pipeline configuration for integration with
[uPortal-home][].

``` xml
<bean id="redirectToWebMaybe" class="org.jasig.portal.rendering.RenderingPipelineBranchPoint">
    <property name="order" value="1" />
    <property name="predicate">
        <bean class="org.jasig.portal.rendering.predicates.GuestUserPredicate" />
    </property>
    <property name="alternatePipe" ref="redirectToWeb" />
</bean>

<bean id="maybeRedirectToExclusive" class="org.jasig.portal.rendering.RenderingPipelineBranchPoint">
    <property name="order" value="2" />
    <property name="predicate">
        <bean class="java.util.function.Predicate" factory-method="and" factory-bean="focusedOnOnePortletPredicate">
            <constructor-arg>
                <bean class="java.util.function.Predicate" factory-method="and" factory-bean="urlNotInExclusiveStatePredicate">
                    <constructor-arg>
                        <bean class="org.jasig.portal.rendering.predicates.RenderOnWebFlagSetPredicate" />
                    </constructor-arg>
                </bean>
            </constructor-arg>
        </bean>
    </property>
    <property name="alternatePipe" ref="redirectToWebExclusive" />
</bean>

<!-- if the request is for a simple content portlet, redirect to
     uPortal-home to render that portlet statically. -->
<bean id="maybeRedirectToWebStatic" class="org.jasig.portal.rendering.RenderingPipelineBranchPoint">
    <property name="order" value="3" />
    <property name="predicate">
        <bean class="java.util.function.Predicate" factory-method="and" factory-bean="focusedOnOnePortletPredicate">
            <constructor-arg>
                <bean class="java.util.function.Predicate" factory-method="and" factory-bean="urlInMaximizedStatePredicate">
                    <constructor-arg>
                        <ref bean="webAppNameContainsSimpleContentPortletPredicate" />
                    </constructor-arg>
                </bean>
            </constructor-arg>
        </bean>
    </property>
    <property name="alternatePipe" ref="redirectToWebStatic" />
</bean>

<!-- Common Predicates -->

<bean id="focusedOnOnePortletPredicate" class="org.jasig.portal.rendering.predicates.FocusedOnOnePortletPredicate" />

<bean id="urlNotInExclusiveStatePredicate" class="org.jasig.portal.rendering.predicates.URLInSpecificStatePredicate">
    <property name="state" value="EXCLUSIVE" />
    <property name="negated" value="true" />
</bean>

<bean id="urlInMaximizedStatePredicate" class="org.jasig.portal.rendering.predicates.URLInSpecificStatePredicate">
    <property name="state" value="MAX" />
</bean>

<bean id="webAppNameContainsSimpleContentPortletPredicate" class="org.jasig.portal.rendering.predicates.WebAppNameContainsStringPredicate">
    <property name="webAppNameToMatch" value="SimpleContentPortlet" />
</bean>

<!-- Pipeline Terminators -->

<bean id="redirectToWeb" class="org.jasig.portal.rendering.RedirectRenderingPipelineTerminator">
    <property name="redirectTo" value="${angular.landing.page}" />
</bean>

<bean id="redirectToWebExclusive" class="org.jasig.portal.rendering.RedirectRenderingPipelineTerminator">
    <property name="redirectTo" value="${angular.landing.page}exclusive/" />
    <property name="appender" value="fname" />
</bean>

<!-- Redirect to uPortal-home,
instructing uPortal-home to render a particular portlet statically. -->
<bean id="redirectToWebStatic" class="org.jasig.portal.rendering.RedirectRenderingPipelineTerminator">
    <property name="redirectTo" value="${angular.landing.page}static/" />
    <property name="appender" value="fname" />
</bean>
```

[Decorator Pattern]: https://en.wikipedia.org/wiki/Decorator_pattern
[uPortal-home]: https://github.com/uPortal-Project/uportal-home
