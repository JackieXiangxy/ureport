/*******************************************************************************
 * Copyright 2017 Bstek
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * <p>
 *   http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p>
 *
 * Modifications by jackie Darcy on 2024-06-12.
 ******************************************************************************/
package com.bstek.ureport.console.cache;

import com.bstek.ureport.console.RequestHolder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * url请求参数解析工具类
 *
 * @author Jacky.gao
 * @since 2017年9月6日
 */
public class TempObjectCache {
    private final static TempObjectCache TEMP_OBJECT_CACHE = new TempObjectCache();
    private final Map<String, ObjectMap> SEESION_MAP = new HashMap<>();

    public static Object getObject(String key) {
        return TEMP_OBJECT_CACHE.get(key);
    }

    public static void putObject(String key, Object obj) {
        TEMP_OBJECT_CACHE.store(key, obj);
    }

    public static void removeObject(String key) {
        TEMP_OBJECT_CACHE.remove(key);
    }

    public void remove(String key) {
        HttpServletRequest req = RequestHolder.getRequest();
        if (req == null) {
            return;
        }
        ObjectMap mapObject = getReportMap(req);
        mapObject.remove(key);
    }

    public Object get(String key) {
        HttpServletRequest req = RequestHolder.getRequest();
        if (req == null) {
            return null;
        }
        ObjectMap mapObject = getReportMap(req);
        return mapObject.get(key);
    }

    public void store(String key, Object obj) {
        HttpServletRequest req = RequestHolder.getRequest();
        if (req == null) {
            return;
        }
        ObjectMap mapObject = getReportMap(req);
        mapObject.put(key, obj);
    }

    private ObjectMap getReportMap(HttpServletRequest req) {
        List<String> expiredList = new ArrayList<>();
        for (String key : SEESION_MAP.keySet()) {
            ObjectMap reportObj = SEESION_MAP.get(key);
            if (reportObj.isExpired()) {
                expiredList.add(key);
            }
        }
        for (String key : expiredList) {
            SEESION_MAP.remove(key);
        }
        String sessionId = req.getSession().getId();
        ObjectMap obj = SEESION_MAP.get(sessionId);
        if (obj != null) {
            return obj;
        } else {
            ObjectMap mapObject = new ObjectMap();
            SEESION_MAP.put(sessionId, mapObject);
            return mapObject;
        }
    }
}
