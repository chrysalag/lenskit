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
package org.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.lenskit.inject.Shareable;
import org.grouplens.lenskit.symbols.Symbol;
import org.lenskit.util.math.Vectors;

import java.io.Serializable;

/**
 * Neighborhood scorer that computes the weighted average of neighbor scores.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public class WeightedAverageNeighborhoodScorer implements NeighborhoodScorer, Serializable {
    private static final long serialVersionUID = 1L;
    public static final Symbol NEIGHBORHOOD_WEIGHT_SYMBOL =
            Symbol.of("org.grouplens.lenskit.knn.item.neighborhoodWeight");

    @Override
    public ItemItemResult score(long item, Long2DoubleMap neighbors, Long2DoubleMap scores) {
        double weight = Vectors.sumAbs(neighbors);
        if (weight > 0) {
            double weightedSum = Vectors.dotProduct(neighbors, scores);
            return new ItemItemResult(item, weightedSum / weight, neighbors.size());
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "[NeighborhoodScorer: WeightedAverage]";
    }
}
