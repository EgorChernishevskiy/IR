import org.apache.commons.text.similarity.FuzzyScore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class RussianSpellChecker {

    private final List<String> dictionary;

    public RussianSpellChecker() {
        List<String> synonymWords = loadDictionaryFromSynonyms("synonyms.json");
        List<String> additionalWords = loadAdditionalWords("unique_words.txt");

        Set<String> combinedSet = new HashSet<>();
        combinedSet.addAll(synonymWords);
        combinedSet.addAll(additionalWords);
        this.dictionary = new ArrayList<>(combinedSet);
    }

    private List<String> loadDictionaryFromSynonyms(String filePath) {
        List<String> terms = new ArrayList<>();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) throw new RuntimeException("Файл не найден: " + filePath);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(inputStream);

            processJsonNode(rootNode, terms, "");

            // Дедупликация терминов
            return new ArrayList<>(new HashSet<>(terms));

        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки словаря", e);
        }
    }

    private List<String> loadAdditionalWords(String filePath) {
        List<String> words = new ArrayList<>();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
             Scanner scanner = new Scanner(inputStream, "UTF-8")) {
            if (inputStream == null) throw new RuntimeException("Файл не найден: " + filePath);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    words.add(line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки дополнительных слов", e);
        }
        return words;
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
        terms.add(baseTerm.replace(".", " "));

        for (JsonNode synonymNode : synonymsNode) {
            String synonym = synonymNode.asText();
            terms.add(synonym);
        }
    }

    public String suggest(String word) {
        String lowerWord = word.toLowerCase();
        LevenshteinDistance ld = new LevenshteinDistance();
        String bestCandidate = word;
        int bestDistance = Integer.MAX_VALUE;

        for (String dictWord : dictionary) {
            int distance = ld.apply(lowerWord, dictWord.toLowerCase());
            if (distance < bestDistance) {
                bestDistance = distance;
                bestCandidate = dictWord;
            }
        }
        return bestCandidate;
    }

}
