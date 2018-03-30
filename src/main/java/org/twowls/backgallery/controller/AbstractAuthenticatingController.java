package org.twowls.backgallery.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twowls.backgallery.model.CollectionDescriptor;
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
     * @param requestedOp the requested operation.
     * @param realmName the requested realm name.
     * @param servletRequest current request.
     * @param servletResponse current response.
     * @return an {@link Equipped}-wrapped {@link RealmDescriptor} instance or {@code null}
     *  if requested realm could not be found/authenticated.
     */
    Equipped<RealmDescriptor> authorizedRealm(RealmOperation requestedOp, String realmName,
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

            authorized = authenticator.authorized(requestedOp, realm, servletRequest);
        }

        if (!authorized) {
            logger.warn("Could not authorize with realm \"{}\" [{}] for operation {}.",
                    realm.description(), namedRealm.name(), requestedOp);
            servletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        return namedRealm;
    }

    Equipped<CollectionDescriptor> authorizedCollection(RealmOperation requestedOp, String realmName,
            String collectionName, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        Equipped<RealmDescriptor> realm;
        if ((realm = authorizedRealm(requestedOp, realmName, servletRequest, servletResponse)) != null) {
            try {
                return coreService.findCollection(realm, collectionName);
            } catch (Exception e) {
                logger.warn("Collection {}.{} not found.", realmName, collectionName);
                servletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        return null;
    }
}
