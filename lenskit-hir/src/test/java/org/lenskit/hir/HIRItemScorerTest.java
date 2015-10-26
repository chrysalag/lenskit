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
import org.lenskit.data.dao.*;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.PreferenceDomainBuilder;
import org.lenskit.data.ratings.Rating;

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

    private static final double EPSILON = 1.0e-6;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    MapItemGenreDAO gdao;
    LongSet idao;

    @Before
    public void createFile() throws IOException {
        File f = folder.newFile("genres.csv");
        PrintStream str = new PrintStream(f);
        try {
            str.println("318,\"Shawshank Redemption, The (1994)\",0|0|0|0|0|1|0|1|0|0|0|0|0|0|0|0|0|0|0|0");
            str.println("2329,American History X (1998),0|0|0|0|0|1|0|1|0|0|0|0|0|0|0|0|0|0|0|0");
            str.println("5475,Z (1969),0|0|0|0|0|0|0|1|0|0|0|0|1|0|0|1|0|0|0|0");
            str.println("7323,\"Good bye, Lenin! (2003)\",0|0|0|0|1|0|0|1|0|0|0|0|0|0|0|0|0|0|0|0");
            str.println("48394,\"Pan's Labyrinth (Laberinto del fauno, El) (2006)\",0|0|0|0|0|0|0|1|1|0|0|0|0|0|0|1|0|0|0|0");
            str.println("64716,Seven Pounds (2008),0|0|0|0|0|0|0|1|0|0|0|0|0|0|0|0|0|0|0|0");
            str.println("117444,Song of the Sea (2014),0|0|1|1|0|0|0|0|1|0|0|0|0|0|0|0|0|0|0|0");
            str.println("140214,Triple Dog (2010),0|0|0|0|0|0|0|1|0|0|0|0|0|0|0|1|0|0|0|0");
        } finally {

            str.close();
        }
        gdao = MapItemGenreDAO.fromCSVFile(f);
        idao = MapItemGenreDAO.fromCSVFile(f).getItemIds();
    }

    @Test
    public void testPredict1() throws RecommenderBuildException {

        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Rating.create(1, 318, 5));
        rs.add(Rating.create(2, 318, 4));
        rs.add(Rating.create(3, 48394, 5));
        rs.add(Rating.create(4, 140214, 1));
        rs.add(Rating.create(1, 117444, 5));
        rs.add(Rating.create(2, 117444, 4));

        Collection<Long> items = new HashSet<>();
        items.add((long)318);
        items.add((long)2329);
        items.add((long)5475);
        items.add((long)7323);
        items.add((long)48394);
        items.add((long)64716);
        items.add((long)117444);
        items.add((long)140214);


        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(MapItemGenreDAO.class).to(gdao);
        config.bind(EventDAO.class).to(EventCollectionDAO.create(rs));
        config.addRoot(ItemDAO.class);
        config.bind(ItemDAO.class).to(gdao);
        config.bind(ItemScorer.class).to(HIRItemScorer.class);
        config.bind(PreferenceDomain.class).to(new PreferenceDomainBuilder(1, 5)
                                                       .setPrecision(1)
                                                       .build());
        ItemScorer predictor = LenskitRecommenderEngine.build(config)
                                                       .createRecommender(config)
                                                       .getItemScorer();

        assertThat(predictor, notNullValue());
//        assertThat(predictor.score(3, 318).getScore(), notNullValue());
//        assertEquals(0.385714, predictor.score(4, 318).getScore());
//        assertEquals(0.085714, predictor.score(2, 64716).getScore(), EPSILON);
//        assertEquals(0.014286, predictor.score(4, 318).getScore(), EPSILON);
  //      assertEquals(0.047619, predictor.score(4, 140214).getScore(), EPSILON);
        //assertEquals(4.250000, predictor.score(1, 117444).getScore(), EPSILON);
    //    assertEquals(0.085714, predictor.score(2, 140214).getScore(), EPSILON);
        //assertEquals(0.250000, predictor.score(3, 117444).getScore(), EPSILON);
      //  assertEquals(0.014286, predictor.score(4, 7323).getScore(), EPSILON);
        //assertEquals(0.014286, predictor.score(4, 2329).getScore(), EPSILON);
//        assertEquals(3.482143, predictor.score(1, 318).getScore(), EPSILON);
//        assertEquals(3.385714, predictor.score(2, 318).getScore(), EPSILON);
    }
}
