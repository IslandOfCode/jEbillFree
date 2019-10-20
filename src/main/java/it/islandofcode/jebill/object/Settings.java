package it.islandofcode.jebill.object;

import it.islandofcode.jebill.ExtractionMode;

public class Settings {
	private boolean isCertUpdateRequested;
	private boolean isOpenSSLVerifyRequested;
	private boolean isOpenFile;
	private boolean isBatchProcessed;
	private ExtractionMode mode;
	
	//EXPERIMENTAL
	private boolean isVTLrequested;
		
	public Settings(boolean isCertUpdateRequested, boolean isOpenSSLVerifyRequested, boolean isOpenFile, boolean isBatchProcessed, ExtractionMode mode) {
		this.isCertUpdateRequested = isCertUpdateRequested;
		this.isOpenSSLVerifyRequested = isOpenSSLVerifyRequested;
		this.isOpenFile = isOpenFile;
		this.mode = mode;
		this.isBatchProcessed = isBatchProcessed;
		this.isVTLrequested = false;
	}
	
	/**
	 * Batch settings.
	 * @param isOpenSSLVerifyRequested
	 * @param isOpenFile
	 * @param isGenericPDF
	 */
	public Settings(boolean isOpenSSLVerifyRequested, ExtractionMode mode, boolean isVTL) {
		this.isCertUpdateRequested = false;
		this.isOpenSSLVerifyRequested = isOpenSSLVerifyRequested;
		this.isOpenFile = false;
		this.mode = mode;
		this.isBatchProcessed = true;
		this.isVTLrequested = isVTL;
	}

	public boolean isCertUpdateRequested() {
		return isCertUpdateRequested;
	}

	public boolean isOpenSSLVerifyRequested() {
		return isOpenSSLVerifyRequested;
	}

	public boolean isOpenFile() {
		return isOpenFile;
	}

	public boolean isBatchProcessed() {
		return isBatchProcessed;
	}
	
	public ExtractionMode getMode() {
		return mode;
	}
	
	public boolean isInvoceOnlyRequested() {
		return mode.equals(ExtractionMode.FATTURA);
	}
	
	public boolean isAttachOnlyRequested() {
		return mode.equals(ExtractionMode.ALLEGATO);
	}
	
	public boolean isBothRequested() {
		return mode.equals(ExtractionMode.ENTRAMBI);
	}
	
	public boolean isNeitherRequested() {
		return mode.equals(ExtractionMode.VERIFICA);
	}
	
	public boolean isVTLRequested() {
		return this.isVTLrequested;
	}
}
