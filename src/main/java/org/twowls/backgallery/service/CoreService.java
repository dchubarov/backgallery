package org.twowls.backgallery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.twowls.backgallery.model.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
@Service
public class CoreService {
    private static final Logger logger = LoggerFactory.getLogger(CoreService.class);
    private final ConcurrentMap<String, Equipped<?>> cache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    @Value("${storage.data-dir}")
    private String dataDir;

    public Equipped<RealmDescriptor> findRealm(String realmName) {
        return cached(realmName, RealmDescriptor.class, (name) -> {
            try {
                Path configPath = Paths.get(dataDir, name, RealmDescriptor.CONFIG).toAbsolutePath();
                return Equipped.of(objectMapper.readValue(configPath.toFile(), RealmDescriptor.class), name);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public RealmAuthenticator authenticatorForRealm(RealmDescriptor realm) {
        return SimpleTokenAuthenticator.INSTANCE;
    }

    public Equipped<CollectionDescriptor> findCollection(Named<RealmDescriptor> realm, String collectionName) {
        return cached(collectionName, CollectionDescriptor.class, (name) -> {
            try {
                Path configPath = Paths.get(dataDir, realm.name(), name, CollectionDescriptor.CONFIG).toAbsolutePath();
                return Equipped.of(objectMapper.readValue(configPath.toFile(), CollectionDescriptor.class), name)
                        .with("realm", realm);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T> Equipped<T> cached(String name, Class<T> clazz, Function<String, Equipped<?>> mapper) {
        String normalizedName = Named.normalize(name);
        Equipped<?> v = cache.computeIfAbsent(clazz.getName() + "$" + normalizedName, (key) -> {
            logger.debug("Loading instance of type {} named \"{}\" into cache.", clazz, normalizedName);
            return mapper.apply(normalizedName).with("loaded", System.nanoTime());
        });

        if (v != null && !v.bare().getClass().isAssignableFrom(clazz)) {
            throw new IllegalStateException("Incompatible cache value class.");
        }

        return (Equipped<T>) v;
    }

    private enum SimpleTokenAuthenticator implements RealmAuthenticator {
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
