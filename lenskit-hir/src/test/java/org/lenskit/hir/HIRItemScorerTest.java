package org.lenskit.hir;

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

    @Test
    public void testPredict1() throws RecommenderBuildException {

        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Rating.create(1, 318, 4));
        rs.add(Rating.create(2, 318, 2));
        rs.add(Rating.create(1, 2329, 3));
        rs.add(Rating.create(2, 2329, 2));
        rs.add(Rating.create(3, 2329, 5));
        rs.add(Rating.create(4, 2329, 2));
        rs.add(Rating.create(1, 5475, 3));
        rs.add(Rating.create(2, 5475, 4));
        rs.add(Rating.create(3, 5475, 3));
        rs.add(Rating.create(4, 5475, 2));
        rs.add(Rating.create(5, 5475, 3));
        rs.add(Rating.create(6, 5475, 2));
        rs.add(Rating.create(1, 7323, 3));
        rs.add(Rating.create(3, 7323, 4));

        LenskitConfiguration config = new LenskitConfiguration();
        config.bind(EventDAO.class).to(EventCollectionDAO.create(rs));
        config.bind(ItemScorer.class).to(HIRItemScorer.class);
        config.bind(PreferenceDomain.class).to(new PreferenceDomainBuilder(1, 5)
                                                       .setPrecision(1)
                                                       .build());
        ItemScorer predictor = LenskitRecommenderEngine.build(config)
                                                       .createRecommender()
                                                       .getItemScorer();


        assertThat(predictor, notNullValue());
        assertEquals(7 / 3.0, predictor.score(2, 9).getScore(), EPSILON);
        assertEquals(13 / 3.0, predictor.score(3, 6).getScore(), EPSILON);
        assertEquals(2, predictor.score(4, 6).getScore(), EPSILON);
        assertEquals(2, predictor.score(4, 9).getScore(), EPSILON);
        assertEquals(2.5, predictor.score(5, 6).getScore(), EPSILON);
        assertEquals(3, predictor.score(5, 7).getScore(), EPSILON);
        assertEquals(3.5, predictor.score(5, 9).getScore(), EPSILON);
        assertEquals(1.5, predictor.score(6, 6).getScore(), EPSILON);
        assertEquals(2, predictor.score(6, 7).getScore(), EPSILON);
        assertEquals(2.5, predictor.score(6, 9).getScore(), EPSILON);

    }
}
