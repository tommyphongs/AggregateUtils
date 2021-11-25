package org.coding.aggregates.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


public class Tracker {

    private final JsonObject data;

    public Tracker() {
        this.data = new JsonObject();
    }

    public void add(String name, Number value) {
        data.add(name, new JsonPrimitive(value));
    }

    @Override
    public String toString() {
        return new Gson().toJson(data);
    }

}
