package org.coding.aggregates.utils;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.json.GsonJsonParser;

class UtilsTest {

    @Test
    public void parseDictStringTest() {
        String dictString = "{\"key1\":\"value1\", \"key2\":\"value2\"}";
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(dictString).getAsJsonObject();
        System.out.println(jsonObject);
    }

}