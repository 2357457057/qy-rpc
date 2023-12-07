package top.yqingyu.qyws.modules.web.bo;


import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IpInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 3000168999023123066L;
    private String Count;
    private String IpAdr;
    private IpData IpData;


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
