package org.noear.sited;

/**
 * Created by yuety on 15/10/10.
 */

import java.util.Date;

class __CacheBlock {
    public String value;
    public Date time;

    public long seconds() {
        return (new Date().getTime() - time.getTime()) / 1000;
    }
}

