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
package org.jbehavesupport.engine.reporter;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.jbehave.core.steps.Timing;
import org.jbehavesupport.engine.descriptor.JBehaveTestDescriptor;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.nonNull;

public class StepLoggingReporter extends AbstractLoggingReporter {

    private final EngineExecutionListener engineExecutionListener;
    private final JBehaveTestDescriptor rootDescriptor;
    private final Configuration configuration;

    private TestDescriptor currentStoryDescriptor;
    private Iterator<? extends TestDescriptor> scenariosDescriptors;
    private TestDescriptor currentScenarioDescriptor;
    private Iterator<TestDescriptor> examplesDescriptors;
    private TestDescriptor currentExampleDescriptor;
    private Iterator<TestDescriptor> stepsDescriptors;
    private Deque<TestDescriptor> currentStepDescriptor = new ArrayDeque<>();

    private boolean isInBeforeStories = false;
    private boolean isInAfterStories = false;
    private boolean isInMainScenario = false;

    public StepLoggingReporter(EngineExecutionListener engineExecutionListener, JBehaveTestDescriptor rootDescriptor,
                               Configuration configuration) {
        this.engineExecutionListener = engineExecutionListener;
        this.rootDescriptor = rootDescriptor;
        this.configuration = configuration;
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        if (givenStory) {
            if (notAGivenStory()) {
                currentStepDescriptor.push(hasNextStepsDescriptions() ? stepsDescriptors.next() : scenariosDescriptors.next());
                engineExecutionListener.executionStarted(currentStepDescriptor.peek());
            }
            this.givenStories++;
        } else {
            beforeStory(story);
        }
        super.beforeStory(story, givenStory);
    }

    private boolean hasNextStepsDescriptions() {
        return nonNull(stepsDescriptors) && stepsDescriptors.hasNext();
    }

    private void beforeStory(Story story) {
        for (TestDescriptor descriptor : rootDescriptor.getChildren()) {
            if (descriptor.isContainer()
                && containerIsEligibleAs(descriptor, story.getName())) {
                currentStoryDescriptor = descriptor;
                engineExecutionListener.executionStarted(currentStoryDescriptor);
                scenariosDescriptors = currentStoryDescriptor.getChildren().iterator();
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
        for (TestDescriptor descriptor : rootDescriptor.getChildren()) {
            if (descriptor.isTest() && testIsEligibleAs(descriptor, storyName)) {
                currentStoryDescriptor = descriptor;
                if (start) {
                    engineExecutionListener.executionStarted(currentStoryDescriptor);
                } else {
                    engineExecutionListener.executionFinished(currentStoryDescriptor, TestExecutionResult.successful());
                }

            }
        }
    }

    @Override
    public void afterStory(boolean givenOrRestartingStory) {
        super.afterStory(givenOrRestartingStory);
        if (this.givenStories == 1) {
            engineExecutionListener.executionFinished(currentStepDescriptor.pop(), TestExecutionResult.successful());
            this.givenStories--;
        } else if (isAGivenStory()) {
            this.givenStories--;
        } else if (nonNull(currentStoryDescriptor)) {
            engineExecutionListener.executionFinished(currentStoryDescriptor, TestExecutionResult.successful());
        }

    }

    @Override
    public void beforeScenario(Scenario scenario) {
        if (notAGivenStory() && (!isInBeforeStories || !isInAfterStories)) {
            currentScenarioDescriptor = scenariosDescriptors.next();
            stepsDescriptors = getAllChildren(currentScenarioDescriptor.getChildren(), new ArrayList<>()).iterator();
            examplesDescriptors = getAllExamples(currentScenarioDescriptor.getChildren()).iterator();
            engineExecutionListener.executionStarted(currentScenarioDescriptor);
            isInMainScenario = true;
            super.beforeScenario(scenario);
        }
    }

    private List<TestDescriptor> getAllExamples(Set<? extends TestDescriptor> children) {
        List<TestDescriptor> result = new ArrayList<>();
        for (TestDescriptor child : children) {
            if (isExample(child)) {
                result.add(child);
            }
        }
        return result;
    }

    private boolean isExample(TestDescriptor descriptor) {
       return  descriptor.getDisplayName().startsWith(configuration.keywords().examplesTableRow() + " ");
    }

    private List<TestDescriptor> getAllChildren(Set<? extends TestDescriptor> children, List<TestDescriptor> result) {
        for (TestDescriptor descriptor : children) {
            if (descriptor.isContainer() && !isExample(descriptor)) {
                result.add(descriptor);
                getAllChildren(descriptor.getChildren(), result);
            } else if (descriptor.isContainer()) {
                getAllChildren(descriptor.getChildren(), result);
            } else {
                result.add(descriptor);
            }
        }
        return result;
    }

    @Override
    public void afterScenario(Timing timing) {
        super.afterScenario(timing);
        if (shouldReportStep()) {
            engineExecutionListener.executionFinished(currentScenarioDescriptor, TestExecutionResult.successful());
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
            currentStepDescriptor.push(stepsDescriptors.next());
            engineExecutionListener.executionStarted(currentStepDescriptor.peek());
        }
        super.beforeStep(step);
    }

    @Override
    public void successful(String step) {
        super.successful(step);
        if (shouldReportStep()) {
            engineExecutionListener.executionFinished(currentStepDescriptor.pop(), TestExecutionResult.successful());
        }
    }

    @Override
    public void failed(String step, Throwable cause) {
        if (cause instanceof UUIDExceptionWrapper) {
            cause = cause.getCause();
        }
        super.failed(step, cause);
        engineExecutionListener.executionFinished(currentStepDescriptor.peek(), TestExecutionResult.failed(cause));
        if (shouldReportStep()) {
            engineExecutionListener.executionFinished(currentStepDescriptor.peek(), TestExecutionResult.successful());
        }
    }

    @Override
    public void notPerformed(String step) {
        super.notPerformed(step);
        if (shouldReportStep()) {
            currentStepDescriptor.push(stepsDescriptors.next());
            engineExecutionListener.executionSkipped(currentStepDescriptor.peek(), "Not performed");
        }
    }

    @Override
    public void pending(String step) {
        super.pending(step);
        if (shouldReportStep()) {
            currentStepDescriptor.push(stepsDescriptors.next());
            engineExecutionListener.executionFinished(currentStepDescriptor.peek(), TestExecutionResult.failed(new PendingStepFound(step)));
        }
    }

    @Override
    public void example(Map<String, String> tableRow, int exampleIndex) {
        if (shouldReportStep()) {
            if (nonNull(currentExampleDescriptor)) {
                engineExecutionListener.executionFinished(currentExampleDescriptor, TestExecutionResult.successful());
            }
            currentExampleDescriptor = examplesDescriptors.next();
            engineExecutionListener.executionStarted(currentExampleDescriptor);
        }
        super.example(tableRow, exampleIndex);
    }

    @Override
    public void afterExamples() {
        if (shouldReportStep()) {
            engineExecutionListener.executionFinished(currentExampleDescriptor, TestExecutionResult.successful());
        }
        super.afterExamples();
    }

    @Override
    public void ignorable(String step) {
        super.ignorable(step);
        if (shouldReportStep()) {
            currentStepDescriptor.push(stepsDescriptors.next());
            engineExecutionListener.executionSkipped(currentStepDescriptor.peek(), "Ignored");
        }
    }

    private boolean shouldReportStep() {
        // not a given story
        // not in before stories or after stories
        // and is in scenario of the main story (e.g. not some custom before story hook on method or something like that)
        return notAGivenStory()
            && (!isInBeforeStories || !isInAfterStories)
            && isInMainScenario;
    }

}
