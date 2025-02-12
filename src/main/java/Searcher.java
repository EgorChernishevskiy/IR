import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Scanner;

public class Searcher {

    private static final String INDEX_DIR = "index";
    private final Analyzer analyzer;

    public Searcher() {
        this.analyzer = new SynonymAnalyzer();
    }

    /**
     * Выполняет поиск по переданному запросу и выводит результаты.
     *
     * @param queryString поисковый запрос
     * @param numResults  число возвращаемых результатов (например, 10)
     */
    public void search(String queryString, int numResults) {
        try {
            FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
            // Используем DirectoryReader, чтобы иметь доступ к методу document(...)
            DirectoryReader reader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(reader);
            // Поиск по полю "description" – при необходимости можно изменить на другое поле.
            QueryParser parser = new QueryParser("description", analyzer);
            Query query = parser.parse(queryString);

            TopDocs results = searcher.search(query, numResults);
            if (results.totalHits.value == 0) {  // метод value() возвращает число найденных документов
                System.out.println("Ничего не найдено по запросу: " + queryString);
                // Если ничего не найдено, предлагаем варианты исправления опечаток
                offerSpellCheck(queryString);
            } else {
                System.out.println("Найдено " + results.totalHits.value + " результатов:");
                for (ScoreDoc scoreDoc : results.scoreDocs) {
                    // Получаем документ через DirectoryReader.document(...)
                    Document doc = reader.document(scoreDoc.doc);
                    System.out.println("ID: " + doc.get("id"));
                    System.out.println("Название: " + doc.get("name"));
                    System.out.println("Цена: " + doc.get("price"));
                    System.out.println("Описание: " + doc.get("description"));
                    System.out.println("--------------------------------------------------");
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Если результатов не найдено, пытаемся предложить исправленный запрос с использованием RussianSpellChecker.
     *
     * @param queryString исходный поисковый запрос
     */
    private void offerSpellCheck(String queryString) {
        RussianSpellChecker spellChecker = new RussianSpellChecker();
        String[] words = queryString.split("\\s+");
        StringBuilder suggestionBuilder = new StringBuilder();
        for (String word : words) {
            String suggestion = spellChecker.suggest(word);
            suggestionBuilder.append(suggestion).append(" ");
        }
        String suggestedQuery = suggestionBuilder.toString().trim();
        if (!suggestedQuery.equalsIgnoreCase(queryString)) {
            System.out.println("Возможно, вы имели в виду: " + suggestedQuery + " ?");
        }
    }

    /**
     * Интерактивный режим поиска через консоль.
     */
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
