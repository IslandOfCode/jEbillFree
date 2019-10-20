package it.islandofcode.jebill;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

public class EBill {

	public static final Map<String, String> TIPODOCUMENTO;
	static {
		Map<String, String> T = new HashMap<>();
		T.put("TD01", "Fattura");
		T.put("TD02", "Acconto/Anticipo su fattura");
		T.put("TD03", "Acconto/Anticipo su parcella");
		T.put("TD04", "Nota di credito");
		T.put("TD05", "Nota di debito");
		T.put("TD06", "Parcella");
		TIPODOCUMENTO = Collections.unmodifiableMap(T);
	}
	
	public static final List<Image> ICONS;
	static {
		ArrayList<Image> i = new ArrayList<>();
		i.add( new ImageIcon(EBill.class.getClass().getResource("/icon16.png")).getImage() );
		i.add( new ImageIcon(EBill.class.getClass().getResource("/icon24.png")).getImage() );
		i.add( new ImageIcon(EBill.class.getClass().getResource("/icon32.png")).getImage() );
		i.add( new ImageIcon(EBill.class.getClass().getResource("/icon64.png")).getImage() );
		i.add( new ImageIcon(EBill.class.getClass().getResource("/icon128.png")).getImage() );
		ICONS = Collections.unmodifiableList(i);
	}
	
	public static final String CERT_URL = "https://applicazioni.cnipa.gov.it/TSL/_IT_TSL_signed.xml";
	public static final String CERT_FILE_NAME = "_IT_TSL_signed.xml";
	public static final String CERT_REAL_FILE_NAME = "CA.pem";
	
	public static final String BATCH_LOG_FILE_NAME = "RISULTATO.txt";
		
	public static final String DatiOrdineAcquisto = "Ordine/Acquisto";
	public static final String DatiDDT = "DDT";
	
	public static final String VERSION = "0.9.5.0";
	public static final String AUTHOR = "Pier Riccardo Monzo";
	public static final String CHANNEL = "BLEEDING"; //STABLE | BETA | BLEEDING

	// CONST
	public static final String MSG_LAST_UPDATE = "<html><i>Ultimo aggiornamento:</i> <b>%s</b></html>";
	public static final String PROP_FILE_NAME = "jebill.conf";
	public static final String PROP_CERT_LAST_UPDATE = "certlastupdate";
	public static final String PROP_BLOCKING_UPDATE = "blockingupdate";
	
	public static final String MSG_BLOCKED = "<html><b>Nuova versione disponibile!<br/>Attenzione!</b>: tutte le versioni precedenti sono state marcate come insicure.<br/>"
			+ "Per favore smetti di usare il programma e scarica la versione più aggiornata.";
	public static final String MSG_EXTRACT_TOOLTIP_BLOCKED = "Questa versione è vulnerabile. Scarica l'ultima disponibile.";
	
	
	public static final String DATE_LOG_FORMAT = "yy-MM-dd_HH.mm.ss";
}
