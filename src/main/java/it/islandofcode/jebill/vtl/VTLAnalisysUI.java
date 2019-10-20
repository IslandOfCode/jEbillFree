package it.islandofcode.jebill.vtl;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import it.islandofcode.jebill.GUI;
import it.islandofcode.jvtllib.JVTLlib;
import it.islandofcode.jvtllib.connector.IConnector;
import it.islandofcode.jvtllib.model.DataSet;
import it.islandofcode.jvtllib.model.VTLObj;
import it.islandofcode.jvtllib.newparser.error.ParseException;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

public class VTLAnalisysUI extends JFrame {

	private static final long serialVersionUID = -6636591967202824448L;
	
	private static final String DS_ORIGIN_NAME = "ORIGINE";
	
	private static final String NOSCRIPT = "NESSUNO SCRIPT PRESENTE";
	private static final String UNSELECTED = "Seleziona script";
	
	private static final String LBL_AUTHOR_TEMPLATE = "<html>Autore:  <b>";
	private static final String LBL_VERSION_TEMPLATE = "<html>Versione: <b>";
	private static final String TXT_DESCRIPTION_TEMPLATE = "Descrizione dello script";
	
	private JPanel contentPane;
	private JTable TABLE;
	private JTextField TXT_dspath;
	private JButton B_opends;
	private JComboBox<String> CBOX_script;
	private JLabel LBL_author;
	private JLabel LBL_version;
	private JTextPane T_description;
	private JButton B_executeVtl;
	
	private JFileChooser FC_filechooser;
	
	private String[] scriptName = new String[]{NOSCRIPT};
	private ArrayList<Script> scripts = new ArrayList<>();
	private HashMap<String, Script> SCRIPTS = new HashMap<>();

	
	public VTLAnalisysUI() {
		setTitle("Analisi VTL");
		setResizable(false);
		setType(Type.UTILITY);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 863, 551);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane SCROLL = new JScrollPane();
		SCROLL.setEnabled(false);
		SCROLL.setBounds(10, 218, 837, 293);
		contentPane.add(SCROLL);
		
		TABLE = new JTable();
		TABLE.setEnabled(false);
		TABLE.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TABLE.setModel(new DefaultTableModel(
			new Object[][] {
				{"Esegui uno script prima"},
			},
			new String[] {
				"Risultato"
			}
		));
		SCROLL.setViewportView(TABLE);
		
