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
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.tuple.Pair;
import org.lenskit.data.dao.ItemDAO;
import org.grouplens.lenskit.vectors.*;

import java.util.Map;

/**
 * Created by chrysalag. Processes ratings data for HIR Item Scorer.
 */

@SuppressWarnings("deprecation")
public class DirectAssociationMatrix {

    private Long2ObjectMap<MutableSparseVector> workMatrix;

    /**
     * Creates an accumulator to process rating data and generate the necessary data for
     * a {@code HIRItemScorer}.
     *
     * @param dao       The DataAccessObject interfacing with the data for the model
     */
    public DirectAssociationMatrix(ItemDAO dao) {
        LongSet items = dao.getItemIds();

        workMatrix = new Long2ObjectOpenHashMap<>(items.size());

        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            long item = iter.nextLong();
            workMatrix.put(item, MutableSparseVector.create(items));
        }
    }

    /**
     * Puts the item pair into the accumulator.
     *
     * @param id1      The id of the first item.
     * @param itemVec1 The rating vector of the first item.
     * @param id2      The id of the second item.
     * @param itemVec2 The rating vector of the second item.
     */
    public void putItemPair(long id1, SparseVector itemVec1, long id2, SparseVector itemVec2) {
        if (workMatrix == null) {
            throw new IllegalStateException("Model is already built");
        }
        // to profit from matrix symmetry, always store by the lesser id
        if (id1 < id2) {
            int coratings = 0;
            for (Pair<VectorEntry,VectorEntry> pair: Vectors.fastIntersect(itemVec1, itemVec2)) {
                coratings++;
            }
            workMatrix.get(id1).set(id2, coratings);
            workMatrix.get(id2).set(id1, coratings);
        } else if (id1 == id2) {
            workMatrix.get(id1).set(id2, 0);
            workMatrix.get(id2).set(id1, 0);
        }
    }

    /**
     * @return A matrix of item corating values to be used by
     *         a {@code HIRItemScorer}.
     */
    public Long2ObjectMap<ImmutableSparseVector> buildMatrix() {

        if (workMatrix == null) {
            throw new IllegalStateException("Model is already built");
        }

        Long2ObjectMap<ImmutableSparseVector> matrix =
                new Long2ObjectOpenHashMap<ImmutableSparseVector>(workMatrix.size());

        for (MutableSparseVector vec : workMatrix.values()) {
            double sum = vec.sum();
            if ( sum != 0 ) {
                vec.multiply(1/sum);
            }
        }

        for (Map.Entry<Long, MutableSparseVector> e : workMatrix.entrySet()) {
            matrix.put(e.getKey(), e.getValue().freeze());
        }

        workMatrix = null;
        return matrix;
    }
}
