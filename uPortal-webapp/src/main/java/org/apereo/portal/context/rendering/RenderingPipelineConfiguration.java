/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.context.rendering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.ehcache.Cache;
import org.apereo.portal.character.stream.CharacterEventSource;
import org.apereo.portal.character.stream.PortletContentPlaceholderEventSource;
import org.apereo.portal.character.stream.PortletHeaderPlaceholderEventSource;
import org.apereo.portal.character.stream.PortletHelpPlaceholderEventSource;
import org.apereo.portal.character.stream.PortletLinkPlaceholderEventSource;
import org.apereo.portal.character.stream.PortletNewItemCountPlaceholderEventSource;
import org.apereo.portal.character.stream.PortletTitlePlaceholderEventSource;
import org.apereo.portal.character.stream.events.ChunkPointPlaceholderEventSource;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.rendering.*;
import org.apereo.portal.rendering.cache.CachingCharacterPipelineComponent;
import org.apereo.portal.rendering.cache.CachingStAXPipelineComponent;
import org.apereo.portal.rendering.xslt.LocaleTransformerConfigurationSource;
import org.apereo.portal.rendering.xslt.MergingTransformerConfigurationSource;
import org.apereo.portal.rendering.xslt.SkinMappingTransformerConfigurationSource;
import org.apereo.portal.rendering.xslt.StaticTransformerConfigurationSource;
import org.apereo.portal.rendering.xslt.StructureStylesheetDescriptorTransformerConfigurationSource;
import org.apereo.portal.rendering.xslt.StructureStylesheetUserPreferencesTransformerConfigurationSource;
import org.apereo.portal.rendering.xslt.StructureTransformerSource;
import org.apereo.portal.rendering.xslt.ThemeStylesheetDescriptorTransformerConfigurationSource;
import org.apereo.portal.rendering.xslt.ThemeStylesheetUserPreferencesTransformerConfigurationSource;
import org.apereo.portal.rendering.xslt.ThemeTransformerSource;
import org.apereo.portal.rendering.xslt.TransformerConfigurationSource;
import org.apereo.portal.rendering.xslt.TransformerSource;
import org.apereo.portal.rendering.xslt.UserImpersonationTransformerConfigurationSource;
import org.apereo.portal.rendering.xslt.XSLTComponent;
import org.apereo.portal.url.xml.XsltPortalUrlProvider;
import org.apereo.portal.web.skin.ResourcesElementsXsltcHelper;
import org.jasig.resourceserver.aggr.ResourcesDao;
import org.jasig.resourceserver.aggr.ResourcesDaoImpl;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This @Configuration class sets up (roughly) the same beans that renderingPipelineContext.xml did
 * in uP4.
 *
 * @since 5.0
 */
@Configuration
public class RenderingPipelineConfiguration {

    private static final String POST_USER_LAYOUT_STORE_LOGGER_NAME =
            "org.apereo.portal.rendering.LoggingStAXComponent.POST_LAYOUT";
    private static final String POST_USER_LAYOUT_STORE_LOGGER_STEP_IDENTIFIER =
            "postUserLayoutStoreLogger";

    private static final String PRE_STRUCTURE_TRANSFORM_LOGGER_NAME =
            "org.apereo.portal.rendering.LoggingStAXComponent.PRE_STRUCTURE";
    private static final String PRE_STRUCTURE_TRANSFORM_LOGGER_STEP_IDENTIFIER =
            "preStructureTransformLogger";

    private static final String POST_STRUCTURE_TRANSFORM_LOGGER_NAME =
            "org.apereo.portal.rendering.LoggingStAXComponent.POST_STRUCTURE";
    private static final String POST_STRUCTURE_TRANSFORM_LOGGER_STEP_IDENTIFIER =
            "postStructureTransformLogger";

    private static final String PRE_THEME_TRANSFORM_LOGGER_NAME =
            "org.apereo.portal.rendering.LoggingStAXComponent.PRE_THEME";
    private static final String PRE_THEME_TRANSFORM_LOGGER_STEP_IDENTIFIER =
            "preThemeTransformLogger";

