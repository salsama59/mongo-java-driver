/*
 * Copyright 2008-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb.client.model;

import org.bson.conversions.Bson;

import static com.mongodb.assertions.Assertions.notNull;
import static com.mongodb.client.model.ReplaceOptions.createReplaceOptions;

/**
 * A model describing the replacement of at most one document that matches the query filter.
 *
 * @param <T> the type of document to replace. This can be of any type for which a {@code Codec} is registered
 * @since 3.0
 * @mongodb.driver.manual tutorial/modify-documents/#replace-the-document Replace
 */
public final class ReplaceOneModel<T> extends WriteModel<T> {
    private final Bson filter;
    private final T replacement;
    private final ReplaceOptions options;

    /**
     * Construct a new instance.
     *
     * @param filter    a document describing the query filter, which may not be null.
     * @param replacement the replacement document
     */
    public ReplaceOneModel(final Bson filter, final T replacement) {
        this(filter, replacement, new ReplaceOptions());
    }

    /**
     * Construct a new instance.
     *
     * @param filter    a document describing the query filter, which may not be null.
     * @param replacement the replacement document
     * @param options     the options to apply
     * @deprecated use {@link #ReplaceOneModel(Bson, Object, ReplaceOptions)} instead
     */
    @Deprecated
    public ReplaceOneModel(final Bson filter, final T replacement, final UpdateOptions options) {
        this(filter, replacement, createReplaceOptions(options));
    }

    /**
     * Construct a new instance.
     *
     * @param filter    a document describing the query filter, which may not be null.
     * @param replacement the replacement document
     * @param options     the options to apply
     * @since 3.7
     */
    public ReplaceOneModel(final Bson filter, final T replacement, final ReplaceOptions options) {
        this.filter = notNull("filter", filter);
        this.options = notNull("options", options);
        this.replacement = notNull("replacement", replacement);
    }

    /**
     * Gets the query filter.
     *
     * @return the query filter
     */
    public Bson getFilter() {
        return filter;
    }

    /**
     * Gets the document which will replace the document matching the query filter.
     *
     * @return the replacement document
     */
    public T getReplacement() {
        return replacement;
    }

    /**
     * Gets the options to apply.
     *
     * @return the update options
     * @deprecated use {@link #getReplaceOptions()} instead
     */
    @Deprecated
    public UpdateOptions getOptions() {
        return new UpdateOptions()
                .bypassDocumentValidation(options.getBypassDocumentValidation())
                .collation(options.getCollation())
                .upsert(options.isUpsert());
    }

    /**
     * Gets the ReplaceOptions to apply.
     *
     * @return the replace options
     * @since 3.7
     */
    public ReplaceOptions getReplaceOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "ReplaceOneModel{"
                + "filter=" + filter
                + ", replacement=" + replacement
                + ", options=" + options
                + '}';
    }
}
