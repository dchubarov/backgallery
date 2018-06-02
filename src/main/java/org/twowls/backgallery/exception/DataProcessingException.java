package org.twowls.backgallery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An exception indicating data processing error while serving a request.
 *
 * @author Dmitry Chubarov
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DataProcessingException extends ApiException {

    public DataProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
