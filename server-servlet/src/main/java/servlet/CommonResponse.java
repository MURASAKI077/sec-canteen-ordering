package servlet;

import java.util.ArrayList;
import java.util.HashMap;

public class CommonResponse {
    private String resCode;
    private String resMsg;
    private HashMap<String, String> property;
    private ArrayList<HashMap<String, String>> list;

    public CommonResponse() {
        resCode = "";
        resMsg = "";
        property = new HashMap<>();
        list = new ArrayList<>();
    }

    public void setResult(String resCode, String resMsg) {
        this.resCode = resCode;
        this.resMsg = resMsg;
    }

    public String getResCode() {
        return resCode;
    }

    public void setResCode(String resCode) {
        this.resCode = resCode;
    }

    public String getResMsg() {
        return resMsg;
    }

    public void setResMsg(String resMsg) {
        this.resMsg = resMsg;
    }

    public HashMap<String, String> getProperty() {
        return property;
    }

    public void addProperty(String key, String value) {
        property.put(key, value);
    }

    public void addListItem(HashMap<String, String> map) {
        list.add(map);
    }

    public ArrayList<HashMap<String, String>> getList() {
        return list;
    }
}
