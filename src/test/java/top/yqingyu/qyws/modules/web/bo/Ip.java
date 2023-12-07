package top.yqingyu.qyws.modules.web.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author YYJ
 * @version 1.0.0
 * @ClassName top.yqingyu.qyws.modules.web.bo.Ip
 * @description
 * @createTime 2023年05月18日 17:48:00
 */
@Data
public class Ip implements Serializable {
    @Serial
    private static final long serialVersionUID = -2393921058039766181L;
    static String regx = "\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z";
    private final String ip;
    int ip1;
    int ip2;
    int ip3;
    int ip4;

    public Ip(String ip) {
        this.ip = ip;
        if (!ip.matches(regx)) {
            throw new IllegalArgumentException("不是一个规范的Ipv4地址");
        }
        String[] split = ip.split("[.]");
        ip1 = Short.parseShort(split[0]);
        ip2 = Short.parseShort(split[1]);
        ip3 = Short.parseShort(split[2]);
        ip4 = Short.parseShort(split[3]);
    }

}
