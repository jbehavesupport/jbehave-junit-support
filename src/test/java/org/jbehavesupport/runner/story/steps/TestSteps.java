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
package org.jbehavesupport.runner.story.steps;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Composite;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michal Bocek
 * @since 26/08/16
 */
public class TestSteps {

    private static final Logger logger = LoggerFactory.getLogger(TestSteps.class);

    @BeforeStory
    public void beforeStory() {
        logger.info("Before story custom step");
    }

    @BeforeStories
    public void beforeStories() {
        logger.info("Before Stories custom step");
    }

    @BeforeScenario
    public void beforeScenario() {
        logger.info("Before scenario custom step");
    }

    @BeforeScenario
    public void beforeScenario2() {
        logger.info("Second before scenario custom step");
    }

    @AfterScenario
    public void afterScenario() {
        logger.info("After scenario custom step");
    }

    @AfterScenario
    public void afterScenario2() {
        logger.info("Second after scenario custom step");
    }

    @AfterStory
    public void afterStory() {
        logger.info("After story custom step");
    }

    @AfterStories
    public void afterStories() {
        logger.info("After stories custom step");
    }

    @Given("say Hello")
    public void sayHello() {
        logger.info("Hello");
    }


    @When("Sign in user $userName")
    public void signInUser(String userName) {
        logger.info("Signing in user: {}", userName);
    }

    @Then("User with name $userName is properly signed in")
    public void verifySignIn(String userName) {
        logger.info("User: {} was properly signed in", userName);
    }

    @Given("Registration data: $data")
    public void parseRegistrationData(ExamplesTable data) {
        logger.info("Registration data: {}", data);
    }

    @When("Sign up user")
    public void signUp() {
        logger.info("Signing up user");
    }

    @When("Auditing user")
    public void auditUser() {
        logger.info("Auditing user");
    }

    @When("Sign up with audit")
    @Composite(steps = { "When Sign up user",
                         "When Auditing user" })
    public void signUpWithAudit() {
    }

    @Then("Failed step")
    public void failedStep() {
        throw new RuntimeException("Failing step...");
    }

}
