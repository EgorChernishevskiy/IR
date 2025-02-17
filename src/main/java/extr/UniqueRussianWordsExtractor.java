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

    private static final Pattern RUSSIAN_WORD_PATTERN = Pattern.compile("[а-яА-ЯёЁ]+");

    public static void main(String[] args) {
        File jsonFile = new File("products.json");
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonFile);

            Set<String> uniqueWords = new HashSet<>();
            extractRussianWords(rootNode, uniqueWords);

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

    private static void extractRussianWords(JsonNode node, Set<String> uniqueWords) {
        if (node.isTextual()) {
            String text = node.asText();
            Matcher matcher = RUSSIAN_WORD_PATTERN.matcher(text);
            while (matcher.find()) {
                uniqueWords.add(matcher.group().toLowerCase());
            }
        } else if (node.isContainerNode()) {
            for (JsonNode child : node) {
                extractRussianWords(child, uniqueWords);
            }
        }
    }
}
