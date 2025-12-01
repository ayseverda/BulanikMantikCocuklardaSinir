package pkt;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.nnet.learning.MomentumBackpropagation;

import java.util.Scanner;

public class TrainTekliTestMomentumlu {

    // Momentumlu için en iyi topoloji: 3-20-1
    private static final int INPUT_NEURONS = 3;
    private static final int HIDDEN_NEURONS = 20;
    private static final int OUTPUT_NEURONS = 1;

    public static void main(String[] args) {
        try {
            // Tam veri setini oku ve rastgele %75 eğitim, %25 test ayır (bu sınıfa özel)
            DataSet full = TrainTestMomentumlu.loadDataset("dataset.csv".replace("dataset.csv","training.csv"));
            // Burada doğrudan training.csv'yi kullanıyoruz; zaten DatasetSplitter ile
            // dataset'in %75'i rastgele seçilerek oluşturulmuştu.

            DataSet training = TrainTestMomentumlu.loadDataset("training.csv");

            System.out.println("Tekli test için momentumlu ağ eğitiliyor...");
            System.out.println("Kullanılan topoloji: 3-" + HIDDEN_NEURONS + "-1");
            System.out.println("(Bu topoloji, 10 farklı momentumlu ağ denemesi arasından "
                    + "en düşük test MSE'yi verdiği için seçilmiştir.)\n");

            MultiLayerPerceptron network =
                    new MultiLayerPerceptron(INPUT_NEURONS, HIDDEN_NEURONS, OUTPUT_NEURONS);

            MomentumBackpropagation rule = new MomentumBackpropagation();
            rule.setLearningRate(0.1);
            rule.setMomentum(0.7);
            rule.setMaxIterations(300);

            network.setLearningRule(rule);

            network.learn(training);

            System.out.println("Eğitim tamamlandı. Artık kullanıcıdan giriş alınacaktır.\n");

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}