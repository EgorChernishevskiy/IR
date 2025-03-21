import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Scanner;

public class Searcher {

    private static final String INDEX_DIR = "index";
    private final Analyzer analyzer;
    private final GuitarClassifier classifier;

    public Searcher() {
        this.analyzer = new SynonymAnalyzer();
        // URL REST-сервиса классификации (убедитесь, что сервис запущен)
        this.classifier = new GuitarClassifier("http://localhost:5000/predict");
    }


    public void search(String queryString, int numResults) {
        try {
            String expandedQuery = expandQueryWithSynonyms(queryString);
            System.out.println("Запрос с синонимами: " + expandedQuery);

            // Теперь классифицируем расширенный запрос
            String predictedType = classifier.predict(expandedQuery);
            System.out.println("Предсказанный тип гитары: " + predictedType);

            FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
            DirectoryReader reader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(reader);

            // Поиск по полям
            String[] fields = {"name", "description", "features"};
            MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
            Query query = parser.parse(queryString);

            // Создаем фильтр по типу
            Query typeFilter = parser.parse("type:" + predictedType);

            // Объединяем основной запрос и фильтр
            BooleanQuery.Builder combinedQuery = new BooleanQuery.Builder();
            combinedQuery.add(query, BooleanClause.Occur.MUST);
            combinedQuery.add(typeFilter, BooleanClause.Occur.FILTER);

            TopDocs results = searcher.search(combinedQuery.build(), numResults);
            // Если результатов нет
            if (results.totalHits.value == 0) {
                String correctedQuery = getCorrectedQuery(queryString);
                if (!correctedQuery.equalsIgnoreCase(queryString)) {
                    System.out.println("По запросу \"" + queryString + "\" ничего не найдено.");
                    System.out.println("Возможно, вы имели в виду: \"" + correctedQuery + "\". Выполняем поиск по исправленному запросу...");
                    Query correctedParsedQuery = parser.parse(correctedQuery);
                    results = searcher.search(correctedParsedQuery, numResults);
                }
            }

            // Вывод результатов, если есть
            if (results.totalHits.value > 0) {
                System.out.println("Найдено " + results.totalHits.value + " результатов:");
                for (ScoreDoc scoreDoc : results.scoreDocs) {
                    Document doc = reader.document(scoreDoc.doc);
                    System.out.println("ID: " + doc.get("id"));
                    System.out.println("Название: " + doc.get("name"));
                    System.out.println("Цена: " + doc.get("price"));
                    System.out.println("Описание: " + doc.get("description"));
                    System.out.println("Характеристики: " + doc.get("features"));
                    System.out.println("--------------------------------------------------");
                }
            } else {
                System.out.println("Ничего не найдено даже по исправленному запросу.");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String expandQueryWithSynonyms(String query) throws IOException {
        TokenStream tokenStream = analyzer.tokenStream("dummyField", new StringReader(query));
        CharTermAttribute charTermAttr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        StringBuilder expanded = new StringBuilder();
        while (tokenStream.incrementToken()) {
            expanded.append(charTermAttr.toString()).append(" ");
        }
        tokenStream.end();
        tokenStream.close();
        return expanded.toString().trim();
    }

    private String getCorrectedQuery(String queryString) {
        RussianSpellChecker spellChecker = new RussianSpellChecker();
        String[] words = queryString.split("\\s+");
        StringBuilder correctedBuilder = new StringBuilder();
        for (String word : words) {
            correctedBuilder.append(spellChecker.suggest(word)).append(" ");
        }
        return correctedBuilder.toString().trim();
    }

    public void interactiveSearch() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Введите поисковый запрос (или 'exit' для выхода):");
            String input = scanner.nextLine();
            if ("exit".equalsIgnoreCase(input)) {
                break;
            }
            System.out.println("Введите число результатов (по умолчанию 10):");
            String numStr = scanner.nextLine();
            int numResults = 10;
            try {
                if (!numStr.trim().isEmpty()) {
                    numResults = Integer.parseInt(numStr.trim());
                }
            } catch (NumberFormatException e) {
                System.out.println("Неверный ввод, используется значение по умолчанию: 10");
            }
            search(input, numResults);
        }
        scanner.close();
    }
}
