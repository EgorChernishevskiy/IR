import org.apache.commons.text.similarity.FuzzyScore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class RussianSpellChecker {

    private final List<String> dictionary;
    private final FuzzyScore fuzzyScore;

    public RussianSpellChecker() {
        this.dictionary = loadDictionaryFromSynonyms("synonyms.json");
        this.fuzzyScore = new FuzzyScore(new Locale("ru"));
    }

    private List<String> loadDictionaryFromSynonyms(String filePath) {
        List<String> terms = new ArrayList<>();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) throw new RuntimeException("Файл не найден: " + filePath);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(inputStream);

            processJsonNode(rootNode, terms, "");

            // Дедупликация терминов
            return new ArrayList<>(terms.stream().distinct().toList());

        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки словаря", e);
        }
    }

    private void processJsonNode(JsonNode node, List<String> terms, String parentKey) {
        if (node.isObject()) {
            for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                String currentKey = parentKey.isEmpty()
                        ? entry.getKey()
                        : parentKey + "." + entry.getKey();

                if (entry.getValue().isArray()) {
                    addTerms(terms, currentKey, entry.getValue());
                } else {
                    processJsonNode(entry.getValue(), terms, currentKey);
                }
            }
        } else if (node.isArray()) {
            addTerms(terms, parentKey, node);
        }
    }

    private void addTerms(List<String> terms, String baseTerm, JsonNode synonymsNode) {
        // Добавляем основной термин
        terms.add(baseTerm.replace(".", " ")); // Заменяем точки на пробелы

        // Добавляем синонимы
        for (JsonNode synonymNode : synonymsNode) {
            String synonym = synonymNode.asText();
            terms.add(synonym);
        }
    }

    public String suggest(String word) {
        String lowerWord = word.toLowerCase();
        List<ScoredWord> candidates = new ArrayList<>();

        for (String dictWord : dictionary) {
            int score = fuzzyScore.fuzzyScore(lowerWord, dictWord.toLowerCase());
            if (score > 0) {
                candidates.add(new ScoredWord(dictWord, score));
            }
        }

        // Сортируем по убыванию score и выбираем лучший
        Collections.sort(candidates);
        return candidates.isEmpty() ? word : candidates.get(0).word;
    }

    // Вспомогательный класс для хранения результатов
    private static class ScoredWord implements Comparable<ScoredWord> {
        String word;
        int score;

        ScoredWord(String word, int score) {
            this.word = word;
            this.score = score;
        }

        @Override
        public int compareTo(ScoredWord other) {
            return Integer.compare(other.score, this.score);
        }
    }
}