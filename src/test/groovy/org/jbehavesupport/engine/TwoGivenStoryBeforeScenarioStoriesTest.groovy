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
package org.jbehavesupport.engine

import org.jbehavesupport.engine.discovery.JBehaveDiscoverer
import org.jbehavesupport.engine.story.TwoGivenStoryBeforeScenarioStories
import org.junit.platform.engine.UniqueId
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.testkit.engine.EngineTestKit
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import static org.junit.platform.testkit.engine.EventConditions.container
import static org.junit.platform.testkit.engine.EventConditions.engine
import static org.junit.platform.testkit.engine.EventConditions.event
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully
import static org.junit.platform.testkit.engine.EventConditions.started
import static org.junit.platform.testkit.engine.EventConditions.test

class TwoGivenStoryBeforeScenarioStoriesTest extends Specification {

    def "Test correct notifications"() {
        given:
        EngineTestKit.Builder builder = EngineTestKit.engine("jbehave")
            .enableImplicitConfigurationParameters(true)
            .selectors(selectClass(TwoGivenStoryBeforeScenarioStories))

        when:
        def executionResults = builder.execute()

        then:
        executionResults.allEvents()
            .assertEventsMatchExactly(
                event(engine(), started()),
                event(container(TwoGivenStoryBeforeScenarioStories), started()),
                event(test("BeforeStories"), started()),
                event(test("BeforeStories"), finishedSuccessfully()),

                event(container("Story: GivenStoryBeforeScenario"), started()),
                event(test("GivenStory1"), started()),
                event(test("GivenStory1"), finishedSuccessfully()),
                event(test("GivenStory2"), started()),
                event(test("GivenStory2"), finishedSuccessfully()),
                event(container("Scenario: Given story test"), started()),
                event(test("Then User with name Tester is properly signed in"), started()),
                event(test("Then User with name Tester is properly signed in"), finishedSuccessfully()),
                event(container("Scenario: Given story test"), finishedSuccessfully()),
                event(container("Story: GivenStoryBeforeScenario"), finishedSuccessfully()),

                event(container("Story: SecondGivenStoryBeforeScenario"), started()),
                event(test("GivenStory1"), started()),
                event(test("GivenStory1"), finishedSuccessfully()),
                event(test("GivenStory2"), started()),
                event(test("GivenStory2"), finishedSuccessfully()),
                event(container("Scenario: Given story test"), started()),
                event(test("Then User with name Tester is properly signed in"), started()),
                event(test("Then User with name Tester is properly signed in"), finishedSuccessfully()),
                event(container("Scenario: Given story test"), finishedSuccessfully()),
                event(container("Story: SecondGivenStoryBeforeScenario"), finishedSuccessfully()),

                event(test("AfterStories"), started()),
                event(test("AfterStories"), finishedSuccessfully()),
                event(container(TwoGivenStoryBeforeScenarioStories), finishedSuccessfully()),
                event(engine(), finishedSuccessfully())
            )
    }

    def "Test descriptions"() {
        given:
        def request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectClass(TwoGivenStoryBeforeScenarioStories))
            .build()

        def discoverer = new JBehaveDiscoverer()

        when:
        def engineDescriptor = discoverer.discover(request, UniqueId.forEngine("jbehave"))
        def desc = engineDescriptor.children[0]
        def children = desc.children

        then:
        desc.displayName == "TwoGivenStoryBeforeScenarioStories"
        children.size() == 4
        children[0].displayName =~ /BeforeStories.*/
        children[1].displayName == "Story: GivenStoryBeforeScenario"
        children[1].children[0].displayName =~ /.*GivenStory1/
        children[1].children[1].displayName =~ /.*GivenStory2/
        children[1].children[2].displayName == "Scenario: Given story test"
        children[1].children[2].children[0].displayName =~ /Then User with name Tester is properly signed in.*/
        children[2].displayName == "Story: SecondGivenStoryBeforeScenario"
        children[2].children[0].displayName =~ /.*GivenStory1/
        children[2].children[1].displayName =~ /.*GivenStory2/
        children[2].children[2].displayName == "Scenario: Given story test"
        children[2].children[2].children[0].displayName =~ /Then User with name Tester is properly signed in.*/
        children[3].displayName =~ /AfterStories.*/
    }

    @RestoreSystemProperties
    def "Test correct notifications for story level reporter"() {
        given:
        System.setProperty("jbehave.report.level", "STORY")
        EngineTestKit.Builder builder = EngineTestKit.engine("jbehave")
            .enableImplicitConfigurationParameters(true)
            .selectors(selectClass(TwoGivenStoryBeforeScenarioStories))

        when:
        def executionResults = builder.execute()

        then:
        executionResults.allEvents()
            .assertEventsMatchExactly(
                event(engine(), started()),
                event(container(TwoGivenStoryBeforeScenarioStories), started()),
                event(test("GivenStoryBeforeScenario"), started()),
                event(test("GivenStoryBeforeScenario"), finishedSuccessfully()),
                event(test("SecondGivenStoryBeforeScenario"), started()),
                event(test("SecondGivenStoryBeforeScenario"), finishedSuccessfully()),
                event(container(TwoGivenStoryBeforeScenarioStories), finishedSuccessfully()),
                event(engine(), finishedSuccessfully())
            )
    }

    @RestoreSystemProperties
    def "Test descriptions for story level reporter"() {
        given:
        System.setProperty("jbehave.report.level", "STORY")
        def request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectClass(TwoGivenStoryBeforeScenarioStories))
            .build()

        def discoverer = new JBehaveDiscoverer()

        when:
        def engineDescriptor = discoverer.discover(request, UniqueId.forEngine("jbehave"))
        def desc = engineDescriptor.children[0]
        def children = desc.children

        then:
        desc.displayName == "TwoGivenStoryBeforeScenarioStories"
        children.size() == 2
        children[0].displayName =~ /Story: GivenStoryBeforeScenario.*/
        children[0].children.size() == 0
        children[1].displayName =~ /Story: SecondGivenStoryBeforeScenario.*/
        children[1].children.size() == 0
    }

}
