package org.twowls.backgallery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.FSDirectory;
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

import static org.twowls.backgallery.utils.Named.compositeName;

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
            return cache.getOrCreate(realmName, RealmDescriptor.class, (cacheKey) -> {
                Path configPath = Paths.get(dataDir, realmName, RealmDescriptor.CONFIG).toAbsolutePath();
                try {
                    // load realm descriptor file into a model bean
                    RealmDescriptorJson bean = objectMapper.readValue(configPath.toFile(),
                            RealmDescriptorJson.class);

                    // register realm directory with file system watcher
                    Paths.get(dataDir, realmName).register(
                            watcher,
                            StandardWatchEventKinds.ENTRY_MODIFY,
                            StandardWatchEventKinds.ENTRY_DELETE);

                    return bean;
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
            return cache.getOrCreate(compositeName(realm.name(), collectionName), collectionName, CollectionDescriptor.class, (cacheKey) -> {
                Path configPath = Paths.get(dataDir, realm.name(), collectionName, CollectionDescriptor.CONFIG).toAbsolutePath();
                try {
                    // load collection descriptor file to a model bean
                    CollectionDescriptorJson bean = objectMapper.readValue(configPath.toFile(),
                            CollectionDescriptorJson.class);

                    // register collection directory with file system watcher
                    Paths.get(dataDir, realm.name(), collectionName).register(
                            watcher,
                            StandardWatchEventKinds.ENTRY_MODIFY,
                            StandardWatchEventKinds.ENTRY_DELETE);

                    return bean;
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

    public CollectionIndexer collectionIndexer(Equipped<? extends CollectionDescriptor> coll) {
        return new LuceneIndexer(coll);
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
                        Path modifiedEntry = (Path) e.context();
                        logger.debug("Watch service event: {} {}/{}", e.kind(), dir, modifiedEntry);

                        if (modifiedEntry.endsWith(RealmDescriptor.CONFIG)) {
                            if (dir.getNameCount() > 0) {
                                cache.evict(dir.getName(dir.getNameCount() - 1).toString(), RealmDescriptor.class);
                            }
                        } else if (modifiedEntry.endsWith(CollectionDescriptor.CONFIG)) {
                            if (dir.getNameCount() > 1) {
                                cache.evict(compositeName(dir.getName(dir.getNameCount() - 2).toString(),
                                        dir.getName(dir.getNameCount() - 1).toString()), CollectionDescriptor.class);
                            }
                        }
                    });

                    key.reset();
                }
            } while (!interrupted);
        }
    }

    private class LuceneIndexer implements CollectionIndexer {
        private static final String INDEX_DIR = "index";
        private final CollectionDescriptor coll;
        private final String collectionName;
        private final String realmName;
        private IndexWriter indexWriter;

        LuceneIndexer(Equipped<? extends CollectionDescriptor> collEquipped) {
            this.coll = collEquipped.bare();
            this.collectionName = collEquipped.name();
            this.realmName = (String) collEquipped.prop(REALM_PROP);
        }

        @Override
        public boolean hasId(String id) {
            try (IndexReader indexReader = DirectoryReader.open(defaultIndexWriter())) {
                IndexSearcher indexSearcher = new IndexSearcher(indexReader);
                Query query = new TermQuery(new Term("id", id));
                return (indexSearcher.search(query, 1).totalHits > 0);
            } catch (IOException e) {
                logger.warn("Open index failed.", e);
            }
            return false;
        }

        @Override
        public void close() {
            if (indexWriter != null) {
                try {
                    indexWriter.close();
                } catch (IOException e) {
                    logger.warn("Could not close default index writer", e);
                } finally {
                    indexWriter = null;
                }
            }
        }

        private IndexWriter defaultIndexWriter() throws IOException {
            if (indexWriter == null) {
                FSDirectory indexDir = FSDirectory.open(Paths.get(dataDir, realmName, collectionName, INDEX_DIR));
                IndexWriterConfig indexConfig = new IndexWriterConfig(new StandardAnalyzer());
                indexConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                //indexConfig.setRAMBufferSizeMB(256.0);
                indexWriter = new IndexWriter(indexDir, indexConfig);
            }
            return indexWriter;
        }
    }
}
