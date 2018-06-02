package org.twowls.backgallery.service;

import org.springframework.web.context.request.WebRequest;
import org.twowls.backgallery.model.RealmDescriptor;
import org.twowls.backgallery.model.UserOperation;

/**
 * Represents a service that authenticates web request for requested operation in a realm.
 *
 * @author Dmitry Chubarov
 */
public interface RealmAuthenticator {

    /**
     * Checks if request is authorized to perform requested operation in a realm.
     *
     * @param requestedOp the requested operation
     * @param realm the realm to perform operation in
     * @param request the request
     * @return {@code true} if authorized, otherwise {@code false}
     */
    boolean authorized(UserOperation requestedOp, RealmDescriptor realm, WebRequest request);
}
