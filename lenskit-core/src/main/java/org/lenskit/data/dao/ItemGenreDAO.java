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

import org.apache.commons.math3.linear.RealVector;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Created by chrysalag.
 * A DAO interface that provides access to item genres.
 * <p>
 * The normal way to get item names, without writing your own DAOs, is to use a {@link org.lenskit.data.dao.MapItemGenreDAO}, often
 * loaded from a CSV file:
 * </p>
 * <pre>{@code
 * bind MapItemGenreDAO to CSVFileItemGenreDAOProvider
 * set ItemFile to "item-genres.csv"
 * }</pre>
 * <p>
 * Note that, while {@link org.lenskit.data.dao.MapItemGenreDAO} implements both this
 * interface and {@link org.lenskit.data.dao.ItemDAO}, binding this interface to the
 * provider instead of the class means that the item name DAO will only be used to satisfy item name
 * DAO requests and not item list requests.
 * </p>
 */

public interface ItemGenreDAO {
    /**
     * Get the genre for an item.
     * @param item The item ID.
     * @return A display genre for the item, or {@code null} if the item is unknown.
     */
    @Nullable
    RealVector getItemGenre(long item);

    int getGenreSize();
}
