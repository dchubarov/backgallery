package org.twowls.backgallery.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.twowls.backgallery.model.CollectionDescriptor;
import org.twowls.backgallery.model.RealmDescriptor;
import org.twowls.backgallery.model.UserOperation;
import org.twowls.backgallery.service.CoreService;
import org.twowls.backgallery.service.RealmAuthenticator;
import org.twowls.backgallery.utils.Equipped;
import org.twowls.backgallery.utils.ThrowingFunction;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
abstract class AbstractAuthenticatingController {
    private static final String REALM_NAME_PATH_VARIABLE = "realmName";
    private static final String COLLECTION_NAME_PATH_VARIABLE = "collectionName";

    private static final Logger logger = LoggerFactory.getLogger(AbstractAuthenticatingController.class);
    final CoreService coreService;

    AbstractAuthenticatingController(CoreService coreService) {
        this.coreService = Objects.requireNonNull(coreService);
    }

    /**
     * TODO no throws Exception, no ISEs
     *
     * @param requestedOp
     * @param request
     * @param handler
     * @param <R>
     * @return
     */
    <R> Optional<R> ifAuthorizedInCollection(UserOperation requestedOp, WebRequest request,
            ThrowingFunction<Equipped<? extends CollectionDescriptor>, R, ? extends Exception> handler)
            throws Exception {

        // attempt to obtain path variables of the current request
        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
                WebRequest.SCOPE_REQUEST);

        String realmName = null, collectionName = null;
        if (pathVariables != null) {
            realmName = (String) pathVariables.get(REALM_NAME_PATH_VARIABLE);
            collectionName = (String) pathVariables.get(COLLECTION_NAME_PATH_VARIABLE);
        }

        if (StringUtils.isAnyBlank(realmName, collectionName)) {
            // TODO not ISE
            throw new IllegalStateException("Realm and (or) collection undefined.");
        }

        Equipped<RealmDescriptor> realm = coreService.findRealm(realmName);
        RealmAuthenticator authenticator = coreService.authenticatorForRealm(realm.bare());
        if (authenticator.authorized(requestedOp, realm.bare(), request)) {
            Equipped<CollectionDescriptor> coll = coreService.findCollection(realm, collectionName);
            return Optional.ofNullable(handler.apply(coll));
        }

        return Optional.empty();
    }
}
