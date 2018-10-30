package com.sam.lowcarbon;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.HashMap;


public class FriendSocket {
    private HashMap<String, Socket> socketMap = new HashMap<String, Socket>();
    private Connection connection = null;

    public FriendSocket() throws IOException {
        try {
            Class.forName(Constant.driver); //加载驱动
            connection = DriverManager.getConnection(Constant.databaseurl, Constant.username, Constant.password);   //连接数据库
            if (!connection.isClosed()) {
                System.out.println("Connection Success");
                System.out.println("开启成功");
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        ServerSocket socket = new ServerSocket(9000);
        System.out.println("socket connect");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Socket client = socket.accept();
                        System.out.println("one more client");
                        DataInputStream in = new DataInputStream(client.getInputStream());
                        DataOutputStream out = new DataOutputStream(client.getOutputStream());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    while (true) {
                                        String msg = in.readUTF();  //此处有bug
                                        System.out.println("Socket Connected Successfully");
                                        System.out.println(msg);

                                        JSONObject jsonObject = new JSONObject(msg);
                                        int method = jsonObject.getInt("method");
                                        System.out.println(method);
                                        String user = jsonObject.getString("user");
                                        System.out.println(user);
                                        switch (method) {
                                            case Constant.SOCKET_ONLINE: { //登录时socket连接
                                                synchronized (this) {
                                                    socketMap.put(user, client);
                                                }
                                                System.out.println("online");
                                                PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM FriendRequest WHERE User2 = ?");
                                                preparedStatement.setString(1, user);
                                                ResultSet resultSet = preparedStatement.executeQuery();
                                                while (resultSet.next()) {   //每次上线推送给用户好友添加的消息
                                                    JSONObject object = new JSONObject();
                                                    object.put("method", Constant.SOCKET_FRIEND_ADD);
                                                    object.put("from", resultSet.getString("User1"));
                                                    object.put("to", resultSet.getString("User2"));
                                                    String response = object.toString();
                                                    out.writeUTF(response);
                                                    out.flush();
                                                }
                                                break;
                                            }
                                            case Constant.SOCKET_FRIEND_ADD: {  //添加好友的socket请求
                                                String adduser = jsonObject.getString("adduser");
                                                System.out.println(adduser);
                                                synchronized (this) {
                                                    if (socketMap.get(adduser) != null && socketMap.get(adduser).isConnected()) {
                                                        JSONObject object = new JSONObject();
                                                        object.put("method", Constant.SOCKET_FRIEND_ADD);
                                                        object.put("from", user);
                                                        object.put("to", adduser);
                                                        String response = object.toString();
                                                        DataOutputStream outputStream = new DataOutputStream(socketMap.get(adduser).getOutputStream());
                                                        outputStream.writeUTF(response);
                                                        outputStream.flush();
                                                        System.out.println(response);
                                                    } else {  //不在线则插入另一个表中
                                                        System.out.println("not online");
                                                        PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO FriendRequest (User1, User2) VALUES (?, ?)");
                                                        preparedStatement.setString(1, user);
                                                        preparedStatement.setString(2, adduser);
                                                        int result = preparedStatement.executeUpdate();
                                                        if (result != 0) {
                                                            System.out.println("insert success");
                                                        } else {
                                                            System.out.println("insert failed");
                                                        }
                                                    }
                                                }
                                                break;
                                            }
                                            case Constant.SOCKET_OFFLINE: {
                                                synchronized (this) {
                                                    socketMap.remove(user);
                                                }
                                                System.out.println("offline");
                                                break;
                                            }
                                            default:
                                                break;
                                        }
                                    }
                                } catch (IOException | SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