    private static final String POST_THEME_TRANSFORM_LOGGER_NAME =
            "org.apereo.portal.rendering.LoggingStAXComponent.POST_THEME";
    private static final String POST_THEME_TRANSFORM_LOGGER_STEP_IDENTIFIER =
            "postThemeTransformLogger";

    private static final String PORTLET_TITLE_PATTERN = "\\{up-portlet-title\\(([^\\)]+)\\)\\}";
    private static final String PORTLET_HELP_PATTERN = "\\{up-portlet-help\\(([^\\)]+)\\)\\}";
    private static final String PORTLET_NEW_ITEM_COUNT_PATTERN =
            "\\{up-portlet-new-item-count\\(([^\\)]+)\\)\\}";
    private static final String PORTLET_LINK_PATTERN =
            "\\{up-portlet-link\\(([^,]+),([^\\)]+)\\)\\}";

    @Value("${org.apereo.portal.version}")
    private String uPortalVersion;

    @Resource(name = "org.apereo.portal.rendering.STRUCTURE_TRANSFORM")
    private Cache structureTransformCache;

    @Autowired private XsltPortalUrlProvider xslPortalUrlProvider;

    @Value("${portal.protocol}://${portal.server}")
    private String portalProtocolAndServer;

    @Value("${org.apereo.portal.channels.CLogin.CasLoginUrl}")
    private String casLoginUrl;

    @Value("${org.apereo.portal.layout.useTabGroups}")
    private String useTabGroups;

    @Value("${org.apereo.portal.layout.useFlyoutMenus:false}")
    private String useFlyoutMenus;

    @Value("${org.apereo.portal.layout.faviconPath:#{null}}")
    private String faviconPath;

    @Value("${org.apereo.portal.layout.useTabsSize:#{null}}")
    private String useTabsSize;

    @Resource(name = "org.apereo.portal.rendering.THEME_TRANSFORM")
    private Cache themeTransformCache;

    @Autowired(required = false)
    private List<RenderingPipelineBranchPoint> branchPoints;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * This bean is the entry point into the uPortal Rendering Pipeline. It supports {@link
     * RenderingPipelineBranchPoint} beans, which are an extension point for adopters.
     */
    @Bean(name = "portalRenderingPipeline")
    @Qualifier(value = "main")
    public IPortalRenderingPipeline getPortalRenderingPipeline() {

        // Rendering Pipeline Branches (adopter extension point)
        final List<RenderingPipelineBranchPoint> sortedList =
                (branchPoints != null) ? new LinkedList<>(branchPoints) : Collections.emptyList();
        Collections.sort(sortedList);
        final List<RenderingPipelineBranchPoint> branches =
                Collections.unmodifiableList(sortedList);

        /*
         * Sanity check:  if you have multiple RenderingPipelineBranchPoint beans, you can specify
         * an 'order' property on some or all of them to control the sequence of processing.
         * Having 2 RenderingPipelineBranchPoint beans with the same order value will produce
         * non-deterministic results and is a likely source of misconfiguration.
         */
        final Set<Integer> usedOderValues = new HashSet<>();
        boolean hasCollision =
                branches.stream()
                        .anyMatch(
                                branchPoint -> {
                                    final boolean rslt =
                                            usedOderValues.contains(branchPoint.getOrder());
                                    usedOderValues.add(branchPoint.getOrder());
                                    return rslt;
                                });
        if (hasCollision) {
            throw new RenderingPipelineConfigurationException(
                    "Multiple RenderingPipelineBranchPoint beans have the same 'order' value, which likely a misconfiguration");
        }

        // "Standard" Pipeline
        final IPortalRenderingPipeline standardRenderingPipeline = getStandardRenderingPipeline();

        return new IPortalRenderingPipeline() {
            @Override
            public void renderState(HttpServletRequest req, HttpServletResponse res)
                    throws ServletException, IOException {
                for (RenderingPipelineBranchPoint branchPoint : branches) {
                    if (branchPoint.renderStateIfApplicable(req, res)) {
                        /*
                         * Rendering bas been processed by the branch point -- no need to continue.
                         */
                        return;
                    }
                }
                /*
                 * Reaching this point means that a branch was not followed; use the "standard"
                 * pipeline.
                 */
                standardRenderingPipeline.renderState(req, res);
            }
        };
    }

