package org.twowls.backgallery.controller;

import org.apache.commons.lang3.StringUtils;
import org.hashids.Hashids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.twowls.backgallery.model.CollectionDescriptor;
import org.twowls.backgallery.model.RealmDescriptor;
import org.twowls.backgallery.model.UserOperation;
import org.twowls.backgallery.service.ContentService;
import org.twowls.backgallery.utils.Equipped;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
@RestController
@RequestMapping(value = "/{realmName}/{collectionName}/inbox")
public class InboxController extends AbstractAuthenticatingController {
    private static final Logger logger = LoggerFactory.getLogger(InboxController.class);
    private static final String TRANSIT_FILE_ATTR = "transitFile";
    private static final String ORIGINAL_NAME_ATTR = "originalName";
    private static final String TARGET_COLLECTION_ATTR = "targetCollection";
    private static final String CONTENT_TYPE_ATTR = "contentType";
    private static final String FILE_SIZE_ATTR = "fileSize";

    @Autowired
    InboxController(ContentService contentService) {
        super(contentService);
    }

    // TODO no throws Exception, no ISE
    @PutMapping(value = "upload")
    public ModelAndView handleUpload(
            @RequestParam("file") MultipartFile file, WebRequest request,
            RedirectAttributes redirectAttributes) throws Exception {

        return ifAuthorized(UserOperation.UPLOAD_IMAGE, request, (coll) -> {
            logger.info("Uploaded {} [{}], {} byte(s)", file.getOriginalFilename(),
                    file.getContentType(), file.getSize());

            File tempFile = Files.createTempFile("upload", ".tmp").toFile();
            file.transferTo(tempFile);

            logger.debug("Uploaded data saved to temporary file {}", tempFile);

            // save attributes and redirect
            redirectAttributes.addFlashAttribute(TRANSIT_FILE_ATTR, tempFile);
            redirectAttributes.addFlashAttribute(ORIGINAL_NAME_ATTR, file.getOriginalFilename());
            redirectAttributes.addFlashAttribute(TARGET_COLLECTION_ATTR, coll.name());
            redirectAttributes.addFlashAttribute(CONTENT_TYPE_ATTR, file.getContentType());
            redirectAttributes.addFlashAttribute(FILE_SIZE_ATTR, file.getSize());

            return new ModelAndView("redirect:uploaded");
        }).orElseThrow(() -> new IllegalStateException("Not authorized"));
    }

    // TODO no throws Exception, no ISE
    @GetMapping(value = "uploaded")
    public void postUpload(@PathVariable String realmName, @PathVariable String collectionName, WebRequest request) throws Exception {
        ifAuthorized(UserOperation.UPLOAD_IMAGE, request, (coll) -> {
            Map attr = (Map) request.getAttribute(DispatcherServlet.INPUT_FLASH_MAP_ATTRIBUTE, WebRequest.SCOPE_REQUEST);
            logger.info(attr.toString());

            Equipped<CollectionDescriptor> targetColl = contentService.findCollection(
                    coll.equippedProp(ContentService.REALM_PROP, RealmDescriptor.class),
                    (String) attr.get(TARGET_COLLECTION_ATTR));

            if (targetColl == null || !StringUtils.equals(coll.name(), targetColl.name())) {
                logger.error("Image initially uploaded to another collection: " +
                        (targetColl != null ? targetColl.name() : "<unknown>"));
                throw new IllegalStateException("Invalid target coll");
            }

            Hashids h = new Hashids("4NtzCVXAnELUvezek3cN7jaXRPKV", 5, "abcdefhijkmnpqrstuvwxyz");
            logger.info("Create new id: {}", h.encode((Objects.hash(realmName, coll.name(),
                    System.currentTimeMillis()) >> 7) & 0xffff));

            return Boolean.TRUE;
        }).orElseThrow(() -> new IllegalStateException("Not authorized"));

        // 1. create image-id
        // 2. check realm & collection match
        // 3. check file info matches
        // 4. move file to inbox
        // 5. create image descriptor
    }
}
