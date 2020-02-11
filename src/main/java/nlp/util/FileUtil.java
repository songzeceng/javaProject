package nlp.util;

import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FileUtil {
    public static void prefixFileName(File file, String prefix) {
        if (file == null || !file.exists() || StringUtils.isEmpty(prefix)) {
            return;
        }

        String oldPath = file.getAbsolutePath();
        String parentPath = oldPath.substring(0, oldPath.lastIndexOf('\\'));
        String oldName = file.getName();
        File destFile = new File(parentPath, prefix + oldName);
        if (destFile.exists()) {
            destFile.delete();
        }

        boolean copyDone = false;
        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
            outputStream = new BufferedOutputStream(new FileOutputStream(destFile));

            byte[] bytes = new byte[1024];
            int len;
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }

            copyDone = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (copyDone) {
                file.delete();
            }
        }
    }

    public static void prefixDirectory(File dir, String prefix) {
        if (dir == null || !dir.exists() || !dir.isDirectory() || StringUtils.isEmpty(prefix)) {
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                prefixDirectory(file, prefix);
            } else {
                prefixFileName(file, prefix);
            }
        }
    }

    public static String getFileTagByPath(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return "";
        }

        File destFile = new File(filePath);
        if (!destFile.exists()) {
            return "";
        }

        String parentPath = destFile.getParent();
        if (parentPath.endsWith("\\")) {
            parentPath = parentPath.substring(0, parentPath.lastIndexOf("\\"));
        }
        String tag = parentPath.substring(parentPath.lastIndexOf("\\") + 1);

        return tag;
    }

    public static void saveFileClassificationResult(String filePath, String resultInfo) {
        if (StringUtils.isEmpty(filePath) || StringUtils.isEmpty(resultInfo)) {
            return;
        }

        saveInfo(getFileTagByPath(filePath), resultInfo);
    }

    public static void saveInfo(String tag, String info) {
        if (StringUtils.isEmpty(tag) || StringUtils.isEmpty(info)) {
            return;
        }

        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH");

        String dateString = simpleDateFormat.format(date);

        BufferedOutputStream outputStream = null;
        try {
            File outputFile = new File(Config.THUCConfig.sTHUC_LOG_SAVE_DIR + "\\"
                    + dateString, tag + ".txt");

            outputFile.getParentFile().mkdirs();

            outputStream = new BufferedOutputStream(new FileOutputStream(outputFile, true));
            outputStream.write(info.getBytes("utf-8"));
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
