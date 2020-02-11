package nlp.util;

public class Config {
    public static class THUCConfig {
        public static final String sARG_SEP = " ";

        public static final String sTHUC_ROOT_DIR = "C:\\Users\\songzeceng\\Desktop\\THUC";
        public static final String sTHUC_TRAIN_DATA_DIR = sTHUC_ROOT_DIR + "\\THUCNews";
        public static final String sTHUC_TEST_DATA_DIR = sTHUC_ROOT_DIR + "\\THUCNews";
        public static final String sTHUC_EVALUATE_DATA_DIR = sTHUC_ROOT_DIR + "\\THUCNewsTest";

        public static final String sTHUC_LOG_SAVE_DIR = sTHUC_ROOT_DIR + "\\output";

        public static final String sTHUC_MODEL_SAVE_DIR = sTHUC_ROOT_DIR + "\\my_novel_model";

        public static final String sLIB_SVM = "libsvm";
        public static final String sLIB_LINEAR = "liblinear";

        public static final int sTOP_N = 1;
        public static final int sFEATURE_SIZE = 5000;
        public static final float sRATIO_TRAIN = 0.8f;
        public static final float sRATIO_TEST = 0.2f;
    }

    public static class HanLPConfig {
        public static final String sHANLP_ROOT_DIR = "D:/develop/Python/Python36/lib/site-packages/pyhanlp/static";
    }
}
