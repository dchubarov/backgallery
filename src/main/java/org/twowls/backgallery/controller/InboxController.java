package org.twowls.backgallery.controller;

import org.apache.commons.lang3.StringUtils;
import org.hashids.Hashids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.twowls.backgallery.exception.ApiException;
import org.twowls.backgallery.exception.DataProcessingException;
import org.twowls.backgallery.exception.InvalidRequestException;
import org.twowls.backgallery.model.CollectionDescriptor;
import org.twowls.backgallery.model.RealmDescriptor;
import org.twowls.backgallery.model.UserOperation;
import org.twowls.backgallery.service.ContentService;
import org.twowls.backgallery.service.IndexingService;
import org.twowls.backgallery.utils.Equipped;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * The controller serving inbox, i.e the area of unpublished photos.
 *
 * <h4>Inbox URL scheme:</h4>
 * <dl>
 *  <dt>GET /{realm}/{collection}/inbox</dt><dd>list items currently in inbox</dd>
 *  <dt>PUT /{realm}/{collection}/inbox/pull</dt><dd>pull local file from server file system into inbox</dd>
 *  <dt>PUT /{realm}/{collection}/inbox/upload</dt><dd>upload file to inbox</dd>
 *  <dt>PUT /{realm}/{collection}/inbox/uploaded</dt><dd>internally handle pulled or uploaded file</dd>
 *  <dt>PUT /{realm}/{collection}/inbox/i/{imageId}</dt><dd>update image info in inbox</dd>
 *  <dt>GET /{realm}/{collection}/inbox/i/{imageId}</dt><dd>returns image info to client</dd>
 *  <dt>GET /{realm}/{collection}/inbox/i/{imageId}/original</dt><dd>TODO (?) return original image data</dd>
 *  <dt>GET /{realm}/{collection}/inbox/i/{imageId}/preview</dt><dd>TODO (?) return original image preview data</dd>
 *  <dt>DELETE /{realm}/{collection}/inbox/i/{imageId}</dt><dd>delete image from inbox</dd>
 * </dl>
 *
 * @author Dmitry Chubarov
 */
@RestController
@RequestMapping(value = AbstractAuthenticatingController.REQUEST_MAPPING_BASE + "/inbox")
public class InboxController extends AbstractAuthenticatingController {
    private static final Logger logger = LoggerFactory.getLogger(InboxController.class);
    private static final String ATTRIBUTE_PREFIX = InboxController.class.getName();
    private static final String TRANSIT_FILE_ATTR = ATTRIBUTE_PREFIX + ".transitFile";
    private static final String ORIGINAL_NAME_ATTR = ATTRIBUTE_PREFIX + ".originalName";
    private static final String TARGET_COLLECTION_ATTR = ATTRIBUTE_PREFIX + ".targetCollection";
    private static final String CONTENT_TYPE_ATTR = ATTRIBUTE_PREFIX + ".contentType";
    private static final String FILE_SIZE_ATTR = ATTRIBUTE_PREFIX + ".fileSize";
    private static final String IMAGE_ID_HEADER = "X-ImageId";

    private final IndexingService indexingService;

    @Autowired
    InboxController(ContentService contentService, IndexingService indexingService) {
        super(contentService);
        this.indexingService = requireNonNull(indexingService);
    }

