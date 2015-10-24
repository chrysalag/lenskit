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
import org.lenskit.api.ItemScorer;
import org.lenskit.api.RatingPredictor;
import org.lenskit.api.Recommender;
import org.lenskit.api.RecommenderBuildException;
import org.lenskit.baseline.BaselineScorer;
import org.lenskit.baseline.ItemMeanRatingItemScorer;
import org.lenskit.baseline.UserMeanBaseline;
import org.lenskit.baseline.UserMeanItemScorer;
import org.lenskit.basic.SimpleRatingPredictor;
import org.lenskit.basic.TopNItemRecommender;
import org.lenskit.data.dao.EventCollectionDAO;
import org.lenskit.data.dao.EventDAO;
import org.lenskit.data.dao.MapItemGenreDAO;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.Rating;

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
 * Created by chrysalag.
 */
public class HIRItemRecommenderTest {
    private LenskitRecommenderEngine engine;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    MapItemGenreDAO gdao;

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
    }

    @SuppressWarnings("deprecation")
    @Before
    public void setup() throws RecommenderBuildException {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Rating.create(1, 318, 2));
        rs.add(Rating.create(1, 5475, 4));
        rs.add(Rating.create(8, 7323, 5));
        rs.add(Rating.create(8, 318, 4));

        EventDAO dao = new EventCollectionDAO(rs);

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(dao);
        config.bind(MapItemGenreDAO.class).to(gdao);
        config.bind(ItemScorer.class).to(HIRItemScorer.class);
        config.bind(PreferenceDomain.class).to(new PreferenceDomain(0, 5));
        // factory.setComponent(UserVectorNormalizer.class, IdentityVectorNormalizer.class);
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
