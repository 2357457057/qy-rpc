package top.yqingyu.rpc;

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
}
