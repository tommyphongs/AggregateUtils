package org.coding.aggregates.aggregations;

import com.coccoc.io.FileUtils;
import org.coding.aggregates.utils.Tracker;
import com.google.common.base.Preconditions;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Aggregator implements Callable<Tracker> {

    public static final Logger LOGGER = LoggerFactory.getLogger(Aggregator.class);
    public static final int NUM_LINE_TO_PRINT = 10_000;

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static final JsonParser JSON_PARSER = new JsonParser();

    private final List<BiConsumer<JsonObject, JsonObject>> handlers;
    private final Map<String, File> sources;
    private final String key;
    private final SupportKeyType keyType;
    private final Comparator<JsonObject> cmp;
    private final boolean isGzipped;
    private final File outputFile;
    private final File tmpFile;
    private final AtomicInteger counter;

    Aggregator(List<BiConsumer<JsonObject, JsonObject>> handlers, Map<String, File> sources,
               String key, SupportKeyType keyType, boolean isGzipped, File outputFile, File tmpFile) {
        this.handlers = handlers;
        this.sources = sources;
        this.key = key;
        this.keyType = keyType;
        this.isGzipped = isGzipped;
        this.outputFile = outputFile;
        this.tmpFile = tmpFile;
        this.counter = new AtomicInteger();
        this.cmp = (o1, o2) -> {
            Preconditions.checkArgument(o1.has(key), String.format("Object %s doesn't not contains key %s", o1, key));
            Preconditions.checkArgument(o2.has(key), String.format("Object %s doesn't not contains key %s", o2, key));
            return keyType.comparator.compare(o1.get(key), o2.get(key));
        };

    }

    static class JsonObjectBuffer {

        JsonObjectBuffer(BufferedReader r, String name, Comparator<JsonObject> cmp) throws IOException {
            this.name = name;
            this.iterator = r.lines().iterator();
            this.cmp = cmp;
            reload();
        }

        boolean empty() {
            return this.cache.size() == 0;
        }

        List<JsonObject> peek() {
            return this.cache;
        }

        List<JsonObject> poll() throws IOException {
            List<JsonObject> answer = this.cache;
            reload();
            return answer;
        }

        void reload() {
            this.cache = new ArrayList<>();
            if (lastJsonObj != null) {
                cache.add(lastJsonObj);
                lastJsonObj = null;
            }
            while (iterator.hasNext()) {
                counter++;
                JsonObject jsonObject = JSON_PARSER.parse(iterator.next()).getAsJsonObject();
                if (this.cache.size() > 0 && cmp.compare(this.cache.get(0), jsonObject) != 0) {
                    lastJsonObj = jsonObject;
                    break;
                }
                else {
                    this.cache.add(jsonObject);
                }
            }
        }

        private final Iterator<String> iterator;
        private List<JsonObject> cache;
        private JsonObject lastJsonObj;
        private final Comparator<JsonObject> cmp;
        private final String name;
        private int counter = 0;

    }

    public Tracker call() throws Exception {

        long start = System.currentTimeMillis();
        Tracker tracker = new Tracker();

        LOGGER.info("Aggregate data");
        LOGGER.info(String.format("output file %s", outputFile));
        if (tmpFile != null) {
            LOGGER.info(String.format("tmp file %s", tmpFile));
        }
        File outFile = tmpFile == null ? outputFile : tmpFile;

        List<Map.Entry<String, Integer>> counters = new ArrayList<>();
        List<Closeable> closeables = new ArrayList<>();
        PriorityQueue<JsonObjectBuffer> pq = new PriorityQueue<>((o1, o2) -> cmp.compare(o1.peek().get(0), o2.peek().get(0)));
        try {
            for (Map.Entry<String, File> entry : sources.entrySet()) {
                BufferedReader bufferedReader;
                if (isGzipped) {
                    bufferedReader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(entry.getValue()))));
                } else {
                    bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(entry.getValue())));
                }
                closeables.add(bufferedReader);
                JsonObjectBuffer bfb = new JsonObjectBuffer(bufferedReader, entry.getKey(), cmp);
                if (!bfb.empty()) {
                    pq.add(bfb);
                }
            }
            BufferedWriter bufferedWriter = isGzipped ? new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(
                    new FileOutputStream(outFile)
            ))) : new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
            closeables.add(bufferedWriter);
            JsonElement currentKeyValue = null;
            JsonObject currentJsonObjs = new JsonObject();
            if (pq.size() > 0) {
                JsonObjectBuffer bfb = pq.poll();
                List<JsonObject> jsonObjectList = bfb.poll();
                currentKeyValue = jsonObjectList.get(0).get(key);
                JsonArray jsonArray = new JsonArray();
                jsonObjectList.forEach(jsonObject -> jsonObject.remove(key));
                jsonObjectList.forEach(jsonArray::add);
                currentJsonObjs.add(bfb.name, jsonArray);
                if (!bfb.empty()) {
                    pq.add(bfb); // add it back
                }
                else {
                    counters.add(new AbstractMap.SimpleEntry<>(bfb.name, bfb.counter));
                }
            }
            while (pq.size() > 0) {
                JsonObjectBuffer bfb = pq.poll();
                List<JsonObject> jsonObjectList = bfb.poll();
                JsonElement keyValue = jsonObjectList.get(0).get(key);
                jsonObjectList.forEach(jsonObject -> jsonObject.remove(key));
                if (keyType.comparator.compare(keyValue, currentKeyValue) == 0) {
                    if (!currentJsonObjs.has(bfb.name)) {
                        currentJsonObjs.add(bfb.name, new JsonArray());
                    }
                } else {
                    handle(currentJsonObjs, bufferedWriter, currentKeyValue);
                    currentJsonObjs = new JsonObject();
                    currentJsonObjs.add(bfb.name, new JsonArray());
                }
                JsonArray array = currentJsonObjs.get(bfb.name).getAsJsonArray();
                jsonObjectList.forEach(array::add);
                currentKeyValue = keyValue;
                if (!bfb.empty()) {
                    pq.add(bfb); // add it back

                }
                else {
                    counters.add(new AbstractMap.SimpleEntry<>(bfb.name, bfb.counter));
                }
            }
            if (!currentJsonObjs.isJsonNull()) {
                handle(currentJsonObjs, bufferedWriter, currentKeyValue);
            }
            if (tmpFile != null) {
                outputFile.delete();
                FileUtils.renameTo(outFile, outputFile);
            }
            LOGGER.info("Stats: \n");
            counters.forEach(entry -> LOGGER.info(entry.toString()));
            long minutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - start);
            tracker.add("Time to aggregate data", minutes);
            return tracker;
        } finally {
            for (Closeable closeable : closeables) {
                closeable.close();
            }
        }
    }

    private void handle(JsonObject currentJsonObjs, BufferedWriter bufferedWriter,
                        JsonElement keyValue) throws IOException {
        JsonObject object2write = new JsonObject();
        counter.incrementAndGet();
        if (counter.get() % NUM_LINE_TO_PRINT == 0) {
            System.out.print(counter.get() + " -> ");
        }
        for (BiConsumer<JsonObject, JsonObject> handler : handlers) {
            handler.accept(currentJsonObjs, object2write);
        }
        object2write.add(key, keyValue);
        bufferedWriter.write(GSON.toJson(object2write));
        bufferedWriter.newLine();
    }

    public static Aggregator createAggregator(File outputFile, Map<String, File> sources,
                                       List<BiConsumer<JsonObject, JsonObject>> handlers, String key, boolean isGzip) {
        AggregatorBuilder builder = new AggregatorBuilder().setIsGzipped(isGzip).setKey(key)
                .setKeyType(SupportKeyType.LONG).setOutputFile(outputFile);
        sources.forEach(builder::addSource);
        handlers.forEach(builder::addLast);
        return builder.createAggregator();
    }

}
