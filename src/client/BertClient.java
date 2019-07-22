package client;

import callback.IEncodeResult;
import callback.IFetchCallback;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import util.Utils;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class BertClient {
    private static final String KEY_ID = "id";
    private static final String KEY_CONTENT = "content";
    public static final String CHARSET_NAME = StandardCharsets.US_ASCII.name();
    private ZContext mContext;
    private ZMQ.Socket mSendSocket;
    private ZMQ.Socket mRecvSocket;

    private long mRequestId = 0L;
    private long mTimeout = -1L;
    private int mMaxRetryCount = 0;

    private HashSet<Long> mPendingRequest = new HashSet<Long>();
    private HashMap<Long, List<byte[]>> mPendingResponse = new HashMap<Long, List<byte[]>>();

    private ExecutorService mExecutorService = Executors.newFixedThreadPool(2);

    private String mOutputFmt = "ndarray";
    private int mPort = 5555;
    private int mPortOut = 5556;
    private String mIp = "localhost";
    private long mLengthLimit = 0L;
    private boolean mTokenInfoAvailable = false;
    private String mIdentity;


    private URLConnection mSendConnection;
    private static long sStartTime, sEndTime;

    public BertClient() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BertClient(String outputFmt, int port, int portOut, String ip) {
        mOutputFmt = outputFmt;
        mPort = port;
        mPortOut = portOut;
        mIp = ip;

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception {
        mContext = new ZContext();
        String url = "tcp://" + mIp + ":";
        mIdentity = UUID.randomUUID().toString();

        mSendSocket = mContext.createSocket(SocketType.PUSH);
        mSendSocket.setLinger(0);
        mSendSocket.connect(url + mPort);

        mRecvSocket = mContext.createSocket(SocketType.SUB);
        mRecvSocket.setLinger(0);
        mRecvSocket.subscribe(mIdentity.getBytes(CHARSET_NAME));
        mRecvSocket.connect(url + mPortOut);

        /*
        curl -X POST http://xx.xx.xx.xx:8125/encode \
  -H 'content-type: application/json' \
  -d '{"id": 123,"texts": ["hello world"], "is_tokenized": false}'
         */
//        mSendConnection = new URL(url + mPort + "/encode").openConnection();
//        mSendConnection.addRequestProperty("content-type", "application/json");
//        mSendConnection.setDoInput(true);
//        mSendConnection.setDoOutput(true);
//        mSendConnection.connect();

//        mRecvSocket.setReceiveTimeOut(2 * 1000);
//        JSONObject status = serverStatus();
//        System.out.println(status.toString());
    }

    public void close() throws IOException {
        mSendSocket.close();
        mRecvSocket.close();
        mContext.destroy();
    }

    private JSONObject serverStatus() {
        long requestId = send("SHOW_CONFIG");
        Map<String, Object> configMap = recv(requestId);
        List<byte[]> content = (List<byte[]>) configMap.get(KEY_CONTENT);
        return new JSONObject(new String(content.get(1)));
    }

    public long send(String message) {
        return send(message, 0);
    }

    public long send(String message, int messageLen) {
        return send(new String[]{message}, messageLen);
    }

    public long send(String[] message, int messageLen) {
        mRequestId++;
        Gson gson = new Gson();
        sendMultiPart(new String[]{mIdentity, gson.toJson(message), mRequestId + "", messageLen + ""});
//        sendHttp(new String[]{mIdentity, gson.toJson(message), mRequestId + "", messageLen + ""});
        mPendingRequest.add(mRequestId);
        return mRequestId;
    }

    private void sendMultiPart(String[] msgParts) {
        try {
            int i;
            for (i = 0; i < msgParts.length - 1; i++) {
                mSendSocket.sendMore(msgParts[i].getBytes(CHARSET_NAME));
            }
            mSendSocket.send(msgParts[i].getBytes(CHARSET_NAME), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendHttp(String[] msgParts) {
        PrintWriter writer = null;
        try {
            int i;
            writer = new PrintWriter(mSendConnection.getOutputStream());
            for (i = 0; i < msgParts.length - 1; i++) {
                writer.print(msgParts[i].getBytes(CHARSET_NAME));
                writer.flush();
            }
            writer.print(msgParts[i].getBytes(CHARSET_NAME));
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public Map<String, Object> recv() {
        return recv(null);
    }

    public Map<String, Object> recv(Long waitForReqId) {
        try {
            while (true) {
                if (waitForReqId != null && mPendingResponse.containsKey(waitForReqId)) {
                    List<byte[]> response = mPendingResponse.get(waitForReqId);
                    HashMap<String, Object> resultMap = new HashMap<String, Object>();
                    resultMap.put(KEY_ID, waitForReqId);
                    resultMap.put(KEY_CONTENT, response);
                    return resultMap;
                }

                List<byte[]> response = recvMutipart(0);
//                List<byte[]> response = readHttp();
                if (response == null || response.size() == 0) {
                    return null;
                }

                long requestId = Utils.byte2Long(response.get(response.size() - 1));


                if (waitForReqId == null || waitForReqId == requestId) {
                    mPendingRequest.remove(requestId);
                    HashMap<String, Object> resultMap = new HashMap<String, Object>();
                    resultMap.put(KEY_ID, requestId);
                    if (response != null) {
                        resultMap.put(KEY_CONTENT, response);
                    }
                    return resultMap;
                } else if (waitForReqId != requestId) {
                    mPendingResponse.put(requestId, response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (waitForReqId != null && mPendingRequest.contains(waitForReqId)) {
                mPendingRequest.remove(waitForReqId);
            }
        }
        return null;
    }

    private List<byte[]> recvMutipart(int flag) {
        ArrayList<byte[]> result = new ArrayList<byte[]>();
        byte[] item = mRecvSocket.recv(flag);
        if (item != null) {
            result.add(item);
        }
        while (mRecvSocket.hasReceiveMore()) {
            item = mRecvSocket.recv(flag);
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }

    private List<byte[]> readHttp() throws IOException {
        BufferedInputStream inputStream = null;
        ArrayList<byte[]> result = new ArrayList<byte[]>();
        byte[] item = new byte[1024];
        try {
            inputStream = new BufferedInputStream(mSendConnection.getInputStream());
            int len;
            while ((len = inputStream.read(item)) != -1) {
                result.add(Arrays.copyOfRange(item, 0, len));
            }

            return result;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return null;
    }

    public List<Object> encode(List<Character> texts) {
        return encode(texts, true, false);
    }

    public List<Object> encodeTokenized(List<List<Character>> texts) {
        return encodeTokenized(texts, true, false);
    }

    public List<Object> encodeTokenized(List<List<Character>> textListList, boolean blocking, boolean showTokens) {
        if (!Utils.checkInputListListStr(textListList)) return null;
        return null;
    }

    public List<Object> encode(List<Character> texts, boolean blocking, boolean showTokens) {
        if (!Utils.checkInputListStr(texts)) return null;

        String[] textStr = Utils.textListToStringArray(texts);
        return encode(textStr, blocking, showTokens);
    }

    public List<Object> encode(String text) {
        String[] textArray = new String[text.length()];
        for (int i = 0; i < text.length(); i++) {
            textArray[i] = "" + text.charAt(i);
        }
        return encode(textArray, true, false);
    }

    public List<Object> encode(String text, boolean blocking, boolean showTokens) {
        String[] textArray = new String[text.length()];
        for (int i = 0; i < text.length(); i++) {
            textArray[i] = "" + text.charAt(i);
        }
        return encode(textArray, blocking, showTokens);
    }

    private List<Object> encode(String[] textStr, boolean blocking, boolean showTokens) {
        sStartTime = System.currentTimeMillis();
        long requestId = send(textStr, textStr.length);
        sEndTime = System.currentTimeMillis();
        System.out.println("send时间：" + (sEndTime - sStartTime) + "毫秒");
        if (!blocking) return null;

        sStartTime = System.nanoTime();
        Map<String, Object> ndarrayMap = recvNdarray(requestId);
        sEndTime = System.nanoTime();
        System.out.println("获取ndarray时间：" + ((sEndTime - sStartTime)/1000000) + "毫秒");
        if (ndarrayMap == null) {
            return null;
        }

        sStartTime = System.currentTimeMillis();
        List<Float> floatList = (List<Float>) ndarrayMap.get("embedding");
        JSONArray shape = (JSONArray) ndarrayMap.get("shape");
        if (mTokenInfoAvailable && showTokens) {
            String token = (String) ndarrayMap.get("token");
            return Arrays.asList(floatList, shape, token);
        } else {
            sEndTime = System.currentTimeMillis();
            System.out.println("处理信息时间：" + (sEndTime - sStartTime) + "毫秒");
            return Arrays.asList(floatList, shape, "");
        }
    }

    public Map<String, Object> recvNdarray() {
        return recvNdarray(null);
    }

    public Map<String, Object> recvNdarray(Long waitForReqId) {
        HashMap<String, Object> recvMap = (HashMap<String, Object>) recv(waitForReqId);
        if (recvMap == null || !recvMap.containsKey(KEY_CONTENT)) {
            return null;
        }
        long requestId = Long.parseLong(String.valueOf(recvMap.get(KEY_ID)));
        List<byte[]> content = (List<byte[]>) recvMap.get(KEY_CONTENT);
        JSONObject jsonObject = new JSONObject(new String(content.get(1)));
        String type = jsonObject.getString("dtype");
        if (type.contains("float")) {
            HashMap<String, Object> retMap = new HashMap<String, Object>();
            retMap.put(KEY_ID, requestId);
            retMap.put("embedding", Utils.byte2float(content.get(2)));
            retMap.put("tokens", jsonObject.optString("tokens", " "));
            retMap.put("shape", jsonObject.get("shape"));
            return retMap;
        }
        return null;
    }

    public void fetch(long delay, final IFetchCallback fetchCallback) {
        try {
            if (delay > 0L) {
                Thread.sleep(delay);
            }
            mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    ArrayList<Map<String, Object>> fetchResults = new ArrayList<>();

                    while (mPendingRequest.size() > 0) {
                        Map<String, Object> recvMap = recvNdarray();
                        if (recvMap != null) {
                            fetchResults.add(recvMap);
                        }
                    }

                    if (fetchCallback != null) {
                        fetchCallback.onFetchResult(fetchResults);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void encodeAsync(final String[] texts, final boolean blocking, final boolean showTokens
            , final long delay, final IEncodeResult encodeCallback, final IFetchCallback fetchCallback) {
        try {
            mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    List<List<Object>> encodeResults = new ArrayList<>();
                    for (int i = 0; i < texts.length; i++) {
                        List<Object> eachResult = encode(texts[i], blocking, showTokens);
                        if (eachResult != null) {
                            encodeResults.add(eachResult);
                        }
                    }

                    if (encodeCallback != null) {
                        encodeCallback.onEncodeResult(encodeResults);
                    }
                }
            });
            fetch(delay, fetchCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
