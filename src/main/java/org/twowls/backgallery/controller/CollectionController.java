package org.twowls.backgallery.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twowls.backgallery.model.*;
import org.twowls.backgallery.service.RealmAuthenticator;
import org.twowls.backgallery.service.CoreService;
import org.twowls.backgallery.utils.Equipped;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
@RestController
public class CollectionController {
    private static final Logger logger = LoggerFactory.getLogger(CollectionController.class);
    private final CoreService coreService;

    @Autowired
    CollectionController(CoreService coreService) {
        this.coreService = Objects.requireNonNull(coreService);
    }

    @GetMapping(value = "/{realmName}/{collectionName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionDescriptor>
    collectionInfo(@PathVariable String realmName, @PathVariable String collectionName,
            HttpServletRequest servletRequest, HttpServletResponse servletResponse) {

        Equipped<RealmDescriptor> realm;
        if ((realm = authorizedRealm(realmName, RealmOperation.GET_COLLECTION_INFO,
                servletRequest, servletResponse)) != null) {

            Equipped<CollectionDescriptor> coll;
            if ((coll = coreService.findCollection(realm, collectionName)) == null) {
                logger.warn("Could not access collection \"{}.{}\"", realm.name(), collectionName);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(coll.bare());
        }

        return null;
    }

    private Equipped<RealmDescriptor> authorizedRealm(String realmName, RealmOperation requestedOp,
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
