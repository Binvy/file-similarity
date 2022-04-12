# file-similarity

simple file similarity on apache-tika, apache-commons-text.

## 实现：

    - 文件读取： 使用Apache-Tika

    - 内容相似度： 使用Apache-commons-text查找内容相似度

## 快速搭建：

### 添加maven依赖：

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-text</artifactId>
            <version>1.9</version>
        </dependency>

        <!-- Apache Tika -->
        <dependency>
            <groupId>org.apache.tika</groupId>
            <artifactId>tika-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.tika</groupId>
            <artifactId>tika-parsers-standard-package</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.tika</groupId>
            <artifactId>tika-parser-sqlite3-package</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.tika</groupId>
            <artifactId>tika-parser-scientific-package</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.tika</groupId>
            <artifactId>tika-serialization</artifactId>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.apache.tika</groupId>
                <artifactId>tika-bom</artifactId>
                <version>2.3.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

### 开放接口

- /similar/search 		# 在指定文件夹下，查询指定文件相似的文件。参数（filename:文件全路径, dirname 待查文件夹，若为空，则默认在指定文件的所在目录内进行查找。）

    请求示例：

        curl --location --request POST 'localhost:8080/similar/search' \
        --form 'filename="D:\\test\\190612010093_69a86d96c7c9075627619b8fc554ec16.doc"'

    结果：

        [
            {
                "path": "D:\\test\\190612010093_69a86d96c7c9075627619b8fc554ec16 - 副本 (2).doc",
                "name": "190612010093_69a86d96c7c9075627619b8fc554ec16 - 副本 (2).doc",
                "score": 1.0
            },
            {
                "path": "D:\\test\\190612010093_69a86d96c7c9075627619b8fc554ec16 - 副本 (5).doc",
                "name": "190612010093_69a86d96c7c9075627619b8fc554ec16 - 副本 (5).doc",
                "score": 1.0
            }
        ]

- /similar/search/dir 	# 查询指定文件夹下相似度高于指定阈值的相似文件。参数（dirname:待查文件夹）

    请求示例：

        curl --location --request POST 'localhost:8080/similar/search/dir' \
        --form 'dirname="D:\\test\\"'

    结果：

        {
            "D:\\test\\doc-demo.doc": [
                {
                    "path": "D:\\test\\doc001 - 副本.doc",
                    "name": "doc001 - 副本.doc",
                    "score": 1.0
                },
                {
                    "path": "D:\\test\\doc001.doc",
                    "name": "doc001.doc",
                    "score": 1.0
                }
            ],
            "D:\\test\\doc001 - 副本.doc": [
                {
                    "path": "D:\\test\\doc-demo.doc",
                    "name": "doc-demo.doc",
                    "score": 1.0
                },
                {
                    "path": "D:\\test\\doc001.doc",
                    "name": "doc001.doc",
                    "score": 1.0
                }
            ]
        }

- /similar/score 			# 获取指定文件的相似度。参数（leftFilename:文件1,rightFilename:文件2）

    请求示例：

        curl --location --request POST 'localhost:8080/similar/score' \
        --form 'leftFilename="D:\\test\\doc001.doc"' \
        --form 'rightFilename="D:\\test\\doc001 - 副本.doc"'

    结果：

        1.0

- /similar/score/dir 		# 查询指定文件夹下所有文件的相似度。参数（dirname:指定文件夹）

    请求示例：

        curl --location --request POST 'localhost:8080/similar/score/dir' \
        --form 'dirname="D:\\test\\"'

    结果：

        {
            "D:\\test\\doc-demo.doc": [
                {
                    "path": "D:\\test\\doc001 - 副本.doc",
                    "name": "doc001 - 副本.doc",
                    "score": 1.0
                },
                {
                    "path": "D:\\test\\doc001.doc",
                    "name": "doc001.doc",
                    "score": 1.0
                }
            ],
            "D:\\test\\doc001 - 副本.doc": [
                {
                    "path": "D:\\test\\doc-demo.doc",
                    "name": "doc-demo.doc",
                    "score": 1.0
                },
                {
                    "path": "D:\\test\\doc001.doc",
                    "name": "doc001.doc",
                    "score": 1.0
                }
            ]
        }

### 相关资源：

- 内容相似度相关：

    - 浅析文本相似度： https://blog.csdn.net/qq_28031525/article/details/79596376

    - NLP点滴——文本相似度： https://www.cnblogs.com/xlturing/p/6136690.html

    - Text Similarities : Estimate the degree of similarity between two texts： https://medium.com/@adriensieg/text-similarities-da019229c894 （https://github.com/adsieg）

    - GitHub:

        - java-string-similarity: https://github.com/tdebatty/java-string-similarity

        - string-similarity: https://github.com/aceakash/string-similarity

        - similarity: https://github.com/shibing624/similarity

    - Classifier4J: http://classifier4j.sourceforge.net/index.html
        
        是一个用于进行文本分类的 Java 库。它带有贝叶斯分类器的实现，现在还有一些其他功能，包括文本摘要工具。

- Apache-Tika:

    官网： https://tika.apache.org/

    说明： The Apache Tika™ toolkit detects and extracts metadata and text from over a thousand different file types (such as PPT, XLS, and PDF). All of these file types can be parsed through a single interface, making Tika useful for search engine indexing, content analysis, translation, and much more.

    开始： https://tika.apache.org/2.3.0/gettingstarted.html

    示例： https://tika.apache.org/2.3.0/examples.html
    
    转化器Parser： https://tika.apache.org/2.3.0/parser.html

    检测器Detector： https://tika.apache.org/2.3.0/detection.html

- Apache-commons-text:

    [官网](https://commons.apache.org/proper/commons-text)

    [指南](https://commons.apache.org/proper/commons-text/userguide.html)

  文字链接 [链接名称](http://链接网址)


















