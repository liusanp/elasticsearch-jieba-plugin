package org.elasticsearch.index.analysis;

import com.huaban.analysis.jieba.JiebaSegmenterDiy;
import com.huaban.analysis.jieba.JiebaSegmenterDiy.SegMode;
import com.huaban.analysis.jieba.SegToken;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

/**
 * @author 1y
 * @date 2021/12/17
 */
public class JiebaAdapter implements Iterator<SegToken> {
    private final static Logger log = Loggers.getLogger(JiebaDict.class, "JiebaAdapter");
    private final static JiebaSegmenterDiy jiebaTagger = new JiebaSegmenterDiy();

    private final SegMode segMode;

    private Iterator<SegToken> tokens;

    private String raw = null;

    public JiebaAdapter(String segModeName) {


        log.info("init jieba adapter");
        if (null == segModeName) {
            segMode = SegMode.SEARCH;
        } else {
            segMode = SegMode.valueOf(segModeName);
        }
    }

    public synchronized void reset(Reader input) {
        try {
            StringBuilder bdr = new StringBuilder();
            char[] buf = new char[1024];
            int size = 0;
            while ((size = input.read(buf, 0, buf.length)) != -1) {
                String tempstr = new String(buf, 0, size);
                bdr.append(tempstr);
            }
            raw = bdr.toString().trim();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        List<SegToken> list = jiebaTagger.process(raw, segMode);
        list.sort((o1, o2) -> {
            if (o1.startOffset < o2.startOffset) {
                return -1;
            }
            if (o1.startOffset > o2.startOffset) {
                return 1;
            }
            if (o1.startOffset == o2.startOffset) {
                if (o1.endOffset < o2.endOffset) {
                    return -1;
                }
                return 1;
            }
            return 0;
        });
        tokens = list.iterator();
    }

    @Override
    public boolean hasNext() {
        return tokens.hasNext();
    }

    @Override
    public SegToken next() {
        return tokens.next();
    }

    @Override
    public void remove() {
        tokens.remove();
    }
}
