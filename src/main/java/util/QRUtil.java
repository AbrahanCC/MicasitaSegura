package util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

public class QRUtil {
  public static byte[] makeQRPng(String text, int size) throws Exception {
    BitMatrix m = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size);
    BufferedImage img = MatrixToImageWriter.toBufferedImage(m);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, "png", baos);
    return baos.toByteArray();
  }
}

