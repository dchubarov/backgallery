package org.twowls.backgallery.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.twowls.backgallery.model.CollectionDescriptor;
import org.twowls.backgallery.model.RealmDescriptor;
import org.twowls.backgallery.model.RealmOperation;
import org.twowls.backgallery.service.CoreService;
import org.twowls.backgallery.utils.Equipped;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
}
