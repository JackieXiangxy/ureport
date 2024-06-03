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
package com.bstek.ureport.console;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 请求信息处理统一接口，提供所有请求信息统一的操作接口
 *
 * @author Jacky.gao
 * @since 2017年1月25日
 */
public interface ServletAction {

    /**
     * 设置统一前缀
     */
    String PREVIEW_KEY = "p";

    /**
     * 具体执行方法
     *
     * @param req  请求信息
     * @param resp 响应信息
     * @throws ServletException exception
     * @throws IOException      io exception
     */
    void execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;

    /**
     * 请求路劲
     *
     * @return url
     */
    String url();
}
