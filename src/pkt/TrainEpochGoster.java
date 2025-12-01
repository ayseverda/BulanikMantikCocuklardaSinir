package pkt;

import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.JFrame;
import java.util.ArrayList;
import java.util.List;

public class TrainEpochGoster {

    // En iyi momentumsuz topoloji: 3-12-1
    private static final int INPUT_NEURONS = 3;
    private static final int HIDDEN_NEURONS = 12;
    private static final int OUTPUT_NEURONS = 1;

    public static void main(String[] args) {
        try {
            DataSet training = TrainTestMomentumlu.loadDataset("training.csv");
            DataSet test = TrainTestMomentumlu.loadDataset("test.csv");

            System.out.println("Epoch bazlı eğitim (momentumsuz BP) başlatılıyor...");
            System.out.println("Kullanılan topoloji: 3-" + HIDDEN_NEURONS + "-1");
            System.out.println("Bu topoloji, 10 farklı momentumsuz ağ denemesi arasından "
                    + "en düşük test MSE'yi verdiği için seçilmiştir.\n");

            MultiLayerPerceptron network =
                    new MultiLayerPerceptron(INPUT_NEURONS, HIDDEN_NEURONS, OUTPUT_NEURONS);

            BackPropagation rule = new BackPropagation();
            rule.setLearningRate(0.1);
            rule.setMaxIterations(10);   // Kaç epoch göstermek istiyorsan burayı değiştir

            List<Double> trainErrors = new ArrayList<>();
            List<Double> testErrors  = new ArrayList<>();

            // Aynı epoch'u iki kez yazmamak için
            final int[] lastEpoch = {0};

            rule.addListener(new LearningEventListener() {
                @Override
                public void handleLearningEvent(LearningEvent event) {
                    if (event.getSource() instanceof BackPropagation) {
                        BackPropagation lr = (BackPropagation) event.getSource();
                        int epoch = lr.getCurrentIteration();

                        if (epoch == lastEpoch[0]) return; // aynı epoch'u tekrar yazma
                        lastEpoch[0] = epoch;

                        double trainErr = lr.getTotalNetworkError();
                        double testErr = TrainTestMomentumlu.testNetwork(network, test);

                        trainErrors.add(trainErr);
                        testErrors.add(testErr);

                        System.out.println("Epoch " + epoch
                                + " -> Eğitim MSE: " + trainErr
                                + " | Test MSE: " + testErr);
                    }
                }
            });

            network.setLearningRule(rule);

            System.out.println("\nEğitim başlıyor...\n");
            network.learn(training);
            System.out.println("\nEpoch bazlı eğitim tamamlandı.");

            // ---- Grafik: Eğitim vs Test MSE ----
            XYSeries trainSeries = new XYSeries("Eğitim MSE");
            XYSeries testSeries  = new XYSeries("Test MSE");

            for (int i = 0; i < trainErrors.size(); i++) {
                int epoch = i + 1;
                trainSeries.add(epoch, trainErrors.get(i));
                testSeries.add(epoch,  testErrors.get(i));
            }

            XYSeriesCollection dataset = new XYSeriesCollection();
            dataset.addSeries(trainSeries);
            dataset.addSeries(testSeries);

            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Seçilen Topoloji için Epoch-MSE Grafiği",
                    "Epoch",
                    "MSE",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            ChartPanel panel = new ChartPanel(chart);
            JFrame frame = new JFrame("Epoch - MSE (Tek Topoloji)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}