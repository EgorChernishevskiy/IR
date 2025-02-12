import java.util.Scanner;

public class SearchApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Indexer indexer = new Indexer();
        Searcher searcher = new Searcher();

        label:
        while (true) {
            System.out.println("Выберите действие:");
            System.out.println("1. Построить индекс");
            System.out.println("2. Выполнить поиск");
            System.out.println("3. Выход");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    System.out.println("Введите путь к JSON файлу (например, products.json):");
                    String jsonPath = scanner.nextLine();
                    indexer.buildIndex(jsonPath);
                    break;
                case "2":
                    searcher.interactiveSearch();
                    break;
                case "3":
                    System.out.println("Выход...");
                    break label;
                case null:
                default:
                    System.out.println("Неверный выбор, попробуйте снова.");
                    break;
            }
        }
        scanner.close();
    }
}
