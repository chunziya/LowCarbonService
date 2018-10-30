package com.sam.lowcarbon;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.commons.io.FileUtils;
import org.apache.struts2.ServletActionContext;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserInfo extends ActionSupport{

    private Connection connection;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private JSONObject requestObject;
    private JSONObject responseObject;
    private PrintWriter printWriter;

    private String requestJson; //json字符串
    private File userImage; //上传过来的用户图片
    private String userImgaeFileName;   //图片名称
    private String userImageContentType;    //图片类型

    private String[] itemName = {"username", "birthday", "gender", "blood", "height", "weight"};
    private ArrayList<String> userItem;
    private int resultNum = 0;
    private int flagNum = 0;

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

    public String userInfo() throws IOException {

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
        int flagChanged = requestObject.getInt("flagChanged");
        String telephone = requestObject.get("telephone").toString().trim();   //获取手机号

        File image = new File(Constant.imageurl, telephone + ".jpg"); //生成图片文件图片名称为手机号.jpg
        FileUtils.copyFile(userImage, image);   //将获得的图片copy到制定目录

        try {
            userItem = new ArrayList<String>();
            for (int i = 0; i < 6; i++) {
                if (i < 4) {
                    userItem.add(requestObject.getString(itemName[i]));
                } else {
                    userItem.add(Integer.toString(requestObject.getInt(itemName[i])));
                }
            }

            for (int i = 0; i < 6; i++) {
                if ((flagChanged >> (5 - i) & 1) == 1) {
                    flagNum += 1;
                    if (i < 4) {
                        String str = "UPDATE User SET " + itemName[i] + "='" + userItem.get(i) + "' WHERE telephone='" + telephone + "'";
                        PreparedStatement preparedStatement = connection.prepareStatement(str);
                        int result = preparedStatement.executeUpdate();
                        resultNum += result;
                    } else {
                        String str = "UPDATE User SET " + itemName[i] + "=" + Integer.parseInt(userItem.get(i)) + " WHERE telephone='" + telephone + "'";
                        PreparedStatement preparedStatement = connection.prepareStatement(str);
                        int result = preparedStatement.executeUpdate();
                        resultNum += result;
                    }
                }
            }
            System.out.println("flagNum:" + Integer.toString(flagNum));
            System.out.println("resultNum:" + Integer.toString(resultNum));
            if (flagNum == resultNum) {
                responseObject.put("result", 1);   //返回结果
            } else {
                responseObject.put("result", 0);   //返回结果
            }
            printWriter.write(responseObject.toString());
            printWriter.flush();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String steps() throws Exception {
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
        Integer steps = requestObject.getInt("steps");

        String str = "UPDATE User SET steps=" + steps + " WHERE telephone='" + telephone + "'";
        PreparedStatement preparedStatement = connection.prepareStatement(str);
        preparedStatement.executeUpdate();
        System.out.println("Update steps success!");
        requestObject.put("resuit", 0);
        printWriter.write(responseObject.toString());
        printWriter.flush();
        return null;
    }

    public String position() throws Exception {
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
        Double wei = requestObject.getDouble("wei");
        Double jing = requestObject.getDouble("jing");
        System.out.println(Double.toString(wei) + "," + Double.toString(jing));

        String str = "UPDATE User SET wei=" + wei + ",jing=" + jing + " WHERE telephone='" + telephone + "'";
        PreparedStatement preparedStatement = connection.prepareStatement(str);
        preparedStatement.executeUpdate();
        System.out.println("Update position success!");
        requestObject.put("resuit", 0);
        printWriter.write(responseObject.toString());
        printWriter.flush();
        return null;
    }

    public void getResponseJsonObject() throws IOException { //获取responsejson对象
        response = ServletActionContext.getResponse();
        response.setCharacterEncoding("UTF-8"); //设置UTF-8编码
        printWriter = response.getWriter();
        responseObject = new JSONObject();
    }
}