    @PutMapping(value = "upload")
    public ModelAndView uploadFile(@RequestParam("file") MultipartFile file, WebRequest request) throws ApiException {
        return ifAuthorizedInCollection(UserOperation.UPLOAD_IMAGE, request, (coll) -> {
            logger.info("Uploaded {} [{}], {} byte(s)", file.getOriginalFilename(),
                    file.getContentType(), file.getSize());

            File tempFile;
            try {
                tempFile = Files.createTempFile("upload", ".tmp").toFile();
                file.transferTo(tempFile);
            } catch (IOException e) {
                throw new DataProcessingException("Failed to save uploaded data to a temporary file.", e);
            }

            logger.debug("Uploaded data saved to temporary file {}", tempFile);

            // save attributes and redirect
            request.setAttribute(TRANSIT_FILE_ATTR, tempFile, RequestAttributes.SCOPE_REQUEST);
            request.setAttribute(ORIGINAL_NAME_ATTR, file.getOriginalFilename(), RequestAttributes.SCOPE_REQUEST);
            request.setAttribute(TARGET_COLLECTION_ATTR, coll.name(), RequestAttributes.SCOPE_REQUEST);
            request.setAttribute(CONTENT_TYPE_ATTR, file.getContentType(), RequestAttributes.SCOPE_REQUEST);
            request.setAttribute(FILE_SIZE_ATTR, file.getSize(), RequestAttributes.SCOPE_REQUEST);

            return new ModelAndView("forward:uploaded");
        });
    }

    @PutMapping(value = "pull")
    public ModelAndView pullLocalFile(WebRequest request, @RequestParam String localPath) throws ApiException {
        return ifAuthorizedInCollection(UserOperation.UPLOAD_IMAGE, request, (coll) -> {
            // TODO double check, security considerations
            Path path = Paths.get(localPath);
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) || !Files.isReadable(path)) {
                throw new InvalidRequestException("Local file is not readable: " + path);
            }

            long size;
            try {
                size = Files.size(path);
            } catch (IOException e) {
                throw new DataProcessingException("Could not get file info: " + path, e);
            }

            // save attributes and redirect
            request.setAttribute(TRANSIT_FILE_ATTR, path.toFile(), RequestAttributes.SCOPE_REQUEST);
            request.setAttribute(ORIGINAL_NAME_ATTR, path.getFileName().toString(), RequestAttributes.SCOPE_REQUEST);
            request.setAttribute(TARGET_COLLECTION_ATTR, coll.name(), RequestAttributes.SCOPE_REQUEST);
            // TODO image content type
//            redirectAttributes.addFlashAttribute(CONTENT_TYPE_ATTR, file.getContentType());
            request.setAttribute(FILE_SIZE_ATTR, size, RequestAttributes.SCOPE_REQUEST);

            return new ModelAndView("forward:uploaded");
        });
    }

    @PutMapping(value = "uploaded")
    public ModelAndView postUpload(@PathVariable String realmName, WebRequest request,
            HttpServletResponse response) throws ApiException {
        return ifAuthorizedInCollection(UserOperation.UPLOAD_IMAGE, request, (coll) -> {
            Equipped<RealmDescriptor> targetRealm = contentService.findRealm(
                    (String) coll.prop(ContentService.REALM_PROP));

            Equipped<CollectionDescriptor> targetColl = contentService.findCollection(
                    targetRealm, (String) request.getAttribute(TARGET_COLLECTION_ATTR, RequestAttributes.SCOPE_REQUEST));

            if (targetColl == null || !StringUtils.equals(coll.name(), targetColl.name())) {
                throw new InvalidRequestException("Current collection does not match upload target: " +
                        (targetColl != null ? targetColl.name() : null));
            }

            // 1. create image-id
            // 2. check realm & collection match
            // 3. check file info matches
            // 4. move file to inbox
            // 5. create image descriptor

            Hashids h = new Hashids("4NtzCVXAnELUvezek3cN7jaXRPKV", 5, "ab0cd1ef2hi3jk4mn5pq6rs7tu8vx9yz");
            String newId = h.encode((Objects.hash(realmName, coll.name(), System.currentTimeMillis()) >> 7) & 0xffff);
            logger.info("Create new id: {}", newId);

            response.addHeader(IMAGE_ID_HEADER, newId);
            return new ModelAndView("redirect:i/" + newId);
        });
    }

    @GetMapping(value = "i/{imageId}")
    public @ResponseBody String imageInfo(WebRequest request, @PathVariable String imageId) throws ApiException {
        // TODO actually provide image info to client
        return ifAuthorizedInCollection(UserOperation.GET_IMAGE_INFO, request, (coll) -> imageId);
    }
}
