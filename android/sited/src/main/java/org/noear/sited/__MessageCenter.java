package org.noear.sited;

import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yuety on 16/7/16.
 */

class __MessageCenter {
    static Map<String, ArrayList<SubscribeItem>> center;

    static void tryInit() {
        if (center == null) {
            center = new HashMap<>();
        }
    }

    static void asynCall( Act0 fun) {
        new Handler().postDelayed(() -> {
            fun.run();
        }, 10);
    }

    public  static void SendMessage(String message,  Object... args) {
        tryInit();

        if (center.containsKey(message)) {
            asynCall(()->{
                ArrayList<SubscribeItem> list = center.get(message);
                for (SubscribeItem item : list) {
                    item.action.run(args);
                }
            });
        }
    }

    public static void UnSubscribe(String message, Object target) {
        tryInit();

        if (message == null || target == null)
            return;

        if (center.containsKey(message)) {
            ArrayList<SubscribeItem> list = center.get(message);
            for (int i = 0, len = list.size(); i < len; i++) {
                SubscribeItem item = list.get(i);
                if (item.target.equals(target)) {
                    list.remove(i);

                    item.target = null;
                    item.action = null;
                    return;
                }
            }
        }
    }

    public static void Subscribe(String message, Object target, Act1<Object[]> action) {
        tryInit();

        if (message==null || target == null)
            return;

        ArrayList<SubscribeItem> list = null;
        if (center.containsKey(message)) {
            list = center.get(message);

            for (SubscribeItem item : list) {
                if (item.target.equals(target)) {
                    item.action = action;
                    return;
                }
            }
        }
        else {
            list = new ArrayList<SubscribeItem>();
            center.put(message, list);
        }

        SubscribeItem item = new SubscribeItem();
        item.target = target;
        item.action = action;

        list.add(item);
    }

    static class SubscribeItem {
        public Object target;
        public Act1<Object[]> action;
    }

    public interface Act1<T> {
        void run(T t);
    }

    public interface Act0 {
        void run();
    }
}