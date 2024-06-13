/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.bstek.ureport.console.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Jacky.gao
 * @since 2017年9月6日
 */
public class ObjectMap {
    //default expired time is 5 minutes.
    private static final int MILLISECOND = 300000;
    private static final int MAX_ITEM = 3;
    private final Map<String, Object> OBJECT_MAP = new LinkedHashMap<>();
    private long start;

    public ObjectMap() {
        this.start = System.currentTimeMillis();
    }

    public void put(String key, Object obj) {
        this.start = System.currentTimeMillis();
        if (OBJECT_MAP.containsKey(key)) {
            OBJECT_MAP.remove(key);
        } else {
            if (OBJECT_MAP.size() > MAX_ITEM) {
                String lastFile = null;
                for (Entry<String, Object> entry : OBJECT_MAP.entrySet()) {
                    lastFile = entry.getKey();
                }
                OBJECT_MAP.remove(lastFile);
            }
        }
        OBJECT_MAP.put(key, obj);
    }

    public Object get(String key) {
        this.start = System.currentTimeMillis();
        return OBJECT_MAP.get(key);
    }

    public void remove(String key) {
        OBJECT_MAP.remove(key);
    }

    public boolean isExpired() {
        long end = System.currentTimeMillis();
        return (end - start) >= MILLISECOND;
    }
}
