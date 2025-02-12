import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.core.FlattenGraphFilter;
import org.apache.lucene.analysis.ru.RussianLightStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.util.CharsRef;
import org.tartarus.snowball.ext.RussianStemmer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

public class SynonymAnalyzer extends Analyzer {

    private SynonymMap synonymMap;
    private final CharArraySet stopWords;

    public SynonymAnalyzer() {
        this.stopWords = RussianAnalyzer.getDefaultStopSet();
        this.synonymMap = loadSynonymsFromFile("synonyms.json");
    }

    private SynonymMap loadSynonymsFromFile(String filePath) {
        SynonymMap.Builder builder = new SynonymMap.Builder(true);
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) throw new RuntimeException("File not found: " + filePath);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(inputStream);

            processJsonNode(rootNode, builder, "");

            return builder.build();
        } catch (IOException e) {
            throw new RuntimeException("Error loading synonyms", e);
        }
    }

    private void processJsonNode(JsonNode node, SynonymMap.Builder builder, String parentKey) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String currentKey = parentKey.isEmpty()
                        ? entry.getKey()
                        : parentKey + "." + entry.getKey();

                if (entry.getValue().isArray()) {
                    addSynonyms(builder, currentKey, entry.getValue());
                } else {
                    processJsonNode(entry.getValue(), builder, currentKey);
                }
            }
        } else if (node.isArray()) {
            addSynonyms(builder, parentKey, node);
        }
    }

    private void addSynonyms(SynonymMap.Builder builder, String baseTerm, JsonNode synonymsNode) {
        CharsRef base = new CharsRef(baseTerm);
        for (JsonNode synonymNode : synonymsNode) {
            String synonym = synonymNode.asText();
            builder.add(base, new CharsRef(synonym), true);
        }
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new StandardTokenizer();
        TokenStream filter = new LowerCaseFilter(source);
        filter = new StopFilter(filter, stopWords);
        filter = new SynonymGraphFilter(filter, synonymMap, true);
        filter = new FlattenGraphFilter(filter);
        filter = new RussianLightStemFilter(filter);

        return new TokenStreamComponents(source, filter);
    }
}
//@Override
//protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
//    Tokenizer source = new StandardTokenizer();
//    TokenStream tokenStream = new LowerCaseFilter(source);
//    tokenStream = new StopFilter(tokenStream, stopWords);
//    tokenStream = new SynonymGraphFilter(tokenStream, synonymMap, true);
//    // Применяем стемминг для русского языка
//    tokenStream = new SnowballFilter(tokenStream, new RussianStemmer());
//    return new Analyzer.TokenStreamComponents(source, tokenStream);
//}