using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace org.noear.sited {
    public class SdAttributeList {
        Dictionary<string, string> _items;
        public SdAttributeList() {
            _items = new Dictionary<string, string>();
        }

        public int count() {
            return _items.Count;
        }

        public void clear() {
            _items.Clear();
        }

        public bool contains(String key) {
            return _items.ContainsKey(key);
        }

        public void set(String key, String val) {
            _items[key] = val;
        }

        public String getString(String key) {
            return getString(key, null);
        }

        public String getString(String key, String def) {
            if (contains(key))
                return _items[key];
            else
                return def;
        }

        public int getInt(String key) {
            return getInt(key, 0);
        }

        public int getInt(String key, int def) {
            if (contains(key))
                return int.Parse(_items[key]);
            else
                return def;
        }

        public long getLong(String key) {
            return getLong(key, 0);
        }

        public long getLong(String key, long def) {
            if (contains(key))
                return long.Parse(_items[key]);
            else
                return def;
        }

        public void addAll(SdAttributeList attrs) {
            foreach (var kv in attrs._items) {
                _items.Add(kv.Key, kv.Value);
            }
        }
    }
}
