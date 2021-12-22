package org.elasticsearch.index.analysis;

import com.huaban.analysis.jieba.WordDictionary;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.env.Environment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author 1y
 * @date 2021/12/17
 */
public class JiebaDict {

    private final static Logger log = Loggers.getLogger(JiebaDict.class, "JiebaDict");

    private static JiebaDict singleton;
    private static Properties props;
    private static Date loadDate;
    private static final String propName = "self-ext.properties";
    private static ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void setLoadDate(Date loadDate) {
        JiebaDict.loadDate = loadDate;
    }

    public static JiebaDict init(Environment environment) {
        log.info("JiebaDict init...");
        if (singleton == null) {
            synchronized (JiebaDict.class) {
                if (singleton == null) {
                    // 获取配置文件
                    log.info("获取配置文件...");
                    props = new Properties();
                    InputStream in = null;
                    try {
                        Path propPath = environment.pluginsFile().resolve("jieba/" + propName);
                        in = new FileInputStream(propPath.toFile());
                        props.load(in);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        try {
                            if (null != in) {
                                in.close();
                            }
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                    long gapTime = Long.parseLong(props.getProperty("gapTime", "30"));
                    String loadType = props.getProperty("loadType", "local");
                    // 先加载本地文件夹字典
                    log.info("===init=== start to load local dict");
                    final WordDictionary wd = WordDictionary.getInstance();
                    wd.init(environment.pluginsFile().resolve("jieba/dic"));
                    // 权限包裹
                    AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                        // 定时器执行加载字典任务
                        if ("local".equalsIgnoreCase(loadType)) {
                            // 加载本地dic文件夹下的字典
                            TimerTask localTask = new TimerTask() {
                                @Override
                                public void run() {
                                    log.info("===loop=== start to load local dict");
                                    wd.init(environment.pluginsFile().resolve("jieba/dic"));
                                    JiebaDict.setLoadDate(new Date());
                                }
                            };
                            threadPool.scheduleAtFixedRate(localTask, 1, 60 * gapTime, TimeUnit.SECONDS);
                        } else {
                            // 再加载mysql字典
                            String mysqlDriver = props.getProperty("mysql.driver");
                            String mysqlUrl = props.getProperty("mysql.url");
                            String mysqlUsername = props.getProperty("mysql.username");
                            String mysqlPassword = props.getProperty("mysql.password");
                            TimerTask mysqlTask = new TimerTask() {
                                @Override
                                public void run() {
                                    log.info("===loop=== start to load mysql dict");
                                    Connection conn = null;
                                    Statement stmt = null;
                                    try {
                                        // 注册 JDBC 驱动
                                        Class.forName(mysqlDriver);
                                        // 打开链接
                                        conn = DriverManager.getConnection(mysqlUrl, mysqlUsername, mysqlPassword);
                                        // 执行查询
                                        stmt = conn.createStatement();
                                        String sql;
                                        if (loadDate != null) {
                                            sql = "SELECT id, word, freq FROM sys_es_word_dict where is_enable = 1 and create_time >= '" + sdf.format(loadDate) + "'";
                                        } else {
                                            sql = "SELECT id, word, freq FROM sys_es_word_dict where is_enable = 1";
                                        }
                                        log.info("===loop=== 查询sql：" + sql);
                                        ResultSet rs = stmt.executeQuery(sql);
                                        rs.last();
                                        log.info("===loop=== 查询结果：" + rs.getRow());
                                        rs.beforeFirst();
                                        JiebaDict.setLoadDate(new Date());
                                        // 展开结果集数据库
                                        while (rs.next()) {
                                            // 通过字段检索
                                            String word = rs.getString("word");
                                            double freq = rs.getDouble("freq");
                                            addWordAndFreq(wd, word, freq);
                                        }
                                        // 完成后关闭
                                        rs.close();
                                    } catch (Exception e) {
                                        log.error(e.getMessage(), e);
                                    } finally {
                                        // 关闭资源
                                        try {
                                            if (stmt != null) stmt.close();
                                        } catch (SQLException se) {
                                            log.error(se.getMessage(), se);
                                        }
                                        try {
                                            if (conn != null) conn.close();
                                        } catch (SQLException se) {
                                            log.error(se.getMessage(), se);
                                        }
                                    }
                                }
                            };
                            threadPool.scheduleAtFixedRate(mysqlTask, 1, 60 * gapTime, TimeUnit.SECONDS);
                        }

                        singleton = new JiebaDict();
                        return singleton;
                    });
                }
            }
        }
        return singleton;
    }

    private static String addWordAndFreq(WordDictionary wd, String word, double freq) {
        try {
            Class<? extends WordDictionary> dictClass = wd.getClass();
            Method addWordMethod = dictClass.getDeclaredMethod("addWord", String.class);
            addWordMethod.setAccessible(true);
            word = (String) addWordMethod.invoke(wd, word);
            Field totalField = dictClass.getDeclaredField("total");
            totalField.setAccessible(true);
            wd.freqs.put(word, Math.log(freq / (double) totalField.get(wd)));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return word;
    }
}
