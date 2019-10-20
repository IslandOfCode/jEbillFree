package it.islandofcode.jebill;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import it.islandofcode.jebill.batch.BatchUI;
import it.islandofcode.jebill.object.Settings;
import it.islandofcode.jebill.vtl.VTLAnalisysUI;

public class GUI implements UIXcommon{

	// LOG STUFF
	public final static Logger logger = Logger.getLogger("jEBill");
	private static FileHandler fh = null;
	private static String fhPath = "";

	// GUI STUFF
	private JFrame frmJebill;
	public JEditorPane TA_log;
	private JTextField TXT_path;
	private JCheckBox CB_updatecert;
	private JButton B_extract;
	private JButton B_openfile;
	private JLabel L_lastupdate;
	private JCheckBox CB_verifysigned;
	private JCheckBox CB_openPDF;
	private JComboBox<ExtractionMode> CBOX_mode;

	private JFileChooser FC_filechooser;

	// OTHER STUFF
	public File choosed = null;
	private BatchUI batch;
	private Help help;
	private VTLAnalisysUI VTL;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		LogginInit();

		logger.info("jEBill v" + EBill.VERSION + ", " + EBill.AUTHOR + ", islandofcode.it, 2018");

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					
					if(!System.getProperty("os.name").toLowerCase().contains("windows")) {
						logger.severe("OS non windows ["+System.getProperty("os.name")+"], impossibile continuare.");
						JOptionPane.showMessageDialog(null, "jEBill può essere eseguito solo su OS Microsoft Windows.", "Attenzione!",
								JOptionPane.ERROR_MESSAGE);
						System.exit(-1);
					}
					
