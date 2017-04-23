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

import org.jbehave.core.embedder.PerformableTree;
import org.jbehavesupport.runner.JUnitRunner;

/**
 * @author Michal Bocek
 * @since 27/08/16
 */
public class StoryParser {

    public static AbstractDescriptionBuilder parse(final PerformableTree story, final JUnitRunner.ReportLevel reportLevel) {
        switch (reportLevel) {
            case STEP:
                return new StepLevelDescriptionBuilder(story);
            case STORY:
                return new StoryLevelDescriptionBuilder(story);
            default:
                throw new IllegalArgumentException("Unsupported report level: " + reportLevel);
        }
    }
}
