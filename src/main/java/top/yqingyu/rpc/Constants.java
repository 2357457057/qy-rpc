package top.yqingyu.rpc;

import top.yqingyu.common.utils.CollectionUtil;

import java.util.*;

public interface Constants {
    String method = "@";
    String param = "#";
    String parameterList = "parameterList";
    String invokeSuccess = "0";
    String invokeNoSuch = "1";
    String invokeThrowError = "2";
    String invokeResult = "invokeResult";
    String serviceIdentifierTag = "serviceIdentifierTag";
    //ms
    long authenticationWaitTime = 3000;
    String SpringCGLib = "$$SpringCGLIB$$";
    String SpringCGLibRegx = "[$]{2}SpringCGLIB[$]{2}";

    List<String> specialMethod = Arrays.asList("toString", "hashcode", "equals");
}
