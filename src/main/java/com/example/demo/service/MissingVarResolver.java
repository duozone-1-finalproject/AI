package com.example.demo.service;

@FunctionalInterface
public interface MissingVarResolver {
    Object resolve(String name);
}