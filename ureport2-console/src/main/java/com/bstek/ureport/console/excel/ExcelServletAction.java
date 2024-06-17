package com.bstek.ureport.console.excel;

import com.bstek.ureport.build.ReportBuilder;
import com.bstek.ureport.console.BaseServletAction;
import com.bstek.ureport.export.ExportManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author jackie
 * @since 1.0.0
 */
public abstract class ExcelServletAction extends BaseServletAction {

    protected ReportBuilder reportBuilder;
    protected ExportManager exportManager;

    protected ExcelServletAction(ReportBuilder reportBuilder, ExportManager exportManager) {
        this.reportBuilder = reportBuilder;
        this.exportManager = exportManager;
    }

    /**
     * 具体执行方法
     *
     * @param req  请求信息
     * @param resp 响应信息
     * @throws ServletException exception
     * @throws IOException      io exception
     */
    @Override
    public void execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = retrieveMethod(req);
        if (method != null) {
            invokeMethod(method, req, resp);
        } else {
            buildExcel(req, resp, false, false);
        }
    }

    /**
     * 分页导出excel
     *
     * @param req  req
     * @param resp resp
     * @throws ServletException EXP
     * @throws IOException      EXP
     */
    protected void paging(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        buildExcel(req, resp, true, false);
    }

    /**
     * 分sheet导出excel
     *
     * @param req  req
     * @param resp resp
     * @throws ServletException EXP
     * @throws IOException      EXP
     */
    protected void sheet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        buildExcel(req, resp, false, true);
    }

    /**
     * 构建excel表格信息
     *
     * @param req    请求信息
     * @param resp   请求响应
     * @param header 头
     * @param footer 尾
     */
    protected abstract void buildExcel(HttpServletRequest req, HttpServletResponse resp, boolean header, boolean footer) throws IOException;

}