    /*
     * Beans below this point are elements of the "standard" uPortal Rendering Pipeline -- where the
     * uPortal webapp renders all elements of the UI and handles all requests.
     */

    @Bean(name = "standardRenderingPipeline")
    public IPortalRenderingPipeline getStandardRenderingPipeline() {
        final DynamicRenderingPipeline rslt = new DynamicRenderingPipeline();
        rslt.setPipeline(getAnalyticsIncorporationComponent());
        return rslt;
    }

    @Bean(name = "userLayoutStoreComponent")
    public StAXPipelineComponent getUserLayoutStoreComponent() {
        return new UserLayoutStoreComponent();
    }

    @Bean(name = "postUserLayoutStoreLogger")
    public StAXPipelineComponent getPostUserLayoutStoreLogger() {
        final LoggingStAXComponent rslt = new LoggingStAXComponent();
        rslt.setWrappedComponent(getUserLayoutStoreComponent());
        rslt.setLoggerName(POST_USER_LAYOUT_STORE_LOGGER_NAME);
        rslt.setLogEvents(false);
        rslt.setLogFullDocument(true);
        rslt.setStepIdentifier(POST_USER_LAYOUT_STORE_LOGGER_STEP_IDENTIFIER);
        return rslt;
    }

    @Bean(name = "themeAttributeSource")
    public ThemeAttributeSource getThemeAttributeSource() {
        return new ThemeAttributeSource();
    }

    @Bean(name = "dashboardWindowStateSettingsStAXComponent")
    public StAXPipelineComponent getDashboardWindowStateSettingsStAXComponent() {
        final WindowStateSettingsStAXComponent rslt = new WindowStateSettingsStAXComponent();
        rslt.setWrappedComponent(getPostUserLayoutStoreLogger());
        rslt.setStylesheetAttributeSource(getThemeAttributeSource());
        return rslt;
    }

    @Bean(name = "attributeSource")
    public PortletWindowAttributeSource getPortletWindowAttributeSource() {
        return new PortletWindowAttributeSource();
    }

    @Bean(name = "portletWindowAttributeIncorporationComponent")
    public StAXPipelineComponent getPortletWindowAttributeIncorporationComponent() {
        final StAXAttributeIncorporationComponent rslt = new StAXAttributeIncorporationComponent();
        rslt.setWrappedComponent(getDashboardWindowStateSettingsStAXComponent());
        rslt.setAttributeSource(getPortletWindowAttributeSource());
        return rslt;
    }

    @Bean(name = "structureAttributeSource")
    public StructureAttributeSource getStructureAttributeSource() {
        return new StructureAttributeSource();
    }

    @Bean(name = "structureAttributeIncorporationComponent")
    public StAXPipelineComponent getStructureAttributeIncorporationComponent() {
        final StAXAttributeIncorporationComponent rslt = new StAXAttributeIncorporationComponent();
        rslt.setWrappedComponent(getPortletWindowAttributeIncorporationComponent());
        rslt.setAttributeSource(getStructureAttributeSource());
        return rslt;
    }

    @Bean(name = "preStructureTransformLogger")
    public StAXPipelineComponent getPreStructureTransformLogger() {
        final LoggingStAXComponent rslt = new LoggingStAXComponent();
        rslt.setWrappedComponent(getStructureAttributeIncorporationComponent());
        rslt.setLoggerName(PRE_STRUCTURE_TRANSFORM_LOGGER_NAME);
        rslt.setLogEvents(false);
        rslt.setLogFullDocument(true);
        rslt.setStepIdentifier(PRE_STRUCTURE_TRANSFORM_LOGGER_STEP_IDENTIFIER);
        return rslt;
    }

