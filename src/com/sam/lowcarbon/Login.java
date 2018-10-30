package com.sam.lowcarbon;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class Login extends ActionSupport {  //登录类

    private Connection connection;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private JSONObject requestObject;
    private JSONObject responseObject;
    private PrintWriter printWriter;

    /* 表单数据 */
    private String requestJson;    //获取表单中的requestJson字符串

    /* set get 方法 */
    public String getRequestJson() {
        return requestJson;
    }

    public void setRequestJson(String requestJson) {
        this.requestJson = requestJson;
    }

    /* 登录活动 */
    public String login() throws IOException, SQLException {
        try {
            Class.forName(Constant.driver); //加载驱动
            connection = DriverManager.getConnection(Constant.databaseurl, Constant.username, Constant.password);   //连接数据库
            if (!connection.isClosed()) {
                System.out.println("Connection Success");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        /* 打印requestJson数据 */
        System.out.println(requestJson);

        requestObject = new JSONObject(requestJson);   //将requestJson字符串解析成requestJson对象
        getResponseJsonObject();    //获取responseJson对象

        switch (requestObject.getInt("loginmethod")) {  //判断登录方式
            case Constant.PHONE_LOGIN:  //手机登录
                try {
                    String telephone = requestObject.getString("telephone").trim();   //获取手机号
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT  * FROM User WHERE telephone=" + "'" + telephone + "'");   //从数据库中查询手机号
                    if (resultSet.next()) { //数据库中存在该手机号
                        /* 获取该用户的个人信息 */
                        String username = resultSet.getString("username");
                        String birthday = resultSet.getString("birthday");
                        String gender = resultSet.getString("gender");
                        String blood = resultSet.getString("blood");
                        int height = resultSet.getInt("height");
                        int weight = resultSet.getInt("weight");

                        /* 将键-值对放入json对象中 */
                        responseObject.put("result", 0);  //则返回0
                        responseObject.put("username", username);
                        responseObject.put("birthday", birthday);
                        responseObject.put("gender", gender);
                        responseObject.put("blood", blood);
                        responseObject.put("height", height);
                        responseObject.put("weight", weight);

                        System.out.println("Login Success");    //登录成功
                    } else {    //若不存在结果
                        responseObject.put("result", 1);  //则返回1

                        System.out.println("Login Failed"); //登录失败
                    }
                    printWriter.write(responseObject.toString()); //将json对象转成字符串传回
                    printWriter.flush();    //刷新
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case Constant.QQ_LOGIN:     //QQ登录
                try {
                    String qqid = requestObject.getString("qqid").trim(); //获取qq的openid
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM User WHERE qqid=" + "'" + qqid + "'");  //从数据库中查询qqid
                    if (resultSet.next()) {
                        responseObject.put("result", 0);  //数据库中存在该qq
                        /* 获取该用户的信息 */
                        String telephone = resultSet.getString("telephone");
                        String username = resultSet.getString("username");
                        String birthday = resultSet.getString("birthday");
                        String gender = resultSet.getString("gender");
                        String blood = resultSet.getString("blood");
                        int height = resultSet.getInt("height");
                        int weight = resultSet.getInt("weight");

                        /* 放入json对象 */
                        responseObject.put("result", 0);  //则返回0
                        responseObject.put("telephone", telephone);
                        responseObject.put("username", username);
                        responseObject.put("birthday", birthday);
                        responseObject.put("gender", gender);
                        responseObject.put("blood", blood);
                        responseObject.put("height", height);
                        responseObject.put("weight", weight);

                        System.out.println("Login Success");
                    } else {
                        responseObject.put("result", 1);

                        System.out.println("Login Failed");
                    }
                    printWriter.write(responseObject.toString());
                    printWriter.flush();
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if (!connection.isClosed()) {
                        connection.close();
                    }
                }
                break;
            default:
                break;
        }

        return null;
    }

    private void getResponseJsonObject() throws IOException{ //获取responseJson对象
        response = ServletActionContext.getResponse();
        response.setCharacterEncoding("UTF-8"); //设置UTF-8编码
        printWriter = response.getWriter();
        responseObject = new JSONObject();
    }
}
