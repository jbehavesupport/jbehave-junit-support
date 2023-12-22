# JUnit support for JBehave
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jbehavesupport/jbehave-junit-support/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jbehavesupport/jbehave-junit-support)
[![Build Status](https://travis-ci.org/jbehavesupport/jbehave-junit-support.svg?branch=master)](https://travis-ci.org/jbehavesupport/jbehave-junit-support)
[![Coverage Status](https://coveralls.io/repos/github/jbehavesupport/jbehave-junit-support/badge.svg?branch=master)](https://coveralls.io/github/jbehavesupport/jbehave-junit-support?branch=master)
[![codebeat badge](https://codebeat.co/badges/adbb13e0-c146-4f58-847b-c1db713efbb7)](https://codebeat.co/projects/github-com-jbehavesupport-jbehave-junit-support-master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c8ccfdab225040f1ae25e128fab9ba4b)](https://www.codacy.com/app/mbocek/jbehave-junit-support?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jbehavesupport/jbehave-junit-support&amp;utm_campaign=Badge_Grade)

Support library which should help with running jbehave BDD tests.

## Integration to java project
Setup in the maven project:
```xml
<dependency>
    <groupId>org.jbehavesupport</groupId>
    <artifactId>jbehave-junit-support</artifactId>
</dependency>
```

### JUnit 5
Very simple java class used by the JUnit 5 compatible test engine:
```java
public class BasicStory extends JUnit5Story {

    public BasicStory() {
        EmbedderConfiguration.recommendedConfiguration(configuredEmbedder());
    }

    @Override
    public Configuration configuration() {
        return new MostUsefulConfiguration();
    }

    ...
}
```

#### Ordering of test classes
The ordering of classes is not guaranteed by default by the engine.
If you need to influence order you can supply your own comparator class name in the parameter `jbehave.execution.order.comparator`
either in system properties or in the JUnit Platform configuration file named `junit-platform.properties`.
This comparator needs to implement the `Comparator<TestDescriptor>` interface and have a no-args constructor available.

Example configuration:
```java
package com.application.comparator;

public class DisplayNameComparator implements Comparator<TestDescriptor> {
    @Override
    public int compare(TestDescriptor o1, TestDescriptor o2) {
        return o1.getDisplayName().compareTo(o2.getDisplayName());
    }
}
```
junit-platform.properties:
```properties
jbehave.execution.order.comparator=com.application.comparator.DisplayNameComparator
```

### JUnit 4
To use JUnit4 runner please add a dependency for `junit` or `junit-vintage-engine` to your project explicitly.
Very simple java class with runner implementation:
```java
@RunWith(JUnitRunner.class)
public class BasicStory extends JUnitStory {

    public BasicStory() {
        JUnitRunnerConfiguration.recommendedConfiguration(configuredEmbedder());
    }

    @Override
    public Configuration configuration() {
        return new MostUsefulConfiguration();
    }

    ...
}
```

Stories can be run in two modes:
- step
- story

Reporting level can be chosen by jvm environment variable for step level:
```
-Djbehave.report.level=STEP
```
In the IDE reporting is shown:
![Step level](docs/images/step-level.png)
Step level is default reporting level. For story level you should use:
```
-Djbehave.report.level=STORY
```
In the IDE reporting is shown:
![Story level](docs/images/story-level.png)

## Compatibility matrix
| jbehave-junit-support | jbehave  |
|-----------------------| --------:|
| 1.0.1                 | 4.0.5    |
| 1.2.7                 | 4.1.3    |
| 4.3.5                 | 4.3.5    |
| 4.6.1                 | 4.6.1    |
| 4.8.0                 | 4.8      |
| 4.8.3                 | 4.8.3    |
| 5.0.0                 | 5.0      |
| 5.0.1                 | 5.0      |