    @Bean(name = "structureTransformSource")
    public TransformerSource getStructureTransformSource() {
        return new StructureTransformerSource();
    }

    @Bean
    public TransformerConfigurationSource
            getStructureStylesheetDescriptorTransformerConfigurationSource() {
        return new StructureStylesheetDescriptorTransformerConfigurationSource();
    }

    @Bean
    public TransformerConfigurationSource
            getStructureStylesheetUserPreferencesTransformerConfigurationSource() {
        return new StructureStylesheetUserPreferencesTransformerConfigurationSource();
    }

    @Bean
    public TransformerConfigurationSource getStaticTransformerConfigurationSourceForStructure() {
        final StaticTransformerConfigurationSource rslt =
                new StaticTransformerConfigurationSource();
        rslt.setParameters(Collections.singletonMap("version-UP_FRAMEWORK", uPortalVersion));
        return rslt;
    }

    @Bean
    public TransformerConfigurationSource getUserImpersonationTransformerConfigurationSource() {
        return new UserImpersonationTransformerConfigurationSource();
    }

    /**
     * This bean is not an element of the rendering pipeline. It is a DAO for reading and writing
     * <code>Resources</code> objects to files.
     */
    @Bean(name = "resourcesDao")
    public ResourcesDao getResourcesDao() {
        return new ResourcesDaoImpl();
    }

    /** This bean is not an element of the rendering pipeline. */
    @Bean(name = "resourcesElementsProvider")
    public ResourcesElementsProvider getResourcesElementsProvider() {
        ResourcesElementsProviderImpl rslt = new ResourcesElementsProviderImpl();
        rslt.setResourcesDao(getResourcesDao());
        return rslt;
    }

    /** A default empty bean that could be overriden by a custom one. */
    @Bean(name = "customSkinsTransformers")
    public List<? extends SkinMappingTransformerConfigurationSource> getCustomSkinTransformers() {
        return new ArrayList<>();
    }

    @Resource(name = "customSkinsTransformers")
    public List<? extends SkinMappingTransformerConfigurationSource> customSkinsTransformers;

    @Bean(name = "structureTransformComponent")
    public StAXPipelineComponentWrapper getStructureTransformComponent() {
        final XSLTComponent rslt = new XSLTComponent();
        rslt.setWrappedComponent(getPreStructureTransformLogger());
        rslt.setTransformerSource(getStructureTransformSource());
        final List<TransformerConfigurationSource> sources = new ArrayList<>();
        sources.add(getStructureStylesheetDescriptorTransformerConfigurationSource());
        if (customSkinsTransformers != null && !customSkinsTransformers.isEmpty()) {
            sources.addAll(customSkinsTransformers);
        }
        sources.add(getStructureStylesheetUserPreferencesTransformerConfigurationSource());
        sources.add(getStaticTransformerConfigurationSourceForStructure());
        sources.add(getUserImpersonationTransformerConfigurationSource());
        final MergingTransformerConfigurationSource mtcs =
                new MergingTransformerConfigurationSource();
        mtcs.setSources(sources);
        rslt.setXsltParameterSource(mtcs);
        return rslt;
    }

    @Bean(name = "postStructureTransformLogger")
    public StAXPipelineComponent getPostStructureTransformLogger() {
        final LoggingStAXComponent rslt = new LoggingStAXComponent();
        rslt.setWrappedComponent(getStructureTransformComponent());
        rslt.setLoggerName(POST_STRUCTURE_TRANSFORM_LOGGER_NAME);
        rslt.setLogEvents(false);
        rslt.setLogFullDocument(true);
        rslt.setStepIdentifier(POST_STRUCTURE_TRANSFORM_LOGGER_STEP_IDENTIFIER);
        return rslt;
    }

