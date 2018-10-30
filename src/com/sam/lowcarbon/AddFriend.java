package com.sam.lowcarbon;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddFriend extends ActionSupport {

    private Connection connection;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private JSONObject requestObject;
    private JSONObject responseObject;
    private PrintWriter printWriter;

    public String getRequestJson() {
        return requestJson;
    }

    public void setRequestJson(String requestJson) {
        this.requestJson = requestJson;
    }

    private String requestJson;

    public String addFriend() throws IOException, SQLException {
        try {
            Class.forName(Constant.driver);
            connection = DriverManager.getConnection(Constant.databaseurl, Constant.username, Constant.password);
            if (!connection.isClosed()) {
                System.out.println("Connection Success");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        System.out.println(requestJson);
        requestObject = new JSONObject(requestJson);
        getResponseJsonObject();

        String user1 = requestObject.getString("user1");
        String user2 = requestObject.getString("user2");
        switch (requestObject.getInt("method")) {
            case Constant.SOCKET_FRIEND_AGREE: {
                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM FriendRequest WHERE User1 = ? AND User2 = ?");
                preparedStatement.setString(1, user1);
                preparedStatement.setString(2, user2);
                preparedStatement.executeUpdate();

                preparedStatement = connection.prepareStatement("INSERT INTO Friend (User1, User2) VALUES (?, ?)");
                if (user1.compareTo(user2) > 0) { //较大字符串存为user1
                    preparedStatement.setString(1, user1);
                    preparedStatement.setString(2, user2);
                } else {
                    preparedStatement.setString(1, user2);
                    preparedStatement.setString(2, user1);
                }
                preparedStatement.executeUpdate();
                System.out.println("friend add success");
                responseObject.put("result", 0);
                break;
            }
            case Constant.SOCKET_FRIEND_REFUSE: {
                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM FriendRequest WHERE User1 = ? AND User2 = ?");
                preparedStatement.setString(1, requestObject.getString("user1"));
                preparedStatement.setString(2, requestObject.getString("user2"));
                int result = preparedStatement.executeUpdate();
                if (result != 0) {
                    System.out.println("refuse");
                    responseObject.put("result", 0);
                }
                break;
            }
            case Constant.SOCKET_FRIEND_DELETE:
                PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM Friend WHERE User1 = ? AND User2 = ?");
                if (user1.compareTo(user2) > 0) { //较大字符串存为user1
                    preparedStatement.setString(1, user1);
                    preparedStatement.setString(2, user2);
                } else {
                    preparedStatement.setString(1, user2);
                    preparedStatement.setString(2, user1);
                }
                int result = preparedStatement.executeUpdate();
                if (result != 0) {
                    System.out.println("delete");
                    responseObject.put("result", 0);
                }
                break;
            default:
                break;
        }
        printWriter.write(responseObject.toString());
        printWriter.flush();
        return null;
    }

    private void getResponseJsonObject() throws IOException { //获取responseJson对象
        response = ServletActionContext.getResponse();
        response.setCharacterEncoding("UTF-8"); //设置UTF-8编码
        printWriter = response.getWriter();
        responseObject = new JSONObject();
    }
}
