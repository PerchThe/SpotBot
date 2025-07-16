package com.olziedev.spotbot.server.suggestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum SuggestionStatus {

    AGREE,
    DISAGREE;

    public List<Long> getMembers(Map<Long, SuggestionStatus> map) {
        List<Long> members = new ArrayList<>();
        map.forEach((key, value) -> {
            if (value == this) members.add(key);
        });
        return members;
    }
}