					GUI window = new GUI();
					window.frmJebill.setVisible(true);
					
				} catch (Exception e) {
					logger.severe("Errore generico in GUI main: " + e.getMessage() );
					JOptionPane.showMessageDialog(null, "<html><center><b>Errore critico, impossibile avviate jEBill!</b></center><br/>"
							+ "<hr/>Messaggio d'errore: <br/><code>"+e.toString(), "ERRORE!",
							JOptionPane.ERROR_MESSAGE);
					System.exit(-1);
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();

		frmJebill.setTitle("jEBill v" + EBill.VERSION);

		frmJebill.setIconImages(EBill.ICONS);
		
		JMenuBar menuBar = new JMenuBar();
		frmJebill.setJMenuBar(menuBar);
		
		JMenu M_file = new JMenu("File");
		menuBar.add(M_file);
		
		JMenuItem MI_singlefile = new JMenuItem("Apri file...");
		MI_singlefile.setIcon(new ImageIcon(GUI.class.getResource("/icon/cartella.png")));
		M_file.add(MI_singlefile);
		
		JMenuItem MI_batchfile = new JMenuItem("Batch");
		MI_batchfile.setIcon(new ImageIcon(GUI.class.getResource("/icon/proprietà.png")));
		M_file.add(MI_batchfile);
		
		JSeparator separator = new JSeparator();
		M_file.add(separator);
		
		JMenuItem MI_exit = new JMenuItem("Esci");
		MI_exit.setIcon(new ImageIcon(GUI.class.getResource("/icon/esci.png")));
		M_file.add(MI_exit);
		
		JMenu M_tools = new JMenu("Strumenti");
		menuBar.add(M_tools);
		
		JMenuItem MI_VTL = new JMenuItem("Analisi VTL");
		MI_VTL.setIcon(new ImageIcon(GUI.class.getResource("/icon/configurazione.png")));
		M_tools.add(MI_VTL);
		
		JMenu M_help = new JMenu("Aiuto");
		menuBar.add(M_help);
		
		JMenuItem MI_update = new JMenuItem("Cerca aggiornamenti");
		MI_update.setIcon(new ImageIcon(GUI.class.getResource("/icon/aggiorna.png")));
		M_help.add(MI_update);
		
		JSeparator separator_1 = new JSeparator();
		M_help.add(separator_1);
		
		JMenuItem MI_help = new JMenuItem("Informazioni su jEBill");
		MI_help.setIcon(new ImageIcon(GUI.class.getResource("/icon/about.png")));
		M_help.add(MI_help);
		
		this.appendLog("jEBill avviato, file di log creato", 1);

		FC_filechooser = new javax.swing.JFileChooser();
		FC_filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		FC_filechooser.setAcceptAllFileFilterUsed(false);
		FC_filechooser.setFileFilter(new FileNameExtensionFilter("xml - File in chiaro", "xml", "XML"));
		FC_filechooser.setFileFilter(new FileNameExtensionFilter("p7m - File firmati digitalmente", "p7m", "P7M"));
		
		TA_log.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(e.getURL().toURI());
						} catch (IOException | URISyntaxException e1) {
							logger.severe(e1.getMessage());
							JOptionPane.showMessageDialog(frmJebill, "Non è possibile aprire questo link.",
									"Errore Link!", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});
		
		MI_VTL.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MI_VTL_actionPerformed(e);
			}
		});
		
		MI_singlefile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				B_openfile_actionPerformed(e);
			}
		});
		
		MI_exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				B_exit_actionPerformed(e);
			}
		});
		
		MI_update.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				B_update_actionPerformed(e);
			}
		});
		
		MI_help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				B_help_actionPerformed(e);
			}
		});
		
		MI_batchfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				B_batch_actionPerformed(e);
			}
		});

		B_openfile.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				B_openfile_actionPerformed(evt);
			}
		});

		B_extract.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				B_extract_actionPerformed(evt);
			}
		});

		logger.info("GUI inizializzata, in attesa di input utente.");

		if (!checkProp()) {
			defaultProp();
			logger.info("File di configurazione generato.");
		} else {
			// leggi la data da scrivere nella label (ultimo aggiornamento cert)
			// verifica inoltre che il file del certificato esista!
			// in caso contrario aggiornare in accordo il valore del file proprierties
			logger.info("File di configurazione individuato.");
			this.setLastCertUpdateLabel(GUI.readProp(EBill.PROP_CERT_LAST_UPDATE));
			
			if ("1".equals(GUI.readProp(EBill.PROP_BLOCKING_UPDATE))) {
				String[] newver = this.checkNewVersion();
				if(newver!=null) { //ho 1 e una nuova versione è presente!
					appendLog("Questa versione è vulnerabile!",3);
					appendLog("Nuova versione: "+ newver[0],1);
					appendLog("Motivo: " + newver[3],1);
					appendLog("Clicca per scaricare la nuova versione: <a href=\"https://www.islandofcode.it/jebill/latest/jebill-"+newver[0]+".zip\">https://www.islandofcode.it/jebill/latest/jebill-"+newver[0]+".zip</a>",1);
					JOptionPane.showMessageDialog(this.frmJebill, String.format(EBill.MSG_BLOCKED, newver[0]), "ATTENZIONE!",
							JOptionPane.ERROR_MESSAGE);
					this.B_extract.setEnabled(false);
					this.B_extract.setToolTipText(EBill.MSG_EXTRACT_TOOLTIP_BLOCKED);
				} else { //ho 1, ma non ci sono nuove versioni. Aggiorno il flag al valore corretto.
					GUI.updateProp(EBill.PROP_BLOCKING_UPDATE, "0");
					appendLog("Il file di configurazione è stato aggiornato.",2);
				}
				
			}//non devo fare niente
		}
		
		if(!checkScriptVTL()) {
			this.defaultScriptVTL();
			logger.warning("Cartella Script non presente, estrazione default.");
		}

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmJebill = new JFrame();
		frmJebill.setTitle("jEBill");
		frmJebill.setResizable(false);
		frmJebill.setMinimumSize(new Dimension(450, 500));
		//frmJebill.setBounds(100, 100, 450, 417);
		frmJebill.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmJebill.getContentPane().setLayout(null);

		B_openfile = new JButton("Apri file...");
		B_openfile.setBounds(6, 11, 89, 23);
		frmJebill.getContentPane().add(B_openfile);

		TXT_path = new JTextField();
		TXT_path.setToolTipText("Qui appare il percorso del file selezionato.");
		TXT_path.setEditable(false);
		TXT_path.setBounds(105, 12, 329, 20);
		frmJebill.getContentPane().add(TXT_path);
		TXT_path.setColumns(10);

		JSeparator separator = new JSeparator();
		separator.setBounds(6, 45, 428, 2);
		frmJebill.getContentPane().add(separator);

		CB_updatecert = new JCheckBox("Aggiorna certificati");
		CB_updatecert.setBounds(6, 54, 424, 23);
		frmJebill.getContentPane().add(CB_updatecert);

		L_lastupdate = new JLabel(String.format(EBill.MSG_LAST_UPDATE, "MAI"));
		L_lastupdate.setHorizontalAlignment(SwingConstants.LEFT);
		L_lastupdate.setBounds(16, 84, 418, 14);
		frmJebill.getContentPane().add(L_lastupdate);

		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(6, 195, 428, 2);
		frmJebill.getContentPane().add(separator_1);

		B_extract = new JButton("Estrai");
		B_extract.setBounds(148, 405, 128, 34);
		frmJebill.getContentPane().add(B_extract);

		CB_verifysigned = new JCheckBox("Verifica firma digitale");
		CB_verifysigned.setSelected(true);
		CB_verifysigned.setToolTipText("Non disponibile in questa versione");
		CB_verifysigned.setBounds(6, 105, 424, 23);
		frmJebill.getContentPane().add(CB_verifysigned);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(null);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(6, 208, 428, 186);
		frmJebill.getContentPane().add(scrollPane);

		TA_log = new JEditorPane();
		TA_log.setContentType("text/html");
		TA_log.setFont(new Font("Tahoma", Font.PLAIN, 13));
		DefaultCaret caret = (DefaultCaret) TA_log.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		TA_log.setCaret(caret);
		scrollPane.setViewportView(TA_log);
		TA_log.setText("<center><h2>jEBill</h2>\r\n<i>made by</i>: <b>Pier Riccardo Monzo</b><br>\r\n<a href=\"https://www.islandofcode.it\">islandofcode.it</a>\r\n<br><br>\r\n<hr style=\"width:60%\">\r\n</center>\r\n<p id=\"logging\"></p>");
		//TA_log.setText("<center><h2>jEBill</h2></center><p id=\"logging\"></p>");//necessario perchè altrimenti non stampa i log
		TA_log.setEditable(false);

		CB_openPDF = new JCheckBox("Apri direttamente il file appena generato");
		CB_openPDF.setToolTipText(
				"Se spuntata, alla fine dell'estrazione il PDF viene aperto con il programma associato a questo tipo di file.");
		CB_openPDF.setSelected(true);
		CB_openPDF.setBounds(6, 156, 225, 23);
		frmJebill.getContentPane().add(CB_openPDF);
		
		CBOX_mode = new JComboBox<>();
		CBOX_mode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if( (e.getStateChange() == ItemEvent.SELECTED) || (e.getStateChange() == ItemEvent.DESELECTED) ) {
					if( ((ExtractionMode)e.getItem()).equals(ExtractionMode.VERIFICA) && !CB_verifysigned.isSelected()) {
						CB_verifysigned.setSelected(true);
					}
				}
			}
		});
		CBOX_mode.setModel(new DefaultComboBoxModel<ExtractionMode>(ExtractionMode.values()));
		CBOX_mode.setSelectedIndex(1);
		CBOX_mode.setBounds(241, 131, 193, 23);
		frmJebill.getContentPane().add(CBOX_mode);
		
		JLabel lblModalitEstrazioneDella = new JLabel("Modalità estrazione della fattura:");
		lblModalitEstrazioneDella.setFont(new Font("Tahoma", Font.ITALIC, 11));
		lblModalitEstrazioneDella.setBounds(16, 135, 215, 14);
		frmJebill.getContentPane().add(lblModalitEstrazioneDella);
		
		frmJebill.pack();
		frmJebill.setLocationRelativeTo(null);
		//logger.info("GUI PACKED.");
	}

	private void B_openfile_actionPerformed(java.awt.event.ActionEvent evt) {
		FC_filechooser.showOpenDialog(frmJebill);

		choosed = FC_filechooser.getSelectedFile();
		if (choosed != null && choosed.exists()) {
			TXT_path.setText(choosed.getAbsolutePath());
			if(ResourceManager.getExtensionByStringHandling(choosed.getPath()).orElse("").toLowerCase()=="xml" ) {
				appendLog("Si sta aprendo un XML non firmato.", 2);
			}
		}
	}

	private void B_exit_actionPerformed(java.awt.event.ActionEvent evt) {
		int dialogResult = JOptionPane.showConfirmDialog(null, "Sei sicuro di voler uscire?", "Uscita",
				JOptionPane.YES_NO_OPTION);
		if (dialogResult == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}
	

	private void MI_VTL_actionPerformed(java.awt.event.ActionEvent evt) {
		if(VTL==null) {
			VTL = new VTLAnalisysUI();
			VTL.setLocationRelativeTo(null);
			VTL.setVisible(true);
		} else {
			VTL.setVisible(true);
			VTL.toFront();
			VTL.repaint();
		}
	}

	private void B_help_actionPerformed(java.awt.event.ActionEvent evt) {
		//JOptionPane.showMessageDialog(this.frmJebill, "Per ulteriori informazioni, consulta la guida in PDF.", "Info",
		//		JOptionPane.INFORMATION_MESSAGE);
		if(help==null) {
			help = new Help();
			help.setLocationRelativeTo(null);
			help.setVisible(true);
		} else {
			help.setVisible(true);
			help.toFront();
			help.repaint();
		}
		
	}

	private void B_extract_actionPerformed(java.awt.event.ActionEvent evt) {
		if (choosed == null || TXT_path.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this.frmJebill, "Non hai selezionato alcun file.", "File mancate!",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		boolean verify = CB_verifysigned.isSelected() || ((ExtractionMode)CBOX_mode.getSelectedItem()).equals(ExtractionMode.VERIFICA);
		
		Settings S = new Settings(CB_updatecert.isSelected(), verify, CB_openPDF.isSelected(), false, (ExtractionMode) CBOX_mode.getSelectedItem());
		//Executor T = new Executor(this, this.choosed.getPath(), logger, S);
		FutureTask<ExecStatus> FT = new FutureTask<>(new Executor(this, this.choosed.getPath(), logger, S));
		Thread T = new Thread(FT);
		T.start();
		try {
			ExecStatus ret = FT.get(); //il valore di ritorno non mi serve
			this.appendLog("THREAD_RET_VALUE: " + ret.getErrorCodeText(), 0);
		} catch (InterruptedException | ExecutionException e) {
			logger.severe("EXTRACTTHREAD: "+e.getMessage());
			appendLog("Estrazione fallita!",3);
			appendLog("EXTRACTTHREAD: "+e.getMessage(),1);
		}
	}

	private void B_batch_actionPerformed(java.awt.event.ActionEvent evt) {
		if(batch==null) {
			batch = new BatchUI(this);
			batch.setLocationRelativeTo(null);
			batch.setVisible(true);
		} else {
			batch.setVisible(true);
			batch.toFront();
			batch.repaint();
		}
		
	}
	
	private void B_update_actionPerformed(java.awt.event.ActionEvent evt) {
		String[] newver = this.checkNewVersion();
		if(newver!=null) {
			appendLog("Nuova versione disponibile!", 4);
			appendLog("Canale: "+EBill.CHANNEL,1);
			appendLog("Versione attuale: "+EBill.VERSION, 1);
			appendLog("Nuova versione: "+newver[0], 1);
			appendLog("Livello: "+newver[2], (newver[2].equals("pericolo")?2:1 ));
			appendLog("Motivo: " + newver[3],1);
			if(newver[2].equals("pericolo")) {
				GUI.updateProp(EBill.PROP_BLOCKING_UPDATE, "1");
				JOptionPane.showMessageDialog(this.frmJebill, String.format(EBill.MSG_BLOCKED, newver[0]), "ATTENZIONE!",
						JOptionPane.ERROR_MESSAGE);
				this.B_extract.setEnabled(false);
				this.B_extract.setToolTipText(EBill.MSG_EXTRACT_TOOLTIP_BLOCKED);
			}
			appendLog("Clicca per scaricare la nuova versione: <a href=\"https://www.islandofcode.it/jebill/latest/jebill-"+newver[0]+".zip\">https://www.islandofcode.it/jebill/latest/jebill-"+newver[0]+".zip</a>",1);
		} else {
			JOptionPane.showMessageDialog(this.frmJebill, "Hai l'ultima versione disponibile.", "jEBill aggiornato!",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	@Override
	public void setLastCertUpdateLabel(String newdate) {
		if (newdate != null && !newdate.isEmpty())
			this.L_lastupdate.setText(String.format(EBill.MSG_LAST_UPDATE, newdate));
	}

	private static void LogginInit() {
		SimpleDateFormat format = new SimpleDateFormat(EBill.DATE_LOG_FORMAT);
		File logsdir = new File("logs");
		logsdir.mkdir();
		fhPath = "logs/jebill_" + format.format(Calendar.getInstance().getTime()) + ".log";
		try {
			fh = new FileHandler(fhPath);
		} catch (Exception e) {
			//e.printStackTrace();
			logger.severe("Eccezione file handler " + e.getMessage());
		}

		fh.setFormatter(new SimpleFormatter());
		logger.setUseParentHandlers(false);
		logger.addHandler(fh);
		
		//a questo punto ho i due nuovi file di log
		try {
			ZipUtil.zipOldLog(fhPath);
		} catch (IOException e) {
			logger.severe("Eccezione creazione ZIP: "+e.getMessage());
			//e.printStackTrace();
		}
		
	}
	
	private boolean checkScriptVTL() {
		File S = new File("Script");
		String[] list = S.list();
		int count = 0;
		
		if(list!=null) {
			for(int s=0; s<list.length; s++) {
				if(list[s].matches("(?i).+(\\.jvtl)$")) {
					count++;
				}
			}
		}

		return (S.exists() && S.isDirectory() && (count>0) );
	}
	
	private void defaultScriptVTL() {
		File S = new File("Script");
		if(!S.exists()) {
			S.mkdir();
		}
		try {
			ZipUtil.extractScriptFile(S);
		} catch (IOException e) {
			logger.severe("DEFAULTSCRIPT: " + e.getMessage());
		}
	}

	public static boolean checkProp() {
		File f = new File(EBill.PROP_FILE_NAME);
		return (f.exists() && !f.isDirectory() && f.canRead());
	}

	private static void defaultProp() {
		Properties P = new Properties();
		OutputStream output = null;

		try {
			output = new FileOutputStream(EBill.PROP_FILE_NAME);

			P.setProperty(EBill.PROP_CERT_LAST_UPDATE, "mai");
			P.setProperty(EBill.PROP_BLOCKING_UPDATE, "0");

			P.store(output, null);

		} catch (IOException io) {
			logger.severe("Impossibile scrivere il file delle proprietà: " + io.getMessage());
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					logger.severe("Impossibile chiudere il file delle proprietà: " + e.getMessage());
				}
			}

		}
	}

	public static void updateProp(String key, String value) {
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(EBill.PROP_FILE_NAME);

			Properties props = new Properties();
			props.load(in);
			// in.close();

			out = new FileOutputStream(EBill.PROP_FILE_NAME);
			props.setProperty(key, value);
			props.store(out, null);
			// out.close();
		} catch (IOException e) {
			logger.severe(e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.severe("Impossibile chiudere il file delle proprietà (lettura): " + e.getMessage());
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.severe("Impossibile chiudere il file delle proprietà (scrittura): " + e.getMessage());
				}
			}
		}
	}

	public static String readProp(String key) {
		String toRet = "";
		FileInputStream in = null;
		try {
			in = new FileInputStream(EBill.PROP_FILE_NAME);

			Properties props = new Properties();
			props.load(in);
			toRet = props.getProperty(key);
			// in.close();
		} catch (IOException e) {
			logger.severe(e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					logger.severe("Impossibile chiudere il file delle proprietà: " + e.getMessage());
				}
			}
		}
		if (toRet == null)
			return "";

		return toRet;
	}
	
	private String[] checkNewVersion() {
		appendLog("Verifica presenza nuova versione...", 1);
		logger.info("Verifica nuova versione.");
		
		String[] toret = new String[4];
		
		/*
		 * 0: versione
		 * 1: url
		 * 2: livello (can be empty)
		 * 3: ragione (can be empty)
		 */
		
		String url = "https://www.islandofcode.it/jebillfree/update.php";
		
		try {
			//Document doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", "", Parser.xmlParser());
			Document doc = Jsoup.connect(url).data("channel", EBill.CHANNEL).parser(Parser.xmlParser()).get();
			Elements E = doc.getElementsByTag("latest");

			if(!E.isEmpty() && /*!E.get(0).text().equals(VERSION) &&*/ E.get(0).text().compareTo(EBill.VERSION)>0 ) {
				toret[0] = E.get(0).text();
				//toret[1] = "https://www.islandofcode.it/jebillfree/latest/jebill-"+toret[0]+".zip";
				//https://github.com/IslandOfCode/jebill/releases/download/0.9.4.6/jebill-0.9.4.6.exe
				toret[1] = "https://github.com/IslandOfCode/jebillfree/releases/download/"+toret[0]+"/jebill-"+toret[0]+".zip";
				E = doc.getElementsByTag("level");
				if(!E.isEmpty()) {
					toret[2] = E.get(0).text();
				} else {
					toret[2] = "";
				}
				E = doc.getElementsByTag("reason");
				if(!E.isEmpty()) {
					toret[3] = E.get(0).text();
				} else {
					toret[3] = "";
				}
			} else {
				return null;
			}
			return toret;
		} catch (IOException e) {
			appendLog("Errore nella verifica di aggiornamenti!", 3);
			logger.severe(e.getMessage());
		}
		return null;
	}
	

	@Override
	public void appendLog(String newline, int type) {
		switch (type) {
		case 0:
			newline = "[<span style=\"font-weight: bold\">DBG</span>] " + newline;
			break;
		case 1:
			newline = "[<span style=\"color:blue\">ii</span>] " + newline;
			break;
		case 2:
			newline = "[<span style=\"color:orange\">WW</span>] " + newline;
			break;
		case 3:
			newline = "[<span style=\"color:red\">EE</span>] <span style=\"color:red;font-weight: bold\">" + newline
					+ "</span>";
			break;
		case 4:
			newline = "[<span style=\"color:green\">OK</span>] <span style=\"color:green;text-decoration: underline\">"
					+ newline + "</span>";
			break;
		default:
			newline = "[??] " + newline;
		}

		try {
			HTMLDocument doc = (HTMLDocument) TA_log.getDocument();
			// Element e = doc.getElement(doc.getDefaultRootElement(),
			// StyleConstants.NameAttribute, HTML.Tag.CENTER);
			Element ex = doc.getElement("logging");
			doc.insertBeforeEnd(ex, newline + "<br>");
		} catch (BadLocationException | IOException exc) {
			logger.severe(exc.getMessage());
		}
	}
}
