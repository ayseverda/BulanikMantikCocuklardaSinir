package pkt;

import java.io.*;
import java.util.*;

public class DatasetSplitter {

    public static void main(String[] args) {
        String inputPath = "dataset.csv";
        String trainPath = "training.csv";
        String testPath = "test.csv";

        List<String> allLines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(inputPath))) {
            String line;
            // Başlığı da oku ama ayrı sakla
            String header = br.readLine();

            while ((line = br.readLine()) != null) {
                allLines.add(line);
            }

            // Karıştır
            Collections.shuffle(allLines);

            int total = allLines.size();
            int trainSize = (int) (total * 0.75);

            List<String> train = allLines.subList(0, trainSize);
            List<String> test = allLines.subList(trainSize, total);

            // TRAIN dosyası yaz
            try (PrintWriter pw = new PrintWriter(new FileWriter(trainPath))) {
                pw.println(header);
                for (String row : train) pw.println(row);
            }

            // TEST dosyası yaz
            try (PrintWriter pw = new PrintWriter(new FileWriter(testPath))) {
                pw.println(header);
                for (String row : test) pw.println(row);
            }

            System.out.println("Veri başarıyla bölündü!");
            System.out.println("Training: " + train.size() + " satır");
            System.out.println("Test: " + test.size() + " satır");

        } catch (IOException e) {
            System.out.println("Hata: " + e.getMessage());
        }
    }
}
