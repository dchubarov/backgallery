package org.twowls.backgallery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DataProcessingException extends ApiException {

    public DataProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
