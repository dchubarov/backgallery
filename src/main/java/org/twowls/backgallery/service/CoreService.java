package org.twowls.backgallery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.twowls.backgallery.model.*;
import org.twowls.backgallery.model.json.CollectionDescriptorJson;
import org.twowls.backgallery.model.json.RealmDescriptorJson;
import org.twowls.backgallery.utils.Equipped;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
@Service
public class CoreService {
    private static final Logger logger = LoggerFactory.getLogger(CoreService.class);
    private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    private final CacheService cache;

    @Value("${backgallery.storage.data-dir}")
    private String dataDir;

    @Autowired
    CoreService(CacheService cache) {
        this.cache = Objects.requireNonNull(cache, "Cache service is required");
    }

    public Equipped<RealmDescriptor> findRealm(String realmName) {
        return cache.getOrCreate(realmName, RealmDescriptor.class, (name) -> {
            Path configPath = Paths.get(dataDir, name, RealmDescriptor.CONFIG).toAbsolutePath();
            try {
                return objectMapper.readValue(configPath.toFile(), RealmDescriptorJson.class);
            } catch (IOException e) {
                logger.error("Error reading realm configuration: " + name, e);
                throw new UncheckedIOException(e);
            }
        });
    }

    public RealmAuthenticator authenticatorForRealm(RealmDescriptor realm) {
        return SimpleTokenAuthenticator.INSTANCE;
    }

    public Equipped<CollectionDescriptor> findCollection(Equipped<RealmDescriptor> realm, String collectionName) {
        return cache.getOrCreate(collectionName, CollectionDescriptor.class, (name) -> {
            Path configPath = Paths.get(dataDir, realm.name(), name, CollectionDescriptor.CONFIG).toAbsolutePath();
            try {
                return objectMapper.readValue(configPath.toFile(), CollectionDescriptorJson.class);
            } catch (IOException e) {
                logger.error("Error reading collection configuration: " + realm.name() + "." + name, e);
                throw new UncheckedIOException(e);
            }
        }).with("realm", realm);
    }

    private enum SimpleTokenAuthenticator implements RealmAuthenticator {
        INSTANCE;

        @Override
        public boolean authorized(RealmOperation requestedOp, RealmDescriptor realm, HttpServletRequest request) {
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
