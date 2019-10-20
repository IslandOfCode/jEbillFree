package it.islandofcode.jebill.vtl;

import java.io.File;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import it.islandofcode.jvtllib.connector.IConnector;
import it.islandofcode.jvtllib.model.DataPoint;
import it.islandofcode.jvtllib.model.DataSet;
import it.islandofcode.jvtllib.model.DataStructure;
import it.islandofcode.jvtllib.model.DataStructure.ROLE;
import it.islandofcode.jvtllib.model.Scalar;
import it.islandofcode.jvtllib.model.Scalar.SCALARTYPE;

public class EBillConnector implements IConnector {
	
	public static final String PASSWORD = "NONPUOICHIAMAREGETDADENTROLOSCRIPT!";
	
	public static final DataStructure DSTR = new DataStructure("jebill_dstr");
	static {
		DSTR.putComponent("NUMERO", new Scalar(SCALARTYPE.String), ROLE.Identifier);
		DSTR.putComponent("TIPO", new Scalar(SCALARTYPE.String), ROLE.Identifier);
		DSTR.putComponent("DATA", new Scalar(SCALARTYPE.Date), ROLE.Identifier);
		
		DSTR.putComponent("NUM_LINEA", new Scalar(SCALARTYPE.Integer), ROLE.Attribute); //String
		DSTR.putComponent("DESCRIZIONE", new Scalar(SCALARTYPE.String), ROLE.Attribute);
		DSTR.putComponent("QUANTITA", new Scalar(SCALARTYPE.Float), ROLE.Measure); //String
		DSTR.putComponent("PREZZO", new Scalar(SCALARTYPE.Float), ROLE.Measure);
		DSTR.putComponent("IVA", new Scalar(SCALARTYPE.Float), ROLE.Measure);
	}
	
	public static final String[] KEYS = {"NUMERO","TIPO","DATA","NUM_LINEA","DESCRIZIONE","QUANTITA","PREZZO","IVA"};
	
	private DefaultTableModel TABLE;

	@Override
	public DataSet get(String location, String[] keep) {
		
		DataSet DS = new DataSet("jebill_ds", "Batch generated DataSet", DSTR, true);
		
		/*
		 * Per sicurezza, rendo una eventuale chiamata da script VTL vana, ritornando
		 * il DS strutturato ma vuoto.
		 * Altrimenti passo a popolarlo.
		 */
		if(keep.length>0 && !keep[0].equals(PASSWORD))
			return DS;
		
		CsvParserSettings parserSettings = new CsvParserSettings();
		//non ho scritto il primo rigo, quindi non lo devo saltare.
		//parserSettings.setHeaderExtractionEnabled(true); // ignora il primo rigo (Header)
		CsvParser p = new CsvParser(parserSettings);
		
		p.beginParsing(new File(location));
		
		
		DataPoint dp;
		
		String[] row;
		
		while ((row = p.parseNext()) != null) {
			dp = new DataPoint();
			
			for(int i=0; i<KEYS.length; i++) {
				Scalar type = (Scalar) DSTR.getComponent(KEYS[i]).getDataType();
				dp.setValue( KEYS[i], new Scalar(row[i], type.getScalarType()) );
			}
			
			DS.setPoint(dp);
		}
		
		return DS;
	}

	@Override
	public boolean put(String location, DataSet data) {
		
		DataStructure dstr = data.getDataStructure();
		
		Vector<String> HEADER = new Vector<>(dstr.getKeys().size());//= new Vector<>(Arrays.asList(KEYS));
		
		for(String K : dstr.getKeys()) {
			HEADER.add(dstr.getComponent(K).getId());
		}
		
		Vector<Vector<String>> BODY = new Vector<>();
		Vector<String> ROW;
		DataPoint P;
		for(int p=0; p<data.getSize();p++) {
			P = data.getPoint(p);
			ROW = new Vector<>(HEADER.size());
			for(String K : P.getKeys()) {
				ROW.add(HEADER.indexOf(K), P.getValue(K).getScalar());
			}
			BODY.add(ROW);
		}
		
		this.TABLE = new DefaultTableModel(BODY,HEADER);
		
		return true;
	}
	
	
	public DefaultTableModel getTableModel() {
		return this.TABLE;
	}

}
