package com.example.js_dynamicdatabase.helpers;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.BinaryOperator;

public class FilterQuery {
    public enum Binary{
        AND,
        OR,
        NOR,
        MATCH
    }
    JSONObject filterQuery;
    JSONObject projection;
    int limit;
    Binary binary;



    public FilterQuery(JSONObject filterQuery, JSONObject projection, int limit, Binary binary) {
        this.filterQuery = filterQuery;
        this.projection = projection;
        this.limit = limit;
        this.binary = binary;
    }

    public String getFilterQuery() {
        if(filterQuery.length()<1)
            return "{}";
        return filterQuery.toString();
    }
    public String getSelectionQuery() {
        if(projection.length()<1)
            return "{}";
        return projection.toString();
    }

    public int getLimit() {
        return limit;
    }
}
