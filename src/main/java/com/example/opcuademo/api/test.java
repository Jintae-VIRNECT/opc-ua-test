package com.example.opcuademo.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.initech.eam.api.NXContext;
import com.initech.eam.api.NXNLSAPI;
import com.initech.eam.nls.CookieManager;
import com.initech.eam.smartenforcer.SECode;

public class test {
	private String SERVICE_NAME = "test";						// 영문 업무시스템명 정의
	private String SERVER_URL 	= "http://test.lgchem.com";				// 업무시스템 URL(도메인)정의
	private String SERVER_PORT = "8080";						//업무시스템 서비스 포트 정의
	private String ASCP_URL = SERVER_URL + ":" + SERVER_PORT + "/sso/login_exec.jsp";

	//Custom Login Url
	//private String custom_url = SERVER_URL + ":" + SERVER_PORT + "/agent/sso/loginFormPageCoustom.jsp";
	private String custom_url = "";
	private String NLS_URL 		 = "http://gsso.lgchem.com";
	private String NLS_PORT 	 = "8001";
	private String NLS_LOGIN_URL = NLS_URL + ":" + NLS_PORT + "/nls3/clientLogin.jsp";
	//private String NLS_LOGIN_URL = NLS_URL + ":" + NLS_PORT + "/nls3/cookieLogin.jsp";
	private String NLS_LOGOUT_URL= NLS_URL + ":" + NLS_PORT + "/nls3/NCLogout.jsp";
	private String NLS_ERROR_URL = NLS_URL + ":" + NLS_PORT + "/nls3/error.jsp";
	private static String ND_URL = "http://gsso.lgchem.com:5480";


	private static Vector<String> PROVIDER_LIST = new Vector<>();

	private static final int COOKIE_SESSTION_TIME_OUT = 3000000;


	private String TOA = "1";
	private String SSO_DOMAIN = "lgchem.com";

	private static final int timeout = 15000;
	private static NXContext context = null;
	static{
		//PropertyConfigurator.configureAndWatch("D:/INISafeNexess/site/4.1.0/src/Web/WebContent/WEB-INF/logger.properties");
		List<String> serverurlList = new ArrayList<String>();
		serverurlList.add(ND_URL);


		context = new NXContext(serverurlList,timeout);
		CookieManager.setEncStatus(true);
		//		CookieManager.setSameSiteStatus(false);

		PROVIDER_LIST.add("gsso.lgchem.com");
		PROVIDER_LIST.add("sso.lgchem.com");			// 최종 구 sso shutdown 시 삭제 가능
		// PROVIDER_LIST.add("sso.lgensol.com");		// shared는 주석 해제 멀티도메인
		// PROVIDER_LIST.add("ssoles.lgchem.com");		//shared는 주석 해제 멀티도메인

		SECode.setCookiePadding("");


	}


	public String getSsoId(HttpServletRequest request) {
		System.out.println("*================== [lgchem new] encStatus : "+CookieManager.getEncStatus());
		if(!CookieManager.getEncStatus()) {
			CookieManager.setEncStatus(true);
		}

		String sso_id = null;
		sso_id = CookieManager.getCookieValue(SECode.USER_ID, request);
		return sso_id;
	}

	public void goLoginPage(HttpServletResponse response)throws Exception {
		CookieManager.addCookie(SECode.USER_URL, ASCP_URL, SSO_DOMAIN, response);
		CookieManager.addCookie(SECode.R_TOA, TOA, SSO_DOMAIN, response);


		if(custom_url.equals(""))
		{
			//CookieManager.addCookie("CLP", "", SSO_DOMAIN, response);
		}else{
			CookieManager.addCookie("CLP", custom_url , SSO_DOMAIN, response);
		}

		response.sendRedirect(NLS_LOGIN_URL);
	}


	public String getEamSessionCheckAndAgentVaild(HttpServletRequest request,HttpServletResponse response){
		String retCode = "";
		try {
			retCode = CookieManager.verifyNexessCookieAndAgentVaild(request, response, 10, COOKIE_SESSTION_TIME_OUT, PROVIDER_LIST, SERVER_URL, context);
		} catch(Exception npe) {
			npe.printStackTrace();
		}
		return retCode;
	}



	//@deprecated
	public String getEamSessionCheck(HttpServletRequest request,HttpServletResponse response){
		String retCode = "";
		try {
			retCode = CookieManager.verifyNexessCookie(request, response, 10, COOKIE_SESSTION_TIME_OUT,PROVIDER_LIST);
		} catch(Exception npe) {
			npe.printStackTrace();
		}
		return retCode;
	}



	public String getEamSessionCheck2(HttpServletRequest request, HttpServletResponse response)
	{
		String retCode = "";
		try {
			NXNLSAPI nxNLSAPI = new NXNLSAPI(context);
			retCode = nxNLSAPI.readNexessCookie(request, response, 0, 0);
		} catch(Exception npe) {
			npe.printStackTrace();
		}
		return retCode;
	}


	public void goErrorPage(HttpServletResponse response, int error_code)throws Exception {
		CookieManager.removeNexessCookie(SSO_DOMAIN, response);
		CookieManager.addCookie(SECode.USER_URL, ASCP_URL, SSO_DOMAIN, response);
		response.sendRedirect(NLS_ERROR_URL + "?errorCode=" + error_code);
	}



}
