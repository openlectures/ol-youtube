
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.util.AuthenticationException;

public class LoginCheck {

	YouTubeService service;
	public static final String DEVELOPER_KEY = "AI39si5Sfx7cQ-LXq6ERi31r5K19y1x5w94Cnirtp1Vza30kDELcLFCr6LjoYDfplqlpRia19SBYn8ZUffQMXtwGbjuc2ezq0w";
	public static final String CLIENT_ID = "ol-youtube";

	public LoginCheck() {
		service = new YouTubeService(CLIENT_ID, DEVELOPER_KEY);
	}

	public boolean checkLogin(String username, String password) {
		try {
			service.setUserCredentials(username, password);
			System.out.println("LoginCheck: Login success.");
			return true;
		} catch (AuthenticationException e) {
			System.out
					.println("LoginCheck: Invalid username / password combination.");
			return false;
		}
	}
}
