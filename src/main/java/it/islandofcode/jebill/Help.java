package it.islandofcode.jebill;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public class Help extends JDialog {

	private static final long serialVersionUID = -5080815611957112000L;
	private JTextPane TP_license;

	/**
	 * Create the dialog.
	 */
	public Help() {
		setTitle("About");
		setResizable(false);
		setType(Type.UTILITY);
		setBounds(100, 100, 450, 326);
		getContentPane().setLayout(null);
		
		JScrollPane SP_scroll = new JScrollPane();
		SP_scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		SP_scroll.setBounds(10, 99, 424, 189);
		getContentPane().add(SP_scroll);
		
		TP_license = new JTextPane();
		TP_license.setContentType("text/html");
		TP_license.setEditable(false);
		SP_scroll.setViewportView(TP_license);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 86, 424, 2);
		getContentPane().add(separator_1);
		
		JLabel L_info = new JLabel("New label");
		L_info.setBounds(10, 11, 424, 64);
		getContentPane().add(L_info);
		
		
		TP_license.setText(generateLicenseHTML());
		//Caret c = TP_license.getCaret();
		TP_license.setCaretPosition(0);
		L_info.setText("<html><table><tr>"
				+ "<td style=\"padding-right:15px; padding-left:15px\"><span style=\"font-size:3em;\">jEBill</span></td>"
				+ "<td style=\"font-size:14pt; padding-left:15px; border-left: 2px solid black\">"
				+ "<i>Autore</i> : <b>"+EBill.AUTHOR+"</b><br/>"
				+ "<i>Versione</i> : <b>"+EBill.VERSION+" ("+EBill.CHANNEL+")</b><br/>"
				+ "<i>Sito dell'autore</i> : <a href=\"\">www.islandofcode.it</a>"
				+ "</td></tr></td>");
	}
	
	private String generateLicenseHTML() {
		
		File licenseFile = new File("LICENSE.md");
		
		if(!licenseFile.exists())
			return "<html><h2>File di licenza non disponibile</h2>Scarica di nuovo <b>jEBill</b> per ottenerne una nuova copia.";
		
		String license = "";
		try {
			FileInputStream fis = new FileInputStream(licenseFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String line;
			while((line = br.readLine())!=null) {
				license +=line+"\n";
			}
			br.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "Non è stato possibile aprire il file di licenza!",
					"Licenza non trovata!", JOptionPane.ERROR_MESSAGE);
		}
		
		Parser parser = Parser.builder().build();
		Node document = parser.parse(license);
		HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();
		return renderer.render(document);

	}
	
	
}
