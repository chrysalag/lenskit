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

package org.grouplens.lenskit.hir;

import org.grouplens.lenskit.data.text.Formats;
import org.grouplens.lenskit.data.text.TextEventDAO;
import org.lenskit.LenskitConfiguration;
import org.lenskit.LenskitRecommender;
import org.lenskit.LenskitRecommenderEngine;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.data.dao.EventDAO;
import org.lenskit.data.dao.ItemNameDAO;
import org.lenskit.data.dao.MapItemGenreDAO;
import org.lenskit.data.dao.MapItemNameDAO;
import org.lenskit.data.ratings.PreferenceDomain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chrysalag.
 */


public class HIRDemo implements Runnable {

    public static void main(String[] args) {
        HIRDemo demo = new HIRDemo(args);
        try {
            demo.run();
        } catch (RuntimeException e) {
            System.err.println(e.toString());
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }


    private File inputFile = new File("/home/chrysalag/IdeaProjects/lenskit/lenskit-hir/data/ratings.csv");
    private File movieFile = new File("/home/chrysalag/IdeaProjects/lenskit/lenskit-hir/data/movies.csv");
    private File genreFile = new File("/home/chrysalag/IdeaProjects/lenskit/lenskit-hir/data/genres.csv");

    private List<Long> users;

    public HIRDemo(String[] args) {
        users = new ArrayList<>(args.length);
        for (String arg: args) {
            users.add(Long.parseLong(arg));
        }
    }

    public void run() {
        EventDAO dao = TextEventDAO.create(inputFile, Formats.movieLensLatest());
        ItemNameDAO names;
        MapItemGenreDAO genres;

        try {
            names = MapItemNameDAO.fromCSVFile(movieFile, 1);
        } catch (IOException e) {
            throw new RuntimeException("cannot load names", e);
        }

        try {
            genres = MapItemGenreDAO.fromCSVFile(genreFile);
        } catch (IOException g) {
            throw new RuntimeException("cannot load names", g);
        }

        // Next: load the LensKit algorithm configuration
        LenskitConfiguration config = null;
        /*try {
            config = ConfigHelpers.load(new File("etc/hir.groovy"));
        } catch (IOException e) {
            throw new RuntimeException("could not load configuration", e);
        }*/
        config.bind(ItemScorer.class).to(HIRItemScorer.class);
        // Add our data component to the configuration
        config.addComponent(dao);
        config.bind(EventDAO.class).to(dao);
        config.bind(MapItemGenreDAO.class).to(genres);
        config.bind(PreferenceDomain.class).to(new PreferenceDomain(0, 1));

        // Now that we have a configuration, build a recommender engine from the configuration
        // and data source. This will compute the similarity matrix and return a recommender
        // engine that uses it.
        LenskitRecommenderEngine engine = LenskitRecommenderEngine.build(config);

        // Finally, get the recommender and use it.
        try (LenskitRecommender rec = engine.createRecommender()) {
            // we want to recommend items
            ItemRecommender irec = rec.getItemRecommender();
            assert irec != null; // not null because we configured one
            // for users
            for (long user : users) {
                // get 10 recommendation for the user
                List<Result> recs = irec.recommendWithDetails(user, 5, null, null);
                System.out.format("Recommendations for user %d:\n", user);
                for (Result item : recs) {
                    String name = names.getItemName(item.getId());
                    System.out.format("\t%d (%s): %.2f\n", item.getId(), name, item.getScore());
                    System.out.format("\t %s \n", name);
                }
            }
        }
    }


}
