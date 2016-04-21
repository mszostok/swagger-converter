package com.anty.model.util;

@FunctionalInterface
public interface Command {
    void collect(Object obj);
}
