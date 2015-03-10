package com.mopub.common.event;

import com.mopub.common.CacheServiceTest;
import com.mopub.common.test.support.SdkTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.PrintWriter;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SdkTestRunner.class)
public class ErrorEventTest {

    private ErrorEvent subject;

    @Before
    public void setUp() {
        subject = new ErrorEvent.Builder("name", "category")
                .withErrorExceptionClassName("error_exception_class_name")
                .withErrorMessage("error_message")
                .withErrorStackTrace("error_stack_trace")
                .withErrorFileName("error_file_name")
                .withErrorClassName("error_class_name")
                .withErrorMethodName("error_method_name")
                .withErrorLineNumber(123)
                .build();
    }

    @Test
    public void constructor_shouldCorrectlyAssignFieldsFromBuilder() throws Exception {
        assertThat(subject.getEventName()).isEqualTo("name");
        assertThat(subject.getEventCategory()).isEqualTo("category");
        assertThat(subject.getScribeCategory()).isEqualTo(BaseEvent.ScribeCategory.EXCHANGE_CLIENT_ERROR);
        assertThat(subject.getErrorExceptionClassName()).isEqualTo("error_exception_class_name");
        assertThat(subject.getErrorMessage()).isEqualTo("error_message");
        assertThat(subject.getErrorStackTrace()).isEqualTo("error_stack_trace");
        assertThat(subject.getErrorFileName()).isEqualTo("error_file_name");
        assertThat(subject.getErrorClassName()).isEqualTo("error_class_name");
        assertThat(subject.getErrorMethodName()).isEqualTo("error_method_name");
        assertThat(subject.getErrorLineNumber()).isEqualTo(123);
    }

    @Test
    public void builder_withException_shouldCorrectlyPopulateErrorFields() throws Exception {
        Exception exception;
        try {
            throw new ClassCastException("bad cast");
        } catch (Exception e)  {
            exception = e;
        }

        subject = new ErrorEvent.Builder("name", "category")
                .withException(exception)
                .build();

        assertThat(subject.getEventName()).isEqualTo("name");
        assertThat(subject.getEventCategory()).isEqualTo("category");
        assertThat(subject.getScribeCategory()).isEqualTo(BaseEvent.ScribeCategory.EXCHANGE_CLIENT_ERROR);
        assertThat(subject.getErrorExceptionClassName()).isEqualTo("java.lang.ClassCastException");
        assertThat(subject.getErrorMessage()).isEqualTo("bad cast");

        // We can't reliably check the stack trace since it changes from one run to another
//        assertThat(subject.getErrorStackTrace()).isEqualTo();

        assertThat(subject.getErrorFileName()).isEqualTo("ErrorEventTest.java");
        assertThat(subject.getErrorClassName()).isEqualTo("com.mopub.common.event.ErrorEventTest");
        assertThat(subject.getErrorMethodName()).isEqualTo("builder_withException_shouldCorrectlyPopulateErrorFields");
        assertThat(subject.getErrorLineNumber()).isEqualTo(56);
    }
}
