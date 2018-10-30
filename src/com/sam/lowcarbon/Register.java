package com.sam.lowcarbon;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.io.FileUtils;
import org.apache.struts2.ServletActionContext;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.*;
import java.util.UUID;

public class Register extends ActionSupport {   //注册类

    private Connection connection;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private JSONObject requestObject;
    private JSONObject responseObject;
    private PrintWriter printWriter;


    /* 表单数据 */
    private String requestJson; //json字符串
    private File userImage; //上传过来的用户图片
    private String userImgaeFileName;   //图片名称
    private String userImageContentType;    //图片类型

    /* set get方法 */
    public String getRequestJson() {
        return requestJson;
    }

    public void setRequestJson(String requestJson) {
        this.requestJson = requestJson;
    }

    public File getUserImage() {
        return userImage;
    }

    public void setUserImage(File userImage) {
        this.userImage = userImage;
    }

    public String getUserImgaeFileName() {
        return userImgaeFileName;
    }

    public void setUserImgaeFileName(String userImgaeFileName) {
        this.userImgaeFileName = userImgaeFileName;
    }

    public String getUserImageContentType() {
        return userImageContentType;
    }

    public void setUserImageContentType(String userImageContentType) {
        this.userImageContentType = userImageContentType;
    }

    public String checkPhoneBindQQ() throws IOException {   //判断手机号是否绑定QQ

        try {
            Class.forName(Constant.driver);
            connection = DriverManager.getConnection(Constant.databaseurl, Constant.username, Constant.password);
            if (!connection.isClosed()) {
                System.out.println("Connection Success");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        requestObject = new JSONObject(requestJson);   //从表单中获取requestJson
        getResponseJsonObject();

        String telephone = requestObject.get("telephone").toString().trim();   //获取手机号
        String qqid = requestObject.get("qqid").toString().trim(); //获取QQ的openid
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM User WHERE telephone=" + "'" + telephone + "'");    //查询手机号是否存在
            if (resultSet.next()) {     //手机号存在
                System.out.println("Exist");
                if (resultSet.getString("qqid") == null || resultSet.getString("qqid").trim().equals("")) { //该手机号未绑定QQ
                    System.out.println("Not Bind");
                    int result = statement.executeUpdate("UPDATE User SET qqid=" + "'" + qqid + "'" + "WHERE telephone=" + "'" + telephone + "'");   //则直接更新该手机号的QQ的openid
                    if (result != 0) {  //更新成功
                        responseObject.put("result", 0);
                        System.out.println("Bind Success");
                    } else {    //更新失败
                        responseObject.put("result", -1);
                        System.out.println("Bind Failed");
                    }
                } else {    //手机号已经被绑定QQ
                    System.out.println(resultSet.getString("qqid"));
                    responseObject.put("result", 2);
                    System.out.println("Alread Bind");
                }
            } else {    //手机号不存在
                responseObject.put("result", 1);
                System.out.println("Not Exist");
            }
            printWriter.write(responseObject.toString());
            printWriter.flush();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String register() throws IOException {
        try {
            Class.forName(Constant.driver);
            connection = DriverManager.getConnection(Constant.databaseurl, Constant.username, Constant.password);
            if (!connection.isClosed()) {
                System.out.println("Connection Success");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        System.out.println("fuck");

        requestObject = new JSONObject(requestJson);    //新建json对象
        getResponseJsonObject();

        System.out.println(requestObject);
        int type = requestObject.getInt("registertype");    //判断注册类型
        String telephone = requestObject.getString("telephone");    //获取手机号
        String username = requestObject.getString("username");  //获取用户名
        System.out.println(telephone+":"+username);

        File image = new File(Constant.imageurl, telephone + ".jpg"); //生成图片文件图片名称为手机号.jpg
        FileUtils.copyFile(userImage, image);   //将获得的图片copy到制定目录

        if (type == Constant.REGISTER_PHONE) {  //手机注册类型
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO User (telephone, username, steps, wei, jing) VALUES (?, ?, 0, 0, 0)");
                /* 往数据库中插入个人信息 */
                preparedStatement.setString(1, telephone);
                preparedStatement.setString(2, username);
                System.out.println(preparedStatement.toString());
                int result = preparedStatement.executeUpdate();
                responseObject.put("result", result);   //返回结果
                printWriter.write(responseObject.toString());
                printWriter.flush();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (type == Constant.REGISTER_QQ_PHONE) {    //QQ注册类型
            String qqid = requestObject.getString("qqid"); //获取qqid
            try {
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO User (telephone, username, qqid, steps, wei, jing) VALUES (?, ?, ?, 0, 0, 0)");
                preparedStatement.setString(1, telephone);
                preparedStatement.setString(2, username);
                preparedStatement.setString(3, qqid);   //相比手机多添加一个qqid
                int result = preparedStatement.executeUpdate();
                responseObject.put("result", result);
                printWriter.write(responseObject.toString());
                printWriter.flush();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
