package org.twowls.backgallery.service;

import java.io.Closeable;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
public interface CollectionIndexer extends Closeable {

    boolean hasId(String id);

    @Override
    void close();
}
