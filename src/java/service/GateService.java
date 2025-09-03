package service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GateService {
  private static String base() {
    String ip = System.getProperty("ESP_IP", "http://192.168.1.130");
    // quita "/" final si viene
    if (ip.endsWith("/")) ip = ip.substring(0, ip.length()-1);
    return ip;
  }

  public boolean abrir() {
    try {
      String key = System.getProperty("ESP_KEY", "talanquera");
      String url = base() + "/open?key=" + URLEncoder.encode(key, StandardCharsets.UTF_8.name());
      HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
      con.setRequestMethod("GET");
      con.setConnectTimeout(2000);
      con.setReadTimeout(4000);
      return con.getResponseCode() == 200;
    } catch (Exception e) {
      System.err.println("GateService error: " + e.getMessage());
      return false;
    }
  }
}

