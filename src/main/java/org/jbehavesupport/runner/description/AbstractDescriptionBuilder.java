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

import lombok.Getter;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.StepCandidate;
import org.junit.runner.Description;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.runner.Description.createTestDescription;

/**
 * @author Michal Bocek
 * @since 4/23/2017
 */
public abstract class AbstractDescriptionBuilder implements DescriptionBuilder {

    private static final String STORIES_BEFORE = "BeforeStories";
    private static final String STORIES_AFTER= "AfterStories";

    private final PerformableTree story;

    @Getter
    private final List<StepCandidate> stepCandidates = new ArrayList<>();

    @Getter
    private Keywords keywords = new Keywords();

    @Getter
    private int testCount;

    public AbstractDescriptionBuilder(final PerformableTree story) {
        this.story = story;
    }

    @Override
    public DescriptionBuilder withCandidateSteps(List<CandidateSteps> candidateSteps) {
        for (CandidateSteps candidateStep : candidateSteps) {
            stepCandidates.addAll(candidateStep.listCandidates());
        }
        return this;
    }

    @Override
    public DescriptionBuilder withKeywords(Keywords keywords) {
        this.keywords = keywords;
        return this;
    }

    @Override
    public StoryResult buildDescription() {
        List<Description> descriptions = story.getRoot()
            .getStories()
            .stream()
            .map(this::createStoryDescription)
            .collect(Collectors.toList());
        descriptions.add(0, createTestDescription(Story.class, STORIES_BEFORE));
        descriptions.add(createTestDescription(Story.class, STORIES_AFTER));
        return new StoryResult(descriptions, testCount);
    }

    public void addTestCount() {
        testCount++;
    }

    protected abstract Description createStoryDescription(PerformableTree.PerformableStory performableStory);
}

