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

public class FriendList extends ActionSupport {

    private Connection connection;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private JSONObject requestObject;
    private JSONArray responseArray;
    private PrintWriter printWriter;
    private String requestJson;
    public String getRequestJson() {
        return requestJson;
    }
    public void setRequestJson(String requestJson) {
        this.requestJson = requestJson;
    }

    public String friendList() throws IOException, SQLException {
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
        getResponseJsonArray();

        String user = requestObject.getString("telephone").trim();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from User WHERE telephone IN (SELECT User2 from Friend WHERE User1 = ?) OR telephone IN (SELECT User1 FROM Friend WHERE User2 = ?)");
        preparedStatement.setString(1, user);
        preparedStatement.setString(2, user);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String telephone = resultSet.getString("telephone");
            String username = resultSet.getString("username");
            Double lat = resultSet.getDouble("wei");
            Double lon = resultSet.getDouble("jing");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("telephone", telephone);
            jsonObject.put("username", username);
            jsonObject.put("wei", lat);
            jsonObject.put("jing", lon);
            responseArray.put(jsonObject);
        }
        printWriter.write(responseArray.toString());
        printWriter.flush();
        return null;
    }

    public String friendRank() throws IOException, SQLException {
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
        getResponseJsonArray();

        String user = requestObject.getString("telephone").trim();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * from User WHERE telephone IN (SELECT User2 from Friend WHERE User1 = ?) OR telephone IN (SELECT User1 FROM Friend WHERE User2 = ?) OR telephone = ? ORDER BY steps DESC ,telephone DESC ");
        preparedStatement.setString(1, user);
        preparedStatement.setString(2, user);
        preparedStatement.setString(3, user);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String telephone = resultSet.getString("telephone");
            String username = resultSet.getString("username");
            Integer steps = resultSet.getInt("steps");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("telephone", telephone);
            jsonObject.put("username", username);
            jsonObject.put("ranksteps", steps);
            responseArray.put(jsonObject);
        }
        printWriter.write(responseArray.toString());
        printWriter.flush();
        return null;
    }

    private void getResponseJsonArray() throws IOException { //获取responseJson对象
        response = ServletActionContext.getResponse();
        response.setCharacterEncoding("UTF-8"); //设置UTF-8编码
        printWriter = response.getWriter();
        responseArray = new JSONArray();
    }

}
