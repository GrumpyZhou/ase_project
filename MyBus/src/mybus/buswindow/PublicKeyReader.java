package mybus.buswindow;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.openssl.PEMReader;

public class PublicKeyReader {
	
	private static String pkPath  = "/Users/jennyzhou/Desktop/T4Y_Codebase/MyBus/res/BlobEncryptionKey-cert.pem";
	
	public static PublicKey getCommon()
		    throws Exception {

		    File f = new File(pkPath);
		    FileInputStream fis = new FileInputStream(f);
		    DataInputStream dis = new DataInputStream(fis);
		    byte[] keyBytes = new byte[(int)f.length()];
		    dis.readFully(keyBytes);
		    dis.close();

		    X509EncodedKeySpec spec =
		      new X509EncodedKeySpec(keyBytes);
		    KeyFactory kf = KeyFactory.getInstance("RSA");
		    return kf.generatePublic(spec);
		  }
	
	public static PublicKey getByBC() throws IOException {
		//Load Blob Public Key
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		FileReader fileReader = new FileReader(pkPath);
        PEMReader pemReader = new PEMReader(fileReader);
        X509Certificate cert = (X509Certificate) pemReader.readObject();
        PublicKey publicKey = (PublicKey) cert.getPublicKey();
        pemReader.close();
        return publicKey;
	}
	
	
}
