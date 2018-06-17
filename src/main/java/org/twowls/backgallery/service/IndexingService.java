package org.twowls.backgallery.service;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.twowls.backgallery.model.CollectionDescriptor;
import org.twowls.backgallery.utils.Equipped;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents a service providing fast search capabilities using index.
 *
 * @author Dmitry Chubarov
 */
@Service
public class IndexingService {
    private static final Logger logger = LoggerFactory.getLogger(IndexingService.class);

    @Value("${backgallery.storage.data-dir}")
    private String dataDir;

    IndexWriter collectionIndexer(Equipped<CollectionDescriptor> coll) throws IOException {
        Path indexPath = Paths.get(dataDir, coll.prop(ContentService.REALM_PROP, String.class), coll.name(), "index");
        logger.info("Initializing collection index at path: " + indexPath.toAbsolutePath());

        IndexWriterConfig indexConfig = new IndexWriterConfig(new StandardAnalyzer());
        indexConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        //indexWriterConfig.setRAMBufferSizeMB(256.0);

        Directory indexDir = FSDirectory.open(indexPath);
        return new IndexWriter(indexDir, indexConfig);
    }
}
