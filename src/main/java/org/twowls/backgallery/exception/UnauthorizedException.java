package org.twowls.backgallery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends ApiException {
    public UnauthorizedException() {
        super();
    }
}
