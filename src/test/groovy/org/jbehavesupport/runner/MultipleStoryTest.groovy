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
package org.jbehavesupport.runner

import org.jbehavesupport.runner.story.MultipleStories
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties
/**
 * @author Michal Bocek
 * @since 21/09/17
 */
class MultipleStoryTest extends Specification {

    def notifier = Mock(RunNotifier)

    def "Test correct notifications"() {
        given:
        def runner = new JUnitRunner(MultipleStories)

        when:
        runner.run(notifier)

        then:
        1 * notifier.fireTestStarted({it.displayName.startsWith("BeforeStories")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.startsWith("BeforeStories")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.equals("Story: Scenario01-1")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.equals("Scenario: login to system 1")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("Given login with data")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("Given login with data")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("When I submit login data on http://test01")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("When I submit login data on http://test01")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("Then user should be logged in successful")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("Then user should be logged in successful")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.equals("Scenario: login to system 1")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.equals("Story: Scenario01-1")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.equals("Story: Scenario01")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.equals("Scenario: login to system 2")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("Given login with data")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("Given login with data")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("When I submit login data on http://test02")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("When I submit login data on http://test02")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("Then user should be logged in successful")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("Then user should be logged in successful")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.equals("Scenario: login to system 2")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.equals("Story: Scenario01")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.equals("Story: Scenario03")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.equals("Scenario: login to system 3")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("Given login with data")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("Given login with data")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("When I submit login data on http://test03")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("When I submit login data on http://test03")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.contains("Then user should be logged in successful")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.contains("Then user should be logged in successful")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.equals("Scenario: login to system 3")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.equals("Story: Scenario03")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.startsWith("AfterStories")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.startsWith("AfterStories")} as Description)
    }

    def "Test descriptions"() {
        given:
        def runner = new JUnitRunner(MultipleStories)

        when:
        def desc = runner.description
        def children = desc.children

        then:
        desc.testClass == MultipleStories
        children.size() == 5
        // children are in the order they are defined in storyPaths, the actual run order depends on StoryExecutionComparator
        children[0].displayName =~ /BeforeStories.*/
        children[1].displayName == "Story: Scenario01"
        children[1].children[0].displayName == "Scenario: login to system 2"
        children[1].children[0].children[0].displayName =~ /.*Given login with data/
        children[1].children[0].children[1].displayName =~ /.*When I submit login data on http:\/\/test02/
        children[1].children[0].children[2].displayName =~ /.*Then user should be logged in successful/
        children[2].displayName == "Story: Scenario01-1"
        children[2].children[0].displayName == "Scenario: login to system 1"
        children[2].children[0].children[0].displayName =~ /.*Given login with data/
        children[2].children[0].children[1].displayName =~ /.*When I submit login data on http:\/\/test01/
        children[2].children[0].children[2].displayName =~ /.*Then user should be logged in successful/
        children[3].displayName == "Story: Scenario03"
        children[3].children[0].displayName == "Scenario: login to system 3"
        children[3].children[0].children[0].displayName =~ /.*Given login with data/
        children[3].children[0].children[1].displayName =~ /.*When I submit login data on http:\/\/test03/
        children[3].children[0].children[2].displayName =~ /.*Then user should be logged in successful/
        children[4].displayName =~ /AfterStories.*/
    }

    @RestoreSystemProperties
    def "Test correct notifications for story level reporter"() {
        given:
        System.setProperty("jbehave.report.level", "STORY")
        def runner = new JUnitRunner(MultipleStories)

        when:
        runner.run(notifier)

        then:
        1 * notifier.fireTestStarted({it.displayName.startsWith("Story: Scenario01-1")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.startsWith("Story: Scenario01-1")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.startsWith("Story: Scenario01")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.startsWith("Story: Scenario01")} as Description)
        then:
        1 * notifier.fireTestStarted({it.displayName.startsWith("Story: Scenario03")} as Description)
        then:
        1 * notifier.fireTestFinished({it.displayName.startsWith("Story: Scenario03")} as Description)
    }

    @RestoreSystemProperties
    def "Test descriptions for story level reporter"() {
        given:
        System.setProperty("jbehave.report.level", "STORY")
        def runner = new JUnitRunner(MultipleStories)

        when:
        def desc = runner.description
        def children = desc.children

        then:
        desc.testClass == MultipleStories
        children.size() == 3
        children[0].displayName =~ "Story: Scenario01"
        children[0].children.size() == 0
        children[1].displayName =~ "Story: Scenario01-1"
        children[1].children.size() == 0
        children[2].displayName =~ "Story: Scenario03"
        children[2].children.size() == 0
    }
}
