package com.mopub.common.event;

import android.support.annotation.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Immutable data class with error event data.
 */
public class ErrorEvent extends BaseEvent {
    @Nullable private final String mErrorExceptionClassName;
    @Nullable private final String mErrorMessage;
    @Nullable private final String mErrorStackTrace;
    @Nullable private final String mErrorFileName;
    @Nullable private final String mErrorClassName;
    @Nullable private final String mErrorMethodName;
    @Nullable private final Integer mErrorLineNumber;

    private ErrorEvent(Builder builder) {
        super(builder);
        mErrorExceptionClassName = builder.mErrorExceptionClassName;
        mErrorMessage = builder.mErrorMessage;
        mErrorStackTrace = builder.mErrorStackTrace;
        mErrorFileName = builder.mErrorFileName;
        mErrorClassName = builder.mErrorClassName;
        mErrorMethodName = builder.mErrorMethodName;
        mErrorLineNumber = builder.mErrorLineNumber;
    }

    public String getErrorExceptionClassName() {
        return mErrorExceptionClassName;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public String getErrorStackTrace() {
        return mErrorStackTrace;
    }

    public String getErrorFileName() {
        return mErrorFileName;
    }

    public String getErrorClassName() {
        return mErrorClassName;
    }

    public String getErrorMethodName() {
        return mErrorMethodName;
    }

    public Integer getErrorLineNumber() {
        return mErrorLineNumber;
    }

    @Override
    public String toString() {
        final String string = super.toString();
        return string +
                "ErrorEvent\n" +
                "ErrorExceptionClassName: " + getErrorExceptionClassName() + "\n" +
                "ErrorMessage: " + getErrorMessage() + "\n" +
                "ErrorStackTrace: " + getErrorStackTrace() + "\n" +
                "ErrorFileName: " + getErrorFileName() + "\n" +
                "ErrorClassName: " + getErrorClassName() + "\n" +
                "ErrorMethodName: " + getErrorMethodName() + "\n" +
                "ErrorLineNumber: " + getErrorLineNumber() + "\n";
    }

    public static class Builder extends BaseEvent.Builder {
        @Nullable private String mErrorExceptionClassName;
        @Nullable private String mErrorMessage;
        @Nullable private String mErrorStackTrace;
        @Nullable private String mErrorFileName;
        @Nullable private String mErrorClassName;
        @Nullable private String mErrorMethodName;
        @Nullable private Integer mErrorLineNumber;

        public Builder(String eventName, String eventCategory) {
            super(ScribeCategory.EXCHANGE_CLIENT_ERROR, eventName, eventCategory);
        }

        public Builder withErrorExceptionClassName(String errorExceptionClassName) {
            mErrorExceptionClassName = errorExceptionClassName;
            return this;
        }

        public Builder withErrorMessage(String errorMessage) {
            mErrorMessage = errorMessage;
            return this;
        }

        public Builder withErrorStackTrace(String errorStackTrace) {
            mErrorStackTrace = errorStackTrace;
            return this;
        }

        public Builder withErrorFileName(String errorFileName) {
            mErrorFileName = errorFileName;
            return this;
        }

        public Builder withErrorClassName(String errorClassName) {
            mErrorClassName = errorClassName;
            return this;
        }

        public Builder withErrorMethodName(String errorMethodName) {
            mErrorMethodName = errorMethodName;
            return this;
        }

        public Builder withErrorLineNumber(Integer errorLineNumber) {
            mErrorLineNumber = errorLineNumber;
            return this;
        }

        public Builder withException(Exception exception) {
            mErrorExceptionClassName = exception.getClass().getName();
            mErrorMessage = exception.getMessage();

            StringWriter stringWriter = new StringWriter();
            exception.printStackTrace(new PrintWriter(stringWriter));
            mErrorStackTrace = stringWriter.toString();

            if (exception.getStackTrace().length > 0) {
                mErrorFileName = exception.getStackTrace()[0].getFileName();
                mErrorClassName = exception.getStackTrace()[0].getClassName();
                mErrorMethodName = exception.getStackTrace()[0].getMethodName();
                mErrorLineNumber = exception.getStackTrace()[0].getLineNumber();
            }
            return this;
        }

        @Override
        public ErrorEvent build() {
            return new ErrorEvent(this);
        }
    }
}
