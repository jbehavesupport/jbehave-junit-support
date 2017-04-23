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

package org.jbehavesupport.runner.description;

import static org.jbehavesupport.runner.JUnitRunnerFormatter.buildExampleText;
import static org.jbehavesupport.runner.JUnitRunnerFormatter.buildScenarioText;
import static org.jbehavesupport.runner.JUnitRunnerFormatter.buildStoryText;
import static org.jbehavesupport.runner.JUnitRunnerFormatter.normalizeStoryName;
import static org.junit.runner.Description.createSuiteDescription;
import static org.junit.runner.Description.createTestDescription;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepCandidate;
import org.junit.runner.Description;

/**
 * @author Michal Bocek
 * @since 4/23/2017
 */
class StepLevelDescriptionBuilder extends AbstractDescriptionBuilder {

    public StepLevelDescriptionBuilder(final PerformableTree story) {
        super(story);
    }

    protected Description createStoryDescription(PerformableTree.PerformableStory performableStory) {
        Description description = createSuiteDescription(buildStoryText(normalizeStoryName(performableStory.getStory().getName())));
        performableStory.getScenarios().forEach(
            performableScenario -> getScenarioDescription(performableScenario).forEach(description::addChild)
        );
        return description;
    }

    private List<Description> getScenarioDescription(PerformableTree.PerformableScenario performableScenario) {
        Description scenarioDescription = createSuiteDescription(buildScenarioText(getKeywords(), performableScenario.getScenario().getTitle()));
        if (performableScenario.hasExamples()) {
            performableScenario.getExamples()
                .stream()
                .map(examplePerformableScenario -> {
                    Description exampleDescription = createSuiteDescription(
                        buildExampleText(getKeywords(), examplePerformableScenario.getParameters().toString()));
                    performableScenario.getScenario()
                        .getSteps()
                        .forEach(step -> exampleDescription.addChild(getStepDescription(step)));
                    return exampleDescription;
                })
                .forEach(scenarioDescription::addChild);
        } else {
            if (hasGivenStories(performableScenario)) {
                addGivenStories(scenarioDescription, performableScenario.getScenario());
            }
            performableScenario.getScenario()
                .getSteps()
                .forEach(step -> scenarioDescription.addChild(getStepDescription(step)));
        }
        return Collections.singletonList(scenarioDescription);
    }

    private void addGivenStories(Description scenarioDescription, Scenario scenario) {
        scenario.getGivenStories()
            .getStories()
            .forEach(story -> scenarioDescription.addChild(
                createTestDescription(Story.class, normalizeStoryName(story.getPath()))));
    }

    private boolean hasGivenStories(PerformableTree.PerformableScenario performableScenario) {
        return !performableScenario.getScenario().getGivenStories().getPaths().isEmpty();
    }

    private Description getStepDescription(String step) {
        Description result;
        StepCandidate stepCandidate = findCandidateStep(step);
        if (stepCandidate != null) {
            result = getStepDescription(stepCandidate, step);
        } else {
            result = getStepDescription(UnknownStep.class, step);
        }
        return result;
    }

    private Description getStepDescription(Class<?> stepClass, String step) {
        addTestCount();
        return createTestDescription(stepClass, step);
    }

    private Description getStepDescription(StepCandidate stepCandidate, String step) {
        Description result;
        if (stepCandidate.isComposite()) {
            result = createSuiteDescription(step);
            Arrays.stream(stepCandidate.composedSteps())
                .forEach(childStep -> result.addChild(getStepDescription(childStep)));
        } else {
            addTestCount();
            result = createTestDescription(stepCandidate.getStepsType(), step);
        }
        return result;
    }

    private StepCandidate findCandidateStep(String step) {
        return getStepCandidates().stream()
            .filter(stepCandidate -> stepCandidate.matches(step))
            .findFirst()
            .orElse(null);
    }
}
