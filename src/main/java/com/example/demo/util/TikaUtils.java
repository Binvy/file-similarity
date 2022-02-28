package com.example.demo.util;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.serialization.JsonMetadataList;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.EmptyParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.RecursiveParserWrapper;
import org.apache.tika.sax.BasicContentHandlerFactory;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ContentHandlerFactory;
import org.apache.tika.sax.RecursiveParserWrapperHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

/**
 * Apache Tika Utils
 * @author hanbinwei
 * @date 2022/2/22 14:48
 */
public class TikaUtils {

    private static final Logger logger = LoggerFactory.getLogger(TikaUtils.class);

    /**
     * use Tika's parseToString method to parse the content of a file,
     * and return any text found.
     * <p>
     * Note: Tika.parseToString() will extract content from the outer container
     * document and any embedded/attached documents.
     *
     * @param filename
     * @return The content of a file.
     */
    public static String parseToString(String filename) {
        Tika tika = new Tika();
        tika.setMaxStringLength(-1);
        try (InputStream stream = new FileInputStream(filename)) {
            return tika.parseToString(stream);
        } catch (IOException | TikaException e) {
            logger.error("parse exception. file:{}", filename, e);
        }
        return null;
    }

    /**
     * use Tika to parse a file when you do not know its file type ahead of time.
     * <p>
     * AutoDetectParser attempts to discover the file's type automatically, then call
     * the exact Parser built for that file type.
     * <p>
     * The stream to be parsed by the Parser. In this case, we get a file from the
     * resources folder of this project.
     * <p>
     * Handlers are used to get the exact information you want out of the host of
     * information gathered by Parsers. The body content handler, intuitively, extracts
     * everything that would go between HTML body tags.
     * <p>
     * The Metadata object will be filled by the Parser with Metadata discovered about
     * the file being parsed.
     * <p>
     * Note: This will extract content from the outer document and all
     * embedded documents.  However, if you choose to use a {@link ParseContext},
     * make sure to set a {@link Parser} or else embedded content will not be
     * parsed.
     *
     * @param filename
     * @return The content of a file.
     */
    public static String parse(String filename) {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        try (InputStream stream = new FileInputStream(filename)) {
            parser.parse(stream, handler, metadata);
            return handler.toString();
        } catch (IOException | TikaException | SAXException e) {
            logger.error("parse exception. file:{}", filename, e);
        }
        return null;
    }

    /**
     * If you don't want content from embedded documents, send in
     * a {@link org.apache.tika.parser.ParseContext} that does contains a
     * {@link EmptyParser}.
     *
     * @param filename
     * @return The content of a file.
     */
    public static String parseNoEmbedded(String filename) {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        ParseContext parseContext = new ParseContext();
        parseContext.set(Parser.class, new EmptyParser());
        try (InputStream stream = new FileInputStream(filename)) {
            parser.parse(stream, handler, metadata, parseContext);
            return handler.toString();
        } catch (IOException | SAXException | TikaException e) {
            logger.error("parse embedded exception. file:{}", filename, e);
        }
        return null;
    }

    /**
     * extract content from the outer document and all embedded documents.
     * The key is to specify a {@link Parser} in the {@link ParseContext}.
     *
     * @param filename
     * @return content, including from embedded documents
     */
    public static String parseEmbedded(String filename) {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();
        context.set(Parser.class, parser);
        try (InputStream stream = new FileInputStream(filename)) {
            parser.parse(stream, handler, metadata, context);
            return handler.toString();
        } catch (IOException | SAXException | TikaException e) {
            logger.error("parse embedded exception. file:{}", filename, e);
        }
        return null;
    }

    /**
     * For documents that may contain embedded documents, it might be helpful
     * to create list of metadata objects, one for the container document and
     * one for each embedded document.  This allows easy access to both the
     * extracted content and the metadata of each embedded document.
     * Note that many document formats can contain embedded documents,
     * including traditional container formats -- zip, tar and others -- but also
     * common office document formats including: MSWord, MSExcel,
     * MSPowerPoint, RTF, PDF, MSG and several others.
     * <p>
     * The "content" format is determined by the ContentHandlerFactory, and
     * the content is stored in {@link org.apache.tika.metadata.TikaCoreProperties#TIKA_CONTENT}}
     * <p>
     * The drawback to the RecursiveParserWrapper is that it caches metadata and contents
     * in memory.  This should not be used on files whose contents are too big to be handled
     * in memory.
     *
     * @return a list of metadata object, one each for the container file and each embedded file
     * @throws IOException
     * @throws SAXException
     * @throws TikaException
     */
    public static List<Metadata> recursiveParserWrapper(String filename) {
        Parser parser = new AutoDetectParser();
        ContentHandlerFactory factory =
                new BasicContentHandlerFactory(BasicContentHandlerFactory.HANDLER_TYPE.HTML, -1);
        RecursiveParserWrapper wrapper = new RecursiveParserWrapper(parser);
        Metadata metadata = new Metadata();
        metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, "test_recursive_embedded.docx");
        ParseContext context = new ParseContext();
        RecursiveParserWrapperHandler handler = new RecursiveParserWrapperHandler(factory, -1);
        try (InputStream stream = new FileInputStream(filename)) {
            wrapper.parse(stream, handler, metadata, context);
            return handler.getMetadataList();
        } catch (IOException | SAXException | TikaException e) {
            logger.error("recursive parser wrapper exception. file:{}", filename, e);
        }
        return null;
    }

    /**
     * We include a simple JSON serializer for a list of metadata with
     * {@link org.apache.tika.metadata.serialization.JsonMetadataList}.
     * That class also includes a deserializer to convert from JSON
     * back to a List<Metadata>.
     * <p>
     * This functionality is also available in tika-app's GUI, and
     * with the -J option on tika-app's commandline.  For tika-server
     * users, there is the "rmeta" service that will return this format.
     *
     * @param filename
     * @return a JSON representation of a list of Metadata objects
     */
    public static String serializedRecursiveParserWrapper(String filename) {
        List<Metadata> metadataList = recursiveParserWrapper(filename);
        try (StringWriter writer = new StringWriter()) {
            JsonMetadataList.toJson(metadataList, writer);
            return writer.toString();
        } catch (IOException e) {
            logger.error("serialized recursive parser wrapper exception. file:{}", filename, e);
        }
        return null;
    }

}
