import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Indexer {

    private static final String INDEX_DIR = "index";

    public void buildIndex(String jsonFilePath) {
        try {
            FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
            Analyzer analyzer = new SynonymAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(dir, config);

            ObjectMapper mapper = new ObjectMapper();
            List<Guitar> guitars = mapper.readValue(new File(jsonFilePath), new TypeReference<List<Guitar>>() {});

            for (Guitar guitar : guitars) {
                Document doc = new Document();
                doc.add(new StringField("id", String.valueOf(guitar.getId()), Field.Store.YES));
                doc.add(new TextField("name", guitar.getName(), Field.Store.YES));
                doc.add(new TextField("description", guitar.getDescription(), Field.Store.YES));

                // все характеристики в одно поле
                StringBuilder featuresText = new StringBuilder();
                Map<String, String> features = guitar.getFeatures();
                if (features != null) {
                    for (Map.Entry<String, String> entry : features.entrySet()) {
                        featuresText.append(entry.getKey()).append(" ")
                                .append(entry.getValue()).append(" ");
                    }
                }
                doc.add(new TextField("features", featuresText.toString(), Field.Store.YES));

                doc.add(new StoredField("price", guitar.getPrice()));

                writer.addDocument(doc);
            }

            writer.commit();
            writer.close();
            System.out.println("Индекс успешно построен.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
