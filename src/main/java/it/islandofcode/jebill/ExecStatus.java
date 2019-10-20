package it.islandofcode.jebill;

public enum ExecStatus {
	INCOMPLETE				("Operazione non completata."),
	SUCCESS					("Operazione completata!"),
	FILE_ALREADY_OPEN		("Il file risulta già aperto in un'altra applicazione"),
	IO_ERROR				("Errore di input/output"),
	TEMPLATE_ERROR			("Il template non è stato popolato correttamente"),
	VERIFY_FAILED			("Verifica firma digitale fallita"),
	GENERIC_ERROR			("Errore generico"),
	CONVERSION_ERROR		("La conversione è fallita"),
	UNSUPPORTED_OPERATION	("Operazione non supportata"),
	UNSUPPORTED_FILE_TYPE	("Tipo o estensione di file non supportata"),
	SECURITY_ERROR			("PIVA non corrispondente o licenza mancante")
	;
	
	private final String code;
	
	ExecStatus(String code){
		this.code = code;
	}
	
	public String getErrorCodeText() {
		return this.code;
	}
}
