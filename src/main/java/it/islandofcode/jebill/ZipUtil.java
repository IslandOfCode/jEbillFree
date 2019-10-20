package it.islandofcode.jebill;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.itextpdf.text.pdf.events.IndexEvents.Entry;

@SuppressWarnings("unused")
public class ZipUtil {

	private static final int TRESHOLD_NUMBER_FILE = 10;
	private static final String BASEPATH_LOG = "logs/";
	private static final String BASEPATH_SCRIPT = "/Scripts.zip";

	/**
	 * 
	 * Gli passo fhPath dalla GUI, gli tolgo logs/ e .log e ho il nome del file.
	 * Se gli tolgo anche jebill_ ho anche solo il timestamp. 
	 * @param actualLogFileName
	 * @return
	 * @throws IOException 
	 */
	public static File zipOldLog(String actualLogFileName) throws IOException {
		File dir = new File(BASEPATH_LOG);
		File[] ls = dir.listFiles();
		if (ls != null && ls.length>=TRESHOLD_NUMBER_FILE+2) { //+2 perchè devo escludere l'attuale e il file .lock dell'attuale
			//Arrays.sort(ls);
			//List<File> lls = Arrays.asList(ls);
			ArrayList<File> lls = new ArrayList<>(Arrays.asList(ls));
			lls.removeIf(entry -> {
				return (
						entry.getName().equals(actualLogFileName.replace(BASEPATH_LOG, ""))
						|| entry.getName().equals(actualLogFileName.replace(BASEPATH_LOG, "")+".lck") 
						|| entry.getName().endsWith(".zip") 
						);
			});
			Collections.sort(lls, (a,b) -> a.getName().compareTo(b.getName()));
			
			//a questo punto ho rimosso i file di log attuali e riodinato i file.
			
			/*
			 * Allora:
			 * 	1)get(0), get(lls.size()-1): prendo rispettivamente il primo e l'ultimo file ordinati lessicograficamente.
			 * 	2).replace("logs/jebill", "").replaceAll(".log", ""): mi rimane solo il timestamp del file.
			 * 	3).split("_")[0]: dato che il timestamp divide data da orario con un "_", faccio lo split e prendo solo il primo elemento.
			 */
			String first = lls.get(0).getName().replace("jebill_", "").replaceAll(".log", "").split("_")[0];
			String last = lls.get(lls.size()-1).getName().replace("jebill_", "").replaceAll(".log", "").split("_")[0];
			
			//ho preso i nomi del primo e dell'ultimo, per creare il nome dello zip
			String zipname = "LOG_da_"+first+"_a_"+last;

			File output = new File(BASEPATH_LOG+zipname+".zip");
			ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(output));
			zout.setLevel(9);
			ZipEntry ZE;
			//FileInputStream fin;
			int len;
			byte[] buffer;// = new byte[1024];
			for (File entry : lls) {
				// Do something with child
				ZE = new ZipEntry(entry.getPath());
				zout.putNextEntry(ZE);
				/*fin = new FileInputStream(entry.getPath());
				while((len = fin.read(buffer,0,buffer.length)) != -1) {
					zout.write(buffer,0,len);
				}
				fin.close();*/
				buffer = Files.readAllBytes(entry.toPath());
				zout.write(buffer);
				zout.closeEntry();
			}
			zout.close();
			
			//rimuovo i vecchi log
			lls.stream().forEach(log->log.delete());
			
		} else {
			// Handle the case where dir is not really a directory.
			// Checking dir.isDirectory() above would not be sufficient
			// to avoid race conditions with another process that deletes
			// directories.
			return null;
		}

		return null;
	}
	
	public static void extractScriptFile(File path) throws IOException {
		InputStream in = ZipUtil.class.getResourceAsStream(BASEPATH_SCRIPT);
		ZipInputStream zin = new ZipInputStream(in);
		ZipEntry ze = null;
		FileOutputStream fout = null;
		while((ze = zin.getNextEntry())!=null) {
			//System.out.println(ze.getName());
			if(ze.getName().endsWith("/"))
				continue;
			fout = new FileOutputStream(path.getPath()+"\\"+ze.getName());
			for (int c = zin.read(); c != -1; c = zin.read()) {
				fout.write(c);
			}
			zin.closeEntry();
			fout.close();
		}
		zin.close();
	}
}
