/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jbehavesupport.engine;

import lombok.SneakyThrows;
import org.jbehavesupport.engine.descriptor.JBehaveTestDescriptor;
import org.jbehavesupport.engine.discovery.JBehaveDiscoverer;
import org.jbehavesupport.engine.executor.JBehaveExecutor;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.platform.engine.TestExecutionResult.successful;

public final class JBehaveTestEngine implements TestEngine  {

    public static final String COMPARATOR_PROPERTY = "jbehave.execution.order.comparator";

    @Override
	public String getId() {
		return "jbehave";
	}

	@Override
	public Optional<String> getGroupId() {
		return Optional.of("org.jbehavesupport");
	}

	@Override
	public Optional<String> getArtifactId() {
		return Optional.of("jbehave-junit-engine");
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        return new JBehaveDiscoverer().discover(discoveryRequest, uniqueId);
	}

    @Override
    public void execute(ExecutionRequest request) {
        Optional<Comparator<TestDescriptor>> sortingComparator = request.getConfigurationParameters()
            .get(COMPARATOR_PROPERTY, JBehaveTestEngine::getComparatorInstance);

        EngineExecutionListener engineExecutionListener = request.getEngineExecutionListener();
        TestDescriptor engineDescriptor = request.getRootTestDescriptor();
        engineExecutionListener.executionStarted(engineDescriptor);
        JBehaveExecutor jBehaveExecutor = new JBehaveExecutor(request);
        Stream<? extends JBehaveTestDescriptor> testDescriptorStream = engineDescriptor.getChildren()
            .stream()
            .map(JBehaveTestDescriptor.class::cast)
            .filter(JBehaveTestDescriptor::isRunnable);

        if (sortingComparator.isPresent()) {
            testDescriptorStream = testDescriptorStream.sorted(sortingComparator.get());
        }
        testDescriptorStream.forEach(jBehaveExecutor::execute);

        engineExecutionListener.executionFinished(engineDescriptor, successful());
    }

    @SneakyThrows(ReflectiveOperationException.class)
    private static Comparator<TestDescriptor> getComparatorInstance(String className) {
        return (Comparator<TestDescriptor>) Class.forName(className).getDeclaredConstructor().newInstance();
    }

}
