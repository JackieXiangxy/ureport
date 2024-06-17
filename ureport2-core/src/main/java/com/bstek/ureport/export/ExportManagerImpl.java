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
package com.bstek.ureport.export;

import com.bstek.ureport.build.paging.Page;
import com.bstek.ureport.cache.CacheUtils;
import com.bstek.ureport.chart.ChartData;
import com.bstek.ureport.definition.ReportDefinition;
import com.bstek.ureport.export.excel.high.ExcelProducer;
import com.bstek.ureport.export.excel.low.Excel97Producer;
import com.bstek.ureport.export.html.HtmlProducer;
import com.bstek.ureport.export.html.HtmlReport;
import com.bstek.ureport.export.pdf.PdfProducer;
import com.bstek.ureport.export.word.high.WordProducer;
import com.bstek.ureport.model.Report;

import java.util.List;
import java.util.Map;

/**
 * @author Jacky.gao
 * @since 2016年12月4日
 */
public class ExportManagerImpl implements ExportManager {
    private final HtmlProducer htmlProducer = new HtmlProducer();
    private final WordProducer wordProducer = new WordProducer();
    private final ExcelProducer excelProducer = new ExcelProducer();
    private final Excel97Producer excel97Producer = new Excel97Producer();
    private final PdfProducer pdfProducer = new PdfProducer();
    private final ReportRender reportRender;

    public ExportManagerImpl(ReportRender reportRender) {
        this.reportRender = reportRender;
    }

    /**
     * 导出Html报表
     *
     * @param file        报表模版文件名
     * @param contextPath 当前项目的context path
     * @param parameters  参数
     * @return 返回一个HtmlReport对象，里面有报表产生的HTML及相关CSS
     */
    @Override
    public HtmlReport exportHtml(String file, String contextPath, Map<String, Object> parameters) {
        ReportDefinition reportDefinition = reportRender.getReportDefinition(file);
        Report report = reportRender.render(reportDefinition, parameters);
        Map<String, ChartData> chartMap = report.getContext().getChartDataMap();
        if (!chartMap.isEmpty()) {
            CacheUtils.storeChartDataMap(chartMap);
        }
        HtmlReport htmlReport = new HtmlReport();
        String content = htmlProducer.produce(report);
        htmlReport.setContent(content);
        if (reportDefinition.getPaper().isColumnEnabled()) {
            htmlReport.setColumn(reportDefinition.getPaper().getColumnCount());
        }
        htmlReport.setStyle(reportDefinition.getStyle());
        htmlReport.setSearchFormData(reportDefinition.buildSearchFormData(report.getContext().getDatasetMap(), parameters));
        htmlReport.setReportAlign(report.getPaper().getHtmlReportAlign().name());
        htmlReport.setChartDatas(report.getContext().getChartDataMap().values());
        htmlReport.setHtmlIntervalRefreshValue(report.getPaper().getHtmlIntervalRefreshValue());
        return htmlReport;
    }

    /**
     * 导出指定页码的Html报表
     *
     * @param file        报表模版文件名
     * @param contextPath 当前项目的context path
     * @param parameters  参数
     * @param pageIndex   页码
     * @return 返回一个HtmlReport对象，里面有报表产生的HTML及相关CSS
     */
    @Override
    public HtmlReport exportHtml(String file, String contextPath, Map<String, Object> parameters, int pageIndex) {
        ReportDefinition reportDefinition = reportRender.getReportDefinition(file);
        Report report = reportRender.render(reportDefinition, parameters);
        Map<String, ChartData> chartMap = report.getContext().getChartDataMap();
        if (!chartMap.isEmpty()) {
            CacheUtils.storeChartDataMap(chartMap);
        }
        SinglePageData pageData = PageBuilder.buildSinglePageData(pageIndex, report);
        List<Page> pages = pageData.getPages();
        String content = null;
        if (pages.size() == 1) {
            content = htmlProducer.produce(report.getContext(), pages.get(0), false);
        } else {
            content = htmlProducer.produce(report.getContext(), pages, pageData.getColumnMargin(), false);
        }
        HtmlReport htmlReport = new HtmlReport();
        htmlReport.setContent(content);
        if (reportDefinition.getPaper().isColumnEnabled()) {
            htmlReport.setColumn(reportDefinition.getPaper().getColumnCount());
        }
        htmlReport.setStyle(reportDefinition.getStyle());
        htmlReport.setSearchFormData(reportDefinition.buildSearchFormData(report.getContext().getDatasetMap(), parameters));
        htmlReport.setPageIndex(pageIndex);
        htmlReport.setTotalPage(pageData.getTotalPages());
        htmlReport.setReportAlign(report.getPaper().getHtmlReportAlign().name());
        htmlReport.setChartDatas(report.getContext().getChartDataMap().values());
        htmlReport.setHtmlIntervalRefreshValue(report.getPaper().getHtmlIntervalRefreshValue());
        return htmlReport;
    }

    /**
     * 导出PDF报表
     *
     * @param config 包含报表模版文件名、参数等信息的配置对象
     */
    @Override
    public void exportPdf(ExportConfigure config) {
        pdfProducer.produce(buildReport(config), config.getOutputStream());
    }

    /**
     * 导出Word
     *
     * @param config 包含报表模版文件名、参数等信息的配置对象
     */
    @Override
    public void exportWord(ExportConfigure config) {
        wordProducer.produce(buildReport(config), config.getOutputStream());
    }

    /**
     * 不分页导出Excel
     *
     * @param config 包含报表模版文件名、参数等信息的配置对象
     */
    @Override
    public void exportExcel(ExportConfigure config) {
        excelProducer.produce(buildReport(config), config.getOutputStream());
    }

    /**
     * 不分页导出Excel97格式文件
     *
     * @param config 包含报表模版文件名、参数等信息的配置对象
     */
    @Override
    public void exportExcel97(ExportConfigure config) {
        excel97Producer.produce(buildReport(config), config.getOutputStream());
    }

    /**
     * 分页导出Excel
     *
     * @param config 包含报表模版文件名、参数等信息的配置对象
     */
    @Override
    public void exportExcelWithPaging(ExportConfigure config) {
        excelProducer.produceWithPaging(buildReport(config), config.getOutputStream());
    }

    /**
     * 分页导出Excel
     *
     * @param config 包含报表模版文件名、参数等信息的配置对象
     */
    @Override
    public void exportExcel97WithPaging(ExportConfigure config) {
        excel97Producer.produceWithPaging(buildReport(config), config.getOutputStream());
    }

    /**
     * 分页分Sheet导出Excel
     *
     * @param config 包含报表模版文件名、参数等信息的配置对象
     */
    @Override
    public void exportExcelWithPagingSheet(ExportConfigure config) {
        excelProducer.produceWithSheet(buildReport(config), config.getOutputStream());
    }

    /**
     * 分页分Sheet导出Excel
     *
     * @param config 包含报表模版文件名、参数等信息的配置对象
     */
    @Override
    public void exportExcel97WithPagingSheet(ExportConfigure config) {
        excel97Producer.produceWithSheet(buildReport(config), config.getOutputStream());
    }

    /**
     * 构建报表对象
     *
     * @param config 传入的参数配置信息
     * @return Report
     */
    private Report buildReport(ExportConfigure config) {
        String file = config.getFile();
        Map<String, Object> parameters = config.getParameters();
        ReportDefinition reportDefinition = reportRender.getReportDefinition(file);
        return reportRender.render(reportDefinition, parameters);
    }


}
