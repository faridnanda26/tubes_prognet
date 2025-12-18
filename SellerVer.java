import java.io.*;
import java.net.*;
import javax.swing.SwingUtilities;

public class SellerVer {
    private static SellerMin adminUI;

    public static void main(String[] args) {
        // 1. Jalankan UI SellerMin
        SwingUtilities.invokeLater(() -> {
            adminUI = new SellerMin();
        });

        // 2. Jalankan Koneksi Server
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("🚀 Server FashionHub Aktif di Port 5000...");
            
            while (true) {
                Socket socket = serverSocket.accept();
                // Tunggu sebentar sampai adminUI selesai dibuat
                if (adminUI != null) {
                    new Thread(new ClientHandler(socket)).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                
                System.out.println("📱 Pembeli Terhubung!");

                // Kirim data stok barang ke pembeli saat pertama kali konek
                String currentData = adminUI.getProductDataString();
                out.println("DATA_PRODUK:" + currentData);

                String request;
                while ((request = in.readLine()) != null) {
                    if (request.startsWith("LOGIN:")) {
                        adminUI.handleLogin(request.substring(6));
                    } else if (request.startsWith("ORDER:")) {
                        adminUI.handleOrder(request.substring(6));
                    }
                }
            } catch (IOException e) {
                System.out.println("🔌 Pembeli terputus.");
            }
        }
    }
}