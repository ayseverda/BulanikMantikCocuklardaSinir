package pkt;
import java.net.URISyntaxException;
import java.util.Scanner;

import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;

public class test {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String secim;

        do {
            System.out.print("Günlük tüketilen şeker miktarı (0-30 arası bir değer giriniz) (gr): ");
            double sekermiktari = in.nextDouble();
            System.out.print("Çocuğun yaşı (0-16 arası bir değer giriniz): ");
            double yas = in.nextDouble();
            System.out.print("Çocuğun Cinsiyeti (erkek=0, kız=1): ");
            double cinsiyet = in.nextDouble();

            try {
                sinirlilikseviyesi sinirlilikseviyesih =
                        new sinirlilikseviyesi(sekermiktari, yas, cinsiyet);

                System.out.println(sinirlilikseviyesih);
                JFuzzyChart.get().chart(sinirlilikseviyesih.getModel());

            } catch (URISyntaxException e) {
                System.out.println("FCL dosyası bulunamadı! Hata: " + e.getMessage());
            }

            System.out.print("Başka bir hesaplama yapmak ister misiniz? (d=devam, ç=çıkış): ");
            secim = in.next();

        } while (secim.equalsIgnoreCase("d"));

        System.out.println("Programdan çıkılıyor.");
    }
}
