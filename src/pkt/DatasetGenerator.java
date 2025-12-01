package pkt;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Random;

public class DatasetGenerator {

    public static void main(String[] args) {
        try {
            SinirlilikFuzzyLabeler labeler = new SinirlilikFuzzyLabeler();

            PrintWriter pw = new PrintWriter(new FileWriter("dataset.csv"));
            pw.println("sekermiktari,yas,cinsiyet,sinirlilik");

            Random rnd = new Random(12345); // Sabit seed (tekrar üretilebilir)

            for (int i = 0; i < 4000; i++) {
                double seker = rnd.nextDouble() * 30.0;     // 0–30 arası
                double yas = rnd.nextDouble() * 16.0;       // 0–16 arası
                int cinsiyet = rnd.nextBoolean() ? 1 : 0;   // 0 veya 1

                // Fuzzy modelden 0–100 arası gelen değeri 0–1 aralığına çekiyoruz
                double sinir = labeler.label(seker, yas, cinsiyet) / 100.0;

                // CSV formatı
                pw.printf(Locale.US, "%.3f,%.3f,%d,%.3f%n",
                        seker, yas, cinsiyet, sinir);
            }

            pw.close();
            System.out.println("4000 satırlık dataset.csv başarıyla oluşturuldu!");

        } catch (Exception e) {
            System.out.println("Hata: " + e.getMessage());
        }
    }
}