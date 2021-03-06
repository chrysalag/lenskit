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
import org.grouplens.lenskit.hir.HIRModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.*;
import org.lenskit.baseline.BaselineScorer;
import org.lenskit.baseline.ItemMeanRatingItemScorer;
import org.lenskit.baseline.UserMeanBaseline;
import org.lenskit.baseline.UserMeanItemScorer;
import org.lenskit.basic.SimpleRatingPredictor;
import org.lenskit.basic.TopNItemRecommender;
import org.lenskit.data.dao.*;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.Rating;
import org.lenskit.util.collections.LongUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Tests if the HIR Item Recommender is created
 * and properly configured.
 */

public class HIRItemRecommenderTest {
    private LenskitRecommenderEngine engine;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    MapItemGenreDAO gdao;
    ItemDAO idao;
    EventDAO dao;

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

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<>();
        rs.add(Rating.create(1, 0, 2));
        rs.add(Rating.create(1, 1, 4));
        rs.add(Rating.create(2, 2, 5));
        rs.add(Rating.create(2, 3, 4));

        dao = new EventCollectionDAO(rs);
        idao = new ItemListItemDAO(LongUtils.packedSet(0, 1, 2, 3, 4, 5));


        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(dao);
        config.bind(ItemDAO.class).to(idao);
        config.bind(MapItemGenreDAO.class).to(gdao);
        config.bind(ItemScorer.class).to(HIRItemScorer.class);
        config.bind(PreferenceDomain.class).to(new PreferenceDomain(0, 1));
        config.bind(BaselineScorer.class, ItemScorer.class)
              .to(UserMeanItemScorer.class);
        config.bind(UserMeanBaseline.class, ItemScorer.class)
              .to(ItemMeanRatingItemScorer.class);
        engine = LenskitRecommenderEngine.build(config);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testHIRRecommenderEngineCreate() {
        try (Recommender rec = engine.createRecommender()) {
            assertThat(rec.getItemScorer(),
                       instanceOf(HIRItemScorer.class));
            RatingPredictor rp = rec.getRatingPredictor();
            assertThat(rp, notNullValue());
            assertThat(rp, instanceOf(SimpleRatingPredictor.class));
            assertThat(((SimpleRatingPredictor) rp).getItemScorer(),
                       sameInstance(rec.getItemScorer()));
            assertThat(rec.getItemRecommender(),
                       instanceOf(TopNItemRecommender.class));
        }
    }

    @Test
    public void testConfigSeparation() {
        try (LenskitRecommender rec1 = engine.createRecommender();
             LenskitRecommender rec2 = engine.createRecommender()) {

            assertThat(rec1.getItemScorer(),
                       not(sameInstance(rec2.getItemScorer())));
            assertThat(rec1.get(HIRModel.class),
                       allOf(not(nullValue()),
                             sameInstance(rec2.get(HIRModel.class))));
        }
    }
}