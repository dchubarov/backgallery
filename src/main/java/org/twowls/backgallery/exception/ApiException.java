package org.twowls.backgallery.exception;

import org.slf4j.Logger;

import java.io.UncheckedIOException;
import java.util.function.BiFunction;

/**
 * <p>TODO add documentation...</p>
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
