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
package org.apereo.portal.xml;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * A URIResolver that uses a provided Spring {@link ResourceLoader} to resolve references in a XSL
 * document. Assumes that the provided 'base + href' argument or if base is null 'href' argument can
 * be correctly resolved by the {@link ResourceLoader}
 *
 */
public class ResourceLoaderURIResolver implements URIResolver {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final ResourceLoader resourceLoader;

    public ResourceLoaderURIResolver(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        final Resource resolvedResource = resolveResource(href, base);

        return createSource(resolvedResource, base);
    }

    /** Create a {@link Source} from the specified {@link Resource} */
    protected Source createSource(final Resource resolvedResource, String base)
            throws TransformerException {
        final InputStream resourceStream;
        try {
            resourceStream = resolvedResource.getInputStream();
        } catch (IOException e) {
            throw new TransformerException("Failed to get InputStream for " + resolvedResource, e);
        }

        final StreamSource streamSource = new StreamSource(resourceStream);
        streamSource.setSystemId(base);
        return streamSource;
    }

    /** Resolve the requested {@link Resource} */
    protected Resource resolveResource(String href, String base) throws TransformerException {
        final Resource resolvedResource;
        if (base != null) {
            final Resource baseResource = this.resourceLoader.getResource(base);

            try {
                resolvedResource = baseResource.createRelative(href);
            } catch (IOException e) {
                throw new TransformerException(
                        "Failed to find '" + href + "' relative to: " + baseResource, e);
            }

            this.logger.debug(
                    "Created resource {} for href: {} and base: {}",
                    new Object[] {resolvedResource, href, baseResource});
        } else {
            resolvedResource = this.resourceLoader.getResource(href);

            this.logger.debug("Created resource {} for href: {}", resolvedResource, href);
        }
        return resolvedResource;
    }
}
