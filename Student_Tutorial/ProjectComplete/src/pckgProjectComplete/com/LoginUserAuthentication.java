package pckgProjectComplete.com;

import pckgProjectComplete.com.User;

public class LoginUserAuthentication {

	public User currentUser = null;

	public LoginUserAuthentication() {

		currentUser = new User();
	}

	public void setUserDetails(String p_user_name, String p_password) {
		// TODO Auto-generated method stub
		currentUser.setUserName(p_user_name);
		currentUser.setPassword(p_password);
	}

	public Object getCurrentUser() {
		// TODO Auto-generated method stub
		return currentUser;
	}

	public boolean checkLogin() {
		// TODO Auto-generated method stub
		String l_user_name = currentUser.getUserName();
		String l_password = currentUser.getPassword();

		//if (l_user_name.equals("sethi") && l_password.equals("5226"))
			if (l_user_name.equals("p") && l_password.equals("p"))
		{
			return true;
		}
		return false;
	}

}
