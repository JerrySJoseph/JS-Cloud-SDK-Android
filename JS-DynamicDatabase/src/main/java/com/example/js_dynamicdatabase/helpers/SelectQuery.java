package com.example.js_dynamicdatabase.helpers;

import org.json.JSONObject;

public class SelectQuery {
    JSONObject fields;

    public SelectQuery(JSONObject fields) {
        this.fields = fields;
    }

    public String getQuery()
    {
        return fields.toString();
    }
}
