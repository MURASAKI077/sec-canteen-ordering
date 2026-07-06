package com.example.sec_android;

public class Constant {
    public static final int HANDLER_HTTP_SEND_FAIL = 1001;
    public static final int HANDLER_HTTP_RECEIVE_FAIL = 1002;
    public static String URL = "http://10.26.52.103:8080/SEC_Servlet/"; // IP地址请改为你自己的IP
    public static String URL_Register = URL + "RegisterServlet";
    public static String URL_Login = URL + "LoginServlet";
    public static String URL_Dish = URL + "DishServlet";
    public static String URL_Order = URL + "OrderServlet";
    public static String URL_OrderRecord = URL + "OrderRecordServlet";
    public static boolean landing;
    public static String account;
}
