package pkt;

import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.BackPropagation;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.JFrame;
import java.util.ArrayList;
import java.util.List;

public class TrainTestMomentumsuz {

    public static void main(String[] args) {
        try {
            // Eğitim ve test veri setlerini yükle
            DataSet training = TrainTestMomentumlu.loadDataset("training.csv");
            DataSet test = TrainTestMomentumlu.loadDataset("test.csv");

            // 10 farklı topoloji
            int[] hiddenNeuronsList = {3, 4, 5, 6, 7, 8, 10, 12, 15, 20};

            System.out.println("Topoloji\tTrain MSE\tTest MSE (Momentumsuz)");

            XYSeriesCollection chartDataset = new XYSeriesCollection();

            for (int hidden : hiddenNeuronsList) {
                // Ağ: 3-input, hidden, 1-output
                MultiLayerPerceptron net = new MultiLayerPerceptron(3, hidden, 1);

                BackPropagation rule = new BackPropagation();
                rule.setLearningRate(0.1);
                rule.setMaxIterations(300);

                // Epoch hatalarını kaydetmek için liste
                List<Double> epochErrors = new ArrayList<>();

                rule.addListener(event -> {
                    if (event.getSource() instanceof BackPropagation) {
                        BackPropagation lr = (BackPropagation) event.getSource();
                        epochErrors.add(lr.getTotalNetworkError());
                    }
                });

                net.setLearningRule(rule);

                System.out.println("Ağ eğitiliyor (momentumsuz) - 3-" + hidden + "-1 ...");
                net.learn(training);

                double trainError = rule.getTotalNetworkError();
                double testError = TrainTestMomentumlu.testNetwork(net, test);

                System.out.println("3-" + hidden + "-1\t" + trainError + "\t" + testError);

                // Bu topoloji için epoch–MSE serisi, grafiğe ekle
                XYSeries series = new XYSeries("3-" + hidden + "-1");
                for (int i = 0; i < epochErrors.size(); i++) {
                    series.add(i + 1, epochErrors.get(i));  // x: epoch, y: error
                }
                chartDataset.addSeries(series);
            }

            // Tüm topolojiler için epoch–MSE grafiği
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Momentumsuz Ağlar için Epoch-MSE Grafiği",
                    "Epoch",
                    "MSE",
                    chartDataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            ChartPanel chartPanel = new ChartPanel(chart);

            JFrame frame = new JFrame("Epoch - MSE (Momentumsuz)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(chartPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}