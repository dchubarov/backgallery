package org.twowls.backgallery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.twowls.backgallery.model.Named;
import org.twowls.backgallery.model.RealmDescriptor;
import org.twowls.backgallery.model.RealmOperation;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
@Service
public class RealmService {
    private static final Logger logger = LoggerFactory.getLogger(RealmService.class);
    private final ConcurrentMap<String, RealmDescriptor> realmCache = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    @Value("${storage.data-dir}")
    private String dataDir;

    public RealmDescriptor findByName(String realmName) {
        return realmCache.computeIfAbsent(Named.normalize(realmName), (name) -> {
            try {
                RealmDescriptor r = mapper.readValue(configFile(name), RealmDescriptor.class);
                logger.debug("Loading info for the first time for realm '{}'.", name);
                return r;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public RealmAuthenticator authenticatorFor(RealmDescriptor realm) {
        return SecurityTokenAuthenticator.INSTANCE;
    }

    private File configFile(String realmName) throws IOException {
        Path configPath = Paths.get(dataDir, realmName, RealmDescriptor.CONFIG).toAbsolutePath();
        if (Files.isRegularFile(configPath, LinkOption.NOFOLLOW_LINKS)) {
            return configPath.toFile();
        }

        throw new IOException("Invalid realm: " + realmName);
    }

    private enum SecurityTokenAuthenticator implements RealmAuthenticator {
        INSTANCE;

        @Override
        public boolean authorized(RealmDescriptor realm, RealmOperation requestedOp, HttpServletRequest request) {
            String inboundToken = request.getHeader("X-AccessToken");
            if (StringUtils.isBlank(inboundToken)) {
                inboundToken = request.getParameter("token");
            }

            if (StringUtils.isBlank(inboundToken)) {
                logger.debug("No inbound token in request.");
            }

            return StringUtils.equals(StringUtils.trimToEmpty(inboundToken), realm.securityToken());
        }
    }
}
