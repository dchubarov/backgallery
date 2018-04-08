package org.twowls.backgallery.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.twowls.backgallery.model.CollectionDescriptor;
import org.twowls.backgallery.model.RealmOperation;
import org.twowls.backgallery.service.CoreService;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
@RestController
public class CollectionController extends AbstractAuthenticatingController {
    private static final Logger logger = LoggerFactory.getLogger(CollectionController.class);

    @Autowired
    CollectionController(CoreService coreService) {
        super(coreService);
    }

    @GetMapping(value = "/{realmName}/{collectionName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionDescriptor>
    collectionInfo(WebRequest request) {
        return doAuthorized(RealmOperation.GET_COLLECTION_INFO, request, (descriptor) ->
                ResponseEntity.ok(descriptor.bare(CollectionDescriptor.class)));
    }
}
