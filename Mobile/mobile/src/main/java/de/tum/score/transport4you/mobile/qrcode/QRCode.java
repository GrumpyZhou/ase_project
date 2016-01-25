package de.tum.score.transport4you.mobile.qrcode;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;


/**
 * Created by yz on 16-1-17.
 */
public class QRCode {

    public static Bitmap createQRCode(String text) throws WriterException {

        Map hintMap = new HashMap();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        BitMatrix matrix = new MultiFormatWriter().encode(
                new String(text),
                BarcodeFormat.QR_CODE, 720, 720, hintMap);

        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; ++ x){
            for (int y = 0; y < height; ++ y){
                bmp.setPixel(x, y, matrix.get(x,y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bmp;
    }

}
