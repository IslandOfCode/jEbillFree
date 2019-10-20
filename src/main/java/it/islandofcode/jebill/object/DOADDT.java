package it.islandofcode.jebill.object;

public class DOADDT {
	
	 //vale per entrambi
	public String rifRiga;
	public String data;
	
	//DOA
	public String idDocumento;
	public String cup;
	public String cig;
	
	//DDT
	public String numeroDDT;
	
	
	//Costruttore DOA
	public DOADDT(String rifRiga, String idDocumento, String data, String cup, String cig) {
		this.rifRiga = rifRiga;
		this.idDocumento = idDocumento;
		this.data = data;
		this.cup = cup;
		this.cig = cig;
	}
	
	//Costruttore DDT
	public DOADDT(String rifRiga, String numeroDDT, String data) {
		this.rifRiga = rifRiga;
		this.idDocumento = numeroDDT;
		this.data = data;
	}

	//GETTER AND SETTER
	public String getRifRiga() {
		return rifRiga;
	}

	public void setRifRiga(String rifRiga) {
		this.rifRiga = rifRiga;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getIdDocumento() {
		return idDocumento;
	}

	public void setIdDocumento(String idDocumento) {
		this.idDocumento = idDocumento;
	}

	public String getCup() {
		return cup;
	}

	public void setCup(String cup) {
		this.cup = cup;
	}

	public String getCig() {
		return cig;
	}

	public void setCig(String cig) {
		this.cig = cig;
	}

	public String getNumeroDDT() {
		return numeroDDT;
	}

	public void setNumeroDDT(String numeroDDT) {
		this.numeroDDT = numeroDDT;
	}
	
}
