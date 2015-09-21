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

package org.grouplens.lenskit.hir;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import java.io.Serializable;

/**
 * Created by chrysalag on 29.08.15.
 */

@DefaultProvider(HIRModelBuilder.class)
@Shareable
public class HIRModel implements Serializable {

    private static final long serialVersionUID  = 1L;

    private final Long2ObjectMap<ImmutableSparseVector> matrix;

    public static final Symbol CORATINGS_SYMBOL = Symbol.of("coratings");

    public HIRModel(Long2ObjectMap<ImmutableSparseVector> matrix) { this.matrix = matrix; }

    public int getCoratings(long item1, long item2) {
        if (item1 == item2) {
            return 0;
        }
        else if (item1 < item2) {
            SparseVector row = matrix.get(item1);
            if (row == null) {
                return 0;
            }
            else {
                double coratings = row.getChannelVector(CORATINGS_SYMBOL).get(item2, 0);
                return (int) coratings;
            }
        }
        else {
            SparseVector row = matrix.get(item2);
            if (row == null) {
                return 0;
            }
            else {
                double coratings = row.getChannelVector(CORATINGS_SYMBOL).get(item1, 0);
                return (int) coratings;
            }
        }
    }
}
