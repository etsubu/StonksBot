package com.etsubu.stonksbot.utility;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Pair<X, Y> {
    public final X first;
    public final Y second;

    public Pair(X first, Y second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(first) ^ Objects.hashCode(second);
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != getClass()) {
            return false;
        }
        Pair<?,?> pair = (Pair<?,?>) o;
        return Objects.equals(first, pair.getFirst()) && Objects.equals(second, pair.getSecond());
    }
}
