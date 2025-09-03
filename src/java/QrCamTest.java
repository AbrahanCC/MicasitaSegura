import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class QrCamTest {

  // Conexion del puerto 
  private static final String CONTEXT_PATH = "MiCasitaSegura";  
  private static final String BACKEND_HOST = "http://localhost:8080";

  // Conexion del node
  private static final String ESP_IP = "http://192.168.1.130";    // 
  private static final String OPEN_KEY = "talanquera";           // misma clave que en el sketch
  private static final int OPEN_TIMEOUT_MS = 5000;

  public static void main(String[] args) throws Exception {
    // Carga tu DLL exacta (déjala como ya la tenías funcionando)
    System.load("C:\\Users\\abrah\\Downloads\\mail\\opencv\\build\\java\\x64\\opencv_java451.dll");

    VideoCapture cam = new VideoCapture(0);
    if (!cam.isOpened()) { cam.open(1); if (!cam.isOpened()) { System.err.println("No hay cámara."); return; } }

    Mat frame = new Mat();
    MultiFormatReader reader = new MultiFormatReader();

    System.out.println("Apunta un QR a la cámara... (Ctrl+C para salir)");
    while (true) {
      if (!cam.read(frame)) continue;

      BufferedImage img = matToGrayBufferedImage(frame);
      if (img == null) continue;

      try {
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(img)));
        Result result = reader.decode(bitmap);
        String qrText = result.getText();
        System.out.println("QR detectado: " + qrText);

        // 1) Construye URL de validación
        String validateUrl = (qrText.startsWith("http://") || qrText.startsWith("https://"))
            ? qrText
            : BACKEND_HOST + "/" + CONTEXT_PATH + "/api/validate?token=" + qrText;

        // 2) Llama al backend para validar
        String json = httpGet(validateUrl, 8000);
        System.out.println("Respuesta del servidor: " + json);

        boolean permitido = json != null && json.contains("\"valid\":true");
        if (permitido) {
          // // //System.out.println("✅ PERMITIDO → ordenando apertura al ESP...");
          // 3) Ordenar apertura al ESP
          // // //String openUrl = ESP_IP + "/open?key=" + OPEN_KEY;
          // // //String openResp = httpGet(openUrl, OPEN_TIMEOUT_MS);
          // // //System.out.println("ESP respondió: " + openResp);
        } else {
          System.out.println("⛔ DENEGADO");
        }
        break; // salimos tras el primer QR procesado
      } catch (NotFoundException e) {
        // sin QR en el frame → continuar
      } catch (Exception e) {
        e.printStackTrace();
        break;
      }
    }
    cam.release();
    System.out.println("Fin de prueba.");
  }

  // Mat (BGR) → BufferedImage (grises)
  private static BufferedImage matToGrayBufferedImage(Mat src) {
    try {
      Mat gray = new Mat();
      Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
      int w = gray.width(), h = gray.height();
      byte[] data = new byte[(int)(gray.total() * gray.channels())];
      gray.get(0, 0, data);
      BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
      image.getRaster().setDataElements(0, 0, w, h, data);
      return image;
    } catch (Exception e) {
      return null;
    }
  }

  // GET simple
  private static String httpGet(String urlStr, int timeoutMs) {
    HttpURLConnection con = null;
    try {
      URL url = new URL(urlStr);
      con = (HttpURLConnection) url.openConnection();
      con.setRequestMethod("GET");
      con.setConnectTimeout(timeoutMs);
      con.setReadTimeout(timeoutMs);

      int code = con.getResponseCode();
      BufferedReader br = new BufferedReader(new InputStreamReader(
          (code >= 200 && code < 300) ? con.getInputStream() : con.getErrorStream(), "UTF-8"));
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = br.readLine()) != null) sb.append(line);
      br.close();
      return sb.toString();
    } catch (Exception e) {
      System.err.println("Error HTTP (" + urlStr + "): " + e.getMessage());
      return null;
    } finally {
      if (con != null) con.disconnect();
    }
  }
}

