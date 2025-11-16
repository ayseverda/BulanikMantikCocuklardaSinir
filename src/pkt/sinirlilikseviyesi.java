package pkt;
import net.sourceforge.jFuzzyLogic.FIS;
import java.io.File;
import java.net.URISyntaxException;
public class sinirlilikseviyesi {
	private FIS fis;
	private double sekermiktari;
	private double yas;
	private double cinsiyet;
	
	public sinirlilikseviyesi(double sekermiktari, double yas, double cinsiyet) throws URISyntaxException {
		this.sekermiktari = sekermiktari;
		this.yas = yas;
		this.cinsiyet = cinsiyet;
				
		File dosya = new File(getClass().getResource("sinirlilikmodeli.fcl").toURI());
		fis = FIS.load(dosya.getPath());
		fis.setVariable("sekermiktari", sekermiktari);
		fis.setVariable("yas", yas);
		fis.setVariable("cinsiyet", cinsiyet);
		
		fis.evaluate();
	}
	public FIS getModel() {
		return fis;
	}
	@Override
	public String toString() {
		 double deger = fis.getVariable("sinirlilikseviyesi").getValue();
		    String kategori;

		    if (deger <= 20) {
		        kategori = "Çok Sakin";
		    } else if (deger <= 40) {
		        kategori = "Sakin";
		    } else if (deger <= 60) {
		        kategori = "Normal";
		    } else if (deger <= 80) {
		        kategori = "Sinirli";
		    } else {
		        kategori = "Çok Sinirli";
		    }

		    return "Sinirlilik Seviyesi: " + Math.round(deger) + "% (" + kategori + ")";
		}

}
