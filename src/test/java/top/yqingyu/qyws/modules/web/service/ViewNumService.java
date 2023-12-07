package top.yqingyu.qyws.modules.web.service;

import com.alibaba.fastjson2.JSONObject;
import top.yqingyu.qyws.modules.web.bo.IpInfo;
import top.yqingyu.qyws.modules.web.po.TD_S_WEBSITE_ACCESS;

import java.util.List;

public interface ViewNumService {


    JSONObject getIpInfoIp138(String host);


    void insertIpInfo(TD_S_WEBSITE_ACCESS td_s_website_access);

    String getViewNum();

    IpInfo getIpInfo(String host) throws Exception;
    /**
     * description: 优化，数据库有的数据直接取数据库中数据
     *
     * @author yqingyu
     * DATE 2021/10/1
     */
    TD_S_WEBSITE_ACCESS getTD_S_WEBSITE_ACCESS(String host);

    /**
     * description: 更新统计每个host统计次数
     *
     * @author yqingyu
     * DATE 2021/10/1
     */
    void updateViewNum(TD_S_WEBSITE_ACCESS access);


    void updateIpInfo(TD_S_WEBSITE_ACCESS access);

    List<TD_S_WEBSITE_ACCESS> getAllAssess();
}
