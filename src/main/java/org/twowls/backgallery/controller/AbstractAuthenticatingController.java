package org.twowls.backgallery.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twowls.backgallery.model.RealmDescriptor;
import org.twowls.backgallery.model.RealmOperation;
import org.twowls.backgallery.service.CoreService;
import org.twowls.backgallery.service.RealmAuthenticator;
import org.twowls.backgallery.utils.Equipped;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
abstract class AbstractAuthenticatingController {
    private static final Logger logger = LoggerFactory.getLogger(AbstractAuthenticatingController.class);
    final CoreService coreService;

    AbstractAuthenticatingController(CoreService coreService) {
        this.coreService = Objects.requireNonNull(coreService);
    }

    /**
     * Returns requested realm's info ensuring authentication.
     *
     * @param realmName the requested realm name.
     * @param requestedOp the requested operation.
     * @param servletRequest current request.
     * @param servletResponse current response.
     * @return an {@link Equipped}-wrapped {@link RealmDescriptor} instance or {@code null}
     *  if requested realm could not be found/authenticated.
     */
    Equipped<RealmDescriptor> authorizedRealm(String realmName, RealmOperation requestedOp,
            HttpServletRequest servletRequest, HttpServletResponse servletResponse) {

        Equipped<RealmDescriptor> namedRealm;
        try {
            namedRealm = coreService.findRealm(realmName);
        } catch (Exception e) {
            logger.warn("Could not find realm \"" + realmName + "\".");
            servletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        boolean authorized = false;
        RealmDescriptor realm = namedRealm.bare();
        RealmAuthenticator authenticator = coreService.authenticatorForRealm(realm);
        if (authenticator != null) {
            logger.debug("Authenticating with realm \"{}\" [{}] using authenticator {}.",
                    realm.description(), namedRealm.name(), authenticator.getClass().getName());

            authorized = authenticator.authorized(realm, requestedOp, servletRequest);
        }

        if (!authorized) {
            logger.warn("Could not authorize with realm \"{}\" [{}] for operation {}.",
                    realm.description(), namedRealm.name(), requestedOp);
            servletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        return namedRealm;
    }
}
