package top.yqingyu.qyws.modules.web.mapper;

import top.yqingyu.qyws.modules.web.po.TD_S_WEBSITE_ACCESS;
import top.yqingyu.rpc.annontation.QyRpcProducer;

import java.util.List;


public interface ViewNumMapper {


    int insertIpInfo(TD_S_WEBSITE_ACCESS access);

    String getViewNum();

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
    int updateViewNum(TD_S_WEBSITE_ACCESS access);

    int updateIpInfo(TD_S_WEBSITE_ACCESS access);

    List<TD_S_WEBSITE_ACCESS> getAllAssess();

}
