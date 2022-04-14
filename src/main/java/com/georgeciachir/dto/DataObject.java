package com.georgeciachir.dto;

public class DataObject {

    public String message;

    public DataObject() {
    }

    public DataObject(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "DataObject{" +
                "message='" + message + '\'' +
                '}';
    }
}