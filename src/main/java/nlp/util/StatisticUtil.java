package nlp.util;

import java.util.HashMap;

public class StatisticUtil {
    private static final HashMap<String, Integer> sTotalCountMap = new HashMap<String, Integer>();
    private static final HashMap<String, Integer> sCorrectCountMap = new HashMap<String, Integer>();
    private static final HashMap<String, Double> sPrecisionMap = new HashMap<String, Double>();

    public static void clear() {
        sTotalCountMap.clear();
        sCorrectCountMap.clear();
        sPrecisionMap.clear();
    }

    public static void totalIncrement(String tag) {
        if (sTotalCountMap.containsKey(tag)) {
            sTotalCountMap.put(tag, sTotalCountMap.get(tag) + 1);
        } else {
            sTotalCountMap.put(tag, 1);
        }
    }

    public static void correctIncrement(String tag) {
        if (sCorrectCountMap.containsKey(tag)) {
            sCorrectCountMap.put(tag, sCorrectCountMap.get(tag) + 1);
        } else {
            sCorrectCountMap.put(tag, 1);
        }
    }

    private static void calculatePrecision() {
        for (String tag : sTotalCountMap.keySet()) {
            int total = sTotalCountMap.get(tag);
            int correct = sCorrectCountMap.get(tag);

            sPrecisionMap.put(tag,  ((double) correct) / total);
        }
    }

    public static String getStatistics() {
        calculatePrecision();

        StringBuffer buffer = new StringBuffer();

        for (String tag : sTotalCountMap.keySet()) {
            String info = tag + "的分类结果如下：";
            buffer.append(info)
                    .append("\n总数：").append(sTotalCountMap.get(tag))
                    .append("\n正确数：").append(sCorrectCountMap.get(tag))
                    .append("\n正确率：").append(sPrecisionMap.get(tag))
                    .append("\n");
        }

        return buffer.toString();
    }
}
