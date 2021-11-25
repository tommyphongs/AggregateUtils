/*
 * @created 14/08/2020 - 12:15
 * @project uda
 * @author phongnh@coccoc.com
 */

package org.coding.aggregates;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.cli.*;
import org.coding.aggregates.aggregations.Aggregator;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * This is a sample to use aggregates package
 * The handlers only copy data from the sources to destination, but
 * we can add handler to add business logic
 */
public class AggregateSample {

    public static void main(String[] args) throws Exception {

        org.apache.commons.cli.Options options = new Options();
        options.addOption(Option.builder().longOpt("output").required(false).hasArg().build())
                .addOption(Option.builder().longOpt("key").required(false).hasArg().build())
                .addOption(Option.builder().longOpt("sources").required(false).hasArg().build())
                .addOption(Option.builder().longOpt("is_gzip").required(false).build())
        ;
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String output = cmd.getOptionValue("output");
        String key = cmd.getOptionValue("key");
        String sources = cmd.getOptionValue("sources");
        boolean isGzip = cmd.hasOption("is_gzip");
        JsonObject jsonObject = new JsonParser().parse(sources).getAsJsonObject();
        Map<String, File> s = new HashMap<>();
        jsonObject.entrySet().forEach(stringJsonElementEntry -> {
            s.put(stringJsonElementEntry.getKey(), Paths.get(stringJsonElementEntry.getValue().getAsString()).toFile());
        });
        Aggregator aggregator = Aggregator.createAggregator(Paths.get(output).toFile(), s,
                List.of((jsonObject1, jsonObject2) -> jsonObject2 = jsonObject1.deepCopy()), key, isGzip);
        // We can run multiple thread to improve performance
        aggregator.call();

    }

}
