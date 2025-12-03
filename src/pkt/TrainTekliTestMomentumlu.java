package pkt;

import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.MomentumBackpropagation;
import org.neuroph.core.events.LearningEvent;
import org.neuroph.core.events.LearningEventListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.JFrame;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TrainTekliTestMomentumlu {

    private static final int INPUT_NEURONS = 3;
    private static final int HIDDEN_NEURONS = 20;   // en iyi topoloji
    private static final int OUTPUT_NEURONS = 1;

    public static void main(String[] args) {
        try {
            // 1) Eğitim ve test setlerini yükle
            DataSet training = TrainTestMomentumlu.loadDataset("training.csv");
            DataSet test     = TrainTestMomentumlu.loadDataset("test.csv");

            // 2) Epoch bazlı hata listeleri
            List<Double> trainMseList = new ArrayList<>();
            List<Double> testMseList  = new ArrayList<>();

            System.out.println("Tekli test için momentumlu ağ eğitiliyor...");
            System.out.println("Kullanılan topoloji: 3-" + HIDDEN_NEURONS + "-1\n");

            // 3) Ağ ve öğrenme kuralı
            MultiLayerPerceptron network =
                    new MultiLayerPerceptron(INPUT_NEURONS, HIDDEN_NEURONS, OUTPUT_NEURONS);

            MomentumBackpropagation rule = new MomentumBackpropagation();
            rule.setLearningRate(0.1);
            rule.setMomentum(0.7);
            rule.setMaxIterations(300);

            network.setLearningRule(rule);

            // 4) Her epoch'ta train/test MSE kaydet
            rule.addListener(new LearningEventListener() {
                @Override
                public void handleLearningEvent(LearningEvent event) {
                    MomentumBackpropagation r = (MomentumBackpropagation) event.getSource();

                    double trainErr = r.getTotalNetworkError();
                    trainMseList.add(trainErr);

                    double testErr  = TrainTestMomentumlu.testNetwork(network, test);
                    testMseList.add(testErr);
                }
            });

            // 5) Ağı eğit
            network.learn(training);

            // 6) 1. grafik: Epoch–MSE
            showEpochMseChart(trainMseList, testMseList);

            System.out.println("Eğitim tamamlandı. Artık kullanıcıdan giriş alınacaktır.\n");

            // 7) Kullanıcıdan tek giriş al
            Scanner scanner = new Scanner(System.in);

            System.out.print("Şeker miktarı (0-30 arası): ");
            double seker = Double.parseDouble(scanner.nextLine());

            System.out.print("Yaş (0-16 arası): ");
            double yas = Double.parseDouble(scanner.nextLine());

            System.out.print("Cinsiyet (0 = Kadın, 1 = Erkek): ");
            double cinsiyet = Double.parseDouble(scanner.nextLine());

            double[] input = new double[]{seker, yas, cinsiyet};
            network.setInput(input);
            network.calculate();

            double output = network.getOutput()[0];    // [0,1]
            double yuzde  = output * 100.0;

            System.out.println("\nAğ Çıkışı (sinirlilik, [0–1]): " + output);
            System.out.println("Ağ Çıkışı (sinirlilik, %): " + yuzde);

            // 8) 2. grafik: Tekli sonucun bar grafiği
            showSingleResultChart(yuzde);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==== Epoch-MSE çizgi grafiği ====
    private static void showEpochMseChart(List<Double> trainMse,
                                          List<Double> testMse) {

        XYSeries trainSeries = new XYSeries("Eğitim MSE");
        XYSeries testSeries  = new XYSeries("Test MSE");

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
                "Seçilen Topoloji için Epoch-MSE Grafiği",
                "Epoch",
                "MSE",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        ChartPanel panel = new ChartPanel(chart);
        JFrame frame = new JFrame("Epoch - MSE (Tek Topoloji)");
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
                "Tekli Test Sonucu",
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
        JFrame frame = new JFrame("Tekli Test Sonucu Grafiği");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
