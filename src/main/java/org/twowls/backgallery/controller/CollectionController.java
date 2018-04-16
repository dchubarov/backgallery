package org.twowls.backgallery.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.twowls.backgallery.exception.ApiException;
import org.twowls.backgallery.exception.UnauthorizedException;
import org.twowls.backgallery.model.CollectionDescriptor;
import org.twowls.backgallery.model.UserOperation;
import org.twowls.backgallery.service.ContentService;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
@RestController
@RequestMapping(value = AbstractAuthenticatingController.REQUEST_MAPPING_BASE)
public class CollectionController extends AbstractAuthenticatingController {
    private static final Logger logger = LoggerFactory.getLogger(CollectionController.class);

    @Autowired
    CollectionController(ContentService contentService) {
        super(contentService);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionDescriptor>
    collectionInfo(WebRequest request) throws ApiException {
        return ifAuthorizedInCollection(UserOperation.GET_COLLECTION_INFO, request,
                (coll) -> ResponseEntity.ok(coll.bare(CollectionDescriptor.class)))
                .orElseThrow(UnauthorizedException::new);
    }
}
