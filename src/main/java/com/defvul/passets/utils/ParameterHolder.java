package com.defvul.passets.utils;

import com.fasterxml.jackson.core.type.TypeReference;

public abstract class ParameterHolder<T> extends TypeReference<T> {
    public ParameterHolder() {
    }
}
