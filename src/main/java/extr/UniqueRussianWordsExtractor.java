package extr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UniqueRussianWordsExtractor {

    // Регулярное выражение для поиска слов, содержащих только русские буквы
    private static final Pattern RUSSIAN_WORD_PATTERN = Pattern.compile("[а-яА-ЯёЁ]+");

    public static void main(String[] args) {
        File jsonFile = new File("products.json"); // Укажите путь к вашему JSON файлу
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonFile);

            Set<String> uniqueWords = new HashSet<>();
            extractRussianWords(rootNode, uniqueWords);

            // Записываем уникальные слова в файл
            File outputFile = new File("unique_words.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                for (String word : uniqueWords) {
                    writer.write(word);
                    writer.newLine();
                }
            }

            System.out.println("Уникальные слова записаны в файл: " + outputFile.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Рекурсивный метод для обхода JSON дерева
    private static void extractRussianWords(JsonNode node, Set<String> uniqueWords) {
        if (node.isTextual()) {
            String text = node.asText();
            Matcher matcher = RUSSIAN_WORD_PATTERN.matcher(text);
            while (matcher.find()) {
                // Приводим слово к нижнему регистру для избежания дублирования
                uniqueWords.add(matcher.group().toLowerCase());
            }
        } else if (node.isContainerNode()) { // Если узел является объектом или массивом
            for (JsonNode child : node) {
                extractRussianWords(child, uniqueWords);
            }
        }
    }
}
