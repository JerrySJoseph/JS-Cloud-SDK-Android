package com.example.js_dynamicdatabase.helpers;

public class DBRequest<T> {

    public enum RequestType{
        INSERT_ONE,
        INSERT_ALL,
        DELETE,
        UPDATE,
        READ_ALL,
        READ_ONE
    }
    RequestType requestType;
    T object;
    String collectionName;
    String filterQuery;
    String selectionQuery;
    int limit=0;

    public DBRequest() {
    }

    public DBRequest(RequestType requestType, T object, String collectionName) {
        this.requestType = requestType;
        this.object = object;
        this.collectionName = collectionName;
    }

    public DBRequest(RequestType requestType, T object, String collectionName, FilterQuery query) {
        this.requestType = requestType;
        this.object = object;
        this.collectionName = collectionName;
        this.filterQuery = query.getFilterQuery();
        this.selectionQuery = query.getSelectionQuery();
        this.limit = query.getLimit();
    }


    public String getFilterQuery() {
        return filterQuery;
    }

    public void setFilterQuery(String filterQuery) {
        this.filterQuery = filterQuery;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }


}
