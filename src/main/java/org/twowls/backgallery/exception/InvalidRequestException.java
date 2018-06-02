package org.twowls.backgallery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An exception indicating invalid request data.
 *
 * @author Dmitry Chubarov
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRequestException extends ApiException {
    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
