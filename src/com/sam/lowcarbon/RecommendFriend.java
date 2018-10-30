package com.sam.lowcarbon;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class RecommendFriend extends ActionSupport {
    private Connection connection;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private JSONObject requestObject;
    private JSONArray requestArray;
    private JSONArray responseArray;
    private PrintWriter printWriter;
    private String requestJsonArray;
    private String requestJson;

    public String getRequestJsonArray() {
        return requestJsonArray;
    }

    public void setRequestJsonArray(String requestJsonArray) {
        this.requestJsonArray = requestJsonArray;
    }

    public String getRequestJson() {
        return requestJson;
    }

    public void setRequestJson(String requestJson) {
        this.requestJson = requestJson;
    }

    public String getContacts() throws Exception {
        try {
            Class.forName(Constant.driver);
            connection = DriverManager.getConnection(Constant.databaseurl, Constant.username, Constant.password);
            if (!connection.isClosed()) {
                System.out.println("Connection Success");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        requestArray = new JSONArray(requestJsonArray);
        requestObject = new JSONObject(requestJson);
        getResponseJsonArray();

        String tele = requestObject.getString("telephone");
        int method=requestObject.getInt("method");
        switch (method){
            case Constant.FRIEND_CONTACTS:
                for (int i = 0; i < requestArray.length(); i++) {
                    JSONObject jsonObject = requestArray.getJSONObject(i);
                    String phoneNum = jsonObject.getString("phoneNum");
                    String phoneName = jsonObject.getString("phoneName");
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM User WHERE telephone=" + "'" + phoneNum + "' AND telephone NOT IN (SELECT User2 from Friend WHERE User1 = '" + tele
                            + "') AND telephone NOT IN (SELECT User1 from Friend WHERE User2 = '" + tele + "')");    //查询手机号是否存在
                    if (resultSet.next()) {
                        String telephone = resultSet.getString("telephone");
                        String username = resultSet.getString("username");
                        if (!telephone.equals(tele)) {
                            JSONObject responseJson = new JSONObject();
                            responseJson.put("telephone", telephone);
                            responseJson.put("username", username);
                            responseJson.put("phoneName", phoneName);
                            responseArray.put(responseJson);
                        }
                    }
                }
                break;
            case Constant.FRIEND_SEARCH:
                for (int i = 0; i < requestArray.length(); i++) {
                    JSONObject jsonObject = requestArray.getJSONObject(i);
                    String phoneNum = jsonObject.getString("phoneNum");
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM User WHERE telephone=" + "'" + phoneNum + "' AND telephone NOT IN (SELECT User2 from Friend WHERE User1 = '" + tele
                            + "') AND telephone NOT IN (SELECT User1 from Friend WHERE User2 = '" + tele + "')");    //查询手机号是否存在
                    if (resultSet.next()) {
                        String telephone = resultSet.getString("telephone");
                        if (!telephone.equals(tele)) {
                            JSONObject responseJson = new JSONObject();
                            responseJson.put("telephone", telephone);
                            responseArray.put(responseJson);
                        }
                    }
                }
                break;
        }
        printWriter.write(responseArray.toString());
        printWriter.flush();
        return null;
    }


    public String QueryText() throws Exception {
        try {
            Class.forName(Constant.driver); //加载驱动
            connection = DriverManager.getConnection(Constant.databaseurl, Constant.username, Constant.password);   //连接数据库
            if (!connection.isClosed()) {
                System.out.println("Connection Success");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        requestObject = new JSONObject(requestJson);
        String queryText = requestObject.getString("querytext");
        getResponseJsonArray();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT  * FROM User WHERE telephone LIKE '" + queryText + "%" + "'");   //从数据库中查询手机号
            while (resultSet.next()) {
                JSONObject responseObject = new JSONObject();
                responseObject.put("phone", resultSet.getString("telephone"));
                responseArray.put(responseObject);
            }
            printWriter.write(responseArray.toString()); //将responseJson转成String返回
            printWriter.flush();    //刷新
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return null;
    }

    private void getResponseJsonArray() throws IOException { //获取responseJson对象
        response = ServletActionContext.getResponse();
        response.setCharacterEncoding("UTF-8"); //设置UTF-8编码
        printWriter = response.getWriter();
        responseArray = new JSONArray();
    }
}
