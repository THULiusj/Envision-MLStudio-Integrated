package com.envisioncn.apiserver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.envision.eeop.api.Constants;
import com.envision.eeop.api.exception.EnvisionApiException;
import com.envision.eeop.api.request.UserLoginByRefreshTokenRequest;
import com.envision.eeop.api.response.UserLoginByRefreshTokenResponse;
import com.envision.eeop.api.util.JsonParser;
import com.envision.eeop.api.util.Sign;
import com.envision.eeop.api.util.WebUtils;

import net.lingala.zip4j.core.ZipFile;

@Controller
public class QueryController {

	@Autowired
	ConfigBean configBean;

	@GetMapping("/query")
	public String search(Model model, HttpSession session) {
		model.addAttribute("querycriteria", new QueryCriteria());
		model.addAttribute("account", session.getAttribute(WebSecurityConfig.SESSION_KEY));
		return "query";
	}

	@RequestMapping(value = "/doquery")
	public String doQuery(Model model, QueryCriteria criteria, HttpSession session) {
		System.out.println("query:" + criteria.getSqlStatement());

		model.addAttribute("querycriteria", criteria);

		try {
			Account account = (Account) session.getAttribute(WebSecurityConfig.SESSION_KEY);

			// check if directory of username exists, create if not
			File nameDir = new File(configBean.getRootpath() + account.getName());
			if (!nameDir.exists()) {
				nameDir.mkdirs();
			}

			checkToken(account);
			String taskId = null; 
			while (true) {
				try {
					taskId = requestDownloadData(configBean.getOrgid(), account.getAccessToken(), criteria.getSqlStatement());
					break;
				} catch (IOException e) {
					refreshToken(account);
				}
			}

			// check if directory of task exists, create if not
			File taskDir = new File(nameDir.getAbsolutePath() + "/" + taskId);
			if (!taskDir.exists()) {
				taskDir.mkdirs();
			}

			DownloadStatusResponse downloadStatusResponse = null;
			while (downloadStatusResponse == null
					|| downloadStatusResponse.getData().get(0).getProgress_code().equals("0")
					|| downloadStatusResponse.getData().get(0).getProgress_code().equals("1")) {
				
				while (true) {
					try {
						downloadStatusResponse = getDownloadStatus(configBean.getOrgid(), account.getAccessToken(), taskId);
						break;
					} catch (IOException e) {
						refreshToken(account);
					}
				}
				
				checkToken(account);
				Thread.sleep(5000);
			}
			DownloadStatus downloadStatus = downloadStatusResponse.getData().get(0);
			System.out.println(downloadStatus.getProgress_desc());
			if (downloadStatus.getProgress_code().equals("3")) {
				String download_url = downloadStatus.getDownload_url();
				System.out.println(download_url);

				File file = new File(taskDir.getAbsolutePath() + "/file.zip");
				FileUtils.copyURLToFile(new URL(download_url), file);
				System.out.println("file.zip has been downloaded");

				ZipFile zipFile = new ZipFile(file);
				zipFile.extractAll(taskDir.getAbsolutePath());

				File csv = new File(taskDir.getAbsolutePath() + "/file.csv");
				if (csv.exists()) {
					FileUtils.moveFile(csv, new File(nameDir.getAbsolutePath() + "/" + Instant.now().toEpochMilli()
							+ "_" + UUID.randomUUID() + ".csv"));
					FileUtils.deleteDirectory(taskDir);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<String> fileList = listFiles(session);
		model.addAttribute("filelist", fileList);

		//return "redirect:/result";
		return "result";
	}

	@GetMapping("/result")
	public String result(Model model, HttpSession session) {
		model.addAttribute("querycriteria", new QueryCriteria());
		List<String> fileList = listFiles(session);
		model.addAttribute("filelist", fileList);
		return "result";
	}
	
	// List available CSV files
	public List<String> listFiles(HttpSession session) {
		Account account = (Account) session.getAttribute(WebSecurityConfig.SESSION_KEY);
		String fullPath = configBean.getRootpath() + account.getName() + "/";
		File file = new File(fullPath);
		File[] files = file.listFiles();
		List<String> fileList = new ArrayList<String>();
		if (files != null) {
			for (File f : files) {
				if (f.isFile())
					if (!Utils.expired(f.getName(), configBean.getExpiration()))
						fileList.add(fullPath + f.getName());
				// System.out.println(f.getName());
			}
		}

		return fileList;
	}
	
	public static void checkToken(Account account) throws EnvisionApiException {
		System.out.println("Token expires at " + account.getTokenExpire());
		if (Instant.now().toEpochMilli() / 1000 - 60 >= account.getTokenExpire()) {
			refreshToken(account);
		}
	}

	public static void refreshToken(Account account) throws EnvisionApiException {
		
		UserLoginByRefreshTokenRequest userLoginByRefreshTokenRequest = new UserLoginByRefreshTokenRequest(
				account.getRefreshToken(), account.getUserId());
		UserLoginByRefreshTokenResponse userLoginByRefreshTokenResponse = account.getClient()
				.execute(userLoginByRefreshTokenRequest);
		String accessToken = userLoginByRefreshTokenResponse.getAccessToken();
		account.setAccessToken(accessToken);
		account.setRefreshToken(userLoginByRefreshTokenResponse.getRefreshToken());
		account.setTokenExpire(userLoginByRefreshTokenResponse.getAccessTokenExpire());
		System.out.println("Refresh token, new token: " + accessToken);
		
	}

	public String requestDownloadData(String mdmId, String token, String sql) throws IOException {
		Map<String, String> textParams = new HashMap<>();
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("sql", sql);
		String params = JsonParser.toJson(paramMap);
		textParams.put("mdmId", mdmId);
		textParams.put("params", params);
		String result = doPost(textParams, token, configBean.getRequestdownloadpath());
		System.out.println(result);
		Map<String, String> resultMap = JsonParser.fromJson(result, Map.class);
		return resultMap.get("id");
	}

	public DownloadStatusResponse getDownloadStatus(String mdmid, String token, String task_id) throws IOException {
		Map<String, String> textParams = new HashMap<>();
		textParams.put("mdmId", mdmid);
		textParams.put("id", task_id);
		String result = doGet(textParams, token, configBean.getRequeststatuspath());
		System.out.println(result);
		DownloadStatusResponse ret = JsonParser.fromJson(result, DownloadStatusResponse.class);
		return ret;
	}

	private String doPost(Map<String, String> textParams, String token, String apiName) throws IOException {
		if (token != null && !token.isEmpty()) {
			textParams.put(Constants.TOKEN, token);
		}

		String url = makeUrl(textParams, configBean.getAppkey(), configBean.getAppsecret(), configBean.getDownloadaddr(), apiName);

		System.out.println(url);

		String ret = null;
		//try {
			ret = WebUtils.doPost(url, textParams, WebUtils.DEFAULT_CHARSET, 30000, 30000);
		//} catch (IOException e) {
		//	e.printStackTrace();
		//}
		return ret;
	}

	private String doGet(Map<String, String> textParams, String token, String apiName) throws IOException {
		if (token != null && !token.isEmpty()) {
			textParams.put(Constants.TOKEN, token);
		}

		String url = makeUrl(textParams, configBean.getAppkey(), configBean.getAppsecret(), configBean.getDownloadaddr(), apiName);

		System.out.println(url);

		String ret = null;
		//try {
			ret = WebUtils.doGet(url, textParams, WebUtils.DEFAULT_CHARSET);
		//} catch (IOException e) {
		//	e.printStackTrace();
		//}
		return ret;
	}

	private static String makeUrl(Map<String, String> textParams, String appKey, String appSecret, String serverUrl,
			String apiName) {
		// Add sign
		String sign = Sign.sign(appKey, appSecret, textParams);

		// Build Url
		StringBuilder url = new StringBuilder(serverUrl);

		// Make Url
		url.append(apiName);
		url.append("?");
		url.append("appKey=");
		url.append(appKey);
		url.append("&sign=");
		url.append(sign);

		return url.toString();
	}

	public class DownloadStatusResponse {
		private String returnCode;
		private List<DownloadStatus> data;

		public String getReturnCode() {
			return returnCode;
		}

		public void setReturnCode(String returnCode) {
			this.returnCode = returnCode;
		}

		public List<DownloadStatus> getData() {
			return data;
		}

		public void setData(List<DownloadStatus> data) {
			this.data = data;
		}
	}

	public class DownloadStatus {
		private String id;
		private String download_url;
		private String progress_desc;
		private String progress_code;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getDownload_url() {
			return download_url;
		}

		public void setDownload_url(String download_url) {
			this.download_url = download_url;
		}

		public String getProgress_desc() {
			return progress_desc;
		}

		public void setProgress_desc(String progress_desc) {
			this.progress_desc = progress_desc;
		}

		public String getProgress_code() {
			return progress_code;
		}

		public void setProgress_code(String progress_code) {
			this.progress_code = progress_code;
		}
	}

}
