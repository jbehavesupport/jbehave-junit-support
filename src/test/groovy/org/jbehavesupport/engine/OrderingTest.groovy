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

import org.jbehavesupport.engine.story.AndStepStories
import org.jbehavesupport.engine.story.BasicStory
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.testkit.engine.EngineTestKit
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.environment.RestoreSystemProperties

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import static org.junit.platform.testkit.engine.EventConditions.container
import static org.junit.platform.testkit.engine.EventConditions.engine
import static org.junit.platform.testkit.engine.EventConditions.event
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully
import static org.junit.platform.testkit.engine.EventConditions.started
import static org.junit.platform.testkit.engine.EventConditions.test

class OrderingTest extends Specification {

    @Unroll
    @RestoreSystemProperties
    def "Test ordering with #comparatorClassName"() {
        given:
        System.setProperty("jbehave.report.level", "STORY")
        System.setProperty(JBehaveTestEngine.COMPARATOR_PROPERTY, comparatorClassName)
        EngineTestKit.Builder builder = EngineTestKit.engine("jbehave")
            .enableImplicitConfigurationParameters(true)
            .selectors(selectClass(BasicStory), selectClass(AndStepStories))

        when:
        def executionResults = builder.execute()

        then:
        executionResults.allEvents()
            .assertEventsMatchExactly(
                event(engine(), started()),
                event(container(firstClass), started()),
                event(test(firstStory), started()),
                event(test(firstStory), finishedSuccessfully()),
                event(container(firstClass), finishedSuccessfully()),
                event(container(secondClass), started()),
                event(test(secondStory), started()),
                event(test(secondStory), finishedSuccessfully()),
                event(container(secondClass), finishedSuccessfully()),
                event(engine(), finishedSuccessfully())
            )

        where:
        comparatorClassName                   || firstStory    || firstClass     || secondStory   || secondClass
        ReverseComparator.class.getName()     || "basic_story" || BasicStory     || "AndStep"     || AndStepStories
        DisplayNameComparator.class.getName() || "AndStep"     || AndStepStories || "basic_story" || BasicStory

    }

}

class ReverseComparator implements Comparator<TestDescriptor> {

    private DisplayNameComparator displayNameComparator = new DisplayNameComparator()

    @Override
    int compare(TestDescriptor o1, TestDescriptor o2) {
        displayNameComparator.reversed().compare(o1, o2)
    }
}

class DisplayNameComparator implements Comparator<TestDescriptor> {
    @Override
    int compare(TestDescriptor o1, TestDescriptor o2) {
        o1.displayName <=> o2.displayName
    }
}
