package org.twowls.backgallery.service;

import org.springframework.web.context.request.WebRequest;
import org.twowls.backgallery.model.RealmDescriptor;
import org.twowls.backgallery.model.UserOperation;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
public interface RealmAuthenticator {

    boolean authorized(UserOperation requestedOp, RealmDescriptor realm, WebRequest request);

}
