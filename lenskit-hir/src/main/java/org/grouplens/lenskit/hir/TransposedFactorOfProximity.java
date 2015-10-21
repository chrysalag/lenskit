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
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.data.dao.ItemDAO;
import org.lenskit.data.dao.ItemGenreDAO;

/**
 * Created by chrysalag.
 */

public class TransposedFactorOfProximity {

    private RealMatrix prepareGenresMatrix;

    private RealMatrix transposed;

    public TransposedFactorOfProximity(ItemDAO dao,
                                   ItemGenreDAO gDao) {
        LongSet items = dao.getItemIds();
        int genreSize = gDao.getGenreSize();

        prepareGenresMatrix = MatrixUtils.createRealMatrix(items.size(), genreSize);
        transposed = MatrixUtils.createRealMatrix(genreSize, items.size());

        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            long item = iter.nextLong();
            prepareGenresMatrix.setRowVector((int) item, gDao.getItemGenre(item));
        }
    }


    public RealMatrix ColumnStochastic() {

        transposed = prepareGenresMatrix.transpose();

        int itemsSize = transposed.getRowDimension();

        for (int i = 0; i < itemsSize; i++) {
            RealVector forIter = transposed.getRowVector(i);

            double sum = forIter.getL1Norm();

            RealVector stochasticRow = forIter.mapDivide(sum);

            transposed.setRowVector(i, stochasticRow);
        }

        return transposed;

    }

}
