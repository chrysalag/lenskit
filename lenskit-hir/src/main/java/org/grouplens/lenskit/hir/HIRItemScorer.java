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

/**
 * An {@link ItemScorer} that implements the HIR algorithm.
 */

import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.data.dao.ItemDAO;
import org.lenskit.data.dao.UserEventDAO;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.lenskit.data.history.UserHistory;
import org.lenskit.data.ratings.PreferenceDomain;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.lenskit.api.ItemScorer;
import org.lenskit.results.Results;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

/**
 * An {@link ItemScorer} that implements the HIR algorithm.
 */

public class HIRItemScorer extends AbstractItemScorer {

    protected final UserEventDAO dao;
    protected final ItemDAO idao;
    protected HIRModel model;
    protected final PreferenceDomain domain;
    protected double directAssociation;
    protected double proximity;

    @Inject
    public HIRItemScorer(UserEventDAO dao,
                         HIRModel model,
                         ItemDAO idao,
                         @Nullable PreferenceDomain dom,
                         @DirectAssociationParameter double direct,
                         @ProximityParameter double prox) {
        this.dao = dao;
        this.model = model;
        this.idao = idao;
        domain = dom;
        directAssociation = direct;
        proximity = prox;
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {

        UserHistory<Rating> history = dao.getEventsForUser(user, Rating.class);

        if (history == null) {
            history = History.forUser(user);
        }

        SparseVector historyVector = RatingVectorUserHistorySummarizer.makeRatingVector(history);

        List<Result> results = new ArrayList<>();
        List<Result> results1 = new ArrayList<>();

        MutableSparseVector preferenceVector = MutableSparseVector.create(idao.getItemIds(), 0);

        double total = 0.0;
        for (VectorEntry e: historyVector.fast()) {
            final long key = e.getKey();
            final double value = e.getValue();
            preferenceVector.set(key, value);
            total = total + value;
        }

        if (total != 0) {
            preferenceVector.multiply(1/total);
        }

        final double preferenceInResults = 1 - directAssociation - proximity;
        MutableSparseVector rankingVector = preferenceVector.copy();
        rankingVector.multiply(preferenceInResults);

        for (VectorEntry e: preferenceVector.fast()) {
            final double prefValue = e.getValue();
            if (prefValue != 0) {
                final long prefKey = e.getKey();
                MutableSparseVector coratingsVector = model.getCoratingsVector(prefKey, items);
                coratingsVector.multiply(directAssociation);

                MutableSparseVector proximityVector = model.getProximityVector(prefKey, items);
                proximityVector.multiply(proximity);

                coratingsVector.add(proximityVector);
                coratingsVector.multiply(prefValue);

                rankingVector.add(coratingsVector);
            }
        }

        for (VectorEntry e: rankingVector.fast()) {
            final long key = e.getKey();
            if (!historyVector.containsKey(key)) {
                results.add(Results.create(key, e.getValue()));
            }
        }

        if (rankingVector.sum() < 1.1 && rankingVector.sum() > 0.9) {
            return Results.newResultMap(results);
        } else {
            return Results.newResultMap(results1);
        }

    }

    public HIRModel getModel() {
        return model;
    }
}
