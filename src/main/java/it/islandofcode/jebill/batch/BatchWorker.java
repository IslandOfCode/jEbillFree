package it.islandofcode.jebill.batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import it.islandofcode.jebill.EBill;
import it.islandofcode.jebill.ExecStatus;
import it.islandofcode.jebill.Executor;
import it.islandofcode.jebill.ExtractionMode;
import it.islandofcode.jebill.GUI;
import it.islandofcode.jebill.UIXcommon;
import it.islandofcode.jebill.object.Settings;

@SuppressWarnings("unused")
public class BatchWorker extends SwingWorker<Void, Void> {

	private int thread = 1;
	private BatchUI UIX;
	private String path;
	private boolean verify;
	private ExtractionMode mode;
	
	private boolean VTL;

	private Writer exeLog;
	private int count = 0;
	private int success = 0;
	private int notSupported = 0;
	private int ioError = 0;
	
	private File VTLresult;

	public BatchWorker(UIXcommon ui, int thread, String path, boolean verifysign, boolean VTL, ExtractionMode mode) {
		this.thread = thread;
		this.UIX = (BatchUI) ui;
		this.path = path;
		this.verify = verifysign;
		this.mode = mode;
		this.VTL = VTL;
		
		if(VTL)
			VTLresult= new File(path+File.separatorChar+"VTL_"+(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))+".dscsv" );
	}


	@Override
	protected Void doInBackground() throws Exception {
		if (thread <= 0)
			thread = 1;

		ExecutorService pool = Executors.newFixedThreadPool(this.thread);
		Map<String,Future<ExecStatus>> set = new HashMap<>();

		// Se ho richiesto la verifica, scarico le chiavi
		if (verify || mode.equals(ExtractionMode.VERIFICA)) {
			Executor tmp = new Executor(UIX, "", GUI.logger, null);
			if(tmp.loadCert()) {
				JOptionPane.showMessageDialog(null, "Certificati aggiornati con successo.", "Aggiornamento certificati.",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null, "Impossibile aggiornare certicati.\n Controllare la connessione ad internet.", "Aggiornamento certificati.",
						JOptionPane.WARNING_MESSAGE);
			}
			
		}
		
		long startTime = System.nanoTime();

		try {
			exeLog = new BufferedWriter(new FileWriter(path + "\\" + EBill.BATCH_LOG_FILE_NAME));
			exeLog.append("---- ELABORAZIONE BATCH @" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())
					+ " ----\r\n");

			Files.newDirectoryStream(Paths.get(path),
					// (?i) rende la regex case insensitive
					P -> (P.toFile().isFile() && P.toString().matches("(?i).*\\.(xml|xml\\.p7m)$"))
			).forEach(P -> {
				count++;
				//boolean verify = UIX.CB_verifysign.isSelected();
				if (mode.equals(ExtractionMode.VERIFICA)) {
					verify = true; // nel caso ho VERIFICA ma non ho spuntato la casella, la imposto di default
				}
				Settings S = new Settings(verify, mode, VTL);
				Callable<ExecStatus> callable = new Executor(UIX, P.toString(), GUI.logger, S);
				set.put(P.toString(),pool.submit(callable));
			});
			
			//System.out.println("AVVIO");
			firePropertyChange("OP", null, "OFF");
			setProgress(0);
			
			int perc = 100/set.size();
			int test = 0;
			while(test<set.size()) {
				test = 0;
				for(String k : set.keySet()) {
					if(set.get(k).isDone()) {
						test++;
					}
				}
				setProgress(test*perc);
				Thread.sleep(100);//FONDAMENTALE, altrimenti non fa vedere nessun avanzamento
			}
			//System.out.println("FINITO!");	
			setProgress(100);
			
			firePropertyChange("OP", null, "ON");
			for(String path : set.keySet()) {
				exeLog.append("["+(new SimpleDateFormat("HH:mm:ss").format(new Date()))+"] "+path);
				exeLog.append(" -> "+ set.get(path).get().getErrorCodeText()+"\r\n");
				
				switch(set.get(path).get()){
				case SUCCESS:{
					success++;
					break;
				}
				case UNSUPPORTED_FILE_TYPE:{
					notSupported++;
					break;
				}
				case IO_ERROR:
				case FILE_ALREADY_OPEN:
					ioError++;
					break;
				default:
					break;
				}
				//Thread.sleep(500);
			}

			if (count <= 0) {
				JOptionPane.showMessageDialog(null, "La cartella selezionata non ha file *.xml o *.xml.p7m !",
						"Nessun file utilizzabile", JOptionPane.WARNING_MESSAGE);
			}

			exeLog.close();
			if (count <= 0) {
				(new File(EBill.BATCH_LOG_FILE_NAME)).delete();
			}
			
			if(VTL) {
				Files.newDirectoryStream(Paths.get(path),
						// (?i) rende la regex case insensitive
						P -> (P.toFile().isFile() && P.toString().matches("(?i).*\\.rcsv$"))
				).forEach(P -> {
					try {
						FileOutputStream fos = new FileOutputStream(VTLresult, true);
						//FileInputStream fis = new FileInputStream(P.toFile());
						byte[] data = Files.readAllBytes(P);
						String temp = new String(data);
						temp = temp.replace("\n\n", "\n");
						temp = temp.replace("\r\n\r\n", "\r\n"); //TODO XXX rimpiazzami con una regex!!!!!
						fos.write(temp.getBytes());
						
						if( !(temp.endsWith("\n") || temp.endsWith("\r\n")) ){
							fos.write('\r');
							fos.write('\n');
						}

						//fis.close();
						fos.flush();
						fos.close();
						//rimuove il file origine.
						P.toFile().delete();
					} catch (IOException e) {
						GUI.logger.severe("VTL CREATION: " + e.getMessage());
					}
				});
			}
			
			//ora ho un file del tipo VTL_20181121_165100.csv
			//mentre tutti i piccoli file sono stati eliminati.
			
			
		} catch (IOException | InterruptedException | ExecutionException e) {
			GUI.logger.severe(e.getMessage());
			JOptionPane
					.showMessageDialog(
							null, "<html>Non è stato possibile operare su un file.<br/>Ragione:<br/><code>"
									+ e.getMessage() + "</code></html>",
							"Errore input/output!", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			firePropertyChange("OP", null, "toggle");
			return null;
		}
		
		
		
		
		long endTime = System.nanoTime();

		String message = "<html>Riepilogo operazione:<br/>" + "File totali con estensioni compatibili: <b>" + count
				+ "</b><br/>" + "<b>Fatture elaborate con successo: " + success + "</b><br/>" + "Dettaglio:"
				+ "<ul><li>Non fatturaPA: " + notSupported + "</li>" + "<li>Errore I/O: " + ioError + "</li></ul><hr/><br/>"
						+ "Durata operazione: "+String.format("%.3f", ((endTime - startTime)/1000000000.0))+" sec"
				+ "</html>";

		JOptionPane.showMessageDialog(null, message, "Batch completato", JOptionPane.INFORMATION_MESSAGE);

		firePropertyChange("OP", null, "toggle");
		return null;
	}

	@Override
	protected void done() {
		// TODO Auto-generated method stub
		super.done();
	}
}
