package org.jbehavesupport.runner.story;

import org.jbehavesupport.runner.story.steps.TestSteps;

import java.util.Collections;
import java.util.List;

/**
 * @author Michal Bocek
 * @since 23/03/2017
 */
public class GivenStories extends AbstractStories {

    @Override
    public List<String> storyPaths() {
        return Collections.singletonList(
            "org/jbehavesupport/runner/story/GivenStory.story"
        );
    }

    @Override
    protected List<?> getStepClasses() {
        return Collections.singletonList(new TestSteps());
    }
}
