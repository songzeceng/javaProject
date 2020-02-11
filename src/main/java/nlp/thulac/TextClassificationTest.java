package nlp.thulac;

import nlp.Corpus;
import nlp.util.ArgsUtil;
import nlp.util.Config;
import nlp.util.FileUtil;
import nlp.util.StatisticUtil;
import org.apache.commons.lang.StringUtils;
import org.thunlp.text.classifiers.BasicTextClassifier;
import org.thunlp.text.classifiers.ClassifyResult;
import org.thunlp.text.classifiers.LinearBigramChineseTextClassifier;

import java.io.File;
import java.util.*;

public class TextClassificationTest {
    private BasicTextClassifier mClassifier = new BasicTextClassifier();
    private String mDefaultArguments = ""
            + " -train " + Config.THUCConfig.sTHUC_TRAIN_DATA_DIR
            + " -test " + Config.THUCConfig.sTHUC_TEST_DATA_DIR
            + " -d1 " + Config.THUCConfig.sRATIO_TRAIN
            + " -d2 " + Config.THUCConfig.sRATIO_TEST
            + " -f " + Config.THUCConfig.sFEATURE_SIZE
            + " -s " + Config.THUCConfig.sTHUC_MODEL_SAVE_DIR
            + " -svm " + Config.THUCConfig.sLIB_SVM;
    private boolean mInited = false;
    private boolean mTrained = false;

    private List<String> mCategories = new ArrayList<String>();

    private void initClassifier(Map<String, String> argsMap) {
        if (loadModel(Config.THUCConfig.sTHUC_MODEL_SAVE_DIR)) {
            mInited = true;
            System.out.println("已从本地加载模型");
            return;
        }

        String[] args = ArgsUtil.mapToArray(argsMap);

        if (args == null || args.length == 0) {
            args = mDefaultArguments.split(Config.THUCConfig.sARG_SEP);
        }
        mClassifier.Init(args);
        mInited = true;
    }

    private void trainClassifier() {
        if (!mInited) {
            initClassifier(null);
        }

        for (int i = 0; i < mClassifier.getCategorySize(); i++) {
            mCategories.add(i, mClassifier.getCategoryName(i));
        }

        mClassifier.runAsBigramChineseTextClassifier();
        mTrained = true;
    }

