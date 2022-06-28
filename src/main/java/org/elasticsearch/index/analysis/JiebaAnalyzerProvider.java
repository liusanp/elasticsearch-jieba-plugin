package org.elasticsearch.index.analysis;

import com.huaban.analysis.jieba.JiebaSegmenterDiy;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

/**
 * @author 1y
 * @date 2021/12/17
 */
public class JiebaAnalyzerProvider extends AbstractIndexAnalyzerProvider<JiebaAnalyzer> {

    private JiebaAnalyzer jiebaAnalyzer;

    public JiebaAnalyzerProvider(IndexSettings indexSettings,
                                 Environment environment,
                                 String name,
                                 Settings settings,
                                 JiebaSegmenterDiy.SegMode mode) {
        super(indexSettings, name, settings);
        if (null != mode) {
            jiebaAnalyzer = new JiebaAnalyzer(mode.name());
        } else {
            jiebaAnalyzer = new JiebaAnalyzer(settings.get("segMode", JiebaSegmenterDiy.SegMode.SEARCH.name()));
        }
        JiebaDict.init(environment);
    }

    @Override
    public JiebaAnalyzer get() {
        return jiebaAnalyzer;
    }

    public static AnalyzerProvider<? extends Analyzer> getJiebaSearchAnalyzerProvider(IndexSettings indexSettings,
                                                                                      Environment environment,
                                                                                      String s,
                                                                                      Settings settings) {
        JiebaAnalyzerProvider jiebaAnalyzerProvider = new JiebaAnalyzerProvider(indexSettings,
                environment,
                s,
                settings,
                JiebaSegmenterDiy.SegMode.SEARCH);

        return jiebaAnalyzerProvider;
    }

    public static AnalyzerProvider<? extends Analyzer> getJiebaIndexAnalyzerProvider(IndexSettings indexSettings,
                                                                                     Environment environment,
                                                                                     String s,
                                                                                     Settings settings) {
        JiebaAnalyzerProvider jiebaAnalyzerProvider = new JiebaAnalyzerProvider(indexSettings,
                environment,
                s,
                settings,
                JiebaSegmenterDiy.SegMode.INDEX);

        return jiebaAnalyzerProvider;
    }

}
