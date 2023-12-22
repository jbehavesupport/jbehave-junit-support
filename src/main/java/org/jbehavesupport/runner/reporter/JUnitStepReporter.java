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
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.jbehave.core.steps.Timing;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.util.*;

import static java.util.Objects.nonNull;

/**
 * @author Michal Bocek
 * @since 29/08/16
 */
public class JUnitStepReporter extends AbstractJUnitReporter {

    private final RunNotifier notifier;
    private final Description rootDescription;
    private final Configuration configuration;

    private Description currentStoryDescription;
    private Iterator<Description> scenariosDescriptions;
    private Description currentScenarioDescription;
    private Iterator<Description> examplesDescriptions;
    private Description currentExampleDescription;
    private Iterator<Description> stepsDescriptions;
    private Deque<Description> currentStepDescription = new ArrayDeque<>();

    private boolean isInBeforeStories = false;
    private boolean isInBeforeScenario = false;
    private boolean isInAfterScenario = false;
    private boolean isInAfterStories = false;
    private boolean isInMainScenario = false;

    public JUnitStepReporter(RunNotifier notifier, Description rootDescription,
                             Configuration configuration) {
        this.notifier = notifier;
        this.rootDescription = rootDescription;
        this.configuration = configuration;
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        if (givenStory) {
            if (notAGivenStory()) {
                currentStepDescription.push(hasNextStepsDescriptions() ? stepsDescriptions.next() : scenariosDescriptions.next());
                notifier.fireTestStarted(currentStepDescription.peek());
            }
            this.givenStories++;
        } else {
            beforeStory(story);
        }
        super.beforeStory(story, givenStory);
    }

    private boolean hasNextStepsDescriptions() {
        return nonNull(stepsDescriptions) && stepsDescriptions.hasNext();
    }

    private void beforeStory(Story story) {
        for (Description description : rootDescription.getChildren()) {
            if (description.isSuite()
                && suiteIsEligibleAs(description, story.getName())) {
                currentStoryDescription = description;
                notifier.fireTestStarted(currentStoryDescription);
                scenariosDescriptions = currentStoryDescription.getChildren().iterator();
            }
        }
    }

    @Override
    public void beforeStoriesSteps(StepCollector.Stage stage) {
        switch (stage) {
            case BEFORE:
                isInBeforeStories = true;
                handleBeforeAfterStoriesExecution(BEFORE_STORIES, true);
                break;
            case AFTER:
                isInAfterStories = true;
                handleBeforeAfterStoriesExecution(AFTER_STORIES, true);
                break;
            default:
                throw new IllegalStateException("StepCollector Stage should not exists: " + stage);
        }
        super.beforeStoriesSteps(stage);
    }

    @Override
    public void afterStoriesSteps(StepCollector.Stage stage) {
        switch (stage) {
            case BEFORE:
                isInBeforeStories = false;
                handleBeforeAfterStoriesExecution(BEFORE_STORIES, false);
                break;
            case AFTER:
                isInAfterStories = false;
                handleBeforeAfterStoriesExecution(AFTER_STORIES, false);
                break;
            default:
                throw new IllegalStateException("StepCollector Stage should not exists: " + stage);
        }
        super.afterStoriesSteps(stage);
    }

    private void handleBeforeAfterStoriesExecution(String storyName, boolean start) {
        for (Description description : rootDescription.getChildren()) {
            if (description.isTest() && testIsEligibleAs(description, storyName)) {
                currentStoryDescription = description;
                if (start) {
                    notifier.fireTestStarted(currentStoryDescription);
                } else {
                    notifier.fireTestFinished(currentStoryDescription);
                }
            }
        }
    }

    @Override
    public void afterStory(boolean givenOrRestartingStory) {
        super.afterStory(givenOrRestartingStory);
        if (this.givenStories == 1) {
            notifier.fireTestFinished(currentStepDescription.pop());
            this.givenStories--;
        } else if (isAGivenStory()) {
            this.givenStories--;
        } else if (nonNull(currentStoryDescription)) {
            notifier.fireTestFinished(currentStoryDescription);
        }

    }

    @Override
    public void beforeScenario(Scenario scenario) {
        if (notAGivenStory() && (!isInBeforeStories || !isInAfterStories)) {
            currentScenarioDescription = scenariosDescriptions.next();
            stepsDescriptions = getAllChildren(currentScenarioDescription.getChildren(), new ArrayList<>()).iterator();
            examplesDescriptions = getAllExamples(currentScenarioDescription.getChildren()).iterator();
            notifier.fireTestStarted(currentScenarioDescription);
            isInMainScenario = true;
            super.beforeScenario(scenario);
        }
    }

