package com.mopub.common.event;

import com.mopub.common.test.support.SdkTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(SdkTestRunner.class)
public class EventTest {

    private Event subject;

    @Before
    public void setUp() {
        subject = new Event.Builder("name", "category").build();
    }

    @Test
    public void constructor_shouldCorrectlyAssignScribeCategoryFromBuilder() throws Exception {
        assertThat(subject.getEventName()).isEqualTo("name");
        assertThat(subject.getEventCategory()).isEqualTo("category");
        assertThat(subject.getScribeCategory()).isEqualTo(BaseEvent.ScribeCategory.EXCHANGE_CLIENT_EVENT);
    }
}
