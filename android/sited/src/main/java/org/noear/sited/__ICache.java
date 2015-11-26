package org.noear.sited;

/**
 * Created by yuety on 15/10/10.
 */
 interface __ICache {
    void save(String key, String data);
    __CacheBlock get(String key);
    void delete(String key);
    boolean isCached(String key);
}
