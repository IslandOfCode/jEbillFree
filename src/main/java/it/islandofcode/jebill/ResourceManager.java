package it.islandofcode.jebill;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Optional;

/**
 * Questa classe si occupa di gestire i file di risorsa.
 * @author Pier Riccardo Monzo 
 */
public class ResourceManager {
	// Stores paths to files with the global jarFilePath as the key
	private static Hashtable<String, String> fileCache = new Hashtable<String, String>();

	/**
	 * Extract the specified resource from inside the jar to the local file system.
	 * 
	 * @param jarFilePath
	 *            absolute path to the resource
	 * @return full file system path if file successfully extracted, else null on
	 *         error
	 */
	public static String extract(String jarFilePath) {

		if (jarFilePath == null)
			return null;

		// See if we already have the file
		if (fileCache.contains(jarFilePath))
			return fileCache.get(jarFilePath);

		// Alright, we don't have the file, let's extract it
		try {
			// Read the file we're looking for
			InputStream fileStream = ResourceManager.class.getResourceAsStream(jarFilePath);

			// Was the resource found?
			if (fileStream == null)
				return null;

			// Grab the file name
			String[] chopped = jarFilePath.split("\\/");
			String fileName = chopped[chopped.length - 1];

			// Create our temp file (first param is just random bits)
			File tempFile = File.createTempFile("JEBILL", fileName);

			// Set this file to be deleted on VM exit
			tempFile.deleteOnExit();

			// Create an output stream to barf to the temp file
			OutputStream out = new FileOutputStream(tempFile);

			// Write the file to the temp file
			byte[] buffer = new byte[1024];
			int len = fileStream.read(buffer);
			while (len != -1) {
				out.write(buffer, 0, len);
				len = fileStream.read(buffer);
			}

			// Store this file in the cache list
			fileCache.put(jarFilePath, tempFile.getAbsolutePath());

			// Close the streams
			fileStream.close();
			out.close();

			// Return the path of this sweet new file
			return tempFile.getAbsolutePath();

		} catch (IOException e) {
			return null;
		}
	}
	
	
	public static Optional<String> getExtensionByStringHandling(String filename) {
	    return Optional.ofNullable(filename)
	      .filter(f -> f.contains("."))
	      .map(f -> f.substring(filename.lastIndexOf(".") + 1));
	}
	
	public static boolean extractOpenSSL() throws IOException{
		// estraggo eseguibile
		InputStream is;
		OutputStream os;
		try {
			// is = getClass().getResource("/openssl.exe").openStream();
			is = new FileInputStream(ResourceManager.extract("/openssl.exe"));
			os = new FileOutputStream("openssl.exe");

			byte[] b = new byte[2048];
			int length;

			while ((length = is.read(b)) != -1) {
				os.write(b, 0, length);
			}

			(new File("openssl.exe")).deleteOnExit();

			is.close();
			os.close();

		} catch (IOException e) {
			throw e;
		}
		return true;
	}
}
