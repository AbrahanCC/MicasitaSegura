package service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class GateService {

  // Cambia aquí la IP por la que necesites en cada sitio
  private static String base() {
    String host = System.getProperty("ESP_HOST", "http://192.168.1.5"); // <— IP por defecto
    if (host.endsWith("/")) host = host.substring(0, host.length() - 1);
    return host;
  }

  public boolean abrir() {
    String key = System.getProperty("ESP_OPEN_KEY", "talanquera");
    String url = base() + "/open?key=" + encode(key, "UTF-8");

    final int intentos = 2; // con 2 alcanza si la IP es correcta
    for (int i = 1; i <= intentos; i++) {
      HttpURLConnection con = null;
      try {
        System.out.println("[GateService] Llamando: " + url + " (intento " + i + ")");
        con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(6000);
        int code = con.getResponseCode();
        System.out.println("[GateService] HTTP " + code);
        if (code == 200) return true;
      } catch (Exception e) {
        System.err.println("[GateService] Error intento " + i + ": " + e.getMessage());
      } finally {
        if (con != null) con.disconnect();
      }
      try { Thread.sleep(600); } catch (InterruptedException ignored) {}
    }
    return false;
  }

  private static String encode(String s, String charset) {
    try { return URLEncoder.encode(s, charset); }
    catch (Exception e) { return s; }
  }
}
