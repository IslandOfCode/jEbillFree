package it.islandofcode.jebill;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import it.islandofcode.jebill.jaxb.*;
import it.islandofcode.jebill.object.DOADDT;
import it.islandofcode.jebill.object.Dettaglio;

public class Templating {

	public static FatturaElettronicaType unmarshalFromFile(File input) {
		try {
			return unmarshalFromStream(new FileInputStream(input));
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public static FatturaElettronicaType unmarshalFromByteArray(byte[] input) {
		return unmarshalFromStream(new ByteArrayInputStream(input));
	}

	public static FatturaElettronicaType unmarshalFromStream(InputStream input) {
		Source source = new StreamSource(input);
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(FatturaElettronicaType.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

			JAXBElement<FatturaElettronicaType> radix = unmarshaller.unmarshal(source, FatturaElettronicaType.class);
			return radix.getValue();
		} catch (JAXBException e) {
			return null;
		}
	}

	private static Map<String, Object> createMap() {
		Map<String, Object> radix = new HashMap<>();
		radix.put("CPDenominazione", "");
		radix.put("CPIndirizzo", "");
		radix.put("CPCAP", "");
		radix.put("CPComune", "");
		radix.put("CPProvincia", "");
		radix.put("CPNazione", "");
		radix.put("CPIdPaese", "");
		radix.put("CPIdCodice", "");
		radix.put("CPCodFiscale", "");
		radix.put("CCDenominazione", "");
		radix.put("CCIndirizzo", "");
		radix.put("CCCAP", "");
		radix.put("CCComune", "");
		radix.put("CCProvincia", "");
		radix.put("CCNazione", "");
		radix.put("CCIdPaese", "");
		radix.put("CCIdCodice", "");
		radix.put("CCCodFiscale", "");
		radix.put("CodiceDestinatario", "");
		radix.put("PECDestinatario", "");
		radix.put("Numero", "");
		radix.put("TipoDocumento", "");
		radix.put("Data", "");
		radix.put("Divisa", "");
		radix.put("ImportoTotaleDocumento", "");
		radix.put("Arrotondamento", "");
		radix.put("BolloVirtuale", "");
		radix.put("ImportoBollo", "");
		radix.put("Causale", "");
		radix.put("DOAs", new ArrayList<DOADDT>());
		radix.put("DDTs", new ArrayList<DOADDT>());
		radix.put("righefattura", new ArrayList<Dettaglio>());
		radix.put("ImponibileImporto", "");
		radix.put("Imposta", "");
		radix.put("AliquotaIVA", "");
		radix.put("RiferimentoNormativo", "");
		radix.put("Natura", "");
		radix.put("Beneficiario", "");
		radix.put("Tipo", "");
		radix.put("Modalita", "");
		radix.put("DataTermine", "");
		radix.put("GiorniPagamento", "");
		radix.put("DataScadenza", "");
		radix.put("Importo", "");
		radix.put("Esigibilita", "");
		radix.put("Esente", ""); // totale esente
		radix.put("DaPagare", "");

		return radix;
	}

	public static Map<String, Object> populateMapTemplate(byte[] input) {
		FatturaElettronicaType FE = unmarshalFromByteArray(input);

		if (FE == null) {
			return null;
		}

		Map<String, Object> radix = createMap();
		// Inserisco valori che sono costanti
		radix.put("EMPTY", "");
		radix.put("DatiOrdineAcquisto", EBill.DatiOrdineAcquisto);
		radix.put("DatiDDT", EBill.DatiDDT);

		BigDecimal bd;

		// HEADER
		FatturaElettronicaHeaderType FEH = FE.getFatturaElettronicaHeader();

		CedentePrestatoreType FEH_CP = FEH.getCedentePrestatore();
		
		DatiAnagraficiCedenteType FEH_CP_DA = FEH_CP.getDatiAnagrafici();
		if(FEH_CP_DA!=null && FEH_CP_DA.getAnagrafica()!=null) {
			radix.put("CPDenominazione", FEH_CP_DA.getAnagrafica().getDenominazione());
			radix.put("CPIdPaese", FEH_CP_DA.getIdFiscaleIVA().getIdPaese());
			radix.put("CPIdCodice", FEH_CP_DA.getIdFiscaleIVA().getIdCodice());
			radix.put("CPCodFiscale", FEH_CP_DA.getCodiceFiscale());
		}
		
		IndirizzoType FEH_CP_I = FEH_CP.getSede();
		if(FEH_CP_I!=null) {
			radix.put("CPIndirizzo", FEH_CP_I.getIndirizzo());
			radix.put("CPCAP", FEH_CP_I.getCAP());
			radix.put("CPComune", FEH_CP_I.getComune());
			radix.put("CPProvincia", FEH_CP_I.getProvincia());
			radix.put("CPNazione", FEH_CP_I.getNazione());
		}
		
		CessionarioCommittenteType FEH_CC = FEH.getCessionarioCommittente();
		DatiAnagraficiCessionarioType FEH_CC_DA = FEH_CC.getDatiAnagrafici();
		if(FEH_CC_DA!=null && FEH_CC_DA.getAnagrafica()!=null) {
			radix.put("CCDenominazione", FEH_CC_DA.getAnagrafica().getDenominazione());
			if(FEH_CC_DA.getIdFiscaleIVA()!=null) {
				radix.put("CCIdPaese", FEH_CC_DA.getIdFiscaleIVA().getIdPaese());
				radix.put("CCIdCodice", FEH_CC_DA.getIdFiscaleIVA().getIdCodice());
			} else {
				radix.put("CCCodFiscale", FEH_CC_DA.getCodiceFiscale());
			}
		}
		
		IndirizzoType FEH_CC_I = FEH_CC.getSede();
		if(FEH_CC_I!=null) {
			radix.put("CCIndirizzo", FEH_CC_I.getIndirizzo());
			radix.put("CCCAP", FEH_CC_I.getCAP());
			radix.put("CCComune", FEH_CC_I.getComune());
			radix.put("CCProvincia", FEH_CC_I.getProvincia());
			radix.put("CCNazione", FEH_CC_I.getNazione());
		}
		
		if(FEH.getDatiTrasmissione()!=null) {
			radix.put("CodiceDestinatario", FEH.getDatiTrasmissione().getCodiceDestinatario());
			radix.put("PECDestinatario", FEH.getDatiTrasmissione().getPECDestinatario());
		}


		// BODY
		FatturaElettronicaBodyType FEB = FE.getFatturaElettronicaBody().get(0);
		DatiGeneraliType FEB_DG = FEB.getDatiGenerali();
		DatiGeneraliDocumentoType FEB_DG_DGD = FEB_DG.getDatiGeneraliDocumento();

		radix.put("Numero", FEB_DG_DGD.getNumero());
		radix.put("TipoDocumento", EBill.TIPODOCUMENTO.get((FEB_DG_DGD.getTipoDocumento()!=null)?FEB_DG_DGD.getTipoDocumento().value():"Documento"));

		radix.put("Data", dataNatural(FEB_DG_DGD.getData()));
		// TODO Si noti l'assenza di workaround. DA TESTARE!
		radix.put("Divisa", FEB_DG_DGD.getDivisa());
		radix.put("ImportoTotaleDocumento",
				FEB_DG_DGD.getImportoTotaleDocumento().setScale(2).toString());
		bd = FEB_DG_DGD.getArrotondamento();
		radix.put("Arrotondamento", (bd != null) ? bd.toPlainString() : "");
		if (FEB_DG_DGD.getDatiBollo() != null) {
			radix.put("BolloVirtuale", (FEB_DG_DGD.getDatiBollo().getBolloVirtuale()!=null)?FEB_DG_DGD.getDatiBollo().getBolloVirtuale().value():"");
			radix.put("ImportoBollo", FEB_DG_DGD.getDatiBollo().getImportoBollo().toPlainString());
		} else {
			radix.put("BolloVirtuale", "");
			radix.put("ImportoBollo", "");
		}

		if (FEB_DG_DGD.getCausale().size() > 0)
			radix.put("Causale", FEB_DG_DGD.getCausale().get(0));
		else
			radix.put("Causale", null);

		// DOAs
		List<DOADDT> DOAs = new ArrayList<>();
		DOADDT D;
		for (DatiDocumentiCorrelatiType d : FEB_DG.getDatiOrdineAcquisto()) {
			D = new DOADDT(
					(d.getRiferimentoNumeroLinea().size() > 0) ? d.getRiferimentoNumeroLinea().get(0) + "" : "",
					d.getIdDocumento(),
					dataNatural(d.getData()),
					d.getCodiceCUP(),
					d.getCodiceCIG());
			DOAs.add(D);
		}
		radix.put("DOAs", DOAs);

		// DDTs
		List<DOADDT> DDTs = new ArrayList<>();
		for (DatiDDTType d : FEB_DG.getDatiDDT()) {
			D = new DOADDT(
					(d.getRiferimentoNumeroLinea().size() > 0) ? d.getRiferimentoNumeroLinea().get(0) + "" : "",
					d.getNumeroDDT(),
					dataNatural(d.getDataDDT())
					);
			DDTs.add(D);
		}
		radix.put("DDTs", DDTs);

		DatiBeniServiziType FEB_DBS = FEB.getDatiBeniServizi();

		// DETTAGLIO
		List<Dettaglio> righefattura = new ArrayList<>();
		Dettaglio d;
		for (DettaglioLineeType L : FEB_DBS.getDettaglioLinee()) {
			d = new Dettaglio(L.getNumeroLinea() + "",
					(L.getCodiceArticolo().size()>0)?L.getCodiceArticolo().get(0).getCodiceTipo():"",
					(L.getCodiceArticolo().size()>0)?L.getCodiceArticolo().get(0).getCodiceValore():"",
					(L.getDescrizione()!=null)?L.getDescrizione():"",
					(L.getQuantita()!=null)?L.getQuantita().toPlainString():"",
					(L.getUnitaMisura()!=null)?L.getUnitaMisura():"",
					toMoney(L.getPrezzoUnitario()),
					(L.getScontoMaggiorazione().size() > 0)
							? L.getScontoMaggiorazione().get(0).getPercentuale().toPlainString()
							: "", // TODO da vedere perchè c'è molta roba
					(L.getRitenuta() != null) ? L.getRitenuta().value() : "",
					"",
					toMoney(L.getPrezzoTotale()),
					L.getAliquotaIVA().toString());
			righefattura.add(d);
		}

		radix.put("righefattura", righefattura);

		DatiRiepilogoType FEB_DBS_DR = (FEB_DBS.getDatiRiepilogo().size()>0)?FEB_DBS.getDatiRiepilogo().get(0):null;
		if(FEB_DBS_DR!=null) {
			radix.put("ImponibileImporto", toMoney(FEB_DBS_DR.getImponibileImporto()));
			radix.put("Imposta",toMoney(FEB_DBS_DR.getImposta()));
			radix.put("AliquotaIVA",toMoney(FEB_DBS_DR.getAliquotaIVA()));
			radix.put("RiferimentoNormativo", FEB_DBS_DR.getRiferimentoNormativo());
			radix.put("Natura", FEB_DBS_DR.getNatura());
			radix.put("Esigibilita", (FEB_DBS_DR.getEsigibilitaIVA()!=null)?FEB_DBS_DR.getEsigibilitaIVA().value():""); // esigibilità
		}
		

		DettaglioPagamentoType FEB_DP = null;
		if(FEB.getDatiPagamento().size()>0 && FEB.getDatiPagamento().get(0).getDettaglioPagamento().size()>0) {
			FEB_DP = FEB.getDatiPagamento().get(0).getDettaglioPagamento().get(0);
			radix.put("Beneficiario", FEB_DP.getBeneficiario());
			radix.put("Tipo", "");
			radix.put("Modalita", (FEB_DP.getModalitaPagamento()!=null)?FEB_DP.getModalitaPagamento().value():"");
			radix.put("DataTermine", dataNatural(FEB_DP.getDataDecorrenzaPenale()));
			radix.put("GiorniPagamento", FEB_DP.getGiorniTerminiPagamento());
			radix.put("DataScadenza",dataNatural(FEB_DP.getDataScadenzaPagamento()));
			radix.put("Importo", toMoney(FEB_DP.getImportoPagamento()));
			radix.put("DaPagare", toMoney(FEB_DP.getImportoPagamento())); // totale+imposte
		}
		
		radix.put("Esente", ""); // totale esente

		// prima di ritornare, assegna a tutti i valori nulli, stringa vuota
		// così da evitare nullpointer nel template.

		for (String S : radix.keySet()) {
			if (radix.get(S) == null) {
				radix.put(S, "");
			}
		}

		// ritorno mappa popolata con tutto il popolabile
		return radix;
	}
	
	private static String dataNatural(XMLGregorianCalendar xgc) {
		if(xgc==null)
			return "";
		StringBuilder sb = new StringBuilder("");
		return sb
				.append(addLeadingZero( xgc.getDay()))
				.append("/")
				.append(addLeadingZero( xgc.getMonth()))
				.append("/")
				.append(xgc.getYear())
				.toString();
	}
	
	private static String toMoney(BigDecimal money) {
		if(money==null)
			return "";
		return money.setScale(2, RoundingMode.HALF_EVEN).toString();
	}
	
	private static String addLeadingZero(int a) {
		if(a>9)
			return ""+a;
		else
			return "0"+a;
	}

}