		B_opends = new JButton("Apri DataSet");
		B_opends.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				B_opends_actionPerformed(e);
			}
		});
		B_opends.setBounds(10, 11, 89, 23);
		contentPane.add(B_opends);
		
		TXT_dspath = new JTextField();
		TXT_dspath.setEditable(false);
		TXT_dspath.setBounds(109, 12, 738, 20);
		contentPane.add(TXT_dspath);
		TXT_dspath.setColumns(10);
		
		CBOX_script = new JComboBox<String>();
		CBOX_script.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				CBOX_script_itemStateChanged(e);
			}
		});
		CBOX_script.setModel(new DefaultComboBoxModel<String>(listScriptName()));
		CBOX_script.setSelectedIndex(0);
		CBOX_script.setBounds(10, 70, 200, 20);
		contentPane.add(CBOX_script);
		
		JLabel lblSelezionaQualeScript = new JLabel("Seleziona quale script vuoi eseguire:");
		lblSelezionaQualeScript.setBounds(10, 45, 200, 14);
		contentPane.add(lblSelezionaQualeScript);
		
		JPanel P_script = new JPanel();
		P_script.setBorder(new TitledBorder(null, "Informazioni Script", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		P_script.setBounds(220, 43, 627, 164);
		contentPane.add(P_script);
		P_script.setLayout(null);
		
		LBL_author = new JLabel(LBL_AUTHOR_TEMPLATE);
		LBL_author.setBounds(10, 25, 196, 14);
		P_script.add(LBL_author);
		
		LBL_version = new JLabel(LBL_VERSION_TEMPLATE);
		LBL_version.setBounds(10, 50, 196, 14);
		P_script.add(LBL_version);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(216, 25, 401, 128);
		P_script.add(scrollPane);
		
		T_description = new JTextPane();
		T_description.setEditable(false);
		T_description.setText(TXT_DESCRIPTION_TEMPLATE);
		scrollPane.setViewportView(T_description);
		
		B_executeVtl = new JButton("Esegui");
		B_executeVtl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				B_executeVtl_actionPerformed(e);
			}
		});
		B_executeVtl.setEnabled(false);
		B_executeVtl.setBounds(10, 130, 196, 23);
		P_script.add(B_executeVtl);
		
		JLabel L_logo = new JLabel("<html><center><span style=\"font-size:3em\">jVTLlib</span> <i style=\"color:white\">powered</i><br/><span style=\"color:white\">Di </span><b>Pier Riccardo Monzo</b><br/><br/><p style=\"color:white\">github.com/IslandOfCode/jVTLlib</p>");
		L_logo.setForeground(Color.ORANGE);
		L_logo.setOpaque(true);
		L_logo.setBackground(Color.DARK_GRAY);
		L_logo.setHorizontalAlignment(SwingConstants.CENTER);
		L_logo.setBounds(10, 101, 200, 106);
		contentPane.add(L_logo);
		
		
		
		FC_filechooser = new javax.swing.JFileChooser();
		FC_filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		FC_filechooser.setAcceptAllFileFilterUsed(false);
		FC_filechooser.setFileFilter(new FileNameExtensionFilter("dscsv - file origine per analisi VTL", "dscsv", "DSCSV"));
	}
	
	public void CBOX_script_itemStateChanged(ItemEvent e) {
		
		if(e.getStateChange() == ItemEvent.SELECTED ) {
			if( ((String)e.getItem()).equals(UNSELECTED) ) {
				emptyScriptPanel();
			} else {
				loadScriptPanel(((String)e.getItem()));
				B_executeVtl.setEnabled(true);
			}
		}
		
	}
	
	public void B_opends_actionPerformed(ActionEvent e) {
		FC_filechooser.showOpenDialog(this);

		File choosed = FC_filechooser.getSelectedFile();
		if (choosed != null && choosed.exists()) {
			TXT_dspath.setText(choosed.getAbsolutePath());
		}
	}
	
	private void B_executeVtl_actionPerformed(ActionEvent evt) {

		if(this.SCRIPTS.size()>0 && !TXT_dspath.getText().trim().isEmpty()) {
			
			GUI.logger.info("VTL_SCRIPT_SELECTED: " + CBOX_script.getSelectedItem() );
			
			Script S = this.SCRIPTS.get((String)CBOX_script.getSelectedItem());
			
			JVTLlib lib = new JVTLlib();
			IConnector conn = new EBillConnector();
			
			lib.addConnector(conn);
			lib.addScript(S.getScript());

			try {
				lib.parseOnly();
			} catch (ParseException | IOException e) {
				JOptionPane.showMessageDialog(null, "<html><b>Errore nel parsing, correggere lo script e riprovate!</b><br/><hr/>"
						+ "Errore segnalato da jVTLlib:<br/>"
						+ "<code>"+e.getMessage().replaceAll("<", "&lt;").replaceAll(">", "&gt;"),
						"Errore parsing!",
						JOptionPane.ERROR_MESSAGE);
				GUI.logger.severe("VTL_PARSING: " + e.getMessage().replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
				return;
			}
			
			HashMap<String,VTLObj> mem = new HashMap<>();
			DataSet ds = conn.get(TXT_dspath.getText().trim(), new String[] {EBillConnector.PASSWORD});
			GUI.logger.info("VTL_CONNECTOR_DSTR_SIZE: " + ds.getDataStructure().getKeys().size());
			GUI.logger.info("VTL_CONNECTOR_DS_SIZE: " + ds.getSize());
			mem.put(DS_ORIGIN_NAME, ds);
			lib.directMemoryInjection(mem);
			
			//altrimenti puoi eseguire
			try {
				lib.execute();
			} catch (RuntimeException | IOException e) {
				JOptionPane.showMessageDialog(null, "<html><b>Errore esecuzione, la libreria ha ritornato un errore!</b><br/><hr/>"
						+ "Errore segnalato:<br/>"
						+ "<b>"+e.getClass().getName()+"</b><br/>"
						+ "<code>"+e.getMessage(), "VTL",
						JOptionPane.ERROR_MESSAGE);
				GUI.logger.severe("VTL_EXECUTE: " + e.getMessage());
				return;
			}
			
			JOptionPane.showMessageDialog(null, "Script eseguito con successo!", "VTL",
					JOptionPane.INFORMATION_MESSAGE);
			
			TABLE.setModel( ((EBillConnector)conn).getTableModel() );
			TABLE.repaint();
			
		} else {
			JOptionPane.showMessageDialog(null, "Nessun file sorgente selezionato!", "Origine mancante!",
					JOptionPane.ERROR_MESSAGE);
		}

	}
	
	
	
	private String[] listScriptName() {
		ArrayList<String> name = new ArrayList<>();
		
		File DIR = new File("Script");
		File[] LS = DIR.listFiles();
		
		if(LS.length>0) {
			name.add(UNSELECTED);
		} else {
			this.CBOX_script.setModel(new DefaultComboBoxModel<String>(new String[]{NOSCRIPT} ));
			this.CBOX_script.setEnabled(false);
		}
		
		for(int f=0; f<LS.length; f++) {
			if(LS[f].isFile() && LS[f].getName().matches("(?i).*\\.jvtl$")) {
				try {
					Document D = Jsoup.parse(new String(Files.readAllBytes(Paths.get(LS[f].toString()))), "/", Parser.xmlParser());
					Elements root = D.select("jvtl");
					if(root.size()==1 && root.get(0).tag().toString().equals("jvtl")) {
						String author = D.select("author").get(0).text();
						String version = D.select("version").get(0).text();
						String desc = D.select("description").get(0).text();
						String script = D.select("vtl").get(0).text();
						scripts.add(new Script(author, version, desc, script));
						this.SCRIPTS.put(LS[f].getName().replace(".jvtl", ""), new Script(author, version, desc, script));
						name.add(LS[f].getName().replace(".jvtl", ""));
					}
					
				} catch (IOException e) {
					GUI.logger.severe("SCRIPTREADING: " + e.getMessage());
				}
			}
		}
		
		this.scriptName = name.toArray(new String[0]);
		GUI.logger.info("SCRIPTS READ: " + (name.size()-1) ); //tolgo uno perchè è il messagio di default!
		return this.scriptName;
	}
	
	
	private void loadScriptPanel(String key) {
		Script S = this.SCRIPTS.get(key);
		LBL_author.setText(LBL_AUTHOR_TEMPLATE+S.getAuthor());
		LBL_version.setText(LBL_VERSION_TEMPLATE+S.getVersion());
		T_description.setText(S.getDescription());
	}
	
	private void emptyScriptPanel() {
		LBL_author.setText(LBL_AUTHOR_TEMPLATE);
		LBL_version.setText(LBL_VERSION_TEMPLATE);
		T_description.setText(TXT_DESCRIPTION_TEMPLATE);
		B_executeVtl.setEnabled(false);
	}
}
