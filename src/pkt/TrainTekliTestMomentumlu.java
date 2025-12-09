package pkt;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.MomentumBackpropagation;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.JFrame;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TrainTekliTestMomentumlu {

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

            // Dataset'i yükle ve rastgele böl
            DataSet full = TrainTestMomentumlu.loadDataset("dataset.csv");
            DataSet[] split = TrainTestMomentumlu.splitDatasetRandomly(full);
            DataSet training = split[0];
            DataSet test = split[1];

            // Epoch bazlı hata listeleri
            List<Double> trainMseList = new ArrayList<>();
            List<Double> testMseList = new ArrayList<>();

            System.out.println("Tekli test için momentumlu ağ eğitiliyor...");
            System.out.println("Kullanılan topoloji: 3-" + hiddenNeurons + "-1");
            System.out.println("(Bu topoloji, 10 farklı momentumlu ağ denemesi arasından "
                    + "en düşük test MSE'yi verdiği için seçilmiştir.)\n");

            MultiLayerPerceptron network =
                    new MultiLayerPerceptron(INPUT_NEURONS, hiddenNeurons, OUTPUT_NEURONS);

            MomentumBackpropagation rule = new MomentumBackpropagation();
            rule.setLearningRate(0.05); // Daha düşük learning rate
            rule.setMomentum(0.8); // Daha yüksek momentum
            rule.setMaxIterations(2000); // Daha fazla epoch
            rule.setMaxError(0.00001); // Çok düşük error threshold

            network.setLearningRule(rule);

            // Her epoch'ta train/test MSE kaydet
            rule.addListener(new LearningEventListener() {
                @Override
                public void handleLearningEvent(LearningEvent event) {
                    MomentumBackpropagation r = (MomentumBackpropagation) event.getSource();
                    double trainErr = r.getTotalNetworkError();
                    trainMseList.add(trainErr);
                    double testErr = TrainTestMomentumlu.testNetwork(network, test);
                    testMseList.add(testErr);
                }
            });

            // Ağı eğit
            network.learn(training);

            // 1. grafik: Epoch–MSE
            showEpochMseChart(trainMseList, testMseList);

            System.out.println("Eğitim tamamlandı. Artık kullanıcıdan giriş alınacaktır.\n");

            // Kullanıcıdan tek giriş al
            Scanner scanner = new Scanner(System.in);

            System.out.print("Şeker miktarı (0-30 arası): ");
            double seker = Double.parseDouble(scanner.nextLine());

            System.out.print("Yaş (0-16 arası): ");
            double yas = Double.parseDouble(scanner.nextLine());

            System.out.print("Cinsiyet (0 = Kadın, 1 = Erkek): ");
            double cinsiyet = Double.parseDouble(scanner.nextLine());

            // Ağa tekli giriş ver
            double[] input = new double[]{seker, yas, cinsiyet};
            network.setInput(input);
            network.calculate();

            double output = network.getOutput()[0];  // 0–1 arası sinirlilik
            double yuzde = output * 100.0;

            System.out.println("\nAğ Çıkışı (sinirlilik, [0–1]): " + output);
            System.out.println("Ağ Çıkışı (sinirlilik, %): " + yuzde);

            // 2. grafik: Tekli sonucun bar grafiği
            showSingleResultChart(yuzde);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==== Epoch-MSE çizgi grafiği ====
    private static void showEpochMseChart(List<Double> trainMse, List<Double> testMse) {
        XYSeries trainSeries = new XYSeries("Eğitim MSE");
        XYSeries testSeries = new XYSeries("Test MSE");

        for (int i = 0; i < trainMse.size(); i++) {
            int epoch = i + 1;
            trainSeries.add(epoch, trainMse.get(i));
        }

        for (int i = 0; i < testMse.size(); i++) {
            int epoch = i + 1;
            testSeries.add(epoch, testMse.get(i));
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(trainSeries);
        dataset.addSeries(testSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Seçilen Topoloji için Epoch-MSE Grafiği (Momentumlu)",
                "Epoch",
                "MSE",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        ChartPanel panel = new ChartPanel(chart);
        JFrame frame = new JFrame("Epoch - MSE (Momentumlu)");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ==== Tekli sonucun bar grafiği ====
    private static void showSingleResultChart(double sinirlilikYuzde) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(sinirlilikYuzde, "Sinirlilik", "Kullanıcı Girdisi");

        JFreeChart chart = ChartFactory.createBarChart(
                "Tekli Test Sonucu (Momentumlu)",
                "Girdi",
                "Sinirlilik (%)",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        var plot = chart.getCategoryPlot();
        var rangeAxis = plot.getRangeAxis();
        rangeAxis.setRange(0.0, 100.0);

        ChartPanel panel = new ChartPanel(chart);
        JFrame frame = new JFrame("Tekli Test Sonucu Grafiği (Momentumlu)");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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