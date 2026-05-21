/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hugegraph.traversal.optimize;

import java.util.Collections;

import org.apache.hugegraph.HugeGraph;
import org.apache.hugegraph.backend.id.Id;
import org.apache.hugegraph.backend.id.IdGenerator;
import org.apache.hugegraph.exception.NotFoundException;
import org.apache.hugegraph.schema.IndexLabel;
import org.apache.hugegraph.schema.PropertyKey;
import org.apache.hugegraph.testutil.Assert;
import org.apache.hugegraph.type.define.DataType;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.junit.Test;
import org.mockito.Mockito;

public class TraversalUtilOptimizeTest {

    @Test
    public void testCanExtractHasContainerWithoutGraph() {
        Assert.assertTrue(TraversalUtil.canExtractHasContainer(
                null, new HasContainer("~label", P.eq("person"))));
        Assert.assertTrue(TraversalUtil.canExtractHasContainer(
                null, new HasContainer("~id", P.eq("1"))));
        Assert.assertFalse(TraversalUtil.canExtractHasContainer(
                null, new HasContainer("name", P.eq("marko"))));
    }

    @Test
    public void testCanExtractHasContainerWithMissingPropertyKey() {
        HugeGraph graph = Mockito.mock(HugeGraph.class);
        Mockito.when(graph.propertyKey("missing"))
               .thenThrow(new NotFoundException("missing"));

        Assert.assertFalse(TraversalUtil.canExtractHasContainer(
                graph, new HasContainer("missing", P.eq("marko"))));
    }

    @Test
    public void testCanExtractHasContainerWithoutPropertyIndex() {
        HugeGraph graph = Mockito.mock(HugeGraph.class);
        PropertyKey age = propertyKey(1L, "age", DataType.INT);
        Mockito.when(graph.propertyKey("age")).thenReturn(age);
        Mockito.when(graph.indexLabels()).thenReturn(Collections.emptyList());

        Assert.assertFalse(TraversalUtil.canExtractHasContainer(
                graph, new HasContainer("age", P.eq(1))));
    }

    @Test
    public void testCanExtractHasContainerWithoutLabelContext() {
        HugeGraph graph = Mockito.mock(HugeGraph.class);
        PropertyKey age = propertyKey(1L, "age", DataType.INT);
        IndexLabel ageIndex = new IndexLabel(null, IdGenerator.of(2L),
                                            "vl1ByAge");
        ageIndex.indexFields(age.id());
        Mockito.when(graph.propertyKey("age")).thenReturn(age);
        Mockito.when(graph.indexLabels())
               .thenReturn(Collections.singletonList(ageIndex));

        Assert.assertFalse(TraversalUtil.canExtractHasContainer(
                graph, new HasContainer("age", P.eq(1))));
    }

    private static PropertyKey propertyKey(long id, String name,
                                           DataType dataType) {
        Id keyId = IdGenerator.of(id);
        PropertyKey key = new PropertyKey(null, keyId, name);
        key.dataType(dataType);
        return key;
    }
}