    @Bean(name = "structureCachingComponent")
    public StAXPipelineComponent getStructureCachingComponent() {
        final CachingStAXPipelineComponent rslt = new CachingStAXPipelineComponent();
        rslt.setWrappedComponent(getPostStructureTransformLogger());
        rslt.setCache(structureTransformCache);
        return rslt;
    }

    @Bean(name = "portletRenderingInitiationComponent")
    public StAXPipelineComponentWrapper getPortletRenderingInitiationComponent() {
        final PortletRenderingInitiationStAXComponent rslt =
                new PortletRenderingInitiationStAXComponent();
        rslt.setWrappedComponent(getStructureCachingComponent());
        return rslt;
    }

    @Bean(name = "themeAttributeIncorporationComponent")
    public StAXPipelineComponent getThemeAttributeIncorporationComponent() {
        final StAXAttributeIncorporationComponent rslt = new StAXAttributeIncorporationComponent();
        rslt.setWrappedComponent(getPortletRenderingInitiationComponent());
        rslt.setAttributeSource(getThemeAttributeSource());
        return rslt;
    }

    @Bean(name = "preThemeTransformLogger")
    public StAXPipelineComponent getPreThemeTransformLogger() {
        final LoggingStAXComponent rslt = new LoggingStAXComponent();
        rslt.setWrappedComponent(getThemeAttributeIncorporationComponent());
        rslt.setLoggerName(PRE_THEME_TRANSFORM_LOGGER_NAME);
        rslt.setLogEvents(false);
        rslt.setLogFullDocument(true);
        rslt.setStepIdentifier(PRE_THEME_TRANSFORM_LOGGER_STEP_IDENTIFIER);
        return rslt;
    }

    @Bean(name = "themeTransformSource")
    public TransformerSource getThemeTransformerSource() {
        return new ThemeTransformerSource();
    }

    @Bean
    public TransformerConfigurationSource
            getThemeStylesheetDescriptorTransformerConfigurationSource() {
        return new ThemeStylesheetDescriptorTransformerConfigurationSource();
    }

    @Bean
    public TransformerConfigurationSource
            getThemeStylesheetUserPreferencesTransformerConfigurationSource() {
        return new ThemeStylesheetUserPreferencesTransformerConfigurationSource();
    }

    @Bean
    public TransformerConfigurationSource getStaticTransformerConfigurationSourceForTheme() {

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put(XsltPortalUrlProvider.XSLT_PORTAL_URL_PROVIDER, xslPortalUrlProvider);
        parameters.put("EXTERNAL_LOGIN_URL", casLoginUrl);
        parameters.put("PORTAL_PROTOCOL_AND_SERVER", portalProtocolAndServer);
        parameters.put("useTabGroups", useTabGroups);
        parameters.put("UP_VERSION", uPortalVersion);
        parameters.put("USE_FLYOUT_MENUS", useFlyoutMenus);
        if (faviconPath != null) {
            parameters.put("PORTAL_SHORTCUT_ICON", faviconPath);
        }
        if (useTabsSize != null) {
            parameters.put("USE_TABS_SIZE", useTabsSize);
        }

        final Map<String, String> parameterExpressions = new HashMap<>();
        parameterExpressions.put("CURRENT_REQUEST", "request.nativeRequest");
        parameterExpressions.put("CONTEXT_PATH", "request.contextPath");
        parameterExpressions.put("HOST_NAME", "request.nativeRequest.serverName");
        parameterExpressions.put("AUTHENTICATED", "!person.guest");
        parameterExpressions.put(
                "EXTERNAL_LOGIN_URL", "@casRefUrlEncoder.getCasLoginUrl(request.nativeRequest)");
        parameterExpressions.put("userName", "person.fullName");
        parameterExpressions.put("USER_ID", "person.userName");
        parameterExpressions.put("SERVER_NAME", "@portalInfoProvider.serverName");
        parameterExpressions.put(
                "STATS_SESSION_ID",
                "@portalEventFactory.getPortalEventSessionId(request.nativeRequest, person)");

        final Set<String> cacheKeyExcludedParameters = new HashSet<>();
        cacheKeyExcludedParameters.add("CURRENT_REQUEST");
        cacheKeyExcludedParameters.add(
                org.apereo.portal.web.skin.ResourcesElementsXsltcHelper.RESOURCES_ELEMENTS_HELPER);
        cacheKeyExcludedParameters.add(
                org.apereo.portal.url.xml.XsltPortalUrlProvider.XSLT_PORTAL_URL_PROVIDER);

        final StaticTransformerConfigurationSource rslt =
                new StaticTransformerConfigurationSource();
        rslt.setParameters(parameters);
        rslt.setParameterExpressions(parameterExpressions);
        rslt.setCacheKeyExcludedParameters(cacheKeyExcludedParameters);
        return rslt;
    }

