package it.islandofcode.jebill;

/**
 * Enumeratore per le modalità di estrazione.
 * @author Pier Riccardo Monzo
 */
public enum ExtractionMode {
	VERIFICA 	("Solo verifica"),			//non in uso, dovrebbe solo controllare che è una fattura valida e bon.
	FATTURA		("Solo fattura"),			//genera la fattura e basta
	ALLEGATO	("Solo allegati"),			//estrai solo gli allegati e basta
	ENTRAMBI	("Fattura e Allegati")		//estrai fattura e allegati
	;
	
	
	private final String mode;
	
	ExtractionMode(String mode){
		this.mode = mode;
	}
	
	public String getModeText() {
		return this.mode;
	}	
}
