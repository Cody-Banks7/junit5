/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.util.List;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.ClassFilter;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.EngineFilter;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.SpecificationResolver;
import org.junit.gen5.engine.junit5.execution.EngineTestExecutionNode;
import org.junit.gen5.engine.junit5.execution.TestExecutionNodeBuilder;

public class JUnit5TestEngine implements TestEngine {

	@Override
	public String getId() {
		// TODO Consider using class names for engine IDs.
		return "junit5";
	}

	@Override
	public void discoverTests(TestPlanSpecification specification, EngineDescriptor engineDescriptor) {
		Preconditions.notNull(specification, "specification must not be null");
		Preconditions.notNull(engineDescriptor, "engineDescriptor must not be null");

		resolveSpecification(specification, engineDescriptor);
	}

	private void resolveSpecification(TestPlanSpecification specification, EngineDescriptor engineDescriptor) {
		SpecificationResolver resolver = new SpecificationResolver(engineDescriptor);
		for (TestPlanSpecificationElement element : specification) {
			resolver.resolveElement(element);
		}
		applyEngineFilters(specification.getEngineFilters(), engineDescriptor);
	}

	private void applyEngineFilters(List<EngineFilter> engineFilters, EngineDescriptor engineDescriptor) {
		// TODO Currently only works with a single ClassFilter
		if (engineFilters.isEmpty())
			return;
		ClassFilter filter = (ClassFilter) engineFilters.get(0);
		TestDescriptor.Visitor filteringVisitor = (descriptor, remove) -> {
			if (descriptor.getClass().equals(ClassTestDescriptor.class)) {
				ClassTestDescriptor classTestDescriptor = (ClassTestDescriptor) descriptor;
				if (!filter.acceptClass(classTestDescriptor.getTestClass()))
					remove.run();
			}
		};
		engineDescriptor.accept(filteringVisitor);
	}

	@Override
	public void execute(EngineExecutionContext request) {

		EngineTestExecutionNode executionNode = new TestExecutionNodeBuilder().buildExecutionTree(
			request.getEngineDescriptor());
		executionNode.executeRequest(request);
	}

}
