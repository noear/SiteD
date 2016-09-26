package org.noear.sited;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuety on 16/2/1.
 */
public class SdAttributeList {
    private Map<String,String> _items;
    protected SdAttributeList(){
        _items  =new HashMap<>();
    }

    public int count(){
        return _items.size();
    }

    public void clear(){
        _items.clear();
    }

    public boolean contains(String key){
        return _items.containsKey(key);
    }

    public void set(String key, String val){
        _items.put(key,val);
    }

    public String getString(String key) {
        return getString(key,null);
    }

    public String getString(String key, String def){
        if(contains(key))
            return _items.get(key);
        else
            return def;
    }

    public int getInt(String key) {
        return getInt(key,0);
    }

    public int getInt(String key, int def) {
        if (contains(key))
            return Integer.parseInt(_items.get(key));
        else
            return def;
    }

    public long getLong(String key) {
       return getLong(key,0);
    }

    public long getLong(String key, long def) {
        if (contains(key))
            return Long.parseLong(_items.get(key));
        else
            return def;
    }
}
