package com.polydome.godemon.domain.usecase;

import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class GodPool {
    private final Map<Integer, Integer> pool;

    public GodPool(Map<Integer, Integer> initialPool) {
        this.pool = new HashMap<>(initialPool);
    }

    public enum ChangeType { GRANT, REVOKE }

    @AllArgsConstructor
    public static class Change {
        public final ChangeType type;
        public final int godId;
    }

    public void grantOne(int godId) {
        pool.put(godId, pool.getOrDefault(godId, 0) + 1);
    }

    public void revokeOne(int godId) {
        pool.put(godId, pool.getOrDefault(godId, 0) - 1);
        if (pool.get(godId) == 0)
            pool.remove(godId);
    }

    public boolean contains(int godId) {
        return pool.containsKey(godId);
    }

    public void applyChanges(Stream<Change> changes) {
        changes
            .forEach(change -> {
                if (change.type == ChangeType.GRANT) grantOne(change.godId);
                else revokeOne(change.godId);
            });
    }

    public int distinctCount() {
        return pool.keySet().size();
    }

    public Map<Integer, Integer> toMap() {
        return new HashMap<>(pool);
    }
}
