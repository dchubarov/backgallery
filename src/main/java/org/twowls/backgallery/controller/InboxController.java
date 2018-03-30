package org.twowls.backgallery.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.twowls.backgallery.model.RealmOperation;
import org.twowls.backgallery.service.CoreService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

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
    InboxController(CoreService coreService) {
        super(coreService);
    }

    @PutMapping(value = "upload")
    public ModelAndView handleUpload(@PathVariable String realmName, @PathVariable String collectionName,
            @RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        if (authorizedCollection(RealmOperation.UPLOAD_IMAGE, realmName, collectionName, request, response) == null) {
            logger.error("Could not upload to collection {}.{}", realmName, collectionName);
            return null;
        }

        logger.info("Uploaded {} [{}], {} byte(s)", file.getOriginalFilename(),
                file.getContentType(), file.getSize());

        try {
            // transfer uploaded content to temporary file
            File tempFile = Files.createTempFile("upload", ".tmp").toFile();
            file.transferTo(tempFile);

            redirectAttributes.addFlashAttribute(TRANSIT_FILE_ATTR, tempFile);
            logger.debug("Uploaded data saved to temporary file {}", tempFile);
        } catch (IOException e) {
            logger.error("Could not save uploaded data to temporary file.", e);
        }

        // save attributes and redirect
        redirectAttributes.addFlashAttribute(ORIGINAL_NAME_ATTR, file.getOriginalFilename());
        redirectAttributes.addFlashAttribute(TARGET_COLLECTION_ATTR, collectionName);
        redirectAttributes.addFlashAttribute(CONTENT_TYPE_ATTR, file.getContentType());
        redirectAttributes.addFlashAttribute(FILE_SIZE_ATTR, file.getSize());
        return new ModelAndView("redirect:uploaded");
    }

    @GetMapping(value = "uploaded")
    public void postUpload(@PathVariable String realmName, @PathVariable String collectionName,
            HttpServletRequest request, HttpServletResponse response) {
        Map<String, ?> attr = RequestContextUtils.getInputFlashMap(request);
        logger.info(attr.toString());
    }
}
