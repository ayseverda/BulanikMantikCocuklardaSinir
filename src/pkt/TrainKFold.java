package pkt;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.MomentumBackpropagation;

// >>> GRAFİK İÇİN EKLENEN IMPORTLAR
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class TrainKFold {

    private static final int INPUT_NEURONS = 3;
    private static final int OUTPUT_NEURONS = 1;
    private static final int DEFAULT_HIDDEN = 20; // Varsayılan değer

    public static void main(String[] args) {
        try {
            // En iyi momentumlu topolojiyi dosyadan oku
            int hiddenNeurons = loadTopologyFromFile("best_topology_momentumlu.txt");
            if (hiddenNeurons <= 0) {
                hiddenNeurons = DEFAULT_HIDDEN; // Dosya yoksa varsayılan
            }

            // Tüm veri setini kullan (4000 satır)
            DataSet full = TrainTestMomentumlu.loadDataset("dataset.csv");

            Scanner scanner = new Scanner(System.in);
            System.out.print("K değerini giriniz: ");
            int k = Integer.parseInt(scanner.nextLine());

            if (k < 2) {
                System.out.println("K en az 2 olmalıdır.");
                return;
            }

            List<DataSetRow> allRows = new ArrayList<>(full.getRows());
            Collections.shuffle(allRows, new Random(12345)); // tekrar üretilebilir karışım

            int total = allRows.size();
            int foldSize = total / k;

            double sumTrainMse = 0.0;
            double sumTestMse  = 0.0;

            // >>> GRAFİK İÇİN HATA LİSTELERİ
            List<Double> foldTrainMseList = new ArrayList<>();
            List<Double> foldTestMseList  = new ArrayList<>();

            System.out.println("\nK-Fold Cross Validation (K = " + k + ", momentumlu BP, topoloji: 3-" + hiddenNeurons + "-1)");
            System.out.println("Fold\tTrain MSE\tTest MSE");

            for (int fold = 0; fold < k; fold++) {
                int start = fold * foldSize;
                int end   = (fold == k - 1) ? total : (fold + 1) * foldSize;

                List<DataSetRow> testRows  = allRows.subList(start, end);
                List<DataSetRow> trainRows = new ArrayList<>();
                trainRows.addAll(allRows.subList(0, start));
                trainRows.addAll(allRows.subList(end, total));

                DataSet trainSet = new DataSet(INPUT_NEURONS, OUTPUT_NEURONS);
                for (DataSetRow r : trainRows) trainSet.add(r);

                DataSet testSet = new DataSet(INPUT_NEURONS, OUTPUT_NEURONS);
                for (DataSetRow r : testRows) testSet.add(r);

                // Yeni ağ
                MultiLayerPerceptron net =
                        new MultiLayerPerceptron(INPUT_NEURONS, hiddenNeurons, OUTPUT_NEURONS);

                MomentumBackpropagation rule = new MomentumBackpropagation();
                rule.setLearningRate(0.1);
                rule.setMomentum(0.7);
                rule.setMaxIterations(300);

                net.setLearningRule(rule);
                net.learn(trainSet);

                double trainMse = rule.getTotalNetworkError();
                double testMse  = TrainTestMomentumlu.testNetwork(net, testSet);

                sumTrainMse += trainMse;
                sumTestMse  += testMse;

                // >>> GRAFİK İÇİN LİSTELERE EKLE
                foldTrainMseList.add(trainMse);
                foldTestMseList.add(testMse);

                System.out.println((fold + 1) + "\t" + trainMse + "\t" + testMse);
            }

            double avgTrain = sumTrainMse / k;
            double avgTest  = sumTestMse  / k;

            System.out.println("\nOrtalama Eğitim Hatası (MSE): " + avgTrain);
            System.out.println("Ortalama Test Hatası (MSE):   " + avgTest);

            // >>> K-FOLD GRAFİĞİ
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int i = 0; i < k; i++) {
                String foldName = "Fold " + (i + 1);
                dataset.addValue(foldTrainMseList.get(i), "Train MSE", foldName);
                dataset.addValue(foldTestMseList.get(i),  "Test MSE",  foldName);
            }

            JFreeChart chart = ChartFactory.createBarChart(
                    "K-Fold Sonuçları (Momentumlu, 3-" + hiddenNeurons + "-1)",
                    "Fold",
                    "MSE",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true, true, false
            );

            ChartFrame frame = new ChartFrame("K-Fold MSE Grafiği (Momentumlu)", chart);
            frame.pack();
            frame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Dosyadan topoloji değerini oku
    private static int loadTopologyFromFile(String filename) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();
            br.close();
            if (line != null && !line.trim().isEmpty()) {
                return Integer.parseInt(line.trim());
            }
        } catch (Exception e) {
            // Dosya yoksa varsayılan değer kullanılacak
        }
        return -1; // Dosya bulunamadı
    }
}
