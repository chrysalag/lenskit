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
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.data.dao.ItemDAO;
import org.lenskit.data.dao.ItemGenreDAO;

/**
 * Created by chrysalag.
 */

public class RowStochasticFactorOfProximity {

    private RealMatrix prepareGenresMatrix;

    private RealMatrix rowStochastic;

    public RowStochasticFactorOfProximity(ItemDAO dao,
                                   ItemGenreDAO gDao) {
        LongSet items = dao.getItemIds();
        int genreSize = gDao.getGenreSize();
        int itemsSize = items.size();

        double[][] data = new double[itemsSize][genreSize];

        //prepareGenresMatrix = MatrixUtils.createRealMatrix(itemsSize, genreSize);
        prepareGenresMatrix = MatrixUtils.createRealMatrix(data);
        //rowStochastic = MatrixUtils.createRealMatrix(itemsSize, genreSize);
        rowStochastic = MatrixUtils.createRealMatrix(data);

        int i = 0;
        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            long item = iter.nextLong();
            //    double[][] rowData = new double[1][genreSize];
            //    rowData[0] = gDao.getItemGenre(item).toArray();
            //    if (rowData[0] != null) {
            //        prepareGenresMatrix.setRow(i, rowData[0]);
            //    }
                prepareGenresMatrix.setRowVector(i, gDao.getItemGenre(item));
                i++;
        }
    }

    public RealMatrix RowStochastic() {

        rowStochastic = prepareGenresMatrix.copy();

        int itemsSize = rowStochastic.getRowDimension();

        for (int i = 0; i < itemsSize; i++) {
            RealVector forIter = rowStochastic.getRowVector(i);

            double sum = forIter.getL1Norm();

            RealVector stochasticRow = forIter.mapDivide(sum);

            double[] row = stochasticRow.toArray();

            rowStochastic.setRow(i, row);
        }

        return rowStochastic;

    }
}
