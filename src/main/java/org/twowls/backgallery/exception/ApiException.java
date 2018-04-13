package org.twowls.backgallery.exception;

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
}
