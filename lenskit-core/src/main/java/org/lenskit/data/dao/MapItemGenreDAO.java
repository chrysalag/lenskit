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

package org.lenskit.data.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.apache.commons.lang3.text.StrTokenizer;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.io.LineStream;
import org.lenskit.util.io.ObjectStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * An item genre DAO backed by a map of item IDs to genres.
 *
 * @see org.lenskit.data.dao.ItemGenreDAO
 */

public class MapItemGenreDAO implements ItemGenreDAO, ItemDAO, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(MapItemGenreDAO.class);
    private static final long serialVersionUID = 1L;
    private final Map<Long, RealVector> itemGenreMap;
    private final LongSortedSet itemIds;
    private static int genreSize = 0;

    public MapItemGenreDAO(Map<Long, RealVector> items) {
        itemGenreMap = ImmutableMap.copyOf(items);
        itemIds = LongUtils.packedSet(itemGenreMap.keySet());
    }

    @Nullable
    @Override
    public LongSet getItemIds() {
        return itemIds;
    }

    @Nullable
    @Override
    public RealVector getItemGenre(long item) {
        return itemGenreMap.get(item);
    }

    @Override
    public int getGenreSize() { return genreSize; }


    /**
     * Read an item list DAO from a file with no header rows.
     * @param file A file of item IDs, one per line.
     * @return The item list DAO.
     * @throws java.io.IOException if there is an error reading the list of items.
     */
    public static MapItemGenreDAO fromCSVFile(File file) throws IOException {
        return fromCSVFile(file, 0);
    }

    /**
     * Read an item list DAO from a file.
     * @param file A file of item IDs, one per line.
     * @param skipLines The number of initial header to skip
     * @return The item list DAO.
     * @throws java.io.IOException if there is an error reading the list of items.
     */
    public static MapItemGenreDAO fromCSVFile(File file, int skipLines) throws IOException {
        Preconditions.checkArgument(skipLines >= 0, "cannot skip negative lines");
        LineStream stream = LineStream.openFile(file, CompressionMode.AUTO);
        try {
            ObjectStreams.consume(skipLines, stream);
            ImmutableMap.Builder<Long, RealVector> genres = ImmutableMap.builder();
            StrTokenizer tok = StrTokenizer.getCSVInstance();
            for (String line : stream) {
                tok.reset(line);
                long item = Long.parseLong(tok.next());
                String title = tok.nextToken();
                String genre = tok.nextToken();
                if (genre != null) {
                    StrTokenizer gen = new StrTokenizer(genre, "|");
                    genreSize = gen.size();
                    double[] genValues = new double[genreSize];
                    int i = 0;
                    while (gen.hasNext()) {
                        double genValue = Double.parseDouble(gen.nextToken());
                        genValues[i] = genValue;
                        i++;
                    }
                    RealVector genVec = MatrixUtils.createRealVector(genValues);
                    genres.put(item, genVec);
                }
            }
            return new MapItemGenreDAO(genres.build());
        } catch (NoSuchElementException ex) {
            throw new IOException(String.format("%s:%s: not enough columns",
                                                file, stream.getLineNumber()),
                                  ex);
        } catch (NumberFormatException ex) {
            throw new IOException(String.format("%s:%s: id not an integer",
                                                file, stream.getLineNumber()),
                                  ex);
        } finally {
            stream.close();
        }
    }
}
