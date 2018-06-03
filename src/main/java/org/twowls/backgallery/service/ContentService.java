package org.twowls.backgallery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.WebRequest;
import org.twowls.backgallery.exception.ApiException;
import org.twowls.backgallery.exception.DataProcessingException;
import org.twowls.backgallery.model.CollectionDescriptor;
import org.twowls.backgallery.model.RealmDescriptor;
import org.twowls.backgallery.model.UserOperation;
import org.twowls.backgallery.model.json.CollectionDescriptorJson;
import org.twowls.backgallery.model.json.RealmDescriptorJson;
import org.twowls.backgallery.utils.Equipped;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.Objects;

/**
 * Represents a service providing access to data.
 *
 * @author Dmitry Chubarov
 */
@Service
public class ContentService {
    private static final String TOKEN_HEADER = "X-AccessToken";
    private static final String TOKEN_PARAM = "token";
    public static final String REALM_PROP = "realm";

    private static final Logger logger = LoggerFactory.getLogger(ContentService.class);
    private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    private final CacheService cache;
    private WatchService watcher;

    @Value("${backgallery.storage.data-dir}")
    private String dataDir;

    @Autowired
    ContentService(CacheService cache) {
        this.cache = Objects.requireNonNull(cache, "Cache service is required");
    }

    public Equipped<RealmDescriptor> findRealm(String realmName) throws ApiException {
        try {
            return cache.getOrCreate(realmName, RealmDescriptor.class, (name) -> {
                Path configPath = Paths.get(dataDir, name, RealmDescriptor.CONFIG).toAbsolutePath();
                try {
                    RealmDescriptorJson descriptor = objectMapper.readValue(configPath.toFile(), RealmDescriptorJson.class);
                    Paths.get(dataDir, name).register(watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
                    return descriptor;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (Exception e) {
            throw ApiException.logged(logger, "Error loading realm info: " +
                    realmName, e, DataProcessingException::new);
        }
    }

    public Equipped<CollectionDescriptor> findCollection(Equipped<RealmDescriptor> realm, String collectionName)
            throws ApiException {
        try {
            return cache.getOrCreate(collectionName, CollectionDescriptor.class, (name) -> {
                Path configPath = Paths.get(dataDir, realm.name(), name, CollectionDescriptor.CONFIG).toAbsolutePath();
                try {
                    CollectionDescriptorJson descriptor = objectMapper.readValue(configPath.toFile(), CollectionDescriptorJson.class);
                    Paths.get(dataDir, realm.name(), name).register(watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
                    return descriptor;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }).with(REALM_PROP, realm.name());
        } catch (Exception e) {
            throw ApiException.logged(logger, "Error loading collection info: '" + collectionName +
                    "' in realm '" + realm.name() + "'", e, DataProcessingException::new);
        }
    }

    public RealmAuthenticator authenticatorForRealm(@SuppressWarnings("unused") RealmDescriptor realm) {
        return SimpleTokenAuthenticator.INSTANCE;
    }

    @PostConstruct
    void startWatcher() {
        try {
            watcher = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            logger.warn("Could not initialize watching service on the data directory.", e);
        }

        new WatcherThread().start();
    }

    private enum SimpleTokenAuthenticator implements RealmAuthenticator {
        INSTANCE;

        @Override
        public boolean authorized(UserOperation requestedOp, RealmDescriptor realm, WebRequest request) {
            return checkToken(StringUtils.defaultIfBlank(request.getHeader(TOKEN_HEADER),
                    request.getParameter(TOKEN_PARAM)), realm);
        }

        private boolean checkToken(String inboundToken, RealmDescriptor realm) {
            if (StringUtils.isBlank(inboundToken)) {
                logger.debug("No inbound token in request.");
            }

            return StringUtils.equals(StringUtils.trimToEmpty(inboundToken), realm.securityToken());
        }
    }

    private class WatcherThread extends Thread {

        WatcherThread() {
            setName("watcher-daemon");
            setDaemon(true);
        }

        @Override
        public void run() {
            boolean interrupted = false;
            do {
                WatchKey key = null;
                try {
                    key = watcher.take();
                } catch (InterruptedException e) {
                    interrupted = true;
                }

                if (key != null && key.isValid()) {
                    Path dir = (Path) key.watchable();
                    key.pollEvents().forEach(e -> {
                        if (e.context() instanceof Path) {
                            Path modifiedEntry = (Path) e.context();
                            logger.info("Watch service event: {} {}/{}", e.kind(), dir, modifiedEntry);
                        }
                    });

                    key.reset();
                }
            } while (!interrupted);
        }
    }
}
