package cat.udl.easymodel.logic.user;

import java.time.LocalDateTime;

import javax.servlet.http.Cookie;

import com.vaadin.server.VaadinService;

import cat.udl.easymodel.main.SharedData;
import cat.udl.easymodel.utils.RandomString;
import cat.udl.easymodel.utils.p;

public class UserCookie {
	private User user;
	private String token;
	private LocalDateTime lastAccess;
	private final String cookieName="user";
	
	public UserCookie(User user) {
		this.user=user;
		resetCookieValues();
	}
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public LocalDateTime getLastAccess() {
		return lastAccess;
	}
	public void setLastAccess(LocalDateTime lastAccess) {
		this.lastAccess = lastAccess;
	}

	public void resetCookieValues() {
		RandomString randStr = new RandomString(300);
		token=randStr.nextString();
//		System.out.println("tok="+token);
		lastAccess = LocalDateTime.now();
	}
	
	public void saveCookieInClient() {
		Cookie cookie = new Cookie(cookieName, token);
		cookie.setMaxAge((int) SharedData.userCookiesExpireDays * 24 * 60 * 60); // seconds
		cookie.setPath(getContextPathFixed());
		VaadinService.getCurrentResponse().addCookie(cookie);
	}
	public void clearCookieInClient() {
		//clear cookie in client browser
		Cookie cookie = new Cookie(cookieName, "");
		cookie.setMaxAge(1); // seconds
		cookie.setPath(getContextPathFixed());
		VaadinService.getCurrentResponse().addCookie(cookie);
	}
	public boolean hasExpired() {
		return this.getLastAccess().plusDays(SharedData.userCookiesExpireDays).isBefore(LocalDateTime.now());
	}
	public String getContextPathFixed() {
		String pathFromVaadin = VaadinService.getCurrentRequest().getContextPath();
		if (pathFromVaadin == null || pathFromVaadin.equals(""))
			return "/";
		return pathFromVaadin;
	}
}
