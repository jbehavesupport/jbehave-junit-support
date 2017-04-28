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
import org.jbehave.core.model.Story;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

import static java.util.Objects.nonNull;

/**
 * @author Michal Bocek
 * @since 21/04/2017
 */
public class JUnitStoryReporter extends AbstractJUnitReporter {

    private final RunNotifier notifier;
    private final Description rootDescription;
    private final Configuration configuration;

    private Description currentStoryDescription;

    public JUnitStoryReporter(RunNotifier notifier, Description rootDescription, Configuration configuration) {
        this.notifier = notifier;
        this.rootDescription = rootDescription;
        this.configuration = configuration;
    }

    @Override
    public void beforeStory(Story story, boolean givenStory) {
        if (givenStory) {
            this.givenStory = true;
        } else {
            beforeStory(story);
        }
        super.beforeStory(story, givenStory);
    }

    private void beforeStory(Story story) {
        for (Description description : rootDescription.getChildren()) {
            if (description.isTest()
                && (isEligibleAs(story, description, BEFORE_STORIES)
                || isEligibleAs(story, description, AFTER_STORIES))) {
                currentStoryDescription = description;
                notifier.fireTestStarted(currentStoryDescription);

            }
            if (description.isTest()
                && isEligibleAs(description, story.getName())) {
                currentStoryDescription = description;
                notifier.fireTestStarted(currentStoryDescription);
            }
        }
    }

    @Override
    public void afterStory(boolean givenOrRestartingStory) {
        super.afterStory(givenOrRestartingStory);
        if (isAGivenStory()) {
            this.givenStory = false;
        } else if (nonNull(currentStoryDescription)) {
            notifier.fireTestFinished(currentStoryDescription);
        }
    }
}