    @Bean
    public TransformerConfigurationSource getLocaleTransformerConfigurationSource() {
        return new LocaleTransformerConfigurationSource();
    }

    @Bean
    public TransformerConfigurationSource getResourcesElementsXsltcHelper() {
        return new ResourcesElementsXsltcHelper();
    }

    @Bean(name = "themeTransformComponent")
    public StAXPipelineComponentWrapper getThemeTransformComponent() {
        final XSLTComponent rslt = new XSLTComponent();
        rslt.setWrappedComponent(getPreThemeTransformLogger());
        rslt.setTransformerSource(getThemeTransformerSource());
        final List<TransformerConfigurationSource> sources = new ArrayList<>();
        sources.add(getThemeStylesheetDescriptorTransformerConfigurationSource());
        if (customSkinsTransformers != null && !customSkinsTransformers.isEmpty()) {
            sources.addAll(customSkinsTransformers);
        }
        sources.add(getThemeStylesheetUserPreferencesTransformerConfigurationSource());
        sources.add(getStaticTransformerConfigurationSourceForTheme());
        sources.add(getLocaleTransformerConfigurationSource());
        sources.add(getResourcesElementsXsltcHelper());
        final MergingTransformerConfigurationSource mtcs =
                new MergingTransformerConfigurationSource();
        mtcs.setSources(sources);
        rslt.setXsltParameterSource(mtcs);
        return rslt;
    }

    @Bean(name = "postThemeTransformLogger")
    public StAXPipelineComponent getPostThemeTransformLogger() {
        final LoggingStAXComponent rslt = new LoggingStAXComponent();
        rslt.setWrappedComponent(getThemeTransformComponent());
        rslt.setLoggerName(POST_THEME_TRANSFORM_LOGGER_NAME);
        rslt.setLogEvents(false);
        rslt.setLogFullDocument(true);
        rslt.setLogFullDocumentAsHtml(true);
        rslt.setStepIdentifier(POST_THEME_TRANSFORM_LOGGER_STEP_IDENTIFIER);
        return rslt;
    }

    @Bean
    public CharacterEventSource getPortletContentPlaceholderEventSource() {
        return new PortletContentPlaceholderEventSource();
    }

    @Bean
    public CharacterEventSource getPortletHeaderPlaceholderEventSource() {
        return new PortletHeaderPlaceholderEventSource();
    }

    @Bean
    public CharacterEventSource getChunkPointPlaceholderEventSource() {
        return new ChunkPointPlaceholderEventSource();
    }

    @Bean
    public CharacterEventSource getPortletAnalyticsDataPlaceholderEventSource() {
        return new PortletAnalyticsDataPlaceholderEventSource();
    }

    @Bean
    public CharacterEventSource getPageAnalyticsDataPlaceholderEventSource() {
        return new PageAnalyticsDataPlaceholderEventSource();
    }

    @Bean
    public CharacterEventSource getPortletTitlePlaceholderEventSource() {
        return new PortletTitlePlaceholderEventSource();
    }

    @Bean
    public CharacterEventSource getPortletHelpPlaceholderEventSource() {
        return new PortletHelpPlaceholderEventSource();
    }

