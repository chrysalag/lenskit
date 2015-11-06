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

import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.grouplens.lenskit.vectors.*;
import org.lenskit.data.dao.ItemDAO;

/**
 * A matrix to store the direct inter-item relationships
 * that derive from the number of their coratings.
 */

public class DirectAssociationMatrix {

    private RealMatrix workMatrix;

    private int itemSize;

    /**
     * Creates a matrix to process rating data and generate coratings for
     * a {@code HIRItemScorer}.
     *
     * @param dao       The DataAccessObject interfacing with the data for the model
     */

    public DirectAssociationMatrix(ItemDAO dao) {
        LongSet items = dao.getItemIds();
        itemSize = items.size();
        workMatrix = MatrixUtils.createRealMatrix(itemSize, itemSize);
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

        if (id1 == id2) {
            workMatrix.setEntry((int) id1, (int)id2, 0);
        } else {
            int coratings = 0;
            for (Pair<VectorEntry,VectorEntry> pair: Vectors.fastIntersect(itemVec1, itemVec2)) {
                coratings++;
            }
            workMatrix.setEntry((int) id1, (int) id2, coratings);
        }
    }

    /**
     * @return A matrix of item corating values to be used by
     *         a {@code HIRItemScorer}.
     */
    public RealMatrix buildMatrix() {

        for (int i=0; i<itemSize; i++) {
            RealVector testRow = workMatrix.getRowVector(i);
            double testSum = testRow.getL1Norm();
            if (testSum != 0){
                testRow.mapDivideToSelf(testSum);
                workMatrix.setRowVector(i, testRow);
            }

        }
        return workMatrix;
    }
}
