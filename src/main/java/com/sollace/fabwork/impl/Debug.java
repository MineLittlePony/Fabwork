package com.sollace.fabwork.impl;

public interface Debug {
    boolean NO_CLIENT = Boolean.getBoolean("fabwork.debug.noclient");
    boolean NO_SERVER = Boolean.getBoolean("fabwork.debug.noserver");
}
