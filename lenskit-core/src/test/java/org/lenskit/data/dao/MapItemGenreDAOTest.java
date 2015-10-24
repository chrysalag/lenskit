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

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by chrysalag.
 */

public class MapItemGenreDAOTest {
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
    public void testMissingItem() {
        assertThat(gdao.getItemGenre(5), nullValue());
    }

    @Test
    public void testGenreSize() {
        assertThat(gdao.getGenreSize(), equalTo(20));
    }

    @Test
    public void testGenreVector() {
        double[] testVec1 = {0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0};
        double[] testVec2 = {0,0,1,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0};
        RealVector testRealVector1 = MatrixUtils.createRealVector(testVec1);
        RealVector testRealVector2 = MatrixUtils.createRealVector(testVec2);
        assertThat(gdao.getItemGenre(318), equalTo(testRealVector1));
        assertThat(gdao.getItemGenre(117444), equalTo(testRealVector2));
        assertThat(testRealVector1.getDimension(), equalTo(gdao.getGenreSize()));
        assertThat(gdao.getItemGenre(318).getDimension(), equalTo(gdao.getGenreSize()));
        assertThat(testVec1.length, equalTo(gdao.getGenreSize()));
    }

    @Test
    public void testItemIds() {
        assertThat(gdao.getItemIds(), containsInAnyOrder(318L, 2329L, 5475L, 7323L, 48394L, 64716L, 117444L, 140214L));
    }
}
