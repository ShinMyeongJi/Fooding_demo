package com.dev.eatit.model;

import java.util.List;

public class MyPushResponse {
    public long multicast_id;
    public int success;
    public int failure;
    public int canonical_ids;
    public List<Result> results;
}
