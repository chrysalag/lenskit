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

import groovy.json.internal.MapItemValue;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.grouplens.lenskit.hir.HIRModel;
import org.grouplens.lenskit.hir.HIRModelBuilder;
import org.grouplens.lenskit.transform.normalize.DefaultUserVectorNormalizer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.lenskit.data.dao.*;
import org.lenskit.data.ratings.Rating;
import org.lenskit.knn.item.model.ItemItemBuildContext;
import org.lenskit.knn.item.model.ItemItemBuildContextProvider;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by chrysalag.
 */

@SuppressWarnings("deprecation")
public class HIRModelBuilderTest {

    public static final double EPSILON = 1.0e-6;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    MapItemGenreDAO gdao;
    Collection<Long> items;


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
        items = MapItemGenreDAO.fromCSVFile(f).getItemIds();
    }

/*    @Before
    public void takeItemData(){
        Collection<Long> items = new HashSet<>();
        items.add((long)318);
        items.add((long)2329);
        items.add((long)5475);
        items.add((long)7323);
        items.add((long)48394);
        items.add((long)64716);
        items.add((long)117444);
        items.add((long)140214);



    }*/

    private HIRModel getModel(List<Rating> ratings) {
        EventDAO dao = EventCollectionDAO.create(ratings);
        UserEventDAO udao = new PrefetchingUserEventDAO(dao);
        ItemDAO idao = new PrefetchingItemDAO(dao);
        UserHistorySummarizer summarizer = new RatingVectorUserHistorySummarizer();
        ItemItemBuildContextProvider contextFactory = new ItemItemBuildContextProvider(
                udao, new DefaultUserVectorNormalizer(), summarizer);
        HIRModelBuilder provider = new HIRModelBuilder(idao, gdao, contextFactory.get());
        return provider.get();
    }

    @Test
    public void testBuild1() {

        List<Rating> rs = new ArrayList<>();
            rs.add(Rating.create(1, 318, 5));
            rs.add(Rating.create(2, 318, 4));
            rs.add(Rating.create(3, 48394, 5));
            rs.add(Rating.create(4, 48394, 1));
            rs.add(Rating.create(1, 117444, 5));
            rs.add(Rating.create(2, 117444, 4));

            HIRModel model1 = getModel(rs);

            MutableSparseVector msv318 = MutableSparseVector.create(318, 48394, 117444);
            MutableSparseVector msv48394 = MutableSparseVector.create(318, 48394, 117444);
            MutableSparseVector msv117444 = MutableSparseVector.create(318, 48394, 117444);

            msv318.set(318, 0);
            msv318.set(48394, 0);
            msv318.set(117444, 1);

            msv48394.set(318, 0);
            msv48394.set(48394, 0);
            msv48394.set(117444, 0);

            msv117444.set(318, 1);
            msv117444.set(48394, 0);
            msv117444.set(117444, 0);

            assertEquals(msv318, model1.getCoratingsVector(318));
            assertEquals(msv48394, model1.getCoratingsVector(48394));
            assertEquals(msv117444, model1.getCoratingsVector(117444));
    }

