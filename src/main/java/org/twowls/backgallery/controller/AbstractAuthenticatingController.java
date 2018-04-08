package org.twowls.backgallery.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.twowls.backgallery.model.CollectionDescriptor;
import org.twowls.backgallery.model.Descriptor;
import org.twowls.backgallery.model.RealmDescriptor;
import org.twowls.backgallery.model.RealmOperation;
import org.twowls.backgallery.service.CoreService;
import org.twowls.backgallery.service.RealmAuthenticator;
import org.twowls.backgallery.utils.Equipped;
import org.twowls.backgallery.utils.ThrowingFunction;

import java.util.Map;
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

    <R> R doAuthorized(RealmOperation requestedOp, WebRequest request,
            ThrowingFunction<Equipped<? extends Descriptor>, R, ? extends Exception> handler) {
        Map m = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, WebRequest.SCOPE_REQUEST);
        String realmName = null, collectionName = null;

        if (m != null) {
            realmName = (String) m.get("realmName");
            collectionName = (String) m.get("collectionName");
        }

        try {
            if (!StringUtils.isBlank(realmName)) {
                Equipped<RealmDescriptor> realm = coreService.findRealm(realmName);
                RealmAuthenticator authenticator = coreService.authenticatorForRealm(realm.bare());
                if (authenticator.authorized(requestedOp, realm.bare(), request)) {
                    if (StringUtils.isBlank(collectionName)) {
                        return handler.apply(realm);
                    } else {
                        Equipped<CollectionDescriptor> coll = coreService.findCollection(realm, collectionName);
                        if (coll != null) {
                            return handler.apply(coll);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // TODO not ise
            throw new IllegalStateException("Error", e);
        }

        // TODO not ise
        throw new IllegalStateException("Cannot do authorized");
    }
}
