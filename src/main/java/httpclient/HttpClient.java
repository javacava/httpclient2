package httpclient;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.*;

/**
 * Created by vlad on 21.03.17.
 */
public class HttpClient {

    private URL url;
    public HttpURLConnection httpURLConnection;
    private String currentUrl;
    private String redirectUrl;
    private String userAgent = "java";
    private String cookie;
    private String referer;
    private String params;
    private String requestMethod = "GET";
    private boolean doOutput = false;
    private boolean doInput = false;
    private boolean doErrorInput = false;
    private boolean https = false;
    private boolean doCookie = false;
    private boolean isHaveProxy = false;
    private boolean doAutoRedirect = false;
    private boolean isHaveProxyWithAuth = false;
    private boolean setUseCaches = false;
    private String HTTPHost;
    private int HTTPPort;
    private int connectAndReadTryCount = 1;
    private String Username;
    private String Password;
    private String response;
    private int connectTimeout = 16650;
    private int readTimeout = 10000;
    private int responseCode;
    private boolean isConnected = false;
    private HashMap<String, String> requestProperties = new HashMap<String, String>();


    public void connect() throws IOException {
        for (int i = 0; i < connectAndReadTryCount; i++) {


            try {
                isConnected = true;
                if (isHaveProxy) {
                    if (https) {

                        httpURLConnection = (HttpsURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(HTTPHost, HTTPPort)));

                    } else {
                        httpURLConnection = (HttpURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(HTTPHost, HTTPPort)));

                    }
                    if (isHaveProxyWithAuth) {
                        String encoded = new String(Base64.encodeBase64((Username + ":" + Password).getBytes()));
                        httpURLConnection.setRequestProperty("Proxy-Authorization", "Basic " + encoded);
                        Authenticator.setDefault(new ProxyAuth(Username, Password));
                    }
                } else {
                    if (https) {

                        httpURLConnection = (HttpsURLConnection) url.openConnection();

                    } else {

                        httpURLConnection = (HttpURLConnection) url.openConnection();

                    }
                }

                httpURLConnection.setUseCaches(setUseCaches);
                httpURLConnection.setRequestMethod(requestMethod);
                httpURLConnection.setRequestProperty("User-Agent", userAgent);
                httpURLConnection.setConnectTimeout(connectTimeout);
                httpURLConnection.setReadTimeout(readTimeout);

                if (cookie != null) {
                    httpURLConnection.setRequestProperty("cookie", cookie);
                }

                if (referer != null) {
                    httpURLConnection.setRequestProperty("referer", referer);
                }

                if (requestProperties.size() > 0) {
                    for (Map.Entry<String, String> entry : requestProperties.entrySet()) {
                        String key = entry.getKey();
                        String value = entry.getValue();
                        httpURLConnection.setRequestProperty(key, value);
                    }
                }

                if (doAutoRedirect) {
                    httpURLConnection.setInstanceFollowRedirects(true);
                } else {
                    httpURLConnection.setInstanceFollowRedirects(false);
                }

                if (doOutput) {
                    httpURLConnection.setDoOutput(true);

                    OutputStreamWriter writer = new OutputStreamWriter(httpURLConnection.getOutputStream());
                    writer.write(params);
                    writer.flush();
                    writer.close();
                }


                responseCode = httpURLConnection.getResponseCode();


                if (doErrorInput) {
                    doInput = false;
                    doErrorInput = true;
                }


                if (doInput) {
                    String inputLine;
                    String result = "";

                    BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((inputLine = reader.readLine()) != null) {
                        result += inputLine;
                    }
                    reader.close();

                    response = result;

                }


                if (doErrorInput) {
                    String inputLine;
                    String result = "";
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
                        while ((inputLine = reader.readLine()) != null) {
                            result += inputLine;
                        }
                        reader.close();
                    } catch (IOException e) {
                        throw new Error(e);
                    }
                    response = result;
                }
                if (doCookie) {
                    String cookie = "";
                    try {
                        Map<String, List<String>> headers = httpURLConnection.getHeaderFields();
                        List<String> list = headers.get("set-cookie");
                        if (list == null) {
                            list = headers.get("Set-Cookie");
                        }
                        for (int j = 0; j < list.size(); j++) {
                            cookie += list.get(j) + ";";
                        }
                    } catch (Exception e) {
                        this.cookie = "Cookie not set";
                    }
                    this.cookie = cookie;


                }

