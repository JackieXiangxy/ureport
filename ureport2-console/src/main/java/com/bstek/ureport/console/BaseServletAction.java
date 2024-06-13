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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Jacky.gao
 * @since 2016年6月3日
 */
public abstract class BaseServletAction implements ServletAction {

    protected static final Logger log = LoggerFactory.getLogger(BaseServletAction.class);


    private static final String UREPORT_PREFIX = "ureport-";
    private static final String UREPORT_XML_SUFFIX = ".ureport.xml";
    private static final String EXTENSION_SEPARATOR = ".";


    protected Throwable buildRootException(Throwable throwable) {
        if (throwable.getCause() == null) {
            return throwable;
        }
        return buildRootException(throwable.getCause());
    }

    /**
     * 对文件名进行解码。
     * 注意：解码方式为UTF-8
     *
     * @param fileName 文件名称
     */
    protected String decode(String fileName) {
        try {
            return URLDecoder.decode(fileName, "UTF-8");
        } catch (Exception ex) {
            return fileName;
        }
    }


    protected String decodeContent(String content) {
        try {
            return URLDecoder.decode(content, "utf-8");
        } catch (Exception ex) {
            return content;
        }
    }

    /**
     * 构建请求参数的映射。
     * 从HttpServletRequest中提取参数，并将其放入一个Map中。
     * 参数名和参数值都经过解码处理。
     * 排除以"_"开头的参数名，以及空的参数名或值。
     *
     * @param req HTTP请求对象，从中获取参数。
     * @return 包含请求参数的Map，参数名和参数值都是字符串。
     */
    protected Map<String, Object> buildParameters(HttpServletRequest req) {
        Map<String, Object> parameters = new HashMap<>();
        Enumeration<?> enumeration = req.getParameterNames();
        while (enumeration.hasMoreElements()) {
            Object obj = enumeration.nextElement();
            if (obj == null) {
                continue;
            }
            String name = obj.toString();
            String value = req.getParameter(name);
            if (name == null || value == null || name.startsWith("_")) {
                continue;
            }
            parameters.put(name, decode(value));
        }
        return parameters;
    }

    /**
     * 调用当前类中指定方法，该方法需接受HttpServletRequest和HttpServletResponse作为参数。
     * 此方法用于根据请求动态调用相应的方法，以实现更灵活的处理逻辑。
     *
     * @param methodName 要调用的方法名。
     * @param req        HttpServletRequest对象，代表客户端的请求。
     * @param resp       HttpServletResponse对象，用于向客户端发送响应。
     * @throws ServletException 如果调用方法过程中发生异常，则抛出ServletException。
     */
    protected void invokeMethod(String methodName, HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            Method method = this.getClass().getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
            method.invoke(this, req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    /**
     * 从HTTP请求中提取方法名。
     * 此方法用于解析请求URL，以获取特定的报告方法名。
     * 它被设计为保护类型，假设在同一个包内或子类中调用，以获取当前请求针对的报告方法的名称。
     *
     * @param req HttpServletRequest对象，代表客户端的请求。
     * @return 返回提取的报告方法名。如果无法确定方法名或方法名为空，则返回null。
     */
    protected String retrieveMethod(HttpServletRequest req) {
        try {
            // 验证输入是否为null，增加健壮性
            if (req == null) {
                return null;
            }

            String path = req.getContextPath() + UReportServlet.URL;
            String uri = req.getRequestURI();

            // 验证URI是否合法，避免NullPointerException
            if (uri == null) {
                return null;
            }

            // 使用java.net.URI处理URL解析，提高性能和安全性
            java.net.URI requestUri = new java.net.URI(uri);
            String targetUrl = requestUri.getPath();

            // 确保路径是相对于context path的
            if (!targetUrl.startsWith(path)) {
                return null;
            }

            targetUrl = targetUrl.substring(path.length());

            int slashPos = targetUrl.indexOf("/", 1);
            if (slashPos > -1) {
                String methodName = targetUrl.substring(slashPos + 1).trim();
                // 验证解析出的methodName是否合法，增加安全性
                if (isValidMethodName(methodName)) {
                    return methodName;
                }
            }
        } catch (URISyntaxException e) {
            // 异常处理，日志记录或其他错误处理机制
            log.error(e.getMessage(), e);
        }

        // 如果无法确定方法名或方法名为空，则返回null
        return null;
    }

    /**
     * 验证方法名是否合法。
     * 这是一个虚拟的方法，具体实现应根据实际需求定义“合法”的标准。
     * 例如，可以检查methodName是否包含非法字符，是否符合命名约定等。
     *
     * @param methodName 待验证的方法名
     * @return 如果方法名合法，则返回true；否则返回false。
     */
    private boolean isValidMethodName(String methodName) {
        // 示例：验证方法名是否为空且不包含非法字符
        return !methodName.isEmpty() && methodName.matches("^[a-zA-Z0-9_]+$");
    }

    /**
     * 构建下载文件名。
     * 根据传入的报告文件名、自定义文件名和文件扩展名，生成最终的下载文件名。
     * 如果自定义文件名不为空，将对自定义文件名进行解码，并检查是否包含指定的文件扩展名。
     * 如果不包含，将扩展名添加到文件名后。如果自定义文件名为空，则从报告文件名中提取信息生成文件名。
     *
     * @param reportFileName 报告文件名，可能包含生成文件名的提示信息。
     * @param fileName       用户自定义的文件名，可能需要解码和添加文件扩展名。
     * @param extName        文件的扩展名，用于确保生成的文件名包含正确的文件类型标识。
     * @return 构建好的下载文件名。
     */
    protected String buildDownloadFileName(String reportFileName, String fileName, String extName) {
        if (StringUtils.isBlank(fileName)) {
            return generateDefaultFileName(reportFileName, extName);
        }

        fileName = decode(fileName);
        return ensureExtension(fileName, extName);
    }

    /**
     * 生成默认的文件名。
     */
    private String generateDefaultFileName(String reportFileName, String extName) {
        if (StringUtils.isBlank(reportFileName)) {
            return UREPORT_PREFIX + "default" + extName;
        }
        int pos = reportFileName.indexOf(":");
        if (pos > 0) {
            reportFileName = reportFileName.substring(pos + 1);
        }

        pos = reportFileName.toLowerCase().indexOf(UREPORT_XML_SUFFIX);
        if (pos > 0) {
            reportFileName = reportFileName.substring(0, pos);
        }

        return UREPORT_PREFIX + reportFileName + extName;
    }

    /**
     * 确保文件名包含指定的扩展名。
     */
    private String ensureExtension(String fileName, String extName) {
        if (!fileName.toLowerCase().endsWith(extName)) {
            fileName += EXTENSION_SEPARATOR + extName;
        }
        return fileName;
    }


}
