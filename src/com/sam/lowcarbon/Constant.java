package com.sam.lowcarbon;

import org.apache.struts2.ServletActionContext;

public class Constant {

    public static final int QQ_LOGIN = 1001;
    public static final int WECHAT_LOGIN = 1002;
    public static final int PHONE_LOGIN = 1003;

    public static final int REGISTER_PHONE = 2000;
    public static final int REGISTER_QQ_PHONE = 2001;
    public static final int REGISTER_WECHAT_PHONE = 2002;

    public static String driver = "com.mysql.jdbc.Driver";
    public static String databaseurl = "jdbc:mysql://localhost:3306/lowcarbon?useUnicode=true&amp;characterEncoding=UTF-8&useSSL=false";
    public static String username = "root";
    public static String password = "11111111";

    //    public static String imageurl = "/root/UserImage/"; //服务器路径
    public static String imageurl = "C:/UserImage/";  //本地服务器

    public static final int SOCKET_ONLINE = 3000;
    public static final int SOCKET_OFFLINE = 3001;
    public static final int SOCKET_FRIEND_ADD = 3002;
    public static final int SOCKET_FRIEND_DELETE = 3003;
    public static final int SOCKET_FRIEND_AGREE = 3004;
    public static final int SOCKET_FRIEND_REFUSE = 3005;
    public static final int SOCKET_RESPONSE = 3006;

    public static final int FRIEND_CONTACTS = 4001;
    public static final int FRIEND_SEARCH = 4002;
}
