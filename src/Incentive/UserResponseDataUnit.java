package Incentive;

import java.util.Date;


/**
 * 描述用户响应激励任务，包括（用户名，响应时间，激励）
 * @author leeying
 *
 */
public class UserResponseDataUnit {

	private String username;
	private Date responseTime;
	// 约定激励
	private double payment;

	public UserResponseDataUnit(String username, Date responseTime,
			double payment) {
		super();
		this.username = username;
		this.responseTime = responseTime;
		this.payment = payment;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Date getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(Date responseTime) {
		this.responseTime = responseTime;
	}

	public double getPayment() {
		return payment;
	}

	public void setPayment(double payment) {
		this.payment = payment;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserResponseDataUnit other = (UserResponseDataUnit) obj;
		if (!username.equals(other.username))
			return false;
		if (!responseTime.equals(other.getResponseTime()))
			return false;
		if (payment != other.payment)
			return false;
		return true;
	}

}
