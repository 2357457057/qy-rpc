package top.yqingyu.qyws.modules.web.bo;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IpData implements Serializable {
    String country;
    String country_id;
    String area;
    String province;
    String province_id;
    String city;
    String city_id;
    String county;
    String isp;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
