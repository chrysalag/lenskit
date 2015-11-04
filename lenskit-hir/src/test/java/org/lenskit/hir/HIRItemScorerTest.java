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

import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.lenskit.hir.HIRItemScorer;
import org.grouplens.lenskit.hir.HIRModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.api.ResultMap;
import org.lenskit.data.dao.*;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.PreferenceDomainBuilder;
import org.lenskit.data.ratings.Rating;
import org.lenskit.util.collections.LongUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by chrysalag.
 */

public class HIRItemScorerTest {

    private static final double EPSILON = 0.001;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    MapItemGenreDAO gdao;
    LongSet idao;

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
        idao = MapItemGenreDAO.fromCSVFile(f).getItemIds();
    }

   @Test
    public void testPredict1() throws RecommenderBuildException {

        List<Rating> rs = new ArrayList<Rating>();
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
        //config.addRoot(ItemDAO.class);
        config.bind(ItemDAO.class).to(idao);
        config.bind(ItemScorer.class).to(HIRItemScorer.class);
        config.bind(PreferenceDomain.class).to(new PreferenceDomainBuilder(1, 5)
                                                       .setPrecision(1)
                                                       .build());

        ResultMap predictor1 = LenskitRecommenderEngine.build(config).createRecommender(config).getItemScorer().scoreWithDetails(1, items);
        ResultMap predictor2 = LenskitRecommenderEngine.build(config).createRecommender(config).getItemScorer().scoreWithDetails(2, items);
        ResultMap predictor3 = LenskitRecommenderEngine.build(config).createRecommender(config).getItemScorer().scoreWithDetails(3, items);


        assert predictor1.size() == 3;
        assert predictor2.size() == 3;
        assert predictor3.size() == 3;

       /*2033/6000    11/2000    23/1000     7/1500  1009/3000     41/120
       345/1697   33/10000    69/5000      3/625  1009/5000    123/200
*/

       assertEquals(30.0 / 400.0, predictor1.getScore(1), 0.1);
       assertEquals(27.0 / 800.0, predictor1.getScore(2), 0.1);
       assertEquals(7.0 / 1600.0, predictor1.getScore(3), 0.1);
       assertEquals(11.0 / 2000.0, predictor2.getScore(1), 0.1);
       assertEquals(23.0 / 1000.0, predictor2.getScore(2), 0.1);
       assertEquals(7.0 / 1500.0, predictor2.getScore(3), 0.1);
       assertEquals(33.0 / 10000.0, predictor3.getScore(1), 0.1);
       assertEquals(69.0 / 5000.0, predictor3.getScore(2), 0.1);
       assertEquals(3.0 / 625.0, predictor3.getScore(3), 0.1);











       /* assertThat(predictor, notNullValue());
          //assertThat(predictor.score(3, 318).getScore(), notNullValue());
          //assert predictor.score(1, 4).hasScore();

          assertEquals((long)3 / 400.0, (long)predictor.score(1, 1).getScore(), EPSILON);
          assertEquals((long)27 / 800.0, (long)predictor.score(1, 2).getScore(), EPSILON);
          assertEquals((long)7 / 1600.0, (long)predictor.score(1, 3).getScore(), EPSILON);
          assertEquals((long)11 / 2000.0, (long)predictor.score(2, 1).getScore(), EPSILON);
          assertEquals((long)23 / 1000.0, (long)predictor.score(2, 2).getScore(), EPSILON);
          assertEquals((long)7 / 1500.0, (long)predictor.score(2, 3).getScore(), EPSILON);
          assertEquals((long)33 / 10000.0, (long)predictor.score(3, 1).getScore(), EPSILON);
          assertEquals((long)69 / 5000.0, (long)predictor.score(3, 2).getScore(), EPSILON);
          assertEquals((long)3 / 625.0, (long)predictor.score(3, 3).getScore(), EPSILON);

    */
    }
}
