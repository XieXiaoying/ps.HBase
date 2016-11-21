package model;


public class Column {
	private byte[] CF_NAME;
	private byte[] COL_NAME;

	public Column(byte[] cF_NAME, byte[] cOL_NAME) {
		CF_NAME = cF_NAME;
		COL_NAME = cOL_NAME;
	}

	public byte[] getCF_NAME() {
		return CF_NAME;
	}

	public void setCF_NAME(byte[] cF_NAME) {
		CF_NAME = cF_NAME;
	}

	public byte[] getCOL_NAME() {
		return COL_NAME;
	}

	public void setCOL_NAME(byte[] cOL_NAME) {
		COL_NAME = cOL_NAME;
	}

}

