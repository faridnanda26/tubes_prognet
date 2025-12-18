import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;

public class SellerVer {
    private static SellerMin adminUI;
    
    // List untuk menyimpan semua PrintWriter dari client yang sedang online
    public static CopyOnWriteArrayList<PrintWriter> allClients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        // 1. Jalankan UI SellerMin
        SwingUtilities.invokeLater(() -> {
            adminUI = new SellerMin();
        });

        // 2. Jalankan Koneksi Server (Thread Terpisah)
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(5000)) {
                System.out.println("🚀 Server FashionHub Aktif di Port 5000...");
                
                while (true) {
                    Socket socket = serverSocket.accept();
                    if (adminUI != null) {
                        new Thread(new ClientHandler(socket)).start();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Method untuk mengirim data produk terbaru ke SEMUA client yang sedang online
     */
    public static void broadcastUpdate() {
        if (adminUI != null) {
            String data = adminUI.getProductDataString();
            System.out.println("📢 Mengirim update stok ke semua pembeli...");
            for (PrintWriter writer : allClients) {
                try {
                    writer.println("DATA_PRODUK:" + data);
                } catch (Exception e) {
                    allClients.remove(writer); // Hapus jika client sudah tidak aktif
                }
            }
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                this.out = new PrintWriter(socket.getOutputStream(), true);
                
                // Tambahkan writer client ini ke daftar broadcast
                allClients.add(out);
                System.out.println("📱 Pembeli Terhubung! Total online: " + allClients.size());

                // Kirim data stok awal saat pertama kali terhubung
                String currentData = adminUI.getProductDataString();
                out.println("DATA_PRODUK:" + currentData);

                String request;
                while ((request = in.readLine()) != null) {
                    if (request.startsWith("LOGIN:")) {
                        adminUI.handleLogin(request.substring(6));
                    } 
                    else if (request.startsWith("ORDER:")) {
                        String orderData = request.substring(6);
                        adminUI.handleOrder(orderData); // Log di Dashboard Admin
                        
                        // Ekstrak bagian Nota: "Kaos (x1), "
                        if (orderData.contains("Nota: ")) {
                            String nota = orderData.substring(orderData.indexOf("Nota: ") + 6);
                            
                            // 1. Potong stok di Admin & Tulis ke File TXT
                            adminUI.reduceStock(nota); 
                            
                            // 2. Broadcast data terbaru ke SEMUA Client online
                            SellerVer.broadcastUpdate(); 
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("🔌 Seorang pembeli terputus.");
            } finally {
                if (out != null) allClients.remove(out); // Hapus dari list jika disconnect
            }
        }
    }
}