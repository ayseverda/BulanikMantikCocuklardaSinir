package pkt;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.MomentumBackpropagation;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.JFrame;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class TrainTestMomentumlu {

    public static void main(String[] args) {
        try {
            // Eğitim ve test veri setlerini yükle
            DataSet training = loadDataset("training.csv");
            DataSet test = loadDataset("test.csv");

            // 10 farklı topoloji (momentumlu)
            int[] hiddenNeuronsList = {3, 4, 5, 6, 7, 8, 10, 12, 15, 20};

            System.out.println("Topoloji\tTrain MSE\tTest MSE (Momentumlu)");

            XYSeriesCollection chartDataset = new XYSeriesCollection();

            double bestTestMse = Double.MAX_VALUE;
            int bestHidden = -1;

            for (int hidden : hiddenNeuronsList) {
                // Ağ: 3-input, hidden, 1-output
                MultiLayerPerceptron net = new MultiLayerPerceptron(3, hidden, 1);

                MomentumBackpropagation rule = new MomentumBackpropagation();
                rule.setLearningRate(0.1);
                rule.setMomentum(0.7);
                rule.setMaxIterations(300);

                // Epoch hatalarını kaydetmek için liste
                List<Double> epochErrors = new ArrayList<>();

                rule.addListener(event -> {
                    if (event.getSource() instanceof MomentumBackpropagation) {
                        MomentumBackpropagation lr = (MomentumBackpropagation) event.getSource();
                        epochErrors.add(lr.getTotalNetworkError());
                    }
                });

                net.setLearningRule(rule);

                System.out.println("Ağ eğitiliyor (momentumlu) - 3-" + hidden + "-1 ...");
                net.learn(training);

                double trainError = rule.getTotalNetworkError();
                double testError = testNetwork(net, test);

                System.out.println("3-" + hidden + "-1\t" + trainError + "\t" + testError);

                // En iyi test hatasını güncelle
                if (testError < bestTestMse) {
                    bestTestMse = testError;
                    bestHidden = hidden;
                }

                // Bu topoloji için epoch–MSE serisi, grafiğe ekle
                XYSeries series = new XYSeries("3-" + hidden + "-1");
                for (int i = 0; i < epochErrors.size(); i++) {
                    series.add(i + 1, epochErrors.get(i));  // x: epoch, y: error
                }
                chartDataset.addSeries(series);
            }

            System.out.println();
            System.out.println("Momentumlu 10 deneme sonunda EN İYİ topoloji: 3-"
                    + bestHidden + "-1 (Test MSE = " + bestTestMse + ")");
         
            // En iyi topolojiyi dosyaya kaydet
            saveBestTopology("best_topology_momentumlu.txt", bestHidden);

            // 10 momentumlu ağ için epoch–MSE grafiği
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Momentumlu Ağlar için Epoch-MSE Grafiği",
                    "Epoch",
                    "MSE",
                    chartDataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            ChartPanel chartPanel = new ChartPanel(chart);

            JFrame frame = new JFrame("Epoch - MSE (Momentumlu)");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(chartPanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // CSV dosyasını okuyup DataSet'e çevirir
    public static DataSet loadDataset(String path) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String header = br.readLine(); // başlık satırını atla

        List<DataSetRow> rows = new ArrayList<>();
        String line;

        while ((line = br.readLine()) != null) {
            String[] parts = line.split(",");
            double sek = Double.parseDouble(parts[0]);
            double yas = Double.parseDouble(parts[1]);
            double cin = Double.parseDouble(parts[2]);
            double sin = Double.parseDouble(parts[3]);

            rows.add(new DataSetRow(new double[]{sek, yas, cin}, new double[]{sin}));
        }

        br.close();

        DataSet ds = new DataSet(3, 1);
        for (DataSetRow r : rows) ds.add(r);

        return ds;
    }

    // Test MSE hesaplanır
    public static double testNetwork(NeuralNetwork<?> network, DataSet testSet) {
        double totalError = 0;
        int count = 0;

        for (DataSetRow row : testSet.getRows()) {
            network.setInput(row.getInput());
            network.calculate();
            double output = network.getOutput()[0];
            double target = row.getDesiredOutput()[0];

            totalError += Math.pow(target - output, 2);
            count++;
        }

        return totalError / count;
    }

    // En iyi topolojiyi dosyaya kaydet
    private static void saveBestTopology(String filename, int hiddenNeurons) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename));
            pw.println(hiddenNeurons);
            pw.close();
        } catch (Exception e) {
            System.out.println("Uyarı: En iyi topoloji dosyaya kaydedilemedi: " + e.getMessage());
        }
    }

    // Grafik göstermeden sadece test yapıp en iyi topolojiyi bul ve kaydet (MainMenu için)
    public static int findBestTopology(boolean showProgress) {
        try {
            DataSet training = loadDataset("training.csv");
            DataSet test = loadDataset("test.csv");

            int[] hiddenNeuronsList = {3, 4, 5, 6, 7, 8, 10, 12, 15, 20};

            if (showProgress) {
                System.out.println("Topoloji\tTrain MSE\tTest MSE (Momentumlu)");
            }

            double bestTestMse = Double.MAX_VALUE;
            int bestHidden = -1;

            for (int hidden : hiddenNeuronsList) {
                MultiLayerPerceptron net = new MultiLayerPerceptron(3, hidden, 1);

                MomentumBackpropagation rule = new MomentumBackpropagation();
                rule.setLearningRate(0.1);
                rule.setMomentum(0.7);
                rule.setMaxIterations(300);

                net.setLearningRule(rule);

                if (showProgress) {
                    System.out.println("Ağ eğitiliyor (momentumlu) - 3-" + hidden + "-1 ...");
                }
                net.learn(training);

                double trainError = rule.getTotalNetworkError();
                double testError = testNetwork(net, test);

                if (showProgress) {
                    System.out.println("3-" + hidden + "-1\t" + trainError + "\t" + testError);
                }

                if (testError < bestTestMse) {
                    bestTestMse = testError;
                    bestHidden = hidden;
                }
            }

            if (showProgress) {
                System.out.println();
                System.out.println("Momentumlu 10 deneme sonunda EN İYİ topoloji: 3-"
                        + bestHidden + "-1 (Test MSE = " + bestTestMse + ")");
            }

            saveBestTopology("best_topology_momentumlu.txt", bestHidden);
            return bestHidden;

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}