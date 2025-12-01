package pkt;

import net.sourceforge.jFuzzyLogic.FIS;
import java.io.File;
import java.net.URISyntaxException;

public class SinirlilikFuzzyLabeler {

    private FIS fis;

    public SinirlilikFuzzyLabeler() throws URISyntaxException {
        File dosya = new File(getClass().getResource("sinirlilikmodeli.fcl").toURI());
        fis = FIS.load(dosya.getPath(), true);

        if (fis == null) {
            throw new RuntimeException("FCL dosyası yüklenemedi!");
        }
    }

    // Tek satır örnek için fuzzy çıktı üret
    public double label(double seker, double yas, double cinsiyet) {
        fis.setVariable("sekermiktari", seker);
        fis.setVariable("yas", yas);
        fis.setVariable("cinsiyet", cinsiyet);

        fis.evaluate();

        return fis.getVariable("sinirlilikseviyesi").getValue(); // 0–100 değer
    }
}
