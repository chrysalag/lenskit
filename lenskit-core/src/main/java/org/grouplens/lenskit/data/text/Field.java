/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.data.text;

import org.lenskit.data.events.EventBuilder;

import java.util.Set;

/**
 * Identifiers for fields in a delimited/columnar database.
 *
 * @since 2.2
 */
@SuppressWarnings("rawtypes")
public interface Field {
    /**
     * Get the builder type that this field operates on.
     * @return The field's builder type.
     */
    Class<? extends EventBuilder> getBuilderType();

    /**
     * Query whether this field is optional.
     *
     * @return {@code true} if the field is optional.
     */
    boolean isOptional();

    /**
     * Apply the field's value to the builder.
     *
     * @param token   The field value (or {@code null} if this is a nonexistent optional field).
     * @param builder The builder into which to set the value.
     */
    void apply(String token, EventBuilder builder);

    /**
     * Get the name for this field.
     *
     * **Note**: If this name does not resolve back to an equivalent field when passed to
     * {@link Fields#byName(Class, String)}, data source specification is likely to break.
     *
     * @return The field name.  May return `null` for an ignored field.
     */
    String getName();
}