    private boolean loadModel(String path) {
        File modelDir = new File(path);
        if (!modelDir.exists() || !modelDir.isDirectory()) {
            return false;
        }

        try {
            mClassifier.loadCategoryListFromFile(path + "\\category");
            mClassifier.setTextClassifier(new LinearBigramChineseTextClassifier(mClassifier.getCategorySize()));
            mClassifier.getTextClassifier().loadModel(path);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void classifyText(String text) {
        classifyText(text, -1);
    }

    private void classifyText(String text, int topN) {
        if (!mTrained) {
            trainClassifier();
        }

        if (StringUtils.isEmpty(text)) {
            System.err.println("待检测字符串为空！");
            return;
        }

        topN = topN <= 0 ? mClassifier.getCategorySize() : topN;
        if (topN <= 0) {
            System.err.println("topN非正！");
            return;
        }
        ClassifyResult[] results = mClassifier.classifyText(text, topN);
        Arrays.sort(results);
        for (int i = 0; i < results.length; i++) {
            System.out.println("分类：" + results[i].label + "，概率：" + results[i].prob);
        }
    }

    private void classifyFile(String filePath) {
        classifyFile(filePath, -1);
    }

    private void classifyFile(String filePath, int topN) {
        if (!mTrained) {
            trainClassifier();
        }

        if (StringUtils.isEmpty(filePath) || !new File(filePath).exists()) {
            System.err.println("待检测文件不存在！");
            return;
        }

        topN = topN <= 0 ? mClassifier.getCategorySize() : topN;
        if (topN <= 0) {
            System.err.println("topN非正！");
            return;
        }

        ClassifyResult[] results = mClassifier.classifyFile(filePath, topN);
        Arrays.sort(results);

        String classifiedTag = "";
        StringBuffer buffer = new StringBuffer();
        buffer.append("文件" + filePath + "的分类结果：\n");
        for (ClassifyResult result : results) {
            String resultInfo = "类别：" + mCategories.get(result.label)
                    + "，概率：" + result.prob;
            buffer.append(resultInfo).append("\n");

            classifiedTag = mCategories.get(result.label);
        }

        buffer.append("**************************************************************\n");
        FileUtil.saveFileClassificationResult(filePath, buffer.toString());

        String tag = FileUtil.getFileTagByPath(filePath);
        StatisticUtil.totalIncrement(tag);
        if (tag.equals(classifiedTag)) {
            StatisticUtil.correctIncrement(tag);
        }

        System.out.println(buffer.toString());
    }

    private void classifyDir(String dirPath) {
        classifyDir(dirPath, -1);
    }

    private void classifyDir(String dirPath, int topN) {
        if (!mTrained) {
            trainClassifier();
        }

        File destDir = new File(dirPath);
        if (StringUtils.isEmpty(dirPath) || !destDir.exists() || !destDir.isDirectory()) {
            System.err.println("待检测目录不存在！");
            return;
        }

        topN = topN <= 0 ? mClassifier.getCategorySize() : topN;
        if (topN <= 0) {
            System.err.println("topN非正！");
            return;
        }

        File[] files = destDir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile()) {
                classifyFile(file.getAbsolutePath());
            } else {
                classifyDir(file.getAbsolutePath());
            }
        }
    }

    public static void main(String[] args) {
        TextClassificationTest test = new TextClassificationTest();
        HashMap<String, String> argsMap = new HashMap<String, String>();

        argsMap.put("-train", Config.THUCConfig.sTHUC_TRAIN_DATA_DIR);
        argsMap.put("-test", Config.THUCConfig.sTHUC_TEST_DATA_DIR);
        argsMap.put("-d1", "" + Config.THUCConfig.sRATIO_TRAIN);
        argsMap.put("-d2", "" + Config.THUCConfig.sRATIO_TEST);
        argsMap.put("-s", Config.THUCConfig.sTHUC_MODEL_SAVE_DIR);
        argsMap.put("-f", "" + Config.THUCConfig.sFEATURE_SIZE);
        argsMap.put("-svm", Config.THUCConfig.sLIB_SVM);

//        test.initClassifier(argsMap);
//
//        System.out.println("测试分类句子");
//        test.classifyText(Corpus.sTEST_SENTENCE);
//
//        System.out.println("测试对目录下文件进行分类");
//        StatisticUtil.clear();
//        test.classifyDir(Config.THUCConfig.sTHUC_EVALUATE_DATA_DIR);
//        FileUtil.saveInfo("statistic", StatisticUtil.getStatistics());
//
//        System.out.println("测试分类法律文件");
//        test.classifyFile(Corpus.sTEST_FILE_PATH);

        File oldDir = new File("D:\\develop\\apache-hive-2.3.5-bin\\lib"); // 257
        File newDir = new File("D:\\develop\\apache-hive-2.3.5-bin\\lib_question"); // 258

        File[] allFilesInNew = newDir.listFiles();
        assert allFilesInNew != null;

        System.out.println("file number in lib:" + allFilesInNew.length);

        for (int i = 0; i < allFilesInNew.length; i++) {
            File file = new File(oldDir.getAbsolutePath(), allFilesInNew[i].getName());
            if (!file.exists()) {
                System.out.println(file.getName());
            }
        }

        System.out.println("classification run over..");
//        FileUtil.prefixDirectoryName(
//                new File("C:\\Users\\songzeceng\\Desktop\\THUC\\THUCNewsTest\\lottery"),
//                "lottery_");


    }
}
