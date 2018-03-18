package org.twowls.backgallery.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
public class CollectionDescriptor {
    public static final String CONFIG = "collection.yml";

    @JsonProperty
    private String description;

    @JsonProperty
    Map<String, Integer> sizes;
}
