package com.github.constantinet.vertxserial.data;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Container implements Copyable<Container> {

    private final int id;
    private final List<Box> contents;

    @JsonCreator
    public Container(final int id, final List<Box> contents) {
        this.id = id;
        this.contents = new ArrayList<>(contents);
    }

    public int getId() {
        return id;
    }

    public List<Box> getContents() {
        return Collections.unmodifiableList(contents);
    }

    @Override
    public Container copy() {
        return new Container(getId(), getContents());
    }
}