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

import org.lenskit.data.ratings.RatingBuilder;

/**
 * Utility classes for event formats.
 *
 * @since 2.2
 */
public final class Formats {
    private Formats() {}

    /**
     * A basic format of ratings in a CSV file.  The expected format is (<em>user</em>, <em>item</em>,
     * <em>rating</em>, <em>timestamp</em>), where the timestamp is optional.
     * @param delim The delimiter.
     * @return An event format for reading ratings from a CSV file.
     */
    public static DelimitedColumnEventFormat delimitedRatings(String delim) {
        return DelimitedColumnEventFormat.create(RatingBuilder.class)
                                         .setDelimiter(delim)
                                         .setFields(Fields.user(), Fields.item(),
                                                    Fields.rating(),
                                                    Fields.timestamp(false));
    }

    /**
     * A basic format of ratings in a CSV file.  The expected format is (<em>user</em>, <em>item</em>,
     * <em>rating</em>, <em>timestamp</em>), where the timestamp is optional.
     * @return An event format for reading ratings from a CSV file.
     */
    public static DelimitedColumnEventFormat csvRatings() {
        return delimitedRatings(",");
    }

    /**
     * Get a format for reading the ML-100K data set.
     *
     * @return A format for using {@link TextEventDAO} to read the ML-100K data set.
     */
    public static DelimitedColumnEventFormat ml100kFormat() {
        DelimitedColumnEventFormat fmt = DelimitedColumnEventFormat.create(RatingBuilder.class);
        fmt.setFields(Fields.user(), Fields.item(), Fields.rating(), Fields.timestamp());
        return fmt;
    }

    /**
     * Get a format for reading the MovieLens 1M and 10M data sets.
     *
     * @return A format for using {@link TextEventDAO} to read the ML-1M and ML-10M data sets.
     */
    public static DelimitedColumnEventFormat movieLensFormat() {
        return DelimitedColumnEventFormat.create(RatingBuilder.class)
                                         .setDelimiter("::")
                                         .setFields(Fields.user(), Fields.item(),
                                                    Fields.rating(), Fields.timestamp());
    }

    /**
     * Get a format for reading the MovieLens 20M and Latest data sets.
     * @return A format for using {@link TextEventDAO} to read the ML-10M and ML-Latest data sets.
     */
    public static DelimitedColumnEventFormat movieLensLatest() {
        return DelimitedColumnEventFormat.create(RatingBuilder.class)
                                         .setDelimiter(",")
                                         .setFields(Fields.user(), Fields.item(),
                                                    Fields.rating(), Fields.timestamp())
                                         .setHeaderLines(1);
    }
}
