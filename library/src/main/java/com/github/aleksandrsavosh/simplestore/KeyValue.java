package com.github.aleksandrsavosh.simplestore;

public class KeyValue {
    public String key;
    public Object value;

    public KeyValue(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "KeyValue{" +
                "key='" + key + '\'' +
                ", value=" + value +
                '}';
    }
}
