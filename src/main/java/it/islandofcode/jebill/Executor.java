package it.islandofcode.jebill;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.itextpdf.text.DocumentException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import it.islandofcode.jebill.object.Dettaglio;
import it.islandofcode.jebill.object.Settings;

public class Executor implements Callable<ExecStatus> {//extends Thread {
	
	private volatile ExecStatus STATUS;

	private UIXcommon gui;
	private Logger logger;
	private boolean isPlainText = false;
	private File choosed;
	private Settings settings;
	
	public Executor(UIXcommon gui, String path2choosed, Logger logger, Settings settings) {
		this.choosed = new File(path2choosed);
		this.gui = gui;
		this.logger = logger;
		this.settings = settings;
		this.isPlainText = ResourceManager.getExtensionByStringHandling(choosed.getPath()).orElse("").toLowerCase().equals("xml");
	}
	
	public ExecStatus getResult() {
		return STATUS;
	}

	/**
	 * Reso pubblico per accedervi anche da altre UI alla bisogna...
	 */
	public boolean loadCert() {
		gui.appendLog("Avvio aggiornamento certificati...", 1);
		try {
			URL website = new URL(EBill.CERT_URL);
			FileOutputStream fos;
			try (ReadableByteChannel rbc = Channels.newChannel(website.openStream())) {
				fos = new FileOutputStream(EBill.CERT_FILE_NAME);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				logger.info(String.format("DOWLOAD DI %s RIUSCITO", EBill.CERT_FILE_NAME));
			}
			fos.flush();
			fos.close();

			try (InputStream in = new FileInputStream(EBill.CERT_FILE_NAME)) {
				fos = new FileOutputStream(EBill.CERT_REAL_FILE_NAME);

				XMLInputFactory inputFactory = XMLInputFactory.newInstance();
				XMLStreamReader SR = inputFactory.createXMLStreamReader(in);

				SR.nextTag();
				String toWrite;
				int count = 0;
				while (SR.hasNext()) {
					if (SR.getEventType() != 1) {
						SR.next();
						continue;
					}

					toWrite = "";
					if (SR.getName().getLocalPart().equals("X509Certificate")) {
						count++;
						toWrite = "-----BEGIN CERTIFICATE-----\n";
						toWrite += SR.getElementText().replaceAll("(.{64})", "$1\n"); // aggiunge un ritorna a capo ogni
																						// 64 caratteri.

						// Risolve un bug problematico che rende questo file incompatibile con OpenSSL
						/*
						 * Nello specifico, può capitare la seguente situazione
						 * ...alshjfdòlahflaihflàahb \n \n -----END CERTIFICATE----- La riga vuota non
						 * viene accettata da OpenSSL. Si verifica quindi se l'ultimo carattere era un
						 * LF. Se no, si aggiunge LF.
						 */
						if (!toWrite.endsWith("\n"))
							toWrite += "\n";

						toWrite += "-----END CERTIFICATE-----\n";
					}
					fos.write(toWrite.getBytes());
					SR.next();
				}
				logger.info(String.format("TROVATI %d CERTIFICATI.", count));

				SR.close();
				fos.flush();
				fos.close();
				in.close();
				(new File(EBill.CERT_FILE_NAME)).delete();
			}

			logger.info("CERTIFICATI CONVERTITI CON SUCCESSO.");

			// UPDATE FILE DI PROPRIETA' con timestamp aggiornato
			String newdate = new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());
			GUI.updateProp(EBill.PROP_CERT_LAST_UPDATE, newdate);

			// Aggiorno la label relativa
			gui.setLastCertUpdateLabel(newdate);

		} catch (IOException | XMLStreamException ex) {
			logger.severe(ex.getMessage());
			// Logger.getLogger(EntryPoint.class.getName()).log(Level.SEVERE, null, ex);
			gui.appendLog("Errore nell'elaborazione dei certificati.", 3);
			return false;
		}
		
