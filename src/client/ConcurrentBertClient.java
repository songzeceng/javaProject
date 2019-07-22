package client;

import java.util.ArrayList;
import java.util.List;

public class ConcurrentBertClient extends BertClient {
    private ArrayList<BertClient> mAvailableClients = new ArrayList<>();
    private int mMaxConcurrency = 10;

    public ConcurrentBertClient() {
        initClients();
    }

    public ConcurrentBertClient(int maxConcurrency) {
        this.mMaxConcurrency = maxConcurrency;
        initClients();
    }

    public ConcurrentBertClient(String outputFmt, int port, int portOut, String ip, int maxConcurrency) {
        super(outputFmt, port, portOut, ip);
        this.mMaxConcurrency = maxConcurrency;
        initClients();
    }

    public ConcurrentBertClient(String outputFmt, int port, int portOut, String ip) {
        super(outputFmt, port, portOut, ip);
        initClients();
    }

    private void initClients() {
        for (int i = 0; i < mMaxConcurrency; i++) {
            mAvailableClients.add(new BertClient());
        }
    }

    public List<Object> encode(String[] text) {
        List<Object> totalResults = new ArrayList<>();
        for (int i = 0; i < mAvailableClients.size(); i++) {
            totalResults.add(mAvailableClients.get(i).encode(text[i]));
        }
        return totalResults;
    }
}
