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
package com.bstek.ureport.console.excel;

import com.bstek.ureport.build.ReportBuilder;
import com.bstek.ureport.console.cache.TempObjectCache;
import com.bstek.ureport.console.exception.ReportDesignException;
import com.bstek.ureport.definition.ReportDefinition;
import com.bstek.ureport.exception.ReportComputeException;
import com.bstek.ureport.export.ExportConfigure;
import com.bstek.ureport.export.ExportConfigureImpl;
import com.bstek.ureport.export.ExportManager;
import com.bstek.ureport.export.excel.low.Excel97Producer;
import com.bstek.ureport.model.Report;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author Jacky.gao
 * @since 2017年7月3日
 */
public class ExportExcel97ServletAction extends ExcelServletAction {
    private final Excel97Producer excelProducer = new Excel97Producer();

    protected ExportExcel97ServletAction(ReportBuilder reportBuilder, ExportManager exportManager) {
        super(reportBuilder, exportManager);
    }

    @Override
    public void buildExcel(HttpServletRequest req, HttpServletResponse resp, boolean withPage, boolean withSheet) throws IOException {
        String file = req.getParameter("_u");
        if (StringUtils.isBlank(file)) {
            throw new ReportComputeException("Report file can not be null.");
        }
        String fileName = req.getParameter("_n");
        if (StringUtils.isNotBlank(fileName)) {
            fileName = decode(fileName);
        } else {
            fileName = "ureport.xls";
        }
        resp.setContentType("application/vnd.ms-excel;charset=ISO8859-1");
        resp.setHeader("Content-Disposition", String.format("attachment;filename= %s", URLEncoder.encode(fileName, StandardCharsets.UTF_8.displayName())));
        Map<String, Object> parameters = buildParameters(req);
        OutputStream outputStream = resp.getOutputStream();
        try {
            if (file.equals(PREVIEW_KEY)) {
                ReportDefinition reportDefinition = (ReportDefinition) TempObjectCache.getObject(PREVIEW_KEY);
                if (reportDefinition == null) {
                    throw new ReportDesignException("Report data has expired,can not do export excel.");
                }
                //构建报表信息
                Report report = reportBuilder.buildReport(reportDefinition, parameters);
                if (withPage) {
                    excelProducer.produceWithPaging(report, outputStream);
                } else if (withSheet) {
                    excelProducer.produceWithSheet(report, outputStream);
                } else {
                    excelProducer.produce(report, outputStream);
                }
            } else {
                ExportConfigure configure = new ExportConfigureImpl(file, parameters, outputStream);
                if (withPage) {
                    exportManager.exportExcel97WithPaging(configure);
                } else if (withSheet) {
                    exportManager.exportExcel97WithPagingSheet(configure);
                } else {
                    exportManager.exportExcel97(configure);
                }
            }
        } finally {
            outputStream.flush();
            outputStream.close();
        }

    }

    @Override
    public String url() {
        return "/excel97";
    }
}
