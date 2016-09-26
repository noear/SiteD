package org.noear.sited;

/**
 * Created by yuety on 15/10/10.
 */

import java.util.Date;

class __CacheBlock {
    public String value;
    public Date time;

    public boolean isOuttime(SdNode config) {
        if(time==null || value == null){
            return true;
        }else {
            if(config.cache == 1)
                return false;
            else {
                long seconds = (new Date().getTime() - time.getTime()) / 1000;
                return seconds > config.cache;
            }
        }
    }
}

