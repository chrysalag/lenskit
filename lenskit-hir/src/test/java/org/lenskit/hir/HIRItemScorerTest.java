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

import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.lenskit.hir.HIRItemScorer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.data.dao.EventCollectionDAO;
import org.lenskit.data.dao.EventDAO;
import org.lenskit.data.dao.ItemGenreDAO;
import org.lenskit.data.dao.MapItemGenreDAO;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.PreferenceDomainBuilder;
import org.lenskit.data.ratings.Rating;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
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

    @Before
    public void createFile() throws IOException {
        File f = folder.newFile("genres.csv");
        PrintStream str = new PrintStream(f);
        try {
            str.println("318,\"Shawshank Redemption, The (1994)\",1|0|1|0");
            str.println("48394,\"Pan's Labyrinth (Laberinto del fauno, El) (2006)\",1|1|0|0");
            str.println("117444,Song of the Sea (2014),1|0|0|0");
        } finally {
            str.close();
        }
        gdao = MapItemGenreDAO.fromCSVFile(f);
    }

    @Test
    public void testPredict1() throws RecommenderBuildException {

        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Rating.create(1, 318, 5));
        rs.add(Rating.create(2, 318, 4));
        rs.add(Rating.create(3, 48394, 5));
        rs.add(Rating.create(4, 48394, 1));
        rs.add(Rating.create(1, 117444, 5));
        rs.add(Rating.create(2, 117444, 4));

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(MapItemGenreDAO.class).to(gdao);
        config.bind(EventDAO.class).to(EventCollectionDAO.create(rs));
        config.bind(ItemScorer.class).to(HIRItemScorer.class);
        config.bind(PreferenceDomain.class).to(new PreferenceDomainBuilder(1, 5)
                                                       .setPrecision(1)
                                                       .build());
        ItemScorer predictor = LenskitRecommenderEngine.build(config)
                                                       .createRecommender()
                                                       .getItemScorer();


        assertThat(predictor, notNullValue());
    /*  assertEquals(7 / 3.0, predictor.score(2, 9).getScore(), EPSILON);
        assertEquals(13 / 3.0, predictor.score(3, 6).getScore(), EPSILON);
        assertEquals(2, predictor.score(4, 6).getScore(), EPSILON);
        assertEquals(2, predictor.score(4, 9).getScore(), EPSILON);
        assertEquals(2.5, predictor.score(5, 6).getScore(), EPSILON);
        assertEquals(3, predictor.score(5, 7).getScore(), EPSILON);
        assertEquals(3.5, predictor.score(5, 9).getScore(), EPSILON);
        assertEquals(1.5, predictor.score(6, 6).getScore(), EPSILON);
        assertEquals(2, predictor.score(6, 7).getScore(), EPSILON);
        assertEquals(2.5, predictor.score(6, 9).getScore(), EPSILON);
    */
    }
}
