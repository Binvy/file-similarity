## API Reference

### Overview
- **Project**: file-similarity (Spring Boot)
- **Purpose**: Compute similarity between files using Apache Tika for content extraction and Apache Commons Text for similarity metrics.
- **Primary packages**: `com.example.demo.controller`, `com.example.demo.service`, `com.example.demo.util`, `com.example.demo.model`, `com.example.demo.task`

---

## REST API
Base URL: `http://localhost:8080`

### Health & Greeting
- **GET /**: Returns "Hello World!"
- **GET /hello?name=YourName**: Returns "Hello {name}!"

Example:
```bash
curl -s 'http://localhost:8080/hello?name=Alice'
```

### Similarity Endpoints (`SimilarityController`)
Notes:
- Content type can be `application/x-www-form-urlencoded`, `multipart/form-data`, or query params for POST.
- All similarity scores are in range [0.0, 1.0]. Default "similar" threshold: 0.75.
- Results are cached in-memory for 60 minutes.

#### 1) POST /similar/search
- **Description**: Find files similar to a specified file within a directory (recursively).
- **Params**:
  - **filename** (string, required): Absolute path to the source file.
  - **dirname** (string, optional): Directory to search. If omitted, uses the directory of `filename`.
- **Response**: `List<SimilarFile>` ordered by `score` desc.
- **Example**:
```bash
curl -X POST 'http://localhost:8080/similar/search' \
  -d 'filename=/data/docs/sample.docx'
```
- **Sample response**:
```json
[
  {"path":"/data/docs/sample-copy.docx","name":"sample-copy.docx","score":1.0},
  {"path":"/data/docs/sample-variant.docx","name":"sample-variant.docx","score":0.86}
]
```

#### 2) POST /similar/search/dir
- **Description**: Find pairs of similar files within a directory (recursively), filtered by threshold (≥ 0.75).
- **Params**:
  - **dirname** (string, required): Directory to search.
- **Response**: `Map<String, List<SimilarFile>>` mapping each file path to its similar files.
- **Example**:
```bash
curl -X POST 'http://localhost:8080/similar/search/dir' \
  -d 'dirname=/data/docs'
```

#### 3) POST /similar/score
- **Description**: Get similarity score between two files.
- **Params**:
  - **leftFilename** (string, required)
  - **rightFilename** (string, required)
- **Response**: `Double` similarity score.
- **Example**:
```bash
curl -X POST 'http://localhost:8080/similar/score' \
  -d 'leftFilename=/data/docs/a.pdf' \
  -d 'rightFilename=/data/docs/b.pdf'
```

#### 4) POST /similar/score/dir
- **Description**: Compute similarity scores for all file pairs within a directory (recursively). No threshold filter.
- **Params**:
  - **dirname** (string, required)
- **Response**: `Map<String, List<SimilarFile>>`
- **Example**:
```bash
curl -X POST 'http://localhost:8080/similar/score/dir' \
  -d 'dirname=/data/docs'
```

---

## Java API

### `SimilarityService`
File: `src/main/java/com/example/demo/service/SimilarityService.java`

- **List<SimilarFile> search(String filename, String dirname)**: Find similar files to `filename` in `dirname` (or `filename`'s parent if null/blank).
- **Map<String, List<SimilarFile>> search(String dirname)**: Find similar files within a directory (threshold ≥ 0.75).
- **Double getSimilarityScore(String leftFilename, String rightFilename)**: Score between two files.
- **Map<String, List<SimilarFile>> getSimilarityScore(String dirname)**: Scores for all pairs within a directory.

Implementation: `SimilarityServiceImpl` (`@Service`), with in-memory caches (size 10,000; TTL 60 minutes).

Usage example:
```java
import com.example.demo.service.SimilarityService;
import com.example.demo.model.SimilarFile;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class SimilarityExample {
    private final SimilarityService similarityService;

    public SimilarityExample(SimilarityService similarityService) {
        this.similarityService = similarityService;
    }

    public void run() {
        Double score = similarityService.getSimilarityScore("/data/a.docx", "/data/b.docx");
        List<SimilarFile> list = similarityService.search("/data/a.docx", null);
        Map<String, List<SimilarFile>> dir = similarityService.search("/data/docs");
    }
}
```

### `SimilarityUtils`
File: `src/main/java/com/example/demo/util/SimilarityUtils.java`

- **Double getJaccardSimilarity(CharSequence left, CharSequence right)**
- **Double getCosineSimilarity(CharSequence left, CharSequence right)**: Returns `1 - CosineDistance`.
- **Double getJaroWinklerSimilarity(CharSequence left, CharSequence right)**
- **Integer getFuzzyDistance(CharSequence term, CharSequence query)**
- **Integer getLevenshteinDistance(CharSequence left, CharSequence right)**

Example:
```java
Double jaccard = SimilarityUtils.getJaccardSimilarity("hello world", "hello world!");
Double cosine = SimilarityUtils.getCosineSimilarity("abc", "abcd");
Integer levenshtein = SimilarityUtils.getLevenshteinDistance("kitten", "sitting");
```

### `TikaUtils`
File: `src/main/java/com/example/demo/util/TikaUtils.java`

Content extraction helpers (use Apache Tika):
- **String parseToString(String filename)**: Simple extraction via `Tika.parseToString`.
- **String parse(String filename)**: Uses `AutoDetectParser`; extracts body content; includes embedded by default.
- **String parseNoEmbedded(String filename)**: Excludes embedded content.
- **String parseEmbedded(String filename)**: Includes embedded content explicitly.
- **List<Metadata> recursiveParserWrapper(String filename)**: Extracts tree of metadata and content.
- **String serializedRecursiveParserWrapper(String filename)**: JSON of metadata list.

Example:
```java
String content = TikaUtils.parse("/data/docs/report.pdf");
```

### Model: `SimilarFile`
File: `src/main/java/com/example/demo/model/SimilarFile.java`
- **Fields**: `path` (String), `name` (String), `score` (Double)
- Used in REST responses and service return types.

---

## Background Task

### `FileSimilarityTask`
File: `src/main/java/com/example/demo/task/FileSimilarityTask.java`
- **Schedule**: Runs every 10 minutes (`@Scheduled(fixedDelay = 10 * 60 * 1000)`).
- **Config**: Uses property `smp.parser.file.dir` for file root directory.
- **Behavior**: Reads pending file IDs from `SmpFileTaskQueue`, computes similarity against existing records via `SimilarityService.getSimilarityScore`, writes results to `SmpFileSimilarityResult`, and marks tasks as completed.
- Requires database tables/entities defined by MyBatis-Plus mappers.

---

## Constants

### `SimilarityConstants`
File: `src/main/java/com/example/demo/constant/SimilarityConstants.java`
- **SCORE_ZERO**: 0.0
- **SCORE_HALF**: 0.5
- **SCORE_SIMILAR_LOW**: 0.7
- **SCORE_SIMILAR**: 0.75 (default threshold for "similar")
- **SCORE_SIMILAR_HIGH**: 0.8
- **SCORE_SIMILAR_ALMOST**: 0.95
- **SCORE_SAME**: 1.0

### `SmpParserDirConstants`
File: `src/main/java/com/example/demo/constant/SmpParserDirConstants.java`
- Directory names used across the system (e.g., `abnormal`, `alarm`, `app_behavior`, `audit`, `block`, `detector_register`, `net_log`, `object_listen`, `plug`, `policy`, `sensitive`, `status`).

---

## Build & Run
- **Build**: `mvn -q -DskipTests package`
- **Run**: `mvn spring-boot:run` or `java -jar target/file-similarity-0.0.1.jar`
- **Java**: 1.8
- **Default port**: 8080

---

## Notes & Best Practices
- Provide absolute file paths accessible to the application process.
- Large files: Tika content extraction may be expensive; ensure adequate heap and consider excluding embedded content if not needed.
- Caching: Similarity results are cached for 60 minutes; clear cache by restarting the app.
- Threshold: Endpoint `/similar/search/dir` uses threshold 0.75; adjust server-side logic to change.