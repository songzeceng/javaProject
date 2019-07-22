package callback;

import java.util.List;
import java.util.Map;

public interface IFetchCallback {
    public void onFetchResult(List<Map<String, Object>> fetchResults);
}
