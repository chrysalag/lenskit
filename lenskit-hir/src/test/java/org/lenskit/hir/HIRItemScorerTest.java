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

package org.lenskit.hir;

import org.grouplens.lenskit.hir.HIRItemScorer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.*;
import org.lenskit.data.dao.*;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.PreferenceDomainBuilder;
import org.lenskit.data.ratings.Rating;
import org.lenskit.util.collections.LongUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Tests HIR Item Scorer
 */

public class HIRItemScorerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    MapItemGenreDAO gdao;
    private static final double EPSILON = 1.0e-6;

    @Before
    public void createFile() throws IOException {
        File f = folder.newFile("genres.csv");
        PrintStream str = new PrintStream(f);
        try {
            str.println("0,\"Shawshank Redemption, The (1994)\",0|0|0|0|0|1|0|1|0|0|0|0|0|0|0|0|0|0|0|0");
            str.println("1,American History X (1998),0|0|0|0|0|1|0|1|0|0|0|0|0|0|0|0|0|0|0|0");
            str.println("2,Z (1969),0|0|0|0|0|0|0|1|0|0|0|0|1|0|0|1|0|0|0|0");
            str.println("3,\"Pan's Labyrinth (Laberinto del fauno, El) (2006)\",0|0|0|0|0|0|0|1|1|0|0|0|0|0|0|1|0|0|0|0");
            str.println("4,Seven Pounds (2008),0|0|0|0|0|0|0|1|0|0|0|0|0|0|0|0|0|0|0|0");
            str.println("5,Song of the Sea (2014),0|0|1|1|0|0|0|0|1|0|0|0|0|0|0|0|0|0|0|0");
        } finally {

            str.close();
        }
        gdao = MapItemGenreDAO.fromCSVFile(f);
    }

    @Test
    public void testPredict1() throws RecommenderBuildException {

        List<Rating> rs = new ArrayList<>();
            rs.add(Rating.create(1, 0, 4));
            rs.add(Rating.create(1, 4, 3));
            rs.add(Rating.create(1, 5, 1));
            rs.add(Rating.create(2, 0, 4));
            rs.add(Rating.create(2, 4, 4));
            rs.add(Rating.create(2, 5, 4));
            rs.add(Rating.create(3, 0, 1));
            rs.add(Rating.create(3, 4, 1));
            rs.add(Rating.create(3, 5, 3));

        Collection<Long> items = new HashSet<>();
        items.add((long)0);
        items.add((long)1);
        items.add((long)2);
        items.add((long)3);
        items.add((long)4);
        items.add((long)5);

        ItemDAO idao = new ItemListItemDAO(LongUtils.packedSet(0, 1, 2, 3, 4, 5));
        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(MapItemGenreDAO.class).to(gdao);
        config.bind(EventDAO.class).to(EventCollectionDAO.create(rs));
        config.bind(ItemDAO.class).to(idao);
        config.bind(ItemScorer.class).to(HIRItemScorer.class);
        config.bind(PreferenceDomain.class).to(new PreferenceDomainBuilder(0, 1)
                                                       .setPrecision(1)
                                                       .build());

        ResultMap predictor1 = LenskitRecommenderEngine.build(config)
                                                       .createRecommender(config)
                                                       .getItemScorer()
                                                       .scoreWithDetails(1, items);
        ResultMap predictor2 = LenskitRecommenderEngine.build(config)
                                                       .createRecommender(config)
                                                       .getItemScorer()
                                                       .scoreWithDetails(2, items);
        ResultMap predictor3 = LenskitRecommenderEngine.build(config)
                                                       .createRecommender(config)
                                                       .getItemScorer()
                                                       .scoreWithDetails(3, items);

        assert predictor1.size() == 3;
        assert predictor2.size() == 3;
        assert predictor3.size() == 3;


/*  Rank =
         11/40       3/40       3/80      7/160      21/80     49/160
        173/600     11/200      3/100      7/150     79/300      19/60
        293/1000    33/1000      9/500      6/125    139/500     33/100
*/

        assertEquals(3.0 / 40.0, predictor1.getScore(1), EPSILON);
        assertEquals(3.0 / 80.0, predictor1.getScore(2), EPSILON);
        assertEquals(7.0 / 160.0, predictor1.getScore(3), EPSILON);
        assertEquals(11.0 / 200.0, predictor2.getScore(1), EPSILON);
        assertEquals(3.0 / 100.0, predictor2.getScore(2), EPSILON);
        assertEquals(7.0 / 150.0, predictor2.getScore(3), EPSILON);
        assertEquals(33.0 / 1000.0, predictor3.getScore(1), EPSILON);
        assertEquals(9.0 / 500.0, predictor3.getScore(2), EPSILON);
        assertEquals(6.0 / 125.0, predictor3.getScore(3), EPSILON);
    }
}
