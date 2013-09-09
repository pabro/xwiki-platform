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
package org.xwiki.wikistream.internal.output;

import java.lang.reflect.Type;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.properties.converter.AbstractConverter;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.parser.ResourceReferenceParser;
import org.xwiki.wikistream.output.OutputTarget;

/**
 * 
 * @version $Id$
 * @since 5.2M2
 */
@Component
@Singleton
public class OutputTargetConverter extends AbstractConverter<OutputTarget>
{
    @Inject
    private ResourceReferenceParser resourceReferenceParser;

    @Override
    protected <G extends OutputTarget> G convertToType(Type targetType, Object value)
    {
        if (value == null) {
            return null;
        }

        if (value instanceof OutputTarget) {
            return (G) value;
        }

        String source = value.toString();

        ResourceReference resourceReference = this.resourceReferenceParser.parse(source);

        OutputTarget outputTarget;

        // TODO: use some OutputTargetParser instead to make it extensible
        outputTarget = new StringWriterOutputTarget();

        return (G) outputTarget;
    }
}