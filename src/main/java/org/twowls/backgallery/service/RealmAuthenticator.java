package org.twowls.backgallery.service;

import org.twowls.backgallery.model.RealmDescriptor;
import org.twowls.backgallery.model.RealmOperation;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
public interface RealmAuthenticator {

    boolean authorized(RealmDescriptor realm, RealmOperation requestedOp, HttpServletRequest request);

}
