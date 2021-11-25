package org.coding.aggregates.aggregations;

import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class AggregatorBuilder {
    private List<BiConsumer<JsonObject, JsonObject>> handlers = new ArrayList<>();
    private Map<String, File> sources = new HashMap<>();
    private String key;
    private SupportKeyType keyType;
    private boolean isGzipped;
    private File outputFile;
    private File tmpFile;

    public AggregatorBuilder setTmpFile(File tmpFile) {
        this.tmpFile = tmpFile;
        return this;
    }

    public AggregatorBuilder addLast(BiConsumer<JsonObject, JsonObject> handler) {
        this.handlers.add(handler);
        return this;
    }

    public AggregatorBuilder addSource(String name, File file) {
        this.sources.put(name, file);
        return this;
    }

    public AggregatorBuilder setKey(String key) {
        this.key = key;
        return this;
    }

    public AggregatorBuilder setKeyType(SupportKeyType keyType) {
        this.keyType = keyType;
        return this;
    }

    public AggregatorBuilder setIsGzipped(boolean isGzipped) {
        this.isGzipped = isGzipped;
        return this;
    }

    public AggregatorBuilder setOutputFile(File outputFile) {
        this.outputFile = outputFile;
        return this;
    }

    public Aggregator createAggregator() {
        return new Aggregator(handlers, sources, key, keyType, isGzipped, outputFile, tmpFile);
    }
}
