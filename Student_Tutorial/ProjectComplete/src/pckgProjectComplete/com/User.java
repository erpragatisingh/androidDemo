package pckgProjectComplete.com;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

public class User {
	private String userName = null;
	private String password = null;

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserName() {
		return userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	Properties l_props = new Properties();

}
