package org.twowls.backgallery.exception;

import org.slf4j.Logger;

import java.io.UncheckedIOException;
import java.util.function.BiFunction;

/**
 * Base application exception class.
 *
 * @author Dmitry Chubarov
 */
public abstract class ApiException extends Exception {
    ApiException() {
        super();
    }

    ApiException(String message) {
        super(message);
    }

    ApiException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates and returns an exception instance using supplied {@code message} and
     * {@code cause}, printing message and exception stack trace to the {@code logger}
     * if provided.
     *
     * @param logger the logger, may be {@code null} to skip logging.
     * @param message the error message.
     * @param cause the cause, may be {@code null}.
     * @param exceptionSupplier the function that creates exception instance, not {@code null}.
     * @param <X> the type of returned exception.
     * @return the exception instance.
     */
    public static <X extends ApiException> X logged(Logger logger, String message, Throwable cause,
            BiFunction<String, Throwable, X> exceptionSupplier) {

        if (cause instanceof UncheckedIOException) {
            cause = cause.getCause();
        }

        if (logger != null) {
            logger.warn(message, cause);
        }

        return exceptionSupplier.apply(message, cause);
    }
}
