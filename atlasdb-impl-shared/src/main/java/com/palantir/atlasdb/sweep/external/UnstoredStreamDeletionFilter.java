/*
 * Copyright 2018 Palantir Technologies, Inc. All rights reserved.
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.atlasdb.sweep.external;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.palantir.atlasdb.keyvalue.api.TableReference;
import com.palantir.atlasdb.protos.generated.StreamPersistence;
import com.palantir.atlasdb.schema.cleanup.StreamStoreCleanupMetadata;
import com.palantir.atlasdb.transaction.api.Transaction;

public class UnstoredStreamDeletionFilter implements GenericStreamDeletionFilter {
    private final StreamStoreMetadataReader metadataReader;

    public UnstoredStreamDeletionFilter(TableReference metadataTableRef, StreamStoreCleanupMetadata cleanupMetadata) {
        this.metadataReader = new StreamStoreMetadataReader(
                metadataTableRef,
                new GenericStreamStoreCellCreator(cleanupMetadata));
    }

    @Override
    public Set<GenericStreamIdentifier> getStreamIdentifiersToDelete(
            Transaction tx, Set<GenericStreamIdentifier> identifiers) {
        Map<GenericStreamIdentifier, StreamPersistence.StreamMetadata> metadataFromDb =
                metadataReader.readMetadata(tx, identifiers);
        return metadataFromDb.entrySet().stream()
                .filter(entry -> entry.getValue().getStatus() != StreamPersistence.Status.STORED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}