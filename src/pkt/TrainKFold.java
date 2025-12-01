package pkt;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.MomentumBackpropagation;

import java.util.*;

public class TrainKFold {

    // Momentumlu için en iyi topoloji: 3-20-1
    private static final int INPUT_NEURONS = 3;
    private static final int HIDDEN_NEURONS = 20;
    private static final int OUTPUT_NEURONS = 1;

    public static void main(String[] args) {
        try {
            // Tüm veri setini kullan (4000 satır)
            DataSet full = TrainTestMomentumlu.loadDataset("dataset.csv");

            Scanner scanner = new Scanner(System.in);
            System.out.print("K değerini giriniz (ör: 5 veya 10): ");
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

            System.out.println("\nK-Fold Cross Validation (K = " + k + ", momentumlu BP, topoloji: 3-20-1)");
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
                        new MultiLayerPerceptron(INPUT_NEURONS, HIDDEN_NEURONS, OUTPUT_NEURONS);

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

                System.out.println((fold + 1) + "\t" + trainMse + "\t" + testMse);
            }

            double avgTrain = sumTrainMse / k;
            double avgTest  = sumTestMse  / k;

            System.out.println("\nOrtalama Eğitim Hatası (MSE): " + avgTrain);
            System.out.println("Ortalama Test Hatası (MSE):   " + avgTest);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}