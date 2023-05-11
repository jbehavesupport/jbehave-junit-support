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

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.reporters.NullStoryReporter;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.Timing;

@Slf4j
public class LoggingReporter extends NullStoryReporter {

    @Override
    public void storyExcluded(Story story, String filter) {
        log.info("Story: {} excluded by filter: {}", story.getName(), filter);
    }

    @Override
    public void storyCancelled(Story story, StoryDuration storyDuration) {
        log.info("Story: {} cancelled in: {}s", story.getName(), storyDuration.getDurationInSecs());
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        log.info("Before story: {}", story.getName() + (givenStory ? "(given story)" : ""));
    }

    @Override
    public void afterStory(boolean givenOrRestartingStory) {
        log.info("After story");
    }

    @Override
    public void narrative(Narrative narrative) {
        if (!narrative.isEmpty()) {
            log.info("Narrative:");
        }
        if (!narrative.inOrderTo().isEmpty()) {
            log.info("In order to {}", narrative.inOrderTo());
        }
        if (!narrative.asA().isEmpty()) {
            log.info("As a {}", narrative.asA());
        }
        if (!narrative.iWantTo().isEmpty()) {
            log.info("I want to {}", narrative.iWantTo());
        }
        if (!narrative.soThat().isEmpty()) {
            log.info("So that {}", narrative.soThat());
        }
    }

    @Override
    public void lifecycle(Lifecycle lifecycle) {
        if (!lifecycle.isEmpty()) {
            log.info("Lifecycle: {}", lifecycle);
        }
    }

    @Override
    public void scenarioExcluded(Scenario scenario, String filter) {
        log.info("Scenario: {} excluded by filer: {}", scenario.getTitle(), filter);
    }

    @Override
    public void beforeScenario(Scenario scenario) {
        log.info("Before scenario: {}", scenario.getTitle());
    }

    @Override
    public void afterScenario(Timing timing) {
        log.info("After scenario, timing: {}", timing);
    }

    @Override
    public void givenStories(GivenStories givenStories) {
        log.info("Given stories: {}", givenStories);
    }

    @Override
    public void givenStories(List<String> storyPaths) {
        log.info("Given stories: {}", storyPaths);
    }

    @Override
    public void beforeExamples(List<String> steps, ExamplesTable table) {
        log.info("Before steps: {} with example table: {}", steps, table);
    }

    @Override
    public void example(Map<String, String> tableRow, int exampleIndex) {
        log.info("Example: {}, index: {}", tableRow, exampleIndex);
    }

    @Override
    public void afterExamples() {
        log.info("After examples");
    }

    @Override
    public void beforeStep(Step step) {
        log.info("Before step: {}", step.getStepAsString());
    }

    @Override
    public void successful(String step) {
        log.info("Successful step: {}", step);
    }

    @Override
    public void ignorable(String step) {
        log.info("Ignorable step: {}", step);
    }

    @Override
    public void pending(String step) {
        log.error("Pending step: {}", step);
    }

    @Override
    public void notPerformed(String step) {
        log.warn("Not performed step: {}", step);
    }

    @Override
    public void failed(String step, Throwable cause) {
        log.error("Failed step: {} cause: {}", step, cause);
    }

    @Override
    public void failedOutcomes(String step, OutcomesTable table) {
        log.error("Failed step: {} outcomes: {}", step, table);
    }

    @Override
    public void restarted(String step, Throwable cause) {
        log.info("Restarted step: {} because of: {}", step, cause);
    }

    @Override
    public void restartedStory(Story story, Throwable cause) {
        log.error("Restarted story: {} because of: {}", story.getName(), cause);
    }

    @Override
    public void dryRun() {
        log.info("Dry run");
    }

    @Override
    public void pendingMethods(List<String> methods) {
        log.error("Pending methods: {}", methods);
    }

    @Override
    public void beforeStoriesSteps(StepCollector.Stage stage) {
        log.info("Before stories steps, stage: {}", stage);
    }

    @Override
    public void afterStoriesSteps(StepCollector.Stage stage) {
        log.info("After stories steps, stage: {}", stage);
    }

    @Override
    public void beforeStorySteps(StepCollector.Stage stage, Lifecycle.ExecutionType type) {
        log.info("Before story steps, stage: {}, type: {}", stage, type);
    }

    @Override
    public void afterStorySteps(StepCollector.Stage stage, Lifecycle.ExecutionType type) {
        log.info("After story steps, stage: {}, type: {}", stage, type);
    }
}
