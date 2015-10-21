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
package org.grouplens.lenskit.data.source;

import org.grouplens.grapht.util.Providers;
import org.grouplens.lenskit.data.text.*;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.lenskit.data.dao.*;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.specs.data.DataSourceSpec;
import org.lenskit.specs.data.TextDataSourceSpec;

import javax.inject.Provider;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Data source backed by a CSV file.  Use {@link CSVDataSourceBuilder} to configure and build one
 * of these, or the <code>csvfile</code> command in an eval script.
 *
 * @since 2.2
 * @see CSVDataSourceBuilder
 */
public class TextDataSource extends AbstractDataSource {
    private final String name;
    private final EventDAO dao;
    private final File sourceFile;
    private final PreferenceDomain domain;
    private final EventFormat format;

    private final Provider<ItemListItemDAO> items;
    private final Provider<MapItemNameDAO> itemNames;
    private final Path itemFile;
    private final Path itemNameFile;

    TextDataSource(String name, File file, EventFormat fmt, PreferenceDomain pdom,
                   Path itemFile, Path itemNameFile) {
        this.name = name;
        sourceFile = file;
        domain = pdom;
        format = fmt;

        dao = TextEventDAO.create(file, format, CompressionMode.AUTO);

        if (itemFile != null) {
            items = Providers.memoize(new SimpleFileItemDAOProvider(itemFile.toFile()));
            this.itemFile = itemFile;
        } else {
            items = null;
            this.itemFile = null;
        }
        if (itemNameFile != null) {
            itemNames = Providers.memoize(new CSVFileItemNameDAOProvider(itemNameFile.toFile()));
            this.itemNameFile = itemNameFile;
        } else {
            itemNames = null;
            this.itemNameFile = null;
        }
    }

    @Override
    public String getName() {
        if (name == null) {
            return sourceFile.getName();
        } else {
            return name;
        }
    }

    public File getFile() {
        return sourceFile;
    }

    public EventFormat getFormat() {
        return format;
    }

    @Override
    public PreferenceDomain getPreferenceDomain() {
        return domain;
    }

    @Override
    public long lastModified() {
        return sourceFile.exists() ? sourceFile.lastModified() : -1L;
    }

    @Override
    public EventDAO getEventDAO() {
        return dao;
    }

    @Override
    public ItemDAO getItemDAO() {
        if (items != null) {
            return items.get();
        } else if (itemNames != null) {
            return itemNames.get();
        } else {
            return super.getItemDAO();
        }
    }

    @Override
    public ItemNameDAO getItemNameDAO() {
        if (itemNames != null) {
            return itemNames.get();
        } else {
            return super.getItemNameDAO();
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("TextData(")
           .append(getName())
           .append(")");
        return str.toString();
    }

    @Override
    public DataSourceSpec toSpec() {
        TextDataSourceSpec spec = new TextDataSourceSpec();
        spec.setName(getName());
        spec.setFile(getFile().toPath());
        if (format instanceof DelimitedColumnEventFormat) {
            DelimitedColumnEventFormat cf = (DelimitedColumnEventFormat) format;
            spec.setDelimiter(cf.getDelimiter());
            List<String> fieldNames = new ArrayList<>();
            for (Field f: cf.getFields()) {
                fieldNames.add(f.getName());
            }
            spec.setFields(fieldNames);
            spec.setBuilderType(cf.getBuilderType().getName());
            spec.setItemFile(itemFile);
            spec.setItemNameFile(itemNameFile);
        }
        if (domain != null) {
            spec.setDomain(domain.toSpec());
        }
        return spec;
    }

    /**
     * Build a text data source from a spec.
     * @param spec The spec.
     * @return The data source.
     */
    public static TextDataSource fromSpec(TextDataSourceSpec spec) {
        TextDataSourceBuilder bld = new TextDataSourceBuilder();
        bld.setName(spec.getName())
           .setFile(spec.getFile().toFile())
           .setDomain(PreferenceDomain.fromSpec(spec.getDomain()));
        DelimitedColumnEventFormat fmt = DelimitedColumnEventFormat.create(spec.getBuilderType());
        fmt.setDelimiter(spec.getDelimiter());
        List<String> fields = spec.getFields();
        if (fields != null) {
            fmt.setFieldsByName(fields);
        }
        bld.setFormat(fmt);
        bld.setItemFile(spec.getItemFile());
        bld.setItemNameFile(spec.getItemNameFile());
        return bld.build();
    }
}
