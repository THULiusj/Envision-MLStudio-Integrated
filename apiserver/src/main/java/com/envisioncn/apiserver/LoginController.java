package com.envisioncn.apiserver;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.envision.eeop.api.EnvisionClient;
import com.envision.eeop.api.EnvisionDefaultClient;
import com.envision.eeop.api.request.UserLoginRequest;
import com.envision.eeop.api.response.UserLoginResponse;

@Controller
public class LoginController {
	
	@Autowired
	ConfigBean configBean;

	@GetMapping("/login")
	public String search(Model model) {
		model.addAttribute("account", new Account());

		return "login";
	}
	
	@RequestMapping(value = "/loginaccount")
	public String loginAccount(Model model, Account account, HttpSession session) {
		try {
			String name = account.getName();
			String password = account.getPassword();
			EnvisionClient client = new EnvisionDefaultClient(configBean.getEnosurl(), configBean.getAppkey(), configBean.getAppsecret());
			UserLoginRequest userLoginRequest = new UserLoginRequest(name, password);
			UserLoginResponse userLoginResponse = client.execute(userLoginRequest);
			
			if (userLoginResponse.isSuccess()) {
				String accessToken = userLoginResponse.getAccessToken();
				System.out.println("Login successfully, token: " + accessToken);
				
				account.setAccessToken(accessToken);
				account.setRefreshToken(userLoginResponse.getRefreshToken());
				account.setTokenExpire(userLoginResponse.getAccessTokenExpire());
				account.setUserId(userLoginResponse.getUserId());
				account.setClient(client);
				session.setAttribute(WebSecurityConfig.SESSION_KEY, account);
				return "redirect:/query";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "login";
	}	

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.removeAttribute(WebSecurityConfig.SESSION_KEY);
		return "redirect:/login";
	}
}
