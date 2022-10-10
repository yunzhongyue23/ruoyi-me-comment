package com.ruoyi.common.utils;

import com.github.pagehelper.PageHelper;
import com.ruoyi.common.core.page.PageDomain;
import com.ruoyi.common.core.page.TableSupport;
import com.ruoyi.common.utils.sql.SqlUtil;

/**
 * 分页工具类
 * 
 * @author ruoyi
 */
public class PageUtils extends PageHelper
{
    /**
     * 设置请求分页数据
     */
    public static void startPage()
    {
//        构建分页请求
        PageDomain pageDomain = TableSupport.buildPageRequest();
//        页码
        Integer pageNum = pageDomain.getPageNum();
//        每页显示条说
        Integer pageSize = pageDomain.getPageSize();
//        检查字符防止sql注入
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
//        分页合理化
        Boolean reasonable = pageDomain.getReasonable();
//        设置是否分页合理化
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
    }

    /**
     * 清理分页的线程变量
     */
    public static void clearPage()
    {
        PageHelper.clearPage();
    }
}
