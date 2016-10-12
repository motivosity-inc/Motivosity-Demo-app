package com.motivosity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.motivosity.model.MvApiResponse;
import com.motivosity.model.MvMessage;
import com.motivosity.model.MvUser;
import com.motivosity.model.OAuthToken;

@RestController
@RequestMapping(value = "/api")
@EnableAutoConfiguration
public class DemoController {

	public static final String MOTIVOSITY_BASE_URL = "https://staging.motivosity.com";

	public static final String CLIENT_ID = "testapp";

	public static final String CLIENT_SECRET = "testapp_passwd";

	public static final String REDIRECT_URI = "http://localhost:9080/api/authorize/code";

	public static final String REQUIRED_SCOPES = "user userlist badge appr platform";

	public static String accessToken;//never store access token in a static variable. This is just a demo :)

	@RequestMapping(value = "/motivosity/userlist", method = RequestMethod.GET)
	public List<MvUser> getUser(HttpServletResponse response) throws URISyntaxException, IOException, GeneralSecurityException {
		Object responseObject = callMotivosityApi("/api/v1/app/user/list");

		List<MvUser> userList = new ArrayList<>();
		if (responseObject == null) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		} else {
			List<Map> list = (List<Map>) responseObject;
			for (Map userObject : list) {
				MvUser user = new MvUser();
				user.setId((String) userObject.get("id"));
				user.setName((String) userObject.get("firstName"));
				userList.add(user);
			}
		}

		return userList;
	}

	private Object callMotivosityApi(String path) throws IOException, GeneralSecurityException {
		Object responseObject = null;

		ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(createAllTrustingClient());
		Invocation.Builder builder = new ResteasyClientBuilder().httpEngine(engine).build().target(MOTIVOSITY_BASE_URL + path).request();

		if (accessToken != null) {
			builder.header("Authorization", "Bearer " + accessToken);
		}

		System.out.println("\n>> calling endpoint: " + path);

		Response restResponse = builder.get();
		String responseText = restResponse.readEntity(String.class);
		restResponse.close();

		System.out.println("<< response: " + responseText);

		MvApiResponse apiResponse = new ObjectMapper().readValue(responseText, MvApiResponse.class);
		if (apiResponse.isSuccess()) {
			responseObject = apiResponse.getResponse();
		} else {
			System.out.println(">> failed: ");
			for (MvMessage message : apiResponse.getMvMessages()) {
				System.out.println("\t\t- " + message.getMessage());
			}
		}

		return responseObject;
	}

	/**
	 * First authorizations step: your user clicks on the authorize link on your page that calls the DemoController.authorize() method.
	 * This method builds an authorization request URI with the right app info and redirects your user's browser to this URI.
	 */
	@RequestMapping(value = "/authorize", method = RequestMethod.GET)
	public void authorize(HttpServletResponse response) throws URISyntaxException, IOException {
		UriBuilder uriBuilder = UriBuilder.fromUri(new URI(MOTIVOSITY_BASE_URL + "/oauth2/v1/auth"));
		uriBuilder.queryParam("client_id", CLIENT_ID);
		uriBuilder.queryParam("redirect_uri", REDIRECT_URI);
		uriBuilder.queryParam("response_type", "code");
		uriBuilder.queryParam("scope", REQUIRED_SCOPES);

		System.out.println("\n>> redirecting to: " + uriBuilder.toTemplate());

		response.sendRedirect(uriBuilder.toTemplate());
	}

	/**
	 * After the successful authorization your user is redirected back to the pre-registered (and sent in the request as a parameter) URI together with Motivosity's temporary code attached as a parameter.
	 * In this sample app the REDIRECT_URI is 'http://localhost:9080/api/authorize/code'.
	 * The DemoController.checkCode() method catches this redirection and - as the next step in the OAUth 2 flow - checks if the passed  code is valid and is really coming from Motivosity's auth server.
	 * This method will call back Motivosity in the background with the 'code' and as a result has to get a valid access token and the expires in parameter.
	 */
	@RequestMapping(value = "/authorize/code", method = RequestMethod.GET)
	public void checkCode(@RequestParam(value = "code", required = true) String code, HttpServletResponse response) throws URISyntaxException, IOException, GeneralSecurityException {
		UriBuilder uriBuilder = UriBuilder.fromUri(new URI(MOTIVOSITY_BASE_URL + "/oauth2/v1/token"));
		uriBuilder.queryParam("code", code);
		uriBuilder.queryParam("client_id", CLIENT_ID);
		uriBuilder.queryParam("client_secret", CLIENT_SECRET);
		uriBuilder.queryParam("redirect_uri", REDIRECT_URI);
		uriBuilder.queryParam("grant_type", "authorization_code");

		System.out.println("\n>> calling token endpoint: " + uriBuilder.toTemplate());

		ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine(createAllTrustingClient());
		Invocation.Builder builder = new ResteasyClientBuilder().httpEngine(engine).build().target(uriBuilder).request();
		Response restResponse = builder.post(null);
		String responseText = restResponse.readEntity(String.class);
		int status = restResponse.getStatus();
		restResponse.close();

		System.out.println("<< response: " + responseText);

		if (status == 200) {
			OAuthToken token = new ObjectMapper().readValue(responseText, OAuthToken.class);
			accessToken = token.getAccessToken();

		} else {
			System.out.println("Token endpoint returned an error: " + status);

			MvApiResponse apiResponse = new ObjectMapper().readValue(responseText, MvApiResponse.class);
			if (apiResponse != null && apiResponse.getMvMessages() != null) {
				for (MvMessage message : apiResponse.getMvMessages()) {
					System.out.println("\t\t- " + message.getMessage());
				}
			}
		}

		response.sendRedirect("/index.html");
	}

	/**
	 * Motivosity authorization server calls back this 'revoke URI' when the user revokes app inside Motivosity.
	 */
	@RequestMapping(value = "/revoke", method = RequestMethod.GET)
	public void revoke(@RequestParam(value = "userId") String userId) throws URISyntaxException, IOException {
		System.out.println("Motivosity user: " + userId + " revoked authorization.");
		accessToken = null;
	}

	private DefaultHttpClient createAllTrustingClient() throws GeneralSecurityException {
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

		TrustStrategy trustStrategy = new TrustStrategy() {

			@Override
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//				LOG.info("Is trusted? return true");
				return true;
			}
		};

		SSLSocketFactory factory = new SSLSocketFactory(trustStrategy, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		registry.register(new Scheme("https", 443, factory));

		ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(registry);
		mgr.setMaxTotal(1000);
		mgr.setDefaultMaxPerRoute(1000);

		DefaultHttpClient client = new DefaultHttpClient(mgr, new DefaultHttpClient().getParams());
		return client;
	}
}
