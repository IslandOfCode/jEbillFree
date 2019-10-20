package it.islandofcode.jebill.batch;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import it.islandofcode.jebill.ExtractionMode;
import it.islandofcode.jebill.GUI;
import it.islandofcode.jebill.UIXcommon;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class BatchUI extends JFrame implements UIXcommon{

	private static final long serialVersionUID = -816486907894464545L;
	private JPanel contentPane;
	public JTextField TXT_dirpath;
	private JFileChooser fileChooser;
	public JCheckBox CB_verifysign;
	public JComboBox<ExtractionMode> CBOX_mode;
	private JComboBox<String> CBOX_thread;
	private JCheckBox CB_resultlog;
	private JCheckBox CB_generateVTL;
	private JButton B_directory;
	private JButton B_execute;
	
	public JProgressBar progressBar;
	
	private UIXcommon main;
	private boolean UIlock = false;

	/**
	 * Create the frame.
	 */
	public BatchUI(UIXcommon main) {
		setTitle("Modalità Batch");
		setType(Type.UTILITY);
		setResizable(false);
		setBounds(100, 100, 485, 293);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		B_directory = new JButton("Cartella...");
		B_directory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				B_directory_actionPerformed(e);
			}
		});
		B_directory.setBounds(10, 11, 89, 23);
		contentPane.add(B_directory);
		
		TXT_dirpath = new JTextField();
		TXT_dirpath.setToolTipText("Path della cartella che contiene le fatture da elaborare");
		TXT_dirpath.setEditable(false);
		TXT_dirpath.setBounds(109, 12, 360, 20);
		contentPane.add(TXT_dirpath);
		TXT_dirpath.setColumns(10);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Impostazioni", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(10, 45, 459, 173);
		contentPane.add(panel);
		panel.setLayout(null);
		
		CB_verifysign = new JCheckBox("Verifica firma digitale delle fatture prima di estrarle");
		CB_verifysign.setToolTipText("Le firme verranno aggiornate (se possibile) automaticamente.");
		CB_verifysign.setBounds(6, 16, 443, 23);
		panel.add(CB_verifysign);
		
		CB_generateVTL = new JCheckBox("(SPERIMENTALE) Genera file per analisi VTL");
		CB_generateVTL.setToolTipText("<html>Genera un file che contiene alcune informazioni sulle fatture analizzate.<br/>\r\nQuesto file può poi essere analizzato tramite VTL per ottenere alcune informazioni aggiuntive (Aggregate).\r\n</html>");
		CB_generateVTL.setFont(new Font("Tahoma", Font.BOLD, 11));
		CB_generateVTL.setForeground(Color.RED);
		CB_generateVTL.setBounds(6, 113, 447, 23);
		panel.add(CB_generateVTL);
		
		CBOX_mode = new JComboBox<ExtractionMode>();
		CBOX_mode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED ) { //|| e.getStateChange() == ItemEvent.DESELECTED) {
					if( ((ExtractionMode)e.getItem()).equals(ExtractionMode.VERIFICA) ) {//&& !CB_verifysign.isSelected()) {
						CB_verifysign.setSelected(true);
						CB_generateVTL.setEnabled(false);
					} else {
						CB_generateVTL.setEnabled(true);
					}
				}
			}
		});
		CBOX_mode.setModel(new DefaultComboBoxModel<ExtractionMode>(ExtractionMode.values()));
		CBOX_mode.setSelectedIndex(1);
		CBOX_mode.setBounds(170, 46, 100, 20);
		panel.add(CBOX_mode);
		
		JLabel lblModalitEstrazioneFattura = new JLabel("Modalità estrazione fattura:");
		lblModalitEstrazioneFattura.setBounds(16, 49, 144, 14);
		panel.add(lblModalitEstrazioneFattura);
		
		CB_resultlog = new JCheckBox("Crea file di log operazione");
		CB_resultlog.setSelected(true);
		CB_resultlog.setBounds(6, 70, 154, 23);
		panel.add(CB_resultlog);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(6, 100, 443, 2);
		panel.add(separator);
		
		CBOX_thread = new JComboBox<>();
		CBOX_thread.setModel(new DefaultComboBoxModel<String>(new String[] {"Auto", "1", "2", "4", "6", "8"}));
		CBOX_thread.setSelectedIndex(1);
		CBOX_thread.setBounds(389, 140, 60, 20);
		panel.add(CBOX_thread);
		
		JLabel lblsperimentaleNumeroDi = new JLabel("(SPERIMENTALE) Numero di thread coinvolti nell'estrazione:");
		lblsperimentaleNumeroDi.setToolTipText("<html>\r\nIndica il numero di thread che si vuole dedicare all'operazione di estrazione.<br/>\r\nSe si seleziona <i>Auto</i>, si userà il numero di thread resi disponibili dalla JVM a jEBill.<br/>\r\n<b>ATTENZIONE!</b> Non usare un numero di thread superiore a quelli forniti dal processore in uso<br/>\r\n(<i>Ad es. se la CPU ha 4 thread, non selezionare 6 o 8</i>).<br/>\r\n<b>ATTENZIONE!</b> un qualsiasi valore superiore ad 1 (singolo thread) potrebbe provocare instabilità nell'esecuzione<br/>\r\ndi jEBill. In questo caso diminuire il numero di thread e riprovare.\r\n</html>");
		lblsperimentaleNumeroDi.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblsperimentaleNumeroDi.setForeground(Color.RED);
		lblsperimentaleNumeroDi.setBounds(16, 143, 363, 14);
		panel.add(lblsperimentaleNumeroDi);
		
		B_execute = new JButton("Esegui estrazione Batch");
		B_execute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				B_execute_actionPerformed(e);
			}
		});
		B_execute.setBounds(298, 229, 161, 24);
		contentPane.add(B_execute);
		
		progressBar = new JProgressBar();
		progressBar.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				progressBarHandle(evt);
			}
		});
		progressBar.setBounds(10, 229, 278, 23);
		contentPane.add(progressBar);

		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);//Directory_only è platform dependent, quindi devo fare qualcosa di particolare
		
		GUI.logger.info("BATCHUI caricata");
		this.main = main;
	}
	
	private void progressBarHandle(PropertyChangeEvent e) {
		if ("progress" == e.getPropertyName()) {
            int progress = (Integer) e.getNewValue();
            //System.out.println(progress);
            progressBar.setValue(progress);
            this.progressBar.setString("Estrazione " + progress + "%");
        } else if("OP".equals(e.getPropertyName())){
        	if("toggle".equals(e.getNewValue())) {
        		this.toggleButton();
        	}
        	if("OFF".equals(e.getNewValue())) {
        		//System.out.println("OFF");
        		this.progressBar.setIndeterminate(false);
        	}
        	if("ON".equals(e.getNewValue())) {
        		//System.out.println("ON");
        		this.progressBar.setIndeterminate(true);
        		this.progressBar.setStringPainted(true);
        		this.progressBar.setString("Finalizzazione...");
        	}
        }
	}
	
	
	private void B_directory_actionPerformed(ActionEvent evt) {
		int option = fileChooser.showDialog(null, "Seleziona cartella");

		if (option == JFileChooser.APPROVE_OPTION) {
		    File f = fileChooser.getSelectedFile();
		    // if the user accidently click a file, then select the parent directory.
		    if (!f.isDirectory()) {
		        f = f.getParentFile();
		    }
		    TXT_dirpath.setText(f.getPath());
		}
	}
	
	private void B_execute_actionPerformed(ActionEvent evt) {
		if (TXT_dirpath.getText().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Non hai selezionato alcun file.", "File mancate!",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		toggleButton();

		int thread;
		if(((String)CBOX_thread.getSelectedItem()).equals("Auto")) {
			thread = Runtime.getRuntime().availableProcessors();
		} else {
			thread = Integer.parseInt( ((String)CBOX_thread.getSelectedItem()) );
		}
		GUI.logger.info("THREADs: " + thread);
		
		BatchWorker task = new BatchWorker(this, thread, TXT_dirpath.getText(), this.CB_verifysign.isSelected(), CB_generateVTL.isSelected(), (ExtractionMode)this.CBOX_mode.getSelectedItem());
		task.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				progressBarHandle(evt);
			}
		});
		task.execute();
		
	}

	
	public void toggleButton() {
		this.B_directory.setEnabled(UIlock);
		this.B_execute.setEnabled(UIlock);
		if(UIlock) {
			this.progressBar.setIndeterminate(false);
			this.progressBar.setString(null);
			this.progressBar.setStringPainted(false);
			this.progressBar.setValue(0);
			this.B_execute.setText("Esegui estrazione Batch");
		} else {
			this.B_execute.setText("In esecuzione...");
			this.progressBar.setIndeterminate(true);
			this.progressBar.setStringPainted(true);
			this.progressBar.setString("Preparazione...");
		}
		UIlock = !UIlock;
	}

	@Override
	public void appendLog(String newline, int type) {
		//Non fare niente, non ho bisogno di log aggiuntivi
	}

	@Override
	public void setLastCertUpdateLabel(String newdate) {
		//passa la chiamata a GUI, dove è implementato.
		main.setLastCertUpdateLabel(newdate);
	}
}
