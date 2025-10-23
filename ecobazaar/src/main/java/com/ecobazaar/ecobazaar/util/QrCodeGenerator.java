package com.ecobazaar.ecobazaar.util;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QrCodeGenerator {

	public static String generateQR(String text, String filePath)throws IOException, WriterException {
		
		// Step 1: Create QR code data from text (product info or link)
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		
		// Step 2: Generate a QR matrix (pixels)
		BitMatrix bitMatrix  = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 300, 300);
		
		// Step 3: Convert matrix → actual PNG image and save it
		Path path = FileSystems.getDefault().getPath(filePath);
		
		MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
		
		// Step 4: Return the file path for saving in DB
		return filePath;
	}
}