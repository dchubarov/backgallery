package org.twowls.backgallery.service;

import org.springframework.web.context.request.WebRequest;
import org.twowls.backgallery.model.RealmDescriptor;
import org.twowls.backgallery.model.RealmOperation;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
public interface RealmAuthenticator {

    boolean authorized(RealmOperation requestedOp, RealmDescriptor realm, WebRequest request);

}
