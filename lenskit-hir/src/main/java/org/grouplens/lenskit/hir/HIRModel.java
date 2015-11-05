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

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.lenskit.inject.Shareable;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A model for a {@link HIRItemScorer}.
 * Stores calculated proximity values and number of co-rating users for each item pair.
 */

@DefaultProvider(HIRModelBuilder.class)
@Shareable
@SuppressWarnings("deprecation")
public class HIRModel implements Serializable {

    private static final long serialVersionUID  = 1L;

    private final RealMatrix cmatrix;

    private final RealMatrix xmatrix;

    private final RealMatrix ymatrix;

    public HIRModel(RealMatrix cmatrix,
                    RealMatrix xmatrix,
                    RealMatrix ymatrix) {
        this.cmatrix = cmatrix;
        this.xmatrix = xmatrix;
        this.ymatrix = ymatrix;
    }

    public MutableSparseVector getCoratingsVector(long item, Collection<Long> items) {

        RealVector data = cmatrix.getRowVector((int) item);

        Map<Long, Double> forResults = new HashMap<>();

        LongIterator iter = LongIterators.asLongIterator(items.iterator());

        int i = 0;
        while (iter.hasNext()) {
            final long meti = iter.nextLong();
            forResults.put(meti, data.getEntry(i));
            i++;
        }

        return MutableSparseVector.create(forResults);

    }

    public MutableSparseVector getProximityVector(long item, Collection<Long> items) {

        RealVector data = xmatrix.getRowVector((int) item);
        RealVector resM = ymatrix.preMultiply(data);

        Map<Long, Double> forResults = new HashMap<>();

        LongIterator iter = LongIterators.asLongIterator(items.iterator());

        int i = 0;
        while (iter.hasNext()) {
            final long meti = iter.nextLong();
            forResults.put(meti, resM.getEntry(i));
            i++;
        }

        return MutableSparseVector.create(forResults);
    }
}
