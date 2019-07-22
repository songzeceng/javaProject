import callback.IEncodeResult;
import callback.IFetchCallback;
import client.BertClient;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.*;
import java.util.*;

public class Main {
    private static long sStartTime = 0L, sEndTime = 0L;

    public static void main(String[] args) {
        // C:\Users\songzeceng\Desktop\postgraduation\hiveJava\res\README.md
        try {
            String textToEncode = "helloworld";
            System.out.println("待编码字符串：" + textToEncode + "，长度：" + textToEncode.length());

            final BertClient bertClient = new BertClient();
            sStartTime = System.currentTimeMillis();
            List<Object> encodeRet = bertClient.encode(textToEncode);
            sEndTime = System.currentTimeMillis();
            System.out.println("编码结束，用时：" + (sEndTime - sStartTime) + "毫秒");
//            if (encodeRet != null) {
//                System.out.println("编码结果大小：" + encodeRet.size());
//                if (encodeRet.size() > 1) {
//                    List<Float> encodeResults = (List<Float>) encodeRet.get(0);
//                    System.out.println("编码浮点列表长度：" + encodeResults.size());
//                    for (int i = 0; i < 10; i++) {
//                        System.out.println(encodeResults.get(i));
//                    }
//                }
//            }

//            sleep();
//            System.out.println("----------------------------------");
//
//            for (int i = 0; i < 3; i++) {
//                bertClient.encode(textToEncode, false, false);
//            }
//
//            sStartTime = System.currentTimeMillis();
//            bertClient.fetch(0L, new IFetchCallback() {
//                @Override
//                public void onFetchResult(List<Map<String, Object>> fetchResults) {
//                    sEndTime = System.currentTimeMillis();
//                    System.out.println("fetch结束，用时：" + (sEndTime - sStartTime) + "毫秒");
////                    printFetchResult(fetchResults);
//                }
//            });

//            sleep();
//            System.out.println("----------------------------------");
//
//            sStartTime = System.currentTimeMillis();
//            bertClient.encodeAsync(new String[]{textToEncode, textToEncode, textToEncode},
//                    false, false,
//                    1000L, new IEncodeResult() {
//                        @Override
//                        public void onEncodeResult(List<List<Object>> encodeResults) {
//                            sEndTime = System.currentTimeMillis();
//                            System.out.println("异步编码结束，用时：" + (sEndTime - sStartTime) + "毫秒");
//
////                            System.out.println("onEncodeResult begin");
////                            System.out.println("总链表长度：" + encodeResults.size());
////                            for (int i = 0; i < encodeResults.size(); i++) {
////                                List<Object> item = encodeResults.get(i);
////                                System.out.println("单个链表长度：" + item.size());
////                            }
////                            System.out.println("onEncodeResult end");
//                        }
//                    }, new IFetchCallback() {
//                        @Override
//                        public void onFetchResult(List<Map<String, Object>> fetchResults) {
//                            sEndTime = System.currentTimeMillis();
//                            System.out.println("异步编码fetch结束，用时：" + (sEndTime - sStartTime) + "毫秒");
//
//                            printFetchResult(fetchResults);
//                            System.exit(0);
//                        }
//                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
//        testHiveJdbc();
    }

    private static void sleep() {
        try {
            Thread.sleep(5 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printFetchResult(List<Map<String, Object>> fetchResults) {
        System.out.println("on fetch result");
        for (int i = 0; i < fetchResults.size(); i++) {
            Map<String, Object> item = fetchResults.get(i);
            if (item == null) {
                continue;
            }
            for (String key : item.keySet()) {
                Object value = item.get(key);
                System.out.println("fetchResult:key:" + key + ", value:" + value.toString());
            }
        }
        System.out.println("end of result");
    }

    private static void printArray(float[] arrays) {
        for (int i = 0; i < arrays.length; i++) {
            System.out.println(arrays[i]);
        }
    }

    private static void testHiveJdbc() {
        String driverName = "org.apache.hive.jdbc.HiveDriver";
        try {
            Class.forName(driverName);
            String url = "jdbc:hive2://localhost:10001/test;transportMode=http;httpPath=cliservice";
            Connection con = DriverManager.getConnection(url,
                    "hive", "hive");

            executeQuery(con, "select * from teammates");
            insertValue(con, 2, "Mike", "Strike");
            updateValue(con, 2, "Mike", "defender");
            executeQuery(con, "select * from teammates");

            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void insertValue(Connection con, int id, String name, String position) throws Exception {
        PreparedStatement preparedStmt = con.prepareStatement("insert into teammates values(?, ?, ?)");
        preparedStmt.setInt(1, id);
        preparedStmt.setString(2, name);
        preparedStmt.setString(3, position);
        int ret = preparedStmt.executeUpdate();
        System.out.println("ret = " + ret);
        preparedStmt.close();
    }

    private static void updateValue(Connection con, int id, String name, String position) throws Exception {
        PreparedStatement preparedStmt = con.prepareStatement("update teammates set name=?, position=? where id=?");
        preparedStmt.setInt(3, id);
        preparedStmt.setString(1, name);
        preparedStmt.setString(2, position);
        int ret = preparedStmt.executeUpdate();
        System.out.println("ret = " + ret);
        preparedStmt.close();
    }

    private static void executeQuery(Connection con, String str) throws Exception {
        Statement stmt = con.createStatement();

        ResultSet result = stmt.executeQuery(str);

        File newFile = new File("C:\\Users\\123\\Desktop\\es.txt");
        if (!newFile.exists()) {
            newFile.createNewFile();
        }
        PrintWriter writer = new PrintWriter(newFile);
        writer.println("[");
        while (result.next()) {

            int id = result.getInt("id");
            String name = result.getString("name");
            String position = result.getString("position");
            writer.println(id + "," + name + "," + position);
            System.out.println(id + "," + name + "," + position);
        }
        writer.println("]");
        writer.close();

        stmt.close();
    }
}
