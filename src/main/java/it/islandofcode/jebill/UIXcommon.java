package it.islandofcode.jebill;

/**
 * Questa interfaccia fittizia serve solo per condividere appendLog tra le varie UI che usano
 * {@link Executor}
 * @author Riccardo
 *
 */
public interface UIXcommon {
	
	/**
	 * Inserire una nuova linea di testo, colorata in base alla sua natura.<br>
	 * <ul>
	 * <li><b>0</b>: Debug, indicatore nero, testo corsivo nero</li>
	 * <li><b>1</b>: Info, indicatore blu, testo normale</li>
	 * <li><b>2</b>: Warning, indicatore giallo, testo normale</li>
	 * <li><b>3</b>: Error, indicatore rosso, testo rosso grassetto</li>
	 * <li><b>4</b>: Success, indicatore e testo verde, testo sottolineato.</li>
	 * <li><b>5</b>: Reserved, attualmente non usato e visivamente pari a Debug, ma
	 * senza il corsivo.</li>
	 * </ul>
	 * 
	 * @param newline
	 * @param type
	 */
	public void appendLog(String newline, int type);
	
	public void setLastCertUpdateLabel(String newdate);
}
