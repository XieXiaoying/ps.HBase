package Pic;

/**
 * WeatherTable相关操作的返回数据类型
 * 是（temp,humi,wspend，precip，weather）的包装
 * @author leeying
 *
 */
public class WeatherDataUnit{
	// 温度
	private int temp;
	// 湿度
	private int humi;
	// 风速
	private int wspend;
	// 降雨量
	private int precip;
	// 压强
	private int pressure;
	// 天气类型
	private int weather;
	
	public WeatherDataUnit(int temp,int humi,int wspeed,int precip,int pressure,int weather){
		this.temp = temp;
		this.humi = humi;
		this.wspend = wspeed;
		this.precip = precip;
		this.pressure = pressure;
		this.weather = weather;
	}

	public int getPressure() {
		return pressure;
	}

	public void setPressure(int pressure) {
		this.pressure = pressure;
	}

	public int getTemp() {
		return temp;
	}

	public void setTemp(int temp) {
		this.temp = temp;
	}

	public int getHumi() {
		return humi;
	}

	public void setHumi(int humi) {
		this.humi = humi;
	}

	public int getWspend() {
		return wspend;
	}

	public void setWspend(int wspend) {
		this.wspend = wspend;
	}

	public int getPrecip() {
		return precip;
	}

	public void setPrecip(int precip) {
		this.precip = precip;
	}

	public int getWeather() {
		return weather;
	}

	public void setWeather(int weather) {
		this.weather = weather;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WeatherDataUnit other = (WeatherDataUnit) obj;
		if (humi != other.humi)
			return false;
		if (precip != other.precip)
			return false;
		if (pressure != other.pressure)
			return false;
		if (temp != other.temp)
			return false;
		if (weather != other.weather)
			return false;
		if (wspend != other.wspend)
			return false;
		return true;
	}

	
	
}