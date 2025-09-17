package com.example.demo.service.graphmain;

@FunctionalInterface
public interface MissingVarResolver {
    Object resolve(String name);
}