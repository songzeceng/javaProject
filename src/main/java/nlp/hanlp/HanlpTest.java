package nlp.hanlp;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.mining.word.WordInfo;
import com.hankcs.hanlp.utility.Predefine;
import nlp.util.Config;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static nlp.Corpus.sTEST_SENTENCE;

public class HanlpTest {


    static {
        System.setProperty("HANLP_ROOT", Config.HanLPConfig.sHANLP_ROOT_DIR);

        Logger.getLogger("HanLP").setLevel(Level.ALL);

        HanLP.Config.Normalization = true;
    }

    public static void main(String[] args) {
        File binFile = new File(HanLP.Config.CoreDictionaryPath + Predefine.BIN_EXT);
        if (binFile.exists()) {
            binFile.delete();
        }

        List<WordInfo> words = HanLP.extractWords(sTEST_SENTENCE, 20);

        System.out.println(HanLP.extractKeyword(sTEST_SENTENCE, 10));
    }
}
