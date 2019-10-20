package it.islandofcode.jebill.object;

public class Dettaglio {
	public String numeroLinea;
	public String codice;
	public String valore;
	public String descrizione;
	public String quantita;
	public String unitaMisura;
	public String prezzoUnitario;
	public String scontoMaggiorazione;
	public String ritenuta;
	public String val; //??? forse validità?
	public String prezzoTotale;
	public String aliquotaIVA;
	
	public Dettaglio(String numeroLinea, String codice, String valore, String descrizione, String quantita,
			String unitaMisura, String prezzoUnitario, String scontoMaggiorazione, String ritenuta, String val,
			String prezzoTotale, String aliquotaIVA) {
		super();
		this.numeroLinea = numeroLinea;
		this.codice = codice;
		this.valore = valore;
		this.descrizione = descrizione;
		this.quantita = quantita;
		this.unitaMisura = unitaMisura;
		this.prezzoUnitario = prezzoUnitario;
		this.scontoMaggiorazione = scontoMaggiorazione;
		this.ritenuta = ritenuta;
		this.val = val;
		this.prezzoTotale = prezzoTotale;
		this.aliquotaIVA = aliquotaIVA;
	}

	public String getNumeroLinea() {
		return numeroLinea;
	}

	public void setNumeroLinea(String numeroLinea) {
		this.numeroLinea = numeroLinea;
	}

	public String getCodice() {
		return codice;
	}

	public void setCodice(String codice) {
		this.codice = codice;
	}

	public String getValore() {
		return valore;
	}

	public void setValore(String valore) {
		this.valore = valore;
	}

	public String getDescrizione() {
		return descrizione;
	}

	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	public String getQuantita() {
		return quantita;
	}

	public void setQuantita(String quantita) {
		this.quantita = quantita;
	}

	public String getUnitaMisura() {
		return unitaMisura;
	}

	public void setUnitaMisura(String unitaMisura) {
		this.unitaMisura = unitaMisura;
	}

	public String getPrezzoUnitario() {
		return prezzoUnitario;
	}

	public void setPrezzoUnitario(String prezzoUnitario) {
		this.prezzoUnitario = prezzoUnitario;
	}

	public String getScontoMaggiorazione() {
		return scontoMaggiorazione;
	}

	public void setScontoMaggiorazione(String scontoMaggiorazione) {
		this.scontoMaggiorazione = scontoMaggiorazione;
	}

	public String getRitenuta() {
		return ritenuta;
	}

	public void setRitenuta(String ritenuta) {
		this.ritenuta = ritenuta;
	}

	public String getVal() {
		return val;
	}

	public void setVal(String val) {
		this.val = val;
	}

	public String getPrezzoTotale() {
		return prezzoTotale;
	}

	public void setPrezzoTotale(String prezzoTotale) {
		this.prezzoTotale = prezzoTotale;
	}

	public String getAliquotaIVA() {
		return aliquotaIVA;
	}

	public void setAliquotaIVA(String aliquotaIVA) {
		this.aliquotaIVA = aliquotaIVA;
	}
	
}