    @Bean
    public CharacterEventSource getPortletNewItemCountPlaceholderEventSource() {
        return new PortletNewItemCountPlaceholderEventSource();
    }

    @Bean
    public CharacterEventSource getPortletLinkPlaceholderEventSource() {
        return new PortletLinkPlaceholderEventSource();
    }

    @Bean(name = "staxSerializingComponent")
    public CharacterPipelineComponent getStAXSerializingComponent() {
        final StAXSerializingComponent rslt = new StAXSerializingComponent();
        rslt.setWrappedComponent(getPostThemeTransformLogger());

        final Map<String, CharacterEventSource> chunkingElements = new HashMap<>();
        chunkingElements.put(IUserLayoutManager.CHANNEL, getPortletContentPlaceholderEventSource());
        chunkingElements.put(
                IUserLayoutManager.CHANNEL_HEADER, getPortletHeaderPlaceholderEventSource());
        chunkingElements.put(
                ChunkPointPlaceholderEventSource.CHUNK_POINT,
                getChunkPointPlaceholderEventSource());
        chunkingElements.put(
                PortletAnalyticsDataPlaceholderEventSource.PORTLET_ANALYTICS_SCRIPT,
                getPortletAnalyticsDataPlaceholderEventSource());
        chunkingElements.put(
                PageAnalyticsDataPlaceholderEventSource.PAGE_ANALYTICS_SCRIPT,
                getPageAnalyticsDataPlaceholderEventSource());
        rslt.setChunkingElements(chunkingElements);

        final Map<String, CharacterEventSource> chunkingPatterns = new HashMap<>();
        chunkingPatterns.put(PORTLET_TITLE_PATTERN, getPortletTitlePlaceholderEventSource());
        chunkingPatterns.put(PORTLET_HELP_PATTERN, getPortletHelpPlaceholderEventSource());
        chunkingPatterns.put(
                PORTLET_NEW_ITEM_COUNT_PATTERN, getPortletNewItemCountPlaceholderEventSource());
        chunkingPatterns.put(PORTLET_LINK_PATTERN, getPortletLinkPlaceholderEventSource());
        rslt.setChunkingPatterns(chunkingPatterns);

        return rslt;
    }

    @Bean(name = "postSerializerLogger")
    public CharacterPipelineComponent getPostSerializerLogger() {
        final LoggingCharacterComponent rslt = new LoggingCharacterComponent();
        rslt.setWrappedComponent(getStAXSerializingComponent());
        rslt.setLoggerName("org.apereo.portal.rendering.LoggingCharacterComponent.POST_SERIALIZER");
        return rslt;
    }

    @Bean(name = "themeCachingComponent")
    public CharacterPipelineComponent getThemeCachingComponent() {
        final CachingCharacterPipelineComponent rslt = new CachingCharacterPipelineComponent();
        rslt.setWrappedComponent(getPostSerializerLogger());
        rslt.setCache(themeTransformCache);
        return rslt;
    }

    @Bean(name = "portletRenderingInitiationCharacterComponent")
    public CharacterPipelineComponent getPortletRenderingInitiationCharacterComponent() {
        final PortletRenderingInitiationCharacterComponent rslt =
                new PortletRenderingInitiationCharacterComponent();
        rslt.setWrappedComponent(getThemeCachingComponent());
        return rslt;
    }

    @Bean(name = "portletRenderingIncorporationComponent")
    public CharacterPipelineComponent getPortletRenderingIncorporationComponent() {
        final PortletRenderingIncorporationComponent rslt =
                new PortletRenderingIncorporationComponent();
        rslt.setWrappedComponent(getPortletRenderingInitiationCharacterComponent());
        return rslt;
    }

    @Bean(name = "analyticsIncorporationComponent")
    public CharacterPipelineComponent getAnalyticsIncorporationComponent() {
        final AnalyticsIncorporationComponent rslt = new AnalyticsIncorporationComponent();
        rslt.setWrappedComponent(getPortletRenderingIncorporationComponent());
        return rslt;
    }
}
