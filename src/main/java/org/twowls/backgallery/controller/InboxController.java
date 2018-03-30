package org.twowls.backgallery.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.twowls.backgallery.model.RealmDescriptor;
import org.twowls.backgallery.model.RealmOperation;
import org.twowls.backgallery.service.CoreService;
import org.twowls.backgallery.utils.Equipped;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
@RestController
@RequestMapping(value = "/{realmName}/{collectionName}/inbox")
public class InboxController extends AbstractAuthenticatingController {
    private static final Logger logger = LoggerFactory.getLogger(InboxController.class);

    @Autowired
    InboxController(CoreService coreService) {
        super(coreService);
    }

    @PutMapping(value = "upload")
    public ModelAndView handleUpload(@PathVariable String realmName, @PathVariable String collectionName,
            @RequestParam("file") MultipartFile uploadedFile, HttpServletRequest servletRequest,
            HttpServletResponse servletResponse, RedirectAttributes redirectAttributes) {

        Equipped<RealmDescriptor> realm;
        if ((realm = authorizedRealm(realmName, RealmOperation.UPLOAD_IMAGE, servletRequest, servletResponse)) != null) {
            logger.info("Uploaded {}, {} byte(s)", uploadedFile.getOriginalFilename(), uploadedFile.getSize());
            try {
                Path tempFile = Files.createTempFile("uploaded", ".tmp");
                uploadedFile.transferTo(tempFile.toFile());

                redirectAttributes.addFlashAttribute("transmission-file", tempFile);
                redirectAttributes.addFlashAttribute("target-collection", collectionName);
                logger.debug("Uploaded data saved to {}", tempFile);
            } catch (IOException e) {
                logger.error("Could not save uploaded data to temporary file.", e);
            }

            redirectAttributes.addFlashAttribute("original-name", uploadedFile.getOriginalFilename());
            return new ModelAndView("redirect:uploaded");
        }

        return null;
    }

    @GetMapping(value = "uploaded")
    public void postUpload(@PathVariable String realmName, @PathVariable String collectionName,
            @ModelAttribute("original-name") String originalName) {
        logger.info(originalName);
    }
}