    @Override
    public void beforeScenarioSteps(StepCollector.Stage stage, Lifecycle.ExecutionType cycle){
        // as in jbehave-core v5.0:
        // Always trigger StoryReporter.beforeStep(Step) hook and report all outcomes (previously only failures were reported, successful outcome was silent) for methods annotated with @BeforeStories, @AfterStories, @BeforeStory, @AfterStory, @BeforeScenario, @AfterScenario
        // @BeforeScenario steps are executed between cycle SYSTEM and stage BEFORE and next stage, so we won't report steps in this combination
        if (cycle == Lifecycle.ExecutionType.SYSTEM && stage == StepCollector.Stage.BEFORE) {
            isInBeforeScenario = true;
        } else {
            isInBeforeScenario = false;
        }
        super.beforeScenarioSteps(stage, cycle);
    }

    @Override
    public void afterScenarioSteps(StepCollector.Stage stage, Lifecycle.ExecutionType cycle){
        // as in jbehave-core v5.0:
        // Always trigger StoryReporter.beforeStep(Step) hook and report all outcomes (previously only failures were reported, successful outcome was silent) for methods annotated with @BeforeStories, @AfterStories, @BeforeStory, @AfterStory, @BeforeScenario, @AfterScenario
        // @AfterScenario steps are executed between cycle USER and stage AFTER and next stage, so we won't report steps in this combination
        if (cycle == Lifecycle.ExecutionType.USER && stage == StepCollector.Stage.AFTER) {
            isInAfterScenario = true;
        } else if (cycle == Lifecycle.ExecutionType.SYSTEM && stage == StepCollector.Stage.AFTER) {
            isInAfterScenario = false;
        }
        super.beforeScenarioSteps(stage, cycle);
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
            if (description.isSuite() && !isExample(description)) {
                result.add(description);
                getAllChildren(description.getChildren(), result);
            } else if (description.isSuite()) {
                getAllChildren(description.getChildren(), result);
            } else {
                result.add(description);
            }
        }
        return result;
    }

    @Override
    public void afterScenario(Timing timing) {
        super.afterScenario(timing);
        if (notAGivenStory() && (!isInBeforeStories || !isInAfterStories)) {
            notifier.fireTestFinished(currentScenarioDescription);
            // main scenario starts before given stories are run,
            // so we need to handle the case of afterScenario of given story
            if (notAGivenStory()) {
                isInMainScenario = false;
            }
        }
    }

    @Override
    public void beforeStep(Step step) {
        if (StepExecutionType.EXECUTABLE == step.getExecutionType() && shouldReportStep()) {
            currentStepDescription.push(stepsDescriptions.next());
            notifier.fireTestStarted(currentStepDescription.peek());
        }
        super.beforeStep(step);
    }

    @Override
    public void successful(String step) {
        super.successful(step);
        if (shouldReportStep()) {
            notifier.fireTestFinished(currentStepDescription.pop());
        }
    }

    @Override
    public void failed(String step, Throwable cause) {
        if (cause instanceof UUIDExceptionWrapper) {
            cause = cause.getCause();
        }
        super.failed(step, cause);
        notifier.fireTestFailure(new Failure(currentStepDescription.peek(), cause));
        if (shouldReportStep()) {
            notifier.fireTestFinished(currentStepDescription.peek());
        }
    }

    @Override
    public void notPerformed(String step) {
        super.notPerformed(step);
        if (shouldReportStep()) {
            currentStepDescription.push(stepsDescriptions.next());
            notifier.fireTestIgnored(currentStepDescription.peek());
        }
    }

    @Override
    public void pending(String step) {
        super.pending(step);
        if (shouldReportStep()) {
            currentStepDescription.push(stepsDescriptions.next());
            notifier.fireTestIgnored(currentStepDescription.peek());
        }
    }

    @Override
    public void example(Map<String, String> tableRow, int exampleIndex) {
        if (shouldReportStep()) {
            if (nonNull(currentExampleDescription)) {
                notifier.fireTestFinished(currentExampleDescription);
            }
            currentExampleDescription = examplesDescriptions.next();
            notifier.fireTestStarted(currentExampleDescription);
        }
        super.example(tableRow, exampleIndex);
    }

    @Override
    public void afterExamples() {
        if (shouldReportStep()) {
            notifier.fireTestFinished(currentExampleDescription);
        }
        super.afterExamples();
    }

    @Override
    public void ignorable(String step) {
        super.ignorable(step);
        if (shouldReportStep()) {
            currentStepDescription.push(stepsDescriptions.next());
            notifier.fireTestIgnored(currentStepDescription.peek());
        }
    }

    private boolean shouldReportStep() {
        // not a given story
        // not in before stories or after stories
        // not in before scenario or after scenario
        // and is in scenario of the main story (e.g. not some custom before story hook on method or something like that)
        return notAGivenStory()
            && (!isInBeforeStories || !isInAfterStories)
            && !isInBeforeScenario
            && !isInAfterScenario
            && isInMainScenario;
    }

}
