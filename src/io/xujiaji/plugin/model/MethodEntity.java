package io.xujiaji.plugin.model;

/**
 * Created by jiana on 11/12/16.
 */
public class MethodEntity {

    private String returnStr;
    private String methodStr;

    public MethodEntity(String returnStr, String methodStr) {
        this.returnStr = returnStr;
        this.methodStr = methodStr;
    }

    public void setReturnStr(String returnStr) {
        this.returnStr = returnStr;
    }

    public void setMethodStr(String methodStr) {
        this.methodStr = methodStr;
    }

    public String getReturnStr() {
        return returnStr;
    }

    public String getMethodStr() {
        return methodStr;
    }
}
