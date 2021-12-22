package org.elasticsearch.index.analysis;

import com.huaban.analysis.jieba.SegToken;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 1y
 * @date 2021/12/17
 */
public class JiebaTokenizer extends Tokenizer {

    private CharTermAttribute termAtt;
    private OffsetAttribute offsetAtt;
    private TypeAttribute typeAtt;
    private PositionIncrementAttribute positionIncrementAttribute;
    private int endPosition;
    private SegToken lastToken = null;
    private Map<Integer, Integer> endOffset2PosIncr = new HashMap<>();

    private JiebaAdapter jieba;

    protected JiebaTokenizer(String segModeName) {

        this.offsetAtt = addAttribute(OffsetAttribute.class);
        this.termAtt = addAttribute(CharTermAttribute.class);
        this.typeAtt = addAttribute(TypeAttribute.class);
        this.positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);

        jieba = new JiebaAdapter(segModeName);
    }

    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();
        if (jieba.hasNext()) {
            SegToken token = jieba.next();
            termAtt.append(token.word);
            termAtt.setLength(token.word.length());
            offsetAtt.setOffset(token.startOffset, token.endOffset);
            Integer posIncr = 0;
            if (null == lastToken) {
                posIncr = 1;
            } else {
                if (token.word.startsWith(lastToken.word)) {
                    posIncr = 0;
                } else {
                    // 判断是否是新的切分
                    if (endOffset2PosIncr.containsKey(token.startOffset)) {
                        posIncr = 1;
                    } else {
                        if (token.endOffset <= lastToken.endOffset) {
                            posIncr = 0;
                        } else {
                            posIncr = 0;
                        }
                    }
                }
            }
            positionIncrementAttribute.setPositionIncrement(posIncr);
            endOffset2PosIncr.put(token.endOffset, posIncr);
            endPosition = token.endOffset;
            lastToken = token;
            return true;
        }
        return false;
    }

    @Override
    public void end() throws IOException {
        int finalOffset = correctOffset(this.endPosition);
        offsetAtt.setOffset(finalOffset, finalOffset);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        jieba.reset(this.input);
        lastToken = null;
        endOffset2PosIncr = new HashMap<>();
    }
}
