package com.teskalabs.bsmtt.ipaddress;

import android.os.AsyncTask;
import android.util.Log;

import com.teskalabs.bsmtt.BSMTTelemetryHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Using this class you can check an IP address and DNS from a server using your phone.
 * @author Stepan Hruska, Premysl Cerny
 */
public class GetIP extends AsyncTask<String, String, String> {
	static final String LOG_TAG = "BSInfoSDK - GetIP";
	public static int FLAG_IPV4 = 0;
	public static int FLAG_IPV6 = 0;

	private GetIPCallback m_getIPCallback;

	public GetIP(GetIPCallback getIPCallback) {
		m_getIPCallback = getIPCallback;
	}

	@Override
	protected String doInBackground(String... params) {
		String mIP;

		// get device IP
		try {
			mIP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		try {
			Enumeration e = NetworkInterface.getNetworkInterfaces();
			while(e.hasMoreElements())
			{
				NetworkInterface n = (NetworkInterface) e.nextElement();
				Enumeration ee = n.getInetAddresses();
				while (ee.hasMoreElements())
				{
					InetAddress i = (InetAddress) ee.nextElement();
					mIP = BSMTTelemetryHelper.str_rem_pct(i.getHostAddress());

					if (!((mIP.contains("::1")) || (mIP.contains("127.0.0.1")))) {
						if (mIP.contains(":")) {
							m_getIPCallback.onIPChanged(FLAG_IPV6, mIP);
						} else {
							m_getIPCallback.onIPChanged(FLAG_IPV4, mIP);
						}
					}

					if (isCancelled()) {
						return "";
					}
				}
			}
		} catch (SocketException e1) {
			e1.printStackTrace();
		}

		// get dns
		Class<?> SystemProperties = null;
		try {
			SystemProperties = Class.forName("android.os.SystemProperties");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Method method = null;
		try {
			method = SystemProperties.getMethod("get", new Class[] { String.class });
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		ArrayList<String> servers = new ArrayList<String>();
		for (String name : new String[] { "net.dns1", "net.dns2", "net.dns3", "net.dns4", }) {
			String value = null;
			try {
				value = (String) method.invoke(null, name);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			if (value != null && !"".equals(value) && !servers.contains(value)){
				servers.add(value);
				m_getIPCallback.onDNSChanged(value);
			}
		}

		try {
			String testData= "roamnest.com";
			String data = URLEncoder.encode("post_test_data", "UTF-8")+"="+URLEncoder.encode(testData, "UTF-8");
			String data0= URLEncoder.encode(testData,"UTF-8");

			URL url = new URL(params[0]);
			Log.d(LOG_TAG, "Connecting to: " + url.getProtocol() + "://" + url.getHost());
			URLConnection conn = url.openConnection();

			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			long wrData = data.length();

			wr.write(data);
			wr.flush();
			// have to be revised...
			for (int i = 0; i < 15; i++) {
				wr.write( data0 );
				wr.flush();
				wrData += data0.length();
				Log.d(LOG_TAG, "Sending... "+ Long.toString(wrData));
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			StringBuilder sb = new StringBuilder();
			String line = null;

			// Read Server Response
			while((line = reader.readLine()) != null) {
				sb.append(line);
				break;
			}

			Log.d(LOG_TAG, sb.toString());
			if (sb.toString().contains(":")) {
				m_getIPCallback.onIPChanged(FLAG_IPV6, sb.toString());
			} else {
				m_getIPCallback.onIPChanged(FLAG_IPV4, sb.toString());
			}

			wr.close();
			return sb.toString();
		}
		catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(String s) {
		super.onPostExecute(s);
	}
}