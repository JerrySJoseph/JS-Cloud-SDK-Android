package com.example.js_dynamicdatabase.helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

public class FilterQueryBuilder {
    JSONObject filterQuery;
    JSONArray fields;
    FilterQuery.Binary binary;
    JSONObject projection;
    int limit=0;

    public FilterQueryBuilder(FilterQuery.Binary binary) {
        this.binary=binary;
        filterQuery=new JSONObject();
        fields=new JSONArray();
        projection=new JSONObject();
    }

    public FilterQueryBuilder selectOnly(String... args) throws JSONException {
        for(String arg:args)
           projection.put(arg,1);
        return this;
    }

    public FilterQueryBuilder setLimit(int limit)
    {
        this.limit=limit;
        return this;
    }

    public FilterQueryBuilder selectExclude(String... args) throws JSONException {
        for(String arg:args)
            projection.put(arg,0);
        return this;
    }

    public FilterQueryBuilder whereEquals(String field,Object value) throws JSONException {
        JSONObject thisField=new JSONObject();
        thisField.put(field,value);
        this.fields.put(thisField);
        return this;
    }
    public FilterQueryBuilder whereGreaterThan(String field,double value) throws JSONException {
       genComparisonObjects("$gt",field,value);
        return this;
    }
    public FilterQueryBuilder whereGreaterOrEqualTo(String field,double value) throws JSONException {
        genComparisonObjects("$gte",field,value);
        return this;
    }
    public FilterQueryBuilder whereLessThan(String field,double value) throws JSONException {
        genComparisonObjects("$lt",field,value);
        return this;
    }
    public FilterQueryBuilder whereLessOrEqualTo(String field,double value) throws JSONException {
        genComparisonObjects("$lte",field,value);
        return this;
    }
    public FilterQueryBuilder whereNotEqualTo(String field,Object value) throws JSONException {
        genComparisonObjects("$ne",field,value);
        return this;
    }
    public FilterQueryBuilder whereNoneIn(String field,Object... values) throws JSONException {
        genComparisonObjects("$nin",field,values);
        return this;
    }
    public FilterQueryBuilder whereAnyIn(String field,Object... values) throws JSONException {
        genComparisonObjects("$in",field,values);
        return this;
    }
    public FilterQueryBuilder whereRangeExcludes(String field,Object upperbound,Object lowerBound) throws JSONException {
        JSONObject thisField=new JSONObject();
        JSONObject params=new JSONObject();
        params.put("$gt",lowerBound);
        params.put("$lt",upperbound);
        thisField.put(field,params);
        this.fields.put(thisField);
        return this;
    }
    public FilterQueryBuilder whereRangeIncludes(String field,Object upperbound,Object lowerBound) throws JSONException {
        JSONObject thisField=new JSONObject();
        JSONObject params=new JSONObject();
        params.put("$gte",lowerBound);
        params.put("$lte",upperbound);
        thisField.put(field,params);
        this.fields.put(thisField);
        return this;
    }
    private void genComparisonObjects(String operator,String field,Object value) throws JSONException {
        JSONObject thisField=new JSONObject();
        JSONObject params=new JSONObject();
        params.put(operator,value);
        thisField.put(field,params);
        this.fields.put(thisField);
    }
    private void genComparisonObjects(String operator,String field,Object... values) throws JSONException {
        JSONObject thisField=new JSONObject();
        JSONObject params=new JSONObject();
        JSONArray array=new JSONArray();
        for(Object va:values)
            array.put(va);
        params.put(operator,array);
        thisField.put(field,params);
        this.fields.put(thisField);
    }
    public FilterQuery build() throws JSONException {
        String use=getBinary();
        if(use!=null)
            filterQuery.put(use,fields);
        else
        {
            for (int i = 0; i <fields.length() ; i++) {
                JSONObject object=fields.getJSONObject(i);
                for (Iterator<String> it = object.keys(); it.hasNext(); ) {
                    String key = it.next();
                    filterQuery.put(key,object.get(key));
                }
            }
        }
        return new FilterQuery(filterQuery,projection,limit,binary);
    }
    private String getBinary()
    {
        if(binary==null)
            return null;
        switch (binary)
        {
            case OR:return "$or";
            case AND: return "$and";
            default:return null;
        }
    }
}
