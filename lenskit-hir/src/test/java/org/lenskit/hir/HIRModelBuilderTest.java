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

import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.grouplens.lenskit.data.text.Formats;
import org.grouplens.lenskit.data.text.TextEventDAO;
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
import org.lenskit.knn.item.model.ItemItemBuildContextProvider;
import org.lenskit.util.collections.LongUtils;

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
    MapItemNameDAO idao;
    Collection<Long> items = new HashSet<>();
    EventDAO dao1, dao2;
    List<Rating> rs1 = new ArrayList<>();
    List<Rating> rs2 = new ArrayList<>();

    @Before
    public void createFile() throws IOException {
        File f = folder.newFile("genres.csv");
        PrintStream str = new PrintStream(f);
        try {
            str.println("0,\"Shawshank Redemption, The (1994)\",0|0|0|0|0|1|0|1|0|0|0|0|0|0|0|0|0|0|0|0");
            str.println("1,American History X (1998),0|0|0|0|0|1|0|1|0|0|0|0|0|0|0|0|0|0|0|0");
            str.println("2,Z (1969),0|0|0|0|0|0|0|1|0|0|0|0|1|0|0|1|0|0|0|0");
            str.println("3,\"Good bye, Lenin! (2003)\",0|0|0|0|1|0|0|1|0|0|0|0|0|0|0|0|0|0|0|0");
            str.println("4,\"Pan's Labyrinth (Laberinto del fauno, El) (2006)\",0|0|0|0|0|0|0|1|1|0|0|0|0|0|0|1|0|0|0|0");
            str.println("5,Seven Pounds (2008),0|0|0|0|0|0|0|1|0|0|0|0|0|0|0|0|0|0|0|0");
            str.println("6,Song of the Sea (2014),0|0|1|1|0|0|0|0|1|0|0|0|0|0|0|0|0|0|0|0");
        } finally {
            str.close();
        }
        gdao = MapItemGenreDAO.fromCSVFile(f);
        idao = MapItemNameDAO.fromCSVFile(f);
        items.add((long)0);
        items.add((long)1);
        items.add((long)2);
        items.add((long)3);
        items.add((long)4);
        items.add((long)5);
        items.add((long)6);
    }

    @Before
    public void createRating1() throws IOException {
        File r1 = folder.newFile("ratings1.csv");
        PrintStream str = new PrintStream(r1);
        rs1.add(Rating.create(1,0,5));
        rs1.add(Rating.create(1,2,5));
        rs1.add(Rating.create(2,0,4));
        rs1.add(Rating.create(2,2,4));
        rs1.add(Rating.create(3,1,5));
        rs1.add(Rating.create(4,1,1));
        try {
            str.println("1,0,5,847117005");
            str.println("1,2,5,847117006");
            str.println("2,0,4,847117007");
            str.println("2,2,4,847117008");
            str.println("3,1,5,847117009");
            str.println("4,1,1,847117010");
        } finally {
            str.close();
        }
        dao1 = TextEventDAO.create(r1, Formats.movieLensLatest());
    }

    @Before
    public void createRating2() throws IOException {
        File r = folder.newFile("ratings2.csv");
        PrintStream str = new PrintStream(r);
        try {
            str.println("1,0,4,847117005");
            str.println("1,4,3,847116893");
            str.println("1,6,1,847641973");
            str.println("2,0,4,847116936");
            str.println("2,4,4,847641938");
            str.println("2,6,4,847642118");
            str.println("3,0,4,847642048");
            str.println("3,4,1,847641919");
            str.println("3,6,3,847116787");
        } finally {
            str.close();
        }
        rs2.add(Rating.create(1, 0, 4));
        rs2.add(Rating.create(1, 4, 3));
        rs2.add(Rating.create(1, 6, 1));
        rs2.add(Rating.create(2, 0, 4));
        rs2.add(Rating.create(2, 4, 4));
        rs2.add(Rating.create(2, 6, 4));
        rs2.add(Rating.create(3, 0, 1));
        rs2.add(Rating.create(3, 4, 1));
        rs2.add(Rating.create(3, 6, 3));

        dao2 = TextEventDAO.create(r, Formats.movieLensLatest());
        //dao2 = TextEventDAO.ratings(r, ",");
    }

    private HIRModel getModel(List<Rating> rs) {
        EventDAO dao = EventCollectionDAO.create(rs);
        UserEventDAO udao = new PrefetchingUserEventDAO(dao);
        ItemDAO idao = new ItemListItemDAO(LongUtils.packedSet(0, 1, 2, 3, 4, 5, 6));
        //ItemDAO idao = new PrefetchingItemDAO(dao);
        UserHistorySummarizer summarizer = new RatingVectorUserHistorySummarizer();
        ItemItemBuildContextProvider contextFactory = new ItemItemBuildContextProvider(
                udao, new DefaultUserVectorNormalizer(), summarizer);
        HIRModelBuilder provider = new HIRModelBuilder(idao, gdao, contextFactory.get());
        return provider.get();
    }

    private HIRModel getModel2() {
        EventDAO dao = EventCollectionDAO.create(rs2);
        UserEventDAO udao = new PrefetchingUserEventDAO(dao);
        ItemDAO idao = new ItemListItemDAO(LongUtils.packedSet(0, 1, 2, 3, 4, 5, 6));
        UserHistorySummarizer summarizer = new RatingVectorUserHistorySummarizer();
        ItemItemBuildContextProvider contextFactory = new ItemItemBuildContextProvider(
                udao, new DefaultUserVectorNormalizer(), summarizer);
        HIRModelBuilder provider = new HIRModelBuilder(idao, gdao, contextFactory.get());
        return provider.get();
    }

    @Test
    public void testBuild1() {

        HIRModel model1 = getModel(rs1);

        MutableSparseVector msv1 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector msv2 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector msv3 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector msv4 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector msv5 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector msv6 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector msv7 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);

        msv1.set(0, 0);
        msv1.set(1, 0);
        msv1.set(2, 1);
        msv1.set(3, 0);
        msv1.set(4, 0);
        msv1.set(5, 0);
        msv1.set(6, 0);

        msv2.set(0, 0);
        msv2.set(1, 0);
        msv2.set(2, 0);
        msv2.set(3, 0);
        msv2.set(4, 0);
        msv2.set(5, 0);
        msv2.set(6, 0);

        msv3.set(0, 1);
        msv3.set(1, 0);
        msv3.set(2, 0);
        msv3.set(3, 0);
        msv3.set(4, 0);
        msv3.set(5, 0);
        msv3.set(6, 0);

        msv4.set(0, 0);
        msv4.set(1, 0);
        msv4.set(2, 0);
        msv4.set(3, 0);
        msv4.set(4, 0);
        msv4.set(5, 0);
        msv4.set(6, 0);

        msv5.set(0, 0);
        msv5.set(1, 0);
        msv5.set(2, 0);
        msv5.set(3, 0);
        msv5.set(4, 0);
        msv5.set(5, 0);
        msv5.set(6, 0);

        msv6.set(0, 0);
        msv6.set(1, 0);
        msv6.set(2, 0);
        msv6.set(3, 0);
        msv6.set(4, 0);
        msv6.set(5, 0);
        msv6.set(6, 0);

        msv7.set(0, 0);
        msv7.set(1, 0);
        msv7.set(2, 0);
        msv7.set(3, 0);
        msv7.set(4, 0);
        msv7.set(5, 0);
        msv7.set(6, 0);

        assertEquals(msv1, model1.getCoratingsVector(0));
        assertEquals(msv2, model1.getCoratingsVector(1));
        assertEquals(msv3, model1.getCoratingsVector(2));
 //       assertEquals(msv4, model1.getCoratingsVector(3, items));
   //     assertEquals(msv5, model1.getCoratingsVector(4, items));
     //   assertEquals(msv6, model1.getCoratingsVector(5, items));
       // assertEquals(msv7, model1.getCoratingsVector(6, items));

    }

    @Test
    public void testBuild2() {

        HIRModel model2 = getModel2();

        MutableSparseVector msv0 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector msv1 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector msv2 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector msv3 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector msv4 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector msv5 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector msv6 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);

        MutableSparseVector pv0 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector pv1 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector pv2 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector pv3 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector pv4 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector pv5 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);
        MutableSparseVector pv6 = MutableSparseVector.create(0, 1, 2, 3, 4, 5, 6);

        msv0.set(0, 0);
        msv0.set(1, 0);
        msv0.set(2, 0);
        msv0.set(3, 0);
        msv0.set(4, 0.5);
        msv0.set(5, 0);
        msv0.set(6, 0.5);

        msv1.set(0, 0);
        msv1.set(1, 0);
        msv1.set(2, 0);
        msv1.set(3, 0);
        msv1.set(4, 0);
        msv1.set(5, 0);
        msv1.set(6, 0);

        msv2.set(0, 0);
        msv2.set(1, 0);
        msv2.set(2, 0);
        msv2.set(3, 0);
        msv2.set(4, 0);
        msv2.set(5, 0);
        msv2.set(6, 0);

        msv3.set(0, 0);
        msv3.set(1, 0);
        msv3.set(2, 0);
        msv3.set(3, 0);
        msv3.set(4, 0);
        msv3.set(5, 0);
        msv3.set(6, 0);

        msv4.set(0, 0.5);
        msv4.set(1, 0);
        msv4.set(2, 0);
        msv4.set(3, 0);
        msv4.set(4, 0);
        msv4.set(5, 0);
        msv4.set(6, 0.5);

        msv5.set(0, 0);
        msv5.set(1, 0);
        msv5.set(2, 0);
        msv5.set(3, 0);
        msv5.set(4, 0);
        msv5.set(5, 0);
        msv5.set(6, 0);

        msv6.set(0, 0.5);
        msv6.set(1, 0);
        msv6.set(2, 0);
        msv6.set(3, 0);
        msv6.set(4, 0.5);
        msv6.set(5, 0);
        msv6.set(6, 0);

        pv0.set(0, 0.33333);
        pv0.set(1, 0.33333);
        pv0.set(2, 0.08333);
        pv0.set(3, 0.08333);
        pv0.set(4, 0.08333);
        pv0.set(5, 0.08333);
        pv0.set(6, 0);

        //0.33333   0.33333   0.08333   0.08333   0.08333   0.08333 0.00000

        pv1.set(0, 0.33333);
        pv1.set(1, 0.33333);
        pv1.set(2, 0.08333);
        pv1.set(3, 0.08333);
        pv1.set(4, 0.08333);
        pv1.set(5, 0.08333);
        pv1.set(6, 0);

        // 0.05556   0.05556   0.55556   0.05556   0.22222   0.05556   0.00000

        pv2.set(0, 0.05556);
        pv2.set(1, 0.05556);
        pv2.set(2, 0.05556);
        pv2.set(3, 0.05556);
        pv2.set(4, 0.22222);
        pv2.set(5, 0.05556);
        pv2.set(6, 0);

        // 0.08333   0.08333   0.08333   0.58333   0.08333   0.08333   0.00000

        pv3.set(0, 0.08333);
        pv3.set(1, 0.08333);
        pv3.set(2, 0.08333);
        pv3.set(3, 0.58333);
        pv3.set(4, 0.08333);
        pv3.set(5, 0.08333);
        pv3.set(6, 0);

        // 0.05556   0.05556   0.22222   0.05556   0.38889   0.05556   0.16667

        pv4.set(0, 0.05556);
        pv4.set(1, 0.05556);
        pv4.set(2, 0.22222);
        pv4.set(3, 0.05556);
        pv4.set(4, 0.38889);
        pv4.set(5, 0.05556);
        pv4.set(6, 0.16667);

        // 0.16667   0.16667   0.16667   0.16667   0.16667   0.16667   0.00000

        pv5.set(0, 0.16667);
        pv5.set(1, 0.16667);
        pv5.set(2, 0.16667);
        pv5.set(3, 0.16667);
        pv5.set(4, 0.16667);
        pv5.set(5, 0.16667);
        pv5.set(6, 0);

        // 0.00000   0.00000   0.00000   0.00000   0.16667   0.00000   0.83333
        pv6.set(0, 0);
        pv6.set(1, 0);
        pv6.set(2, 0);
        pv6.set(3, 0);
        pv6.set(4, 0.16667);
        pv6.set(5, 0);
        pv6.set(6, 0.83333);

        //assertEquals(msv0, model2.getCoratingsVector(0));
        //assertEquals(msv1, model2.getCoratingsVector(1));
        //assertEquals(msv2, model2.getCoratingsVector(2));
    //    assertEquals(msv3, model2.getCoratingsVector(3, items));
      //  assertEquals(msv4, model2.getCoratingsVector(4, items));
        //assertEquals(msv5, model2.getCoratingsVector(5, items));
        //assertEquals(msv6, model2.getCoratingsVector(6, items));


        assertEquals(pv0, model2.getProximityVector(0, items));
        assertEquals(pv1, model2.getProximityVector(1, items));
        assertEquals(pv2, model2.getProximityVector(2, items));
        assertEquals(pv3, model2.getProximityVector(3, items));
        assertEquals(pv4, model2.getProximityVector(4, items));
        assertEquals(pv5, model2.getProximityVector(5, items));
        assertEquals(pv6, model2.getProximityVector(6, items));
    }

}