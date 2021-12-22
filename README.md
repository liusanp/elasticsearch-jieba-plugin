# elasticsearch-jieba-plugin
Forked from：[sing1ee](https://github.com/sing1ee)/**[elasticsearch-jieba-plugin](https://github.com/sing1ee/elasticsearch-jieba-plugin)**

## 特点
- 支持动态添加字典，不重启ES。
- 添加热更新自定义配置

### 支持动态添加字典，ES不需要重启

```properties
# 字典加载方式：mysql | local
loadType=local
# 字典加载时间：分钟。默认30分钟
gapTime=30
# Mysql连接
mysql.driver=com.mysql.cj.jdbc.Driver
mysql.url=jdbc:mysql://121.0.0.1:3306/test?serverTimezone=Asia/Shanghai&characterEncoding=utf-8&useSSL=true
mysql.username=root
mysql.password=root
```

* 选择热更新本地字典时，新添加的字典文件需要不同文件名

  

### more details
- choose right version source code.
- run

```shell
git clone https://github.com/liusanp/elasticsearch-jieba-plugin.git
mvn clean package
```
- copy the zip file to plugin directory

```shell
cp target/releases/elasticsearch-jieba-plugin-6.8.8-bin.zip ${path.home}/plugins
```
- unzip and rm zip file

```shell
unzip elasticsearch-jieba-plugin-6.8.8-bin.zip
rm elasticsearch-jieba-plugin-6.8.8-bin.zip
```
- start elasticsearch

```shell
./bin/elasticsearch
```


### Custom User Dict
Just put you dict file with suffix ***.dict*** into  ${path.home}/plugins/jieba/dic. Your dict
file should like this:

```shell
小清新 3
百搭 3
显瘦 3
隨身碟 100
your_word word_freq

```


### Using stopwords
- find stopwords.txt in ${path.home}/plugins/jieba/dic.
- create folder named ***stopwords*** under ${path.home}/config

```shell
mkdir -p {path.home}/config/stopwords
```
- copy stopwords.txt into the folder just created

```shell
cp ${path.home}/plugins/jieba/dic/stopwords.txt {path.home}/config/stopwords
```
- create index:

```shell
PUT http://localhost:9200/jieba_index
```

```json
{
  "settings": {
    "analysis": {
      "filter": {
        "jieba_stop": {
          "type":        "stop",
          "stopwords_path": "stopwords/stopwords.txt"
        },
        "jieba_synonym": {
          "type":        "synonym",
          "synonyms_path": "synonyms/synonyms.txt"
        }
      },
      "analyzer": {
        "my_ana": {
          "tokenizer": "jieba_index",
          "filter": [
            "lowercase",
            "jieba_stop",
            "jieba_synonym"
          ]
        }
      }
    }
  }
}
```
- test analyzer:

```shell
PUT http://localhost:9200/jieba_index/_analyze
{
  "analyzer" : "my_ana",
  "text" : "黄河之水天上来"
}
```
Response as follow:

```json
{
    "tokens": [
        {
            "token": "黄河",
            "start_offset": 0,
            "end_offset": 2,
            "type": "word",
            "position": 0
        },
        {
            "token": "黄河之水天上来",
            "start_offset": 0,
            "end_offset": 7,
            "type": "word",
            "position": 0
        },
        {
            "token": "之水",
            "start_offset": 2,
            "end_offset": 4,
            "type": "word",
            "position": 1
        },
        {
            "token": "天上",
            "start_offset": 4,
            "end_offset": 6,
            "type": "word",
            "position": 2
        },
        {
            "token": "上来",
            "start_offset": 5,
            "end_offset": 7,
            "type": "word",
            "position": 2
        }
    ]
}
```



