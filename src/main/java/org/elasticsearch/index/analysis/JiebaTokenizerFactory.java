package org.elasticsearch.index.analysis;

import com.huaban.analysis.jieba.JiebaSegmenter;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

/**
 * @author 1y
 * @date 2021/12/17
 */
public class JiebaTokenizerFactory extends AbstractTokenizerFactory {

    public static final String TokenizerName = "JiebaTokenizer";

    private String segMode;

    public JiebaTokenizerFactory(IndexSettings indexSettings, Environment env, Settings settings) {
        super(indexSettings, TokenizerName, settings);
        JiebaDict.init(env);
    }

    @Override
    public Tokenizer create() {
        return new JiebaTokenizer(segMode);
    }

    public String getSegMode() {
        return segMode;
    }

    public void setSegMode(String segMode) {
        this.segMode = segMode;
    }

    public static TokenizerFactory getJiebaSearchTokenizerFactory(IndexSettings indexSettings,
                                                                  Environment environment,
                                                                  String s,
                                                                  Settings settings) {
        JiebaTokenizerFactory jiebaTokenizerFactory = new JiebaTokenizerFactory(indexSettings,
                environment,
                settings);
        jiebaTokenizerFactory.setSegMode(JiebaSegmenter.SegMode.SEARCH.name());
        return jiebaTokenizerFactory;
    }

    public static TokenizerFactory getJiebaIndexTokenizerFactory(IndexSettings indexSettings,
                                                                 Environment environment,
                                                                 String s,
                                                                 Settings settings) {
        JiebaTokenizerFactory jiebaTokenizerFactory = new JiebaTokenizerFactory(indexSettings,
                environment,
                settings);
        jiebaTokenizerFactory.setSegMode(JiebaSegmenter.SegMode.INDEX.name());
        return jiebaTokenizerFactory;
    }
}
