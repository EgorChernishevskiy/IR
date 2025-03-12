import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

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

            Set<Integer> seenIds = new HashSet<>();
            boolean electricMode = true; // Сначала считаем, что все гитары электрические

            for (Guitar guitar : guitars) {
                Document doc = new Document();
                doc.add(new StringField("id", String.valueOf(guitar.getId()), Field.Store.YES));
                doc.add(new TextField("name", guitar.getName(), Field.Store.YES));
                doc.add(new TextField("description", guitar.getDescription(), Field.Store.YES));

                StringBuilder featuresText = new StringBuilder();
                Map<String, String> features = guitar.getFeatures();
                if (features != null) {
                    for (Map.Entry<String, String> entry : features.entrySet()) {
                        featuresText.append(entry.getKey())
                                .append(" ")
                                .append(entry.getValue())
                                .append(" ");
                    }
                }
                doc.add(new TextField("features", featuresText.toString(), Field.Store.YES));
                doc.add(new StoredField("price", guitar.getPrice()));

                // Если id уже встречался, значит, начались акустические гитары
                if (seenIds.contains(guitar.getId())) {
                    electricMode = false;
                }
                seenIds.add(guitar.getId());

                String type = electricMode ? "electric" : "acoustic";
                doc.add(new StringField("type", type, Field.Store.YES));

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
