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

    private RealMatrix transposed;

    private int genreSize;

    private int itemSize;

    public TransposedFactorOfProximity(ItemDAO dao,
                                   ItemGenreDAO gDao) {

        LongSet items = dao.getItemIds();
        genreSize = gDao.getGenreSize();
        itemSize = items.size();

        double[][] dataTransposed = new double[genreSize][itemSize];

        transposed = MatrixUtils.createRealMatrix(dataTransposed);

        int i = 0;
        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            long item = iter.nextLong();
            transposed.setColumnVector(i, gDao.getItemGenre(item));
            i++;
        }
    }


    public RealMatrix ColumnStochastic() {

        //int genreSize = transposed.getRowDimension();

        for (int i = 0; i < genreSize; i++) {
            RealVector forIter = transposed.getRowVector(i);
            double sum = forIter.getL1Norm();
            if (sum!=0){
                //RealVector stochasticRow = forIter.mapDivide(sum);
                forIter.mapDivideToSelf(sum);
                transposed.setRowVector(i, forIter);
            }
        }

        return transposed;
    }
}
