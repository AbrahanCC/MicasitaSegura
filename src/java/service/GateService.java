package service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GateService {
  private static String base() {
    // usa exactamente la IP que te funciona en el navegador
    String ip = "http://10.97.11.182";
    if (ip.endsWith("/")) ip = ip.substring(0, ip.length()-1);
    return ip;
  }

  public boolean abrir() {
    try {
      String key = "talanquera";
      String url = base() + "/open?key=" + URLEncoder.encode(key, StandardCharsets.UTF_8.name());
      HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
      con.setRequestMethod("GET");
      con.setConnectTimeout(6000);
      con.setReadTimeout(7000);
      return con.getResponseCode() == 200;
    } catch (Exception e) {
      System.err.println("[GateService] " + e.getMessage());
      return false;
    }
  }
}