/*
    @Test
    public void testBuild2() {

        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Rating.create(1, 318, 4));
        rs.add(Rating.create(2, 318, 5));
        rs.add(Rating.create(3, 318, 4));
        rs.add(Rating.create(1, 48394, 3));
        rs.add(Rating.create(2, 48394, 5));
        rs.add(Rating.create(3, 48394, 1));
        rs.add(Rating.create(1, 117444, 1));
        rs.add(Rating.create(2, 117444, 5));
        rs.add(Rating.create(3, 117444, 3));

        HIRModel model2 = getModel(rs);

        MutableSparseVector msv1 = MutableSparseVector.create(318, 2329, 5475, 7323, 48394, 64716, 117444, 140214);
        MutableSparseVector msv2 = MutableSparseVector.create(318, 2329, 5475, 7323, 48394, 64716, 117444, 140214);
        MutableSparseVector msv3 = MutableSparseVector.create(318, 2329, 5475, 7323, 48394, 64716, 117444, 140214);
        MutableSparseVector msv4 = MutableSparseVector.create(318, 2329, 5475, 7323, 48394, 64716, 117444, 140214);
        MutableSparseVector msv5 = MutableSparseVector.create(318, 2329, 5475, 7323, 48394, 64716, 117444, 140214);
        MutableSparseVector msv6 = MutableSparseVector.create(318, 2329, 5475, 7323, 48394, 64716, 117444, 140214);
        MutableSparseVector msv7 = MutableSparseVector.create(318, 2329, 5475, 7323, 48394, 64716, 117444, 140214);
        MutableSparseVector msv8 = MutableSparseVector.create(318, 2329, 5475, 7323, 48394, 64716, 117444, 140214);


        MutableSparseVector pv1 = MutableSparseVector.create(318, 2329, 5475, 7323, 48394, 64716, 117444, 140214);
        MutableSparseVector pv2 = MutableSparseVector.create(318, 2329, 5475, 7323, 48394, 64716, 117444, 140214);
        MutableSparseVector pv3 = MutableSparseVector.create(318, 2329, 5475, 7323, 48394, 64716, 117444, 140214);
        MutableSparseVector pv4 = MutableSparseVector.create(318, 2329, 5475, 7323, 48394, 64716, 117444, 140214);
        MutableSparseVector pv5 = MutableSparseVector.create(318, 2329, 5475, 7323, 48394, 64716, 117444, 140214);
        MutableSparseVector pv6 = MutableSparseVector.create(318, 2329, 5475, 7323, 48394, 64716, 117444, 140214);
        MutableSparseVector pv7 = MutableSparseVector.create(318, 2329, 5475, 7323, 48394, 64716, 117444, 140214);
        MutableSparseVector pv8 = MutableSparseVector.create(318, 2329, 5475, 7323, 48394, 64716, 117444, 140214);

        // 318
        msv1.set(318, 0);
        msv1.set(2329, 0);
        msv1.set(5475, 0);
        msv1.set(7323, 0);
        msv1.set(48394, 1/8);
        msv1.set(64716, 0);
        msv1.set(117444, 1/8);
        msv1.set(140214, 0);

        // 2329
        msv2.set(318, 0);
        msv2.set(2329, 0);
        msv2.set(5475, 0);
        msv2.set(7323, 0);
        msv2.set(48394, 0);
        msv2.set(64716, 0);
        msv2.set(117444, 0);
        msv2.set(140214, 0);

        // 5475
        msv3.set(318, 0);
        msv3.set(2329, 0);
        msv3.set(5475, 0);
        msv3.set(7323, 0);
        msv3.set(48394, 0);
        msv3.set(64716, 0);
        msv3.set(117444, 0);
        msv3.set(140214, 0);

        // 7323
        msv4.set(318, 0);
        msv4.set(2329, 0);
        msv4.set(5475, 0);
        msv4.set(7323, 0);
        msv4.set(48394, 0);
        msv4.set(64716, 0);
        msv4.set(117444, 0);
        msv4.set(140214, 0);

        // 48394
        msv5.set(318, 1/8);
        msv5.set(2329, 0);
        msv5.set(5475, 0);
        msv5.set(7323, 0);
        msv5.set(48394, 0);
        msv5.set(64716, 0);
        msv5.set(117444, 1/8);
        msv5.set(140214, 0);

        // 64716
        msv6.set(318, 0);
        msv6.set(2329, 0);
        msv6.set(5475, 0);
        msv6.set(7323, 0);
        msv6.set(48394, 0);
        msv6.set(64716, 0);
        msv6.set(117444, 0);
        msv6.set(140214, 0);

        // 117444
        msv7.set(318, 1/8);
        msv7.set(2329, 0);
        msv7.set(5475, 0);
        msv7.set(7323, 0);
        msv7.set(48394, 1/8);
        msv7.set(64716, 0);
        msv7.set(117444, 0);
        msv7.set(140214, 0);

        // 140214
        msv8.set(318, 0);
        msv8.set(2329, 0);
        msv8.set(5475, 0);
        msv8.set(7323, 0);
        msv8.set(48394, 0);
        msv8.set(64716, 0);
        msv8.set(117444, 0);
        msv8.set(140214, 0);

        // 318
        pv1.set(318, 0.32143);
        pv1.set(2329, 0.32143);
        pv1.set(5475, 0.07143);
        pv1.set(7323, 0.07143);
        pv1.set(48394, 0.07143);
        pv1.set(64716, 0.07143);
        pv1.set(117444, 0);
        pv1.set(140214, 0.07143);

        // 2329
        pv2.set(318, 0.32143);
        pv2.set(2329, 0.32143);
        pv2.set(5475, 0.07143);
        pv2.set(7323, 0.07143);
        pv2.set(48394, 0.07143);
        pv2.set(64716, 0.07143);
        pv2.set(117444, 0);
        pv2.set(140214, 0.07143);

        // 5475
        pv3.set(318, 0.04762);
        pv3.set(2329, 0.04762);
        pv3.set(5475, 0.49206);
        pv3.set(7323, 0.04762);
        pv3.set(48394, 0.15873);
        pv3.set(64716,  0.04762);
        pv3.set(117444, 0);
        pv3.set(140214, 0.15873);

        // 7323
        pv4.set(318, 0.07143);
        pv4.set(2329, 0.07143);
        pv4.set(5475, 0.07143);
        pv4.set(7323, 0.57143);
        pv4.set(48394, 0.07143);
        pv4.set(64716, 0.07143);
        pv4.set(117444, 0);
        pv4.set(140214, 0.07143);

        // 48394
        pv5.set(318, 0.04762);
        pv5.set(2329, 0.04762);
        pv5.set(5475, 0.15873);
        pv5.set(7323, 0.04762);
        pv5.set(48394, 0.32540);
        pv5.set(64716, 0.04762);
        pv5.set(117444, 0.16667);
        pv5.set(140214, 0.15873);

        // 64716
        pv6.set(318, 0.14286);
        pv6.set(2329, 0.14286);
        pv6.set(5475, 0.14286);
        pv6.set(7323, 0.14286);
        pv6.set(48394, 0.14286);
        pv6.set(64716, 0.14286);
        pv6.set(117444, 0);
        pv6.set(140214, 0.14286);

        // 117444
        pv7.set(318, 0);
        pv7.set(2329, 0);
        pv7.set(5475, 0);
        pv7.set(7323, 0);
        pv7.set(48394, 0.16667);
        pv7.set(64716, 0);
        pv7.set(117444, 0.83333);
        pv7.set(140214, 0);

        // 140214
        pv8.set(318, 0.07143);
        pv8.set(2329, 0.07143);
        pv8.set(5475, 0.23810);
        pv8.set(7323, 0.07143);
        pv8.set(48394, 0.23810);
        pv8.set(64716, 0.07143);
        pv8.set(117444, 0);
        pv8.set(140214, 0.23810);

        assertEquals(msv1, model2.getCoratingsVector(318));
        assertEquals(msv2, model2.getCoratingsVector(2329));
        assertEquals(msv3, model2.getCoratingsVector(5475));
        assertEquals(msv4, model2.getCoratingsVector(7323));
        assertEquals(msv5, model2.getCoratingsVector(48394));
        assertEquals(msv6, model2.getCoratingsVector(64716));
        assertEquals(msv7, model2.getCoratingsVector(117444));
        assertEquals(msv8, model2.getCoratingsVector(140214));

        assertEquals(pv1, model2.getProximityVector(318, items));
        assertEquals(pv2, model2.getProximityVector(2329, items));
        assertEquals(pv3, model2.getProximityVector(5475, items));
        assertEquals(pv4, model2.getProximityVector(7323, items));
        assertEquals(pv5, model2.getProximityVector(48394, items));
        assertEquals(pv6, model2.getProximityVector(64716, items));
        assertEquals(pv7, model2.getProximityVector(117444, items));
        assertEquals(pv8, model2.getProximityVector(140214, items));
    }*/

    /*
    @Test
    public void testBuild3() {

        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Rating.create(1, 6, 4));
        rs.add(Rating.create(2, 6, 2));
        rs.add(Rating.create(1, 7, 3));
        rs.add(Rating.create(2, 7, 2));
        rs.add(Rating.create(3, 7, 5));
        rs.add(Rating.create(4, 7, 2));
        rs.add(Rating.create(1, 8, 3));
        rs.add(Rating.create(2, 8, 4));
        rs.add(Rating.create(3, 8, 3));
        rs.add(Rating.create(4, 8, 2));
        rs.add(Rating.create(5, 8, 3));
        rs.add(Rating.create(6, 8, 2));
        rs.add(Rating.create(1, 9, 3));
        rs.add(Rating.create(3, 9, 4));

        SlopeOneModel model3 = getModel(rs);

        assertEquals(2, model3.getCoratings(6, 7));
        assertEquals(2, model3.getCoratings(7, 6));
        assertEquals(2, model3.getCoratings(6, 8));
        assertEquals(2, model3.getCoratings(8, 6));
        assertEquals(1, model3.getCoratings(6, 9));
        assertEquals(1, model3.getCoratings(9, 6));
        assertEquals(4, model3.getCoratings(7, 8));
        assertEquals(4, model3.getCoratings(8, 7));
        assertEquals(2, model3.getCoratings(7, 9));
        assertEquals(2, model3.getCoratings(9, 7));
        assertEquals(2, model3.getCoratings(8, 9));
        assertEquals(2, model3.getCoratings(9, 8));
        assertEquals(0.5, model3.getDeviation(6, 7), EPSILON);
        assertEquals(-0.5, model3.getDeviation(7, 6), EPSILON);
        assertEquals(-0.5, model3.getDeviation(6, 8), EPSILON);
        assertEquals(0.5, model3.getDeviation(8, 6), EPSILON);
        assertEquals(1, model3.getDeviation(6, 9), EPSILON);
        assertEquals(-1, model3.getDeviation(9, 6), EPSILON);
        assertEquals(0, model3.getDeviation(7, 8), EPSILON);
        assertEquals(0, model3.getDeviation(8, 7), EPSILON);
        assertEquals(0.5, model3.getDeviation(7, 9), EPSILON);
        assertEquals(-0.5, model3.getDeviation(9, 7), EPSILON);
        assertEquals(-0.5, model3.getDeviation(8, 9), EPSILON);
        assertEquals(0.5, model3.getDeviation(9, 8), EPSILON);
    }

    @Test
    public void testBuild4() {
        List<Rating> rs = new ArrayList<Rating>();
        rs.add(Rating.create(1, 4, 3.5));
        rs.add(Rating.create(2, 4, 5));
        rs.add(Rating.create(3, 5, 4.25));
        rs.add(Rating.create(2, 6, 3));
        rs.add(Rating.create(1, 7, 4));
        rs.add(Rating.create(2, 7, 4));
        rs.add(Rating.create(3, 7, 1.5));

        SlopeOneModel model4 = getModel(rs);

        assertEquals(0, model4.getCoratings(4, 5));
        assertEquals(0, model4.getCoratings(5, 4));
        assertEquals(1, model4.getCoratings(4, 6));
        assertEquals(1, model4.getCoratings(6, 4));
        assertEquals(2, model4.getCoratings(4, 7));
        assertEquals(2, model4.getCoratings(7, 4));
        assertEquals(0, model4.getCoratings(5, 6));
        assertEquals(0, model4.getCoratings(6, 5));
        assertEquals(1, model4.getCoratings(5, 7));
        assertEquals(1, model4.getCoratings(7, 5));
        assertEquals(1, model4.getCoratings(6, 7));
        assertEquals(1, model4.getCoratings(7, 6));
        assertEquals(Double.NaN, model4.getDeviation(4, 5), 0);
        assertEquals(Double.NaN, model4.getDeviation(5, 4), 0);
        assertEquals(2, model4.getDeviation(4, 6), EPSILON);
        assertEquals(-2, model4.getDeviation(6, 4), EPSILON);
        assertEquals(0.25, model4.getDeviation(4, 7), EPSILON);
        assertEquals(-0.25, model4.getDeviation(7, 4), EPSILON);
        assertEquals(Double.NaN, model4.getDeviation(5, 6), 0);
        assertEquals(Double.NaN, model4.getDeviation(6, 5), 0);
        assertEquals(2.75, model4.getDeviation(5, 7), EPSILON);
        assertEquals(-2.75, model4.getDeviation(7, 5), EPSILON);
        assertEquals(-1, model4.getDeviation(6, 7), EPSILON);
        assertEquals(1, model4.getDeviation(7, 6), EPSILON);
    } */
}