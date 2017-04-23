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
package org.jbehavesupport.runner.reporter;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.Story;
import org.jbehavesupport.runner.JUnitRunnerFormatter;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;

/**
 * @author Michal Bocek
 * @since 29/08/16
 */
public class JUnitStepReporter extends LoggingReporter {

    private static final String BEFORE_STORIES = "BeforeStories";
    private static final String AFTER_STORIES = "AfterStories";

    private final RunNotifier notifier;
    private final Description rootDescription;
    private final Configuration configuration;

    private Description currentStoryDescription;
    private Iterator<Description> scenariosDescriptions;
    private Description currentScenarioDescription;
    private Iterator<Description> examplesDescriptions;
    private Description currentExampleDescription;
    private Iterator<Description> stepsDescriptions;
    private Description currentStepDescription;
    private boolean givenStory;

    public JUnitStepReporter(RunNotifier notifier, Description rootDescription,
                             Configuration configuration) {
        this.notifier = notifier;
        this.rootDescription = rootDescription;
        this.configuration = configuration;
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        if (givenStory) {
            currentStepDescription = stepsDescriptions.next();
            notifier.fireTestStarted(currentStepDescription);
            this.givenStory = true;
        } else {
            for (Description description : rootDescription.getChildren()) {
                if (description.isTest()
                    && (isEligibleAs(story, description, BEFORE_STORIES)
                    || isEligibleAs(story, description, AFTER_STORIES))) {
                    currentStoryDescription = description;
                    notifier.fireTestStarted(currentStoryDescription);

                }
                if (description.isSuite()
                    && isEligibleAs(description, story.getName())) {
                    currentStoryDescription = description;
                    notifier.fireTestStarted(currentStoryDescription);
                    scenariosDescriptions = currentStoryDescription.getChildren().iterator();
                }
            }
        }
        super.beforeStory(story, givenStory);
    }

    @Override
    public void afterStory(boolean givenOrRestartingStory) {
        super.afterStory(givenOrRestartingStory);
        if (isAGivenStory()) {
            notifier.fireTestFinished(currentStepDescription);
            this.givenStory = false;
        } else if (nonNull(currentStoryDescription)) {
            notifier.fireTestFinished(currentStoryDescription);
        }
    }

    @Override
    public void beforeScenario(String scenarioTitle) {
        if (notAGivenStory()) {
            currentScenarioDescription = scenariosDescriptions.next();
            stepsDescriptions = getAllChildren(currentScenarioDescription.getChildren(), new ArrayList<>()).iterator();
            examplesDescriptions = getAllExamples(currentScenarioDescription.getChildren()).iterator();
            notifier.fireTestStarted(currentScenarioDescription);
            super.beforeScenario(scenarioTitle);
        }
    }

    private List<Description> getAllExamples(ArrayList<Description> children) {
        List<Description> result = new ArrayList<>();
        for (Description child : children) {
            if (isExample(child)) {
                result.add(child);
            }
        }
        return result;
    }

    private boolean isExample(Description description) {
       return  description.getDisplayName().startsWith(configuration.keywords().examplesTableRow() + " ");
    }

    private List<Description> getAllChildren(ArrayList<Description> children, List<Description> result) {
        for (Description description : children) {
            if (description.isSuite()) {
                if (!isExample(description)) {
                    result.add(description);
                }
                getAllChildren(description.getChildren(), result);
            } else {
                result.add(description);
            }
        }
        return result;
    }

    @Override
    public void afterScenario() {
        super.afterScenario();
        if (notAGivenStory()) {
            notifier.fireTestFinished(currentScenarioDescription);
        }
    }

    @Override
    public void beforeStep(String step) {
        if (notAGivenStory()) {
            currentStepDescription = stepsDescriptions.next();
            notifier.fireTestStarted(currentStepDescription);
        }
        super.beforeStep(step);
    }

    @Override
    public void successful(String step) {
        super.successful(step);
        if (notAGivenStory()) {
            notifier.fireTestFinished(currentStepDescription);
        }
    }

    @Override
    public void failed(String step, Throwable cause) {
        if (cause instanceof UUIDExceptionWrapper) {
            cause = cause.getCause();
        }
        super.failed(step, cause);
        if (notAGivenStory()) {
            notifier.fireTestFailure(new Failure(currentStepDescription, cause));
            notifier.fireTestFinished(currentStepDescription);
        }
    }

    @Override
    public void notPerformed(String step) {
        super.notPerformed(step);
        if (notAGivenStory()) {
            currentStepDescription = stepsDescriptions.next();
            notifier.fireTestIgnored(currentStepDescription);
        }
    }

    @Override
    public void pending(String step) {
        super.pending(step);
        if (notAGivenStory()) {
            currentStepDescription = stepsDescriptions.next();
            notifier.fireTestIgnored(currentStepDescription);
        }
    }

    @Override
    public void example(Map<String, String> tableRow) {
        if (notAGivenStory()) {
            if (nonNull(currentExampleDescription)) {
                notifier.fireTestFinished(currentExampleDescription);
            }
            currentExampleDescription = examplesDescriptions.next();
            notifier.fireTestStarted(currentExampleDescription);
        }
        super.example(tableRow);
    }

    @Override
    public void afterExamples() {
        if (notAGivenStory()) {
            notifier.fireTestFinished(currentExampleDescription);
        }
        super.afterExamples();
    }

    private boolean isEligibleAs(Story story, Description description, String storyName) {
        return story.getName().equals(storyName) && description.getDisplayName().startsWith(storyName);
    }

    private boolean isEligibleAs(Description description, String storyName) {
        return description.getDisplayName().equals(JUnitRunnerFormatter.buildStoryText(JUnitRunnerFormatter.normalizeStoryName(storyName)));
    }

    private boolean isAGivenStory() {
        return this.givenStory;
    }

    private boolean notAGivenStory() {
        return !this.givenStory;
    }
}
