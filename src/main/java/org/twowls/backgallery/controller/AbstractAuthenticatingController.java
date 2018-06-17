package org.twowls.backgallery.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.twowls.backgallery.exception.ApiException;
import org.twowls.backgallery.exception.InvalidRequestException;
import org.twowls.backgallery.exception.UnauthorizedException;
import org.twowls.backgallery.model.CollectionDescriptor;
import org.twowls.backgallery.model.RealmDescriptor;
import org.twowls.backgallery.model.UserOperation;
import org.twowls.backgallery.service.ContentService;
import org.twowls.backgallery.service.RealmAuthenticator;
import org.twowls.backgallery.utils.Equipped;
import org.twowls.backgallery.utils.ThrowingFunction;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a controller whose methods require authentication.
 *
 * @author Dmitry Chubarov
 */
abstract class AbstractAuthenticatingController {
    private static final String REALM_PATH_VARIABLE = "realmName";
    private static final String COLLECTION_PATH_VARIABLE = "collectionName";
    static final String REQUEST_MAPPING_BASE = "/{" + REALM_PATH_VARIABLE + "}/{" + COLLECTION_PATH_VARIABLE + "}";

    private static final Logger logger = LoggerFactory.getLogger(AbstractAuthenticatingController.class);
    final ContentService contentService;

    AbstractAuthenticatingController(ContentService contentService) {
        this.contentService = Objects.requireNonNull(contentService);
    }

    /**
     * Invokes a {@code handler} function if and only if current session is authorized
     * to perform requested operation in the specified realm and collection.
     *
     * @param requestedOperation the requested operation.
     * @param request the request, not {@code null}.
     * @param handler the handler function, not {@code null}.
     * @param <R> type of {@code handler}'s return value.
     * @return an {@code Optional} that either containing a value returned by the {@code handler},
     *  or is empty if requested operation is not permitted.
     */
    <R> R ifAuthorizedInCollection(UserOperation requestedOperation, WebRequest request,
            ThrowingFunction<Equipped<? extends CollectionDescriptor>, R, ? extends ApiException> handler)
            throws ApiException {

        // attempt to obtain path variables of the current request
        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
                WebRequest.SCOPE_REQUEST);

        String realmName = null, collectionName = null;
        if (pathVariables != null) {
            realmName = (String) pathVariables.get(REALM_PATH_VARIABLE);
            collectionName = (String) pathVariables.get(COLLECTION_PATH_VARIABLE);
        }

        if (StringUtils.isAnyBlank(realmName, collectionName)) {
            throw ApiException.logged(logger, "Realm and (or) collection undefined",
                    null, InvalidRequestException::new);
        }

        Equipped<RealmDescriptor> realm = contentService.findRealm(realmName);
        RealmAuthenticator authenticator = contentService.authenticatorForRealm(realm.bare());
        if (authenticator.authorized(requestedOperation, realm.bare(), request)) {
            Equipped<CollectionDescriptor> coll = contentService.findCollection(realm, collectionName);
            return handler.apply(coll);
        }

        throw ApiException.logged(logger, "Not authorized for " + requestedOperation,
                null, UnauthorizedException::new);
    }
}
