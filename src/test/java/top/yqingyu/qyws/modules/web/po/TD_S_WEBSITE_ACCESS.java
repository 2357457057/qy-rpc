package top.yqingyu.qyws.modules.web.po;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TD_S_WEBSITE_ACCESS implements Serializable {
    @Serial
    private static final long serialVersionUID = -888972979338249806L;
    private String ID;
    private LocalDateTime ACCESS_TIME;
    private String HOST;
    private String IP_DATA;
    private Integer COUNT;

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
