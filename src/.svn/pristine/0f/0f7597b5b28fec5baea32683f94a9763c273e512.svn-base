package Pic;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * MonitorSiteTable相关操作的返回数据类型
 * 是（fpm,cpm,aqi）的包装
 * @author leeying
 *
 */

public class MonitorSiteDataUnit {
	//PM2.5浓度
	private int fpm;
	//PM10浓度
	private int cpm;
	//AQI指数
	private int aqi;
	
	public MonitorSiteDataUnit(byte[] fpm, byte[] cpm, byte[] aqi){
		this.fpm = Bytes.toInt(fpm);
		this.cpm = Bytes.toInt(cpm);;
		this.aqi = Bytes.toInt(aqi);;
	}
	
	public MonitorSiteDataUnit(int fpm, int cpm, int aqi){
		this.fpm = fpm;
		this.cpm = cpm;
		this.aqi = aqi;
	}

	public int getFpm() {
		return fpm;
	}

	public void setFpm(int fpm) {
		this.fpm = fpm;
	}

	public int getCpm() {
		return cpm;
	}

	public void setCpm(int cpm) {
		this.cpm = cpm;
	}

	public int getAqi() {
		return aqi;
	}

	public void setAqi(int aqi) {
		this.aqi = aqi;
	}

	@Override
	public String toString() {
		return "MonitorSiteDataUnit [fpm=" + fpm + ", cpm=" + cpm + ", aqi="
				+ aqi + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MonitorSiteDataUnit other = (MonitorSiteDataUnit) obj;
		if (aqi != other.aqi)
			return false;
		if (cpm != other.cpm)
			return false;
		if (fpm != other.fpm)
			return false;
		return true;
	}
	
	
	
	

}
