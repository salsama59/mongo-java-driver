/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
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

package com.mongodb.operation;

import com.mongodb.MongoCredential;
import com.mongodb.MongoNamespace;
import com.mongodb.WriteConcern;
import com.mongodb.async.MongoFuture;
import com.mongodb.binding.AsyncWriteBinding;
import com.mongodb.binding.WriteBinding;
import com.mongodb.connection.Connection;
import com.mongodb.protocol.InsertProtocol;
import com.mongodb.protocol.Protocol;
import org.bson.BsonDocument;
import org.mongodb.WriteResult;

import static com.mongodb.assertions.Assertions.notNull;
import static com.mongodb.operation.CommandOperationHelper.executeWrappedCommandProtocol;
import static com.mongodb.operation.CommandOperationHelper.executeWrappedCommandProtocolAsync;
import static com.mongodb.operation.OperationHelper.AsyncCallableWithConnection;
import static com.mongodb.operation.OperationHelper.CallableWithConnection;
import static com.mongodb.operation.OperationHelper.VoidTransformer;
import static com.mongodb.operation.OperationHelper.executeProtocolAsync;
import static com.mongodb.operation.OperationHelper.serverIsAtLeastVersionTwoDotSix;
import static com.mongodb.operation.OperationHelper.withConnection;
import static com.mongodb.operation.UserOperationHelper.asCollectionDocument;
import static com.mongodb.operation.UserOperationHelper.asCommandDocument;
import static java.util.Arrays.asList;

/**
 * An operation to create a user.
 *
 * @since 3.0
 */
public class CreateUserOperation implements AsyncWriteOperation<Void>, WriteOperation<Void> {
    private final MongoCredential credential;
    private final boolean readOnly;

    /**
     * Constructs a new instance.
     *
     * @param credential the users credentials.
     * @param readOnly true if the user is a readOnly user.
     */
    public CreateUserOperation(final MongoCredential credential, final boolean readOnly) {
        this.credential = notNull("credential", credential);
        this.readOnly = readOnly;
    }

    /**
     * Gets the users credentials.
     *
     * @return the users credentials.
     */
    public MongoCredential getCredential() {
        return credential;
    }

    /**
     * Returns true if the user is a readOnly user.
     *
     * @return true if the user is a readOnly user.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public Void execute(final WriteBinding binding) {
        return withConnection(binding, new CallableWithConnection<Void>() {
            @Override
            public Void call(final Connection connection) {
                if (serverIsAtLeastVersionTwoDotSix(connection)) {
                    executeWrappedCommandProtocol(getCredential().getSource(), getCommand(), connection);
                } else {
                    getCollectionBasedProtocol().execute(connection);
                }
                return null;
            }
        });
    }

    @Override
    public MongoFuture<Void> executeAsync(final AsyncWriteBinding binding) {
        return withConnection(binding, new AsyncCallableWithConnection<Void>() {
            @Override
            public MongoFuture<Void> call(final Connection connection) {
                if (serverIsAtLeastVersionTwoDotSix(connection)) {
                    return executeWrappedCommandProtocolAsync(credential.getSource(), getCommand(), connection,
                                                              new VoidTransformer<BsonDocument>());
                } else {
                    return executeProtocolAsync(getCollectionBasedProtocol(), connection, new VoidTransformer<WriteResult>());
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Protocol<WriteResult> getCollectionBasedProtocol() {
        MongoNamespace namespace = new MongoNamespace(credential.getSource(), "system.users");
        return new InsertProtocol(namespace, true, WriteConcern.ACKNOWLEDGED,
                                  asList(new InsertRequest(asCollectionDocument(credential, readOnly)))
        );
    }

    private BsonDocument getCommand() {
        return asCommandDocument(credential, readOnly, "createUser");
    }
}