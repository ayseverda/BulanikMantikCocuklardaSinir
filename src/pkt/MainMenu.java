package pkt;

import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.nnet.learning.MomentumBackpropagation;

import java.util.Scanner;

public class MainMenu {

    // 10 denemelik analizlerden seçilen en iyi topolojiler
    private static final int INPUT_NEURONS = 3;
    private static final int HIDDEN_NEURONS_MOMENTUMLU = 20;  // 3-20-1
    private static final int HIDDEN_NEURONS_MOMENTUMSUZ = 12; // 3-12-1
    private static final int OUTPUT_NEURONS = 1;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMenu();
            System.out.print("Seçiminiz (0 = Çıkış): ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Geçersiz giriş, lütfen sayı girin.\n");
                continue;
            }

            switch (choice) {
                case 1:
                    trainTestMomentumluOnce();
                    break;
                case 2:
                    trainTestMomentumsuzOnce();
                    break;
                case 3:
                    trainEpochGoster();           // ayrı sınıfı çağırıyoruz
                    break;
                case 4:
                    tekliTestMomentumlu();       // ayrı sınıfı çağırıyoruz
                    break;
                case 5:
                    kFoldTestMomentumlu();
                    break;
                case 0:
                    System.out.println("Programdan çıkılıyor...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Geçersiz seçim, lütfen 0–5 arasında bir değer girin.\n");
            }
        }
    }

    private static void printMenu() {
        System.out.println("=== Sinir Ağı Menüsü ===");
        System.out.println("1- Ağı Eğit ve Test Et (Momentumlu)");
        System.out.println("2- Ağı Eğit ve Test Et (Momentumsuz)");
        System.out.println("3- Ağı Eğit Epoch Göster");
        System.out.println("4- Ağı Eğit ve Tekli Test (Momentumlu)");
        System.out.println("5- K-Fold Test");
        System.out.println("0- Çıkış");
    }

    // 1- Ağı Eğit ve Test Et (Momentumlu)
    private static void trainTestMomentumluOnce() {
        try {
            DataSet training = TrainTestMomentumlu.loadDataset("training.csv");
            DataSet test = TrainTestMomentumlu.loadDataset("test.csv");

            System.out.println("Seçilen topoloji (momentumlu): 3-" 
                    + HIDDEN_NEURONS_MOMENTUMLU + "-1");
            System.out.println("Bu topoloji, 10 farklı momentumlu ağ denemesi "
                    + "arasından en düşük test MSE'yi verdiği için seçilmiştir.\n");

            MultiLayerPerceptron network =
                    new MultiLayerPerceptron(INPUT_NEURONS, HIDDEN_NEURONS_MOMENTUMLU, OUTPUT_NEURONS);

            MomentumBackpropagation rule = new MomentumBackpropagation();
            rule.setLearningRate(0.1);
            rule.setMomentum(0.7);
            rule.setMaxIterations(300);

            network.setLearningRule(rule);

            System.out.println("Ağ eğitiliyor (momentumlu)...");
            network.learn(training);
            System.out.println("Eğitim tamamlandı.");
            System.out.println("---------------------------------------");

            double trainError = rule.getTotalNetworkError();
            double testError = TrainTestMomentumlu.testNetwork(network, test);

            System.out.println("Eğitim Hatası (MSE): " + trainError);
            System.out.println("Test Hatası (MSE): " + testError);
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 2- Ağı Eğit ve Test Et (Momentumsuz)
    private static void trainTestMomentumsuzOnce() {
        try {
            DataSet training = TrainTestMomentumlu.loadDataset("training.csv");
            DataSet test = TrainTestMomentumlu.loadDataset("test.csv");

            System.out.println("Seçilen topoloji (momentumsuz): 3-" 
                    + HIDDEN_NEURONS_MOMENTUMSUZ + "-1");
            System.out.println("Bu topoloji, 10 farklı momentumsuz ağ denemesi "
                    + "arasından en düşük test MSE'yi verdiği için seçilmiştir.\n");

            MultiLayerPerceptron network =
                    new MultiLayerPerceptron(INPUT_NEURONS, HIDDEN_NEURONS_MOMENTUMSUZ, OUTPUT_NEURONS);

            BackPropagation rule = new BackPropagation();
            rule.setLearningRate(0.1);
            rule.setMaxIterations(300);

            network.setLearningRule(rule);

            System.out.println("Ağ eğitiliyor (momentumsuz)...");
            network.learn(training);
            System.out.println("Eğitim tamamlandı.");
            System.out.println("---------------------------------------");

            double trainError = rule.getTotalNetworkError();
            double testError = TrainTestMomentumlu.testNetwork(network, test);

            System.out.println("Eğitim Hatası (MSE): " + trainError);
            System.out.println("Test Hatası (MSE): " + testError);
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 3- Ağı Eğit Epoch Göster (ayrı sınıfı çağır)
    private static void trainEpochGoster() {
        try {
            TrainEpochGoster.main(new String[] {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 // 5- K-Fold Test (momentumlu BP, 3-20-1 topoloji)
    private static void kFoldTestMomentumlu() {
        try {
            TrainKFold.main(new String[] {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 4- Ağı Eğit ve Tekli Test (Momentumlu) – ayrı sınıfı çağır
    private static void tekliTestMomentumlu() {
        try {
            TrainTekliTestMomentumlu.main(new String[] {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}