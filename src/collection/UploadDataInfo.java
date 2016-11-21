package collection;

import java.util.Date;

public class UploadDataInfo {

	private Date uploadtime;
	
	private String username;

	public UploadDataInfo(Date uploadtime, String username) {
		super();
		this.uploadtime = uploadtime;
		this.username = username;
	}

	public Date getUploadtime() {
		return uploadtime;
	}

	public void setUploadtime(Date uploadtime) {
		this.uploadtime = uploadtime;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String toString() {
		return "UploadDataInfo [uploadtime=" + uploadtime + ", username="
				+ username + "]";
	}
	
	

}
