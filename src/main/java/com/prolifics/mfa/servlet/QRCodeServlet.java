package com.prolifics.mfa.servlet;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * QRCodeServlet generates QR code images for MFA setup.
 *
 * GET: Generate and return QR code image as PNG
 *
 * Security:
 * - Requires authenticated session
 * - Uses QR code URL from session
 *
 * @author Bob
 * @version 1.0
 */
@WebServlet("/qr-code")
public class QRCodeServlet extends HttpServlet {
    
    private static final int QR_CODE_SIZE = 400; // Increased size for better scanning
    
    /**
     * Generate QR code image from session data.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // Check if user is authenticated
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
            return;
        }
        
        // Get QR code URL from session
        String qrCodeURL = (String) session.getAttribute("qrCodeURL");
        
        if (qrCodeURL == null || qrCodeURL.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No QR code data available");
            return;
        }
        
        // Extract otpauth:// URL from Google Charts URL if present
        String otpauthURL = qrCodeURL;
        if (qrCodeURL.contains("chl=")) {
            // Extract the otpauth URL from the Google Charts URL
            int chlIndex = qrCodeURL.indexOf("chl=");
            if (chlIndex != -1) {
                otpauthURL = qrCodeURL.substring(chlIndex + 4);
                // Decode URL encoding if present
                otpauthURL = java.net.URLDecoder.decode(otpauthURL, "UTF-8");
            }
        }
        
        try {
            // Configure QR code generation with higher error correction
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);
            
            // Generate QR code
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(otpauthURL, BarcodeFormat.QR_CODE,
                                                      QR_CODE_SIZE, QR_CODE_SIZE, hints);
            
            // Set response headers
            response.setContentType("image/png");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            
            // Write QR code image to response
            OutputStream outputStream = response.getOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            outputStream.flush();
            
        } catch (WriterException e) {
            throw new ServletException("Failed to generate QR code", e);
        }
    }
}

// Made with Bob