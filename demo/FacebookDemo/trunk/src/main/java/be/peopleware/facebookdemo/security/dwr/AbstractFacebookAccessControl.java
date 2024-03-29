package be.peopleware.facebookdemo.security.dwr;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.directwebremoting.WebContextFactory;
import org.directwebremoting.extend.Creator;
import org.directwebremoting.impl.DefaultAccessControl;
import org.directwebremoting.util.Logger;

import com.google.code.facebookapi.FacebookSignatureUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public abstract class AbstractFacebookAccessControl extends DefaultAccessControl {

	/**
	 * This method verifies whether the client has a valid Facebook connect session.  Facebook
	 * Connect is used in JavaScript on the browser.  DWR methods can only be called if
	 * there is a valid Facebook connect session.
	 * 
	 * There are currently no JAAS compatible security checks implemented in the application.
	 * Application specific security checks are also not implemented.  E.g. It is currently
	 * possible to create movies for other users than yourself, you can request the list of
	 * movies of someone that is not in your friendslist, etc.  These checks must obviously be
	 * added in the future.  To be able to implement the example security checks,
	 * modififations too PPWCode will be required.
	 */
	@Override
	public void assertExecutionIsPossible(Creator creator, String className, Method method) throws SecurityException {
		super.assertExecutionIsPossible(creator, className, method);
		log.info("FacebookAccessControl::assertExecutionIsPossible for " + method.toString());
		HttpServletRequest req = WebContextFactory.get().getHttpServletRequest();
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			Map<String, String[]> parameters = new HashMap<String, String[]>();
			for (int i = 0; i < cookies.length; i++) {
				String cookiename = cookies[i].getName();
				if (isInNameSpace(cookiename)) {
					parameters.put(transform(cookiename), new String[] { cookies[i].getValue() } );
				}
			}
			//System.out.println("Cookies are:");
			//for (Map.Entry<String,String[]> entry : parameters.entrySet() ) {
			//	System.out.println(entry.getKey() + ": " + entry.getValue()[0]);
			//}
			if (!FacebookSignatureUtil.autoVerifySignature(parameters, getSecret())) {
				throw new FacebookSecurityException("You're not authenticated by facebook");
			}
		} else {
			throw new FacebookSecurityException("You're not authenticated by facebook");
		}
	}


	private boolean isInNameSpace(String key) {
		return null != key && key.startsWith( getApiKey() );
	}

	private String transform(String key) {
		return key.replace(getApiKey(), "fb_sig");
	}

	/**
	 * The log stream
	 */
	private static final Logger log = Logger.getLogger(AbstractFacebookAccessControl.class);

	public abstract String getApiKey();

	public abstract String getSecret();
}