		gui.appendLog("Certificati aggiornati!", 4);
		return true;
	}

	private boolean verifyCert(File input) {
		
		boolean result = false;

		if ("mai".equals(GUI.readProp(EBill.PROP_CERT_LAST_UPDATE)) || !(new File(EBill.CERT_REAL_FILE_NAME)).exists()) {
			gui.appendLog("Verifica richiesta, ma il file dei certificati è mancante.", 2);
			gui.appendLog("Aggiornamento dei certificati forzato.", 2);
			loadCert();
		}

		/*
		 * Questo if dovrebbe risolvere un problema con batch, nel caso si richieda molteplici verifiche parallele
		 */
		if(!(new File("openssl.exe")).exists()) {
			
			try {
				if(ResourceManager.extractOpenSSL()) {
					gui.appendLog("OpenSSL estratto", 1);
					logger.info("Eseguibile OpenSSL estratto.");
				}
			} catch (IOException e) {
				logger.severe(e.getMessage());
				gui.appendLog("Impossibile estrarre OpenSSL!", 3);
				return false;
			}
			
		}

		// estraggo certificato

		File certpem = new File(
				java.nio.file.Paths.get(choosed.getPath()).getParent().toString() + "\\" + "cert.pem");
		//certpem.deleteOnExit(); //non lo devo rimuovere all'uscita, ma subito dopo averlo verificato.
		

		Process p;
		try {
			p = Runtime.getRuntime().exec("openssl.exe pkcs7 -inform DER -in \"" + input.getPath()
					+ "\" -print_certs -out \"" + certpem.getPath() + "\"", null, new File("."));
			int exitv = p.waitFor();
			if (exitv == 0 && certpem.exists()) {
				gui.appendLog("Certificato estratto", 1);
				logger.info("Certificato estratto in: " + certpem.getPath());

				// verifica certificato
				// verify -CAfile %s cert.pem
				p = Runtime.getRuntime().exec("openssl.exe verify -CAfile CA.pem \"" + certpem.getPath() + "\"", null,
						new File("."));
				exitv = p.waitFor();

				/*
				 * BufferedReader stdErr = new BufferedReader(new
				 * InputStreamReader(p.getErrorStream())); // ready is ture if the input buffer
				 * is not empty. // In this case it would also be empty if the server has not
				 * yet // responded, so it's no guarantee that no error occured.
				 * while(stdErr.ready()) { String errLine = stdErr.readLine();
				 * System.out.println("OpenSSL output: " + errLine); }
				 * 
				 * stdErr.close();
				 */

				BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line = stdOut.readLine();
				if (line != null && line.endsWith(" OK")) {
					gui.appendLog("Certificato verificato!", 4);
					logger.info("Certificato verificato, output [" + line + "]");
					result=true;
				} else {
					gui.appendLog("Il certificato non è stato verificato!", 2);
					logger.severe("Certificato NON verificato, output [" + line + "]");
					result=false;
				}
				/*
				 * while(line != null) { System.out.println("OpenSSL output: " + line); line =
				 * stdOut.readLine(); }
				 */
				stdOut.close();
				certpem.delete();

				if (exitv != 0) {
					gui.appendLog("Errore nella verifica del certificato!", 3);
					return false;
				}

			} else {
				logger.severe("Impossibile estrarre il certificato");
				gui.appendLog("Impossibile estrarre il certificato!", 3);
				return false;
			}
		} catch (IOException | InterruptedException e) {
			logger.severe(e.getMessage());
			gui.appendLog("Errore nell'esecuzione di OpenSSL!", 3);
			return false;
		}
		return result;
	}

	private File createGenericPDF(byte[] data, File input) throws IOException {

		Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);

		File template = new File(ResourceManager.extract("/template.ftl"));
		cfg.setDirectoryForTemplateLoading(template.toPath().getParent().toFile());

		cfg.setDefaultEncoding("ISO-8859-1");// ("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
		cfg.setWrapUncheckedExceptions(true);

		//Map<String, Object> root = populateMapTemplate(doc, input);
		Map<String, Object> root = Templating.populateMapTemplate(data);
		
		if(settings.isVTLRequested()) {
			//popola il file csv
			FileOutputStream fos = new FileOutputStream(input+".rcsv");
			String out = "";
			//So che è un Arraylist di dettaglio, per contratto, purtroppo il suppress è d'obbligo.
			@SuppressWarnings("unchecked")
			ArrayList<Dettaglio> row = (ArrayList<Dettaglio>)root.get("righefattura");
			for(Dettaglio D : row) {
				out = root.get("Numero")+","+root.get("TipoDocumento")+","+root.get("Data")+",";
				out += D.numeroLinea+","+D.getDescrizione()+","+D.quantita+","+D.prezzoUnitario+","+D.aliquotaIVA+"\r\n";
				fos.write(out.getBytes());
			}
			fos.flush();
			fos.close();
		}
		
		String otherTag = input.getName().replaceAll("\\.(xml|XML)\\.(p7m|P7M)$", "");
		if(otherTag.split("_").length==2) //è nella forma PIVA_CODICE
			otherTag = otherTag.split("_")[1];
		//altrimenti metti tutto il nome del file, senza estensione
		root.put("NOMEFILE", otherTag);
		
		logger.info("mappa template popolata.");

		// Template temp = cfg.getTemplate("invoice.ftl");
		Template temp = cfg.getTemplate(template.getName());
		File output =null;
		if(isPlainText) {
			output = new File(input.getPath().replace(".xml", ".html"));
		} else {
			output = new File(input.getPath().replace(".xml.p7m", ".html"));
		}
		FileOutputStream fos = new FileOutputStream(output);
		Writer out = new OutputStreamWriter(fos);
		try {
			temp.process(root, out);
		} catch (TemplateException e) {
			logger.severe(e.getMessage());
			gui.appendLog("Impossibile operare sul template!", 3);
			out.close();
			fos.close();
			output.delete();
		}
		out.close();
		fos.close();

		logger.info("Template processato.");

		input = output; // ho creato l'html, diventa il nuovo input

		output = new File(output.getPath().replace(".html", ".pdf"));
		fos = new FileOutputStream(output);
		
		//WORKAROUND
		/*
		 * ITextREnder non legge correttamente '&', prchè si aspetta che sia un'entità html.
		 * quindi seguita da &amp; o %lt; etc...
		 * Per risolvere, dato che il problema si presenta solo con '&', devo leggere il file e
		 * rimpiazzare tutte le occorrenze con &#38; o &amp;
		 */
		cleanHTMLfile(input);

		// converto HTML a PDF
		ITextRenderer renderer = new ITextRenderer();
		try {
			renderer.setDocument(input.toURI().toURL().toString());
			renderer.layout();
			renderer.createPDF(fos);
		} catch (DocumentException | org.xhtmlrenderer.util.XRRuntimeException e) {
			logger.severe(e.getMessage());
			gui.appendLog("Impossibile creare il PDF dal template", 3);
		} finally {
			try {
				if (fos != null)
					fos.close();

				input.delete();
			} catch (IOException e) {
				gui.appendLog("Cancella manualmente il file " + input.getName(), 3);
				logger.severe("Non riesco a chiudere lo stream output del PDF.");
			}
		}
		logger.info("Template correttamente processato in PDF.");
		return output;
	}

	private void cleanHTMLfile(File dirty) {
		try {
			InputStream in = new FileInputStream(dirty);
			byte[] data = new byte[(int) dirty.length()];
			in.read(data);
			
			String tmp = new String(data);
			in.close();
			//rimpiazza le & con &amp;, tranne se è una entità HTML, anche nel formato &#00;
			tmp = tmp.replaceAll("\\&(?!nbsp\\;|lt\\;|gt\\;|amp\\;|quot\\;|apos\\;|cent\\;|pound\\;|yen\\;|euro\\;|copy\\;|reg\\;|\\#\\d+\\;)", "&amp;");
			//tmp = tmp.replaceAll("\\&", "&amp;");

			FileOutputStream fos = new FileOutputStream(dirty);
			fos.write(tmp.getBytes());
			
			fos.flush();
			fos.close();
		} catch(IOException ex) {
			logger.severe("CLEAN:"+ex.getMessage());
		}
	}

	//public void run() {
	@Override
	public ExecStatus call() throws Exception {

		// questa variabile contiene il riferimento al file di input p7m
		File input = choosed;
		
		//boolean isPlainText = ResourceManager.getExtensionByStringHandling(input.getPath()).orElse("").toLowerCase().equals("xml");

		byte[] data = null;
		if (!isPlainText) {
			logger.info("Il file è firmato digitalmente.");

			// *******************************************************************

			if (settings.isCertUpdateRequested()) {
				logger.info("Aggiornamento dei certificati richiesto.");
				loadCert();
			}

			if (settings.isOpenSSLVerifyRequested()) {
				gui.appendLog("Richiesta verifica del certificato...", 1);
				logger.info("Avvio verifica certificato...");

				if(!verifyCert(input)) {
					return ExecStatus.VERIFY_FAILED;
					
				}
			}
			
			if(settings.isNeitherRequested()) {
				//ho richiesto solo la verifica, che ho eseguito, quindi posso uscire
				gui.appendLog("Operazione completata.", 4);
				logger.info("Thread terminato.");
				return ExecStatus.SUCCESS;
				
			}

			gui.appendLog("Avvio lettura contenuto firmato...", 1);

			byte[] buffer = new byte[(int) input.length()];
			CMSSignedData signature = null;
			try {
				DataInputStream in = new DataInputStream(new FileInputStream(input));
				in.readFully(buffer);
				in.close();

				signature = new CMSSignedData(buffer);
			} catch (IOException | CMSException e) {
				logger.severe("EX_CMS:"+e.getMessage());
				gui.appendLog("Errore nella lettura del file firmato.", 3);
				return ExecStatus.IO_ERROR;
				
			}
			CMSProcessable sc = signature.getSignedContent();
			data = (byte[]) sc.getContent();

			logger.info("Componente processabile estratta.");
			
		} else {
			if(settings.isCertUpdateRequested() || settings.isOpenSSLVerifyRequested())
				gui.appendLog("Aggiornamento certificati e verifica firma ignorati.", 1);
			
			if(settings.isNeitherRequested()) {
				gui.appendLog("Richiesta verifica firma su file in chiaro, esco...", 3);
				return ExecStatus.UNSUPPORTED_OPERATION;
				
			}
			
			//leggi il file come byte[]
			data = new byte[(int) input.length()];
			FileInputStream fis = null;
			try {
				 
				fis = new FileInputStream(input);
				fis.read(data);
				fis.close();

			} catch (IOException e) {
				logger.severe("PLAIN_TEXT_EX:"+e.getMessage());
				gui.appendLog("Errore nella lettura del file di input!", 3);
				return ExecStatus.IO_ERROR;
				
			}
			logger.info("file letto come testo in chiaro");
			
		}
		// *******************************************************************

		gui.appendLog("Estrazione struttura...", 1);

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new StringReader(new String(data, StandardCharsets.UTF_8))));
			doc.getDocumentElement().normalize();
			
			/*
			 * QUI VERIFICO CHE SIA UNA FATTURAPA
			 */
			org.jsoup.nodes.Document doh = Jsoup.parse(new String(data), "/", Parser.xmlParser());
			Elements header = doh.select("FatturaElettronicaHeader");
			if(header.size()<=0) {//non è una fattura elettronica!
				gui.appendLog("Il file XML non è una fattura elettronica!", 3);
				logger.info("Il file input non è una fattura elettronica, manca l'header!");
				logger.info("Primi "+Math.min(data.length, 1500)+" char: \n"+(new String(data)).substring(0, Math.min(data.length, 1500)));
				return ExecStatus.UNSUPPORTED_FILE_TYPE;
				
			}

			/*
			 * QUI VERIFICO LA PIVA CONTRO LA LICENZA!
			 */
			
			Elements piva = doh.select("CessionarioCommittente DatiAnagrafici IdFiscaleIVA IdCodice");
			
			//Se la partita iva è assente, controlla il codice fiscale
			if(piva!=null && piva.isEmpty())
				piva = doh.select("CessionarioCommittente DatiAnagrafici CodiceFiscale");
			
			//Se anche il codice fiscale manca, vai in errore.
			if(piva!=null && piva.isEmpty()) {
				gui.appendLog("Non è possibile verificare la P.IVA o il CODFIS di questa fattura.", 3);
				logger.severe("P.IVA o CodiceFiscale Assenti da CessionarioCommittente!");
				return ExecStatus.UNSUPPORTED_FILE_TYPE;
			}
			
			/*
			 * TERMINE VERIFICA PIVA CONTRO LA LICENZA!
			 */

			NodeList allegati = doc.getElementsByTagName("Allegati");
			File output = null;
			if ( (allegati.getLength() <= 0) && (settings.isAttachOnlyRequested() || settings.isBothRequested())) {
				gui.appendLog("Nessun allegato presente!", 2);
				logger.warning("Nessun allegato presente!");

				// Non c'è allegato, ma ho richiesto SOLO l'allegato, quindi devo uscire senza fare niente
				if (settings.isAttachOnlyRequested()) {
					gui.appendLog("Creazione fattura non richiesta, esco...", 2);
					logger.info("Richiesto allegato esplicitamente, quindi esco.");
					return ExecStatus.SUCCESS;
					
				}			
				
			}

			
			if( (allegati.getLength() > 0) && (settings.isAttachOnlyRequested() || settings.isBothRequested()) ) { //c'è allegato e l'ho richiesto
				Element allegato = (Element) allegati.item(0);

				// String pdfName =
				// allegato.getElementsByTagName("NomeAttachment").item(0).getTextContent();
				String attExt = allegato.getElementsByTagName("FormatoAttachment").item(0).getTextContent();
				String attName = allegato.getElementsByTagName("NomeAttachment").item(0).getTextContent();
				String pdf64Content = allegato.getElementsByTagName("Attachment").item(0).getTextContent();

				if (!attExt.toUpperCase().equals("PDF")) {
					logger.severe("L'estensione dell'allegato non è in formato valido.");
					gui.appendLog("L'estensione dell'allegato non è in formato valido.", 3);
					return ExecStatus.UNSUPPORTED_FILE_TYPE;
					
				}

				gui.appendLog("Allegato estratto", 1);
				logger.info("Allegato estratto.");

				// output = new File(pdfName);
				//output = new File(input.getPath().replace(".xml.p7m", ".pdf"));
				String complete = (input.getParent().endsWith("/"))?attName+"."+attExt:"/"+attName+"."+attExt;//TODO
				output = new File(input.getParent()+complete);

				Decoder dec64 = Base64.getMimeDecoder();

				byte[] pdfContent = dec64.decode(pdf64Content);// .getBytes("UTF-8"));
				logger.info("Allegato convertito da BASE64 a PDF.");
				
				FileOutputStream fos = new FileOutputStream(output);
				fos.write(pdfContent);
				fos.flush();
				fos.close();
				logger.info("Allegato salvato nella directory: " + output.getPath());
			}
			
			//ora passo alla fattura.

			if(settings.isInvoceOnlyRequested() || settings.isBothRequested()) { //fattura o entrambe richiesti
				gui.appendLog("Conversione fattura PDF...", 1);
				output = createGenericPDF(data,input);
				logger.info("Fattura salvata nella directory: " + output.getPath());
			}

			/*
			 * Per l'apertura del file, si considera l'ultimo output generato.
			 * Si noti che sto usando proprio la variabile output:
			 * dato che la fattura viene estratta dopo l'allegato, se la fattura viene estratta,
			 * allora output conterrà la fattura. Altrimenti, conterrà ancora l'allegato,
			 * dato che l'if impedirà la sua modifica.
			 */
			if (settings.isOpenFile()) {
				gui.appendLog("File estratto, apertura in corso...", 1);
				if (output.length() > 0) {
					Desktop.getDesktop().open(output);
					logger.info("File ["+output.getName()+"] aperto con l'applicazione predefinita.");
				} else {
					gui.appendLog("Il file non è stato creato correttamente.", 2);
					logger.severe("File non esiste! " + output.getPath());
					return ExecStatus.IO_ERROR;
					
				}
			}

		} catch (ParserConfigurationException | SAXException | IOException e) {
			gui.appendLog("Errore elaborazione file!", 3);
			gui.appendLog(e.getMessage(), 3);
			logger.severe("SAXEX|IOEX|PARSEREX:"+e.getMessage());
			//e.printStackTrace();
			return ExecStatus.GENERIC_ERROR;
			
		}

		gui.appendLog("Operazione completata.", 4);
		logger.info("Thread terminato.");
		return ExecStatus.SUCCESS;
	}


}
