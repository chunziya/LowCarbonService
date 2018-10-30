package com.sam.lowcarbon;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class SearchUser {
    private Connection connection;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private JSONObject requestObject;
    private JSONObject responseObject;
    private PrintWriter printWriter;
    private String requestJson;     //用于接收用户的请求

    public String getRequestJson() {
        return requestJson;
    }

    public void setRequestJson(String requestJson) {
        this.requestJson = requestJson;
    }

    public String SearchUser() throws IOException {
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
        String user1 = requestObject.getString("user1");    //user1
        String user2 = requestObject.getString("user2");    //user2
        getResponseJsonObject();    //获取requestJson对象

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT  * FROM User WHERE telephone=" + "'" + user2 + "'");   //从数据库中查询手机号
            if (resultSet.next()) {
                /* 获取用户数据 */
                String username = resultSet.getString("username");
                String birthday = resultSet.getString("birthday");
                String gender = resultSet.getString("gender");
                String blood = resultSet.getString("blood");
                int height = resultSet.getInt("height");
                int weight = resultSet.getInt("weight");

                responseObject.put("result", 0);  //则返回0
                responseObject.put("username", username);
                responseObject.put("birthday", birthday);
                responseObject.put("gender", gender);
                responseObject.put("blood", blood);
                responseObject.put("height", height);
                responseObject.put("weight", weight);

            } else {    //若不存在结果
                responseObject.put("result", 1);  //则返回1
            }
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Friend WHERE User1 = ? AND User2 = ?");
            preparedStatement.setString(1, user1);
            preparedStatement.setString(2, user2);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                responseObject.put("relation", 1);
            } else {
                responseObject.put("relation", 0);
            }
            printWriter.write(responseObject.toString()); //将responseJson转成String返回
            printWriter.flush();    //刷新
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void getResponseJsonObject() throws IOException { //获取responsejson对象
        response = ServletActionContext.getResponse();
        response.setCharacterEncoding("UTF-8"); //设置UTF-8编码
        printWriter = response.getWriter();
        responseObject = new JSONObject();
    }

}
