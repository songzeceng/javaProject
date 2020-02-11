package nlp.util;

import java.util.Map;

public class ArgsUtil {
    public static String[] mapToArray(Map<String, String> argsMap) {
        if (argsMap == null || argsMap.isEmpty()) {
            return null;
        }

        StringBuffer buffer = new StringBuffer();

        for (String s : argsMap.keySet()) {
            buffer.append(s).append(Config.THUCConfig.sARG_SEP)
                    .append(argsMap.get(s)).append(Config.THUCConfig.sARG_SEP);
        }

        return buffer.substring(0, buffer.lastIndexOf(Config.THUCConfig.sARG_SEP))
                .split(Config.THUCConfig.sARG_SEP);
    }
}