                currentUrl = httpURLConnection.getURL().toString();
                this.redirectUrl = httpURLConnection.getHeaderField("Location");
                break;
            } catch (SocketTimeoutException e) {
                if (i == connectAndReadTryCount - 1) {
                    throw new SocketTimeoutException();
                }
            } catch (SocketException e) {
                if (i == connectAndReadTryCount - 1) {
                    throw new SocketException();
                }
            }
        }
    }

    public String getResponse() {

        return this.response;
    }

    public int getResponseCode() {
        return this.responseCode;
    }

    public void setUrl(String link) {
        try {
            if (link.lastIndexOf("https://") != -1) this.https = true;
            this.url = new URL(link);

        } catch (MalformedURLException e) {
            throw new Error(e);
        }
    }

    public void setConnectAndReadTimeout(int timeout) {
        connectTimeout = timeout;
        readTimeout = timeout;

    }

    public void setConnectTimeout(int timeout) {
        connectTimeout = timeout;
    }

    public void setReadTimeout(int timeout) {
        readTimeout = timeout;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRedirectLink() {
        return this.redirectUrl;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public void setConnectAndReadTryCount(int count) {
        this.connectAndReadTryCount = count;
    }

    public void setDoAutoRedirect() {
        this.doAutoRedirect = true;
    }

    public void doCookie() {
        this.doCookie = true;
    }

    public String getCookie() {
        return this.cookie;
    }

    public void setRequestProperty(String key, String value) {
        requestProperties.put(key, value);
    }

    public void setProxyWithAuth(String IpPortUsernamePass) {
        isHaveProxy = true;
        isHaveProxyWithAuth = true;
        this.HTTPHost = IpPortUsernamePass.split(":")[0];
        this.HTTPPort = Integer.parseInt(IpPortUsernamePass.split(":")[1]);
        this.Username = IpPortUsernamePass.split(":")[2];
        this.Password = IpPortUsernamePass.split(":")[3];
    }

    public void setProxy(String IpPort) {
        if (IpPort.split(":").length > 2) {
            setProxyWithAuth(IpPort);
        } else {
            try {
                this.isHaveProxy = true;
                this.HTTPHost = IpPort.split(":")[0];
                this.HTTPPort = Integer.parseInt(IpPort.split(":")[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void doOutput(String params) {
        this.doOutput = true;
        this.params = params;
    }

    public String getCookieViaKey(String key) {
        String r = "";
        try {
            r = cookie.split(key + "=")[1].split(";")[0];
        } catch (ArrayIndexOutOfBoundsException e) {
            r = "null";
        }

        return r;
    }

    public String getRandomUserAgent() {
        String userAgent = "Mozilla/5.0";
        ArrayList<String> OSVersion = new ArrayList<String>();
        OSVersion.add("(Windows NT 6.1)");
        OSVersion.add("(Windows NT 6.3)");
        OSVersion.add("(Windows NT 6.2)");
        OSVersion.add("(Windows NT 6.0)");
        OSVersion.add("(Windows NT 5.1)");
        OSVersion.add("(Windows NT 5.2)");
        OSVersion.add("(Windows NT 6.1)");
        OSVersion.add("(Macintosh; Intel Mac OS X 10_12_1)");
        OSVersion.add("(Macintosh; Intel Mac OS X 10_11_1)");
        OSVersion.add("(Macintosh; Intel Mac OS X 10_10_1)");
        OSVersion.add("(Macintosh; Intel Mac OS X 10_9_1)");
        OSVersion.add("(Macintosh; Intel Mac OS X 10_8_2)");
        ArrayList<String> AppleWebKit = new ArrayList<String>();
        AppleWebKit.add("AppleWebKit/537.36");
        userAgent = userAgent + " " + OSVersion.get(new Random().nextInt(OSVersion.size())) + " " + AppleWebKit.get(new Random().nextInt(AppleWebKit.size())) +
                " (KHTML, like Gecko) " + "Chrome/" + (new Random().nextInt(9) + 45) + ".0." + (new Random().nextInt(100) + 2780) + "." + (new Random().nextInt(48) + 50) +
                " " + "Safari/537.36";
        if (new Random().nextBoolean()) {
            userAgent += " OPR/" + (new Random().nextInt(15) + 30) + ".0." + (new Random().nextInt(100) + 2280) + "." + (new Random().nextInt(22) + 29);

        }
        return userAgent;
    }


    public void notDoInput() {
        this.doInput = false;
    }


    public void doInput() {
        this.doInput = true;
    }

    public void doErrorInput() {
        this.doErrorInput = true;
    }


    public String getCurrentUrl() {
        return this.currentUrl;
    }

    public String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }

    public void setUseCaches(boolean setUseCaches) {
        this.setUseCaches = setUseCaches;
    }

    public String randomString(int from, int to) {
        String pass = "";
        Random r = new Random();
        int cntchars = from + r.nextInt(to - from + 1);

        for (int i = 0; i < cntchars; ++i) {
            char next = 0;
            int range = 10;

            switch (r.nextInt(1)) {
                //    case 0: {next = '0'; range = 10;} break;
                case 0: {
                    next = 'a';
                    range = 26;
                }
                break;
            }

            pass += (char) ((r.nextInt(range)) + next);
        }

        return pass;
    }

    private class ProxyAuth extends Authenticator {

        private PasswordAuthentication auth;

        public ProxyAuth(String user, String password) {
            auth = new PasswordAuthentication(user, password == null ? new char[]{} : password.toCharArray());
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            return auth;
        }

    }




}
