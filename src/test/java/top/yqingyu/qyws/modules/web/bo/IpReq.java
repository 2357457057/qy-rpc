package top.yqingyu.qyws.modules.web.bo;

import com.alibaba.fastjson2.JSON;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author YYJ
 * @version 1.0.0
 * @date 2022/5/16 4:13
 * @description
 * @modified by
 */
@Data
public class IpReq implements Serializable, Cloneable {
    @Serial
    private static final long serialVersionUID = -6841489620740104383L;
    private String ip;


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    @Override
    public IpReq clone() {
        try {
            IpReq clone = (IpReq) super.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
