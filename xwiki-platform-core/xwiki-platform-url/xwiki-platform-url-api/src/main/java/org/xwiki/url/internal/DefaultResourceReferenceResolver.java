/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.url.internal;

import java.net.URL;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.resource.CreateResourceReferenceException;
import org.xwiki.resource.ResourceReference;
import org.xwiki.resource.ResourceReferenceResolver;
import org.xwiki.resource.UnsupportedResourceReferenceException;
import org.xwiki.url.URLConfiguration;

/**
 * Delegates the work to the Resource Reference Resolver specified in the XWiki Configuration
 * (see {@link org.xwiki.url.URLConfiguration#getURLFormatId()}.
 *
 * @version $Id$
 * @since 6.1M2
 */
@Component
@Singleton
public class DefaultResourceReferenceResolver implements ResourceReferenceResolver<URL>
{
    /**
     * Used to get the hint of the {@link org.xwiki.resource.ResourceReferenceResolver} to use.
     */
    @Inject
    private URLConfiguration configuration;

    /**
     * Used to lookup the correct {@link org.xwiki.resource.ResourceReferenceResolver} component.
     */
    @Inject
    @Named("context")
    private ComponentManager componentManager;

    @Override
    public ResourceReference resolve(URL urlRepresentation, Map<String, Object> parameters)
        throws CreateResourceReferenceException, UnsupportedResourceReferenceException
    {
        ResourceReferenceResolver resolver;
        try {
            resolver = this.componentManager.getInstance(ResourceReferenceResolver.TYPE_URL,
                this.configuration.getURLFormatId());
        } catch (ComponentLookupException e) {
            throw new CreateResourceReferenceException(
                String.format("Invalid configuration hint [%s]. Cannot create Resource Reference for [%s].",
                    this.configuration.getURLFormatId(), urlRepresentation), e);
        }
        return resolver.resolve(urlRepresentation, parameters);
    }
}
