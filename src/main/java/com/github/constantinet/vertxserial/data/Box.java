package com.github.constantinet.vertxserial.data;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Box implements Copyable<Box> {

    private final int id;
    private final String description;

    @JsonCreator
    public Box(final int id, final String description) {
        this.id = id;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Box{id=" + id + ", description=" + description + "}";
    }

    @Override
    public Box copy() {
        return new Box(getId(), getDescription());
    }
}