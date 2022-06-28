package org.elasticsearch.index.analysis;

import com.huaban.analysis.jieba.JiebaSegmenterDiy;
import org.apache.lucene.analysis.Analyzer;

/**
 * @author 1y
 * @date 2021/12/17
 */
public class JiebaAnalyzer extends Analyzer {

    private String segMode;

    /**
     *
     */
    public JiebaAnalyzer() {
        this(JiebaSegmenterDiy.SegMode.SEARCH.name());
    }

    public JiebaAnalyzer(String segMode) {
        this.segMode = segMode;
    }

    /**
     * @param reuseStrategy
     */
    public JiebaAnalyzer(ReuseStrategy reuseStrategy) {
        super(reuseStrategy);
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        return new TokenStreamComponents(new JiebaTokenizer(this.segMode));
    }
}
