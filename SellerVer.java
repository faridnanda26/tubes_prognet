import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;

public class SellerVer {
    private static SellerMin adminUI;
    
    // List untuk menyimpan semua koneksi pembeli (Client) yang sedang online
    // Menggunakan CopyOnWriteArrayList agar aman diakses banyak thread sekaligus
    public static CopyOnWriteArrayList<PrintWriter> allClients = new CopyOnWriteArrayList<>();

    // --- VARIABEL SINKRONISASI WAKTU ---
    // Menyimpan waktu "target" kapan flash sale berakhir (dalam milidetik/Epoch Time)
    public static long flashSaleEndTime = 0; 

    public static void main(String[] args) {
        // 1. Jalankan Tampilan Admin (SellerMin)
        SwingUtilities.invokeLater(() -> {
            adminUI = new SellerMin();
        });

        // 2. Jalankan Server Socket di Thread terpisah agar GUI tidak macet
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(5000)) {
                System.out.println("🚀 Server FashionHub Aktif di Port 5000...");
                
                while (true) {
                    // Tunggu pembeli terhubung
                    Socket socket = serverSocket.accept();
                    
                    // Pastikan Admin UI sudah siap sebelum melayani klien
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
     * LOGIKA TIMER: 
     * Method ini dipanggil oleh Admin (SellerMin) saat tombol Simpan ditekan.
     * Menerima input menit, mengubahnya menjadi waktu absolut, lalu kirim ke semua user.
     */
    public static void broadcastTimer(int menit) {
        // Hitung waktu selesai: (Waktu Sekarang) + (Menit * 60 detik * 1000 milidetik)
        flashSaleEndTime = System.currentTimeMillis() + ((long) menit * 60 * 1000);
        
        System.out.println("⏰ Timer Flash Sale diaktifkan: " + menit + " menit.");
        
        // Kirim sinyal ke semua client yang sedang online
        for (PrintWriter writer : allClients) {
            writer.println("SYNC_TIMER:" + flashSaleEndTime);
        }
    }

    /**
     * Mengirim data produk terbaru (stok/harga) ke semua pembeli
     */
    public static void broadcastUpdate() {
        if (adminUI != null) {
            String data = adminUI.getProductDataString();
            System.out.println("📢 Update stok dikirim ke " + allClients.size() + " user.");
            
            for (PrintWriter writer : allClients) {
                try {
                    writer.println("DATA_PRODUK:" + data);
                } catch (Exception e) {
                    allClients.remove(writer); // Hapus jika user sudah disconnect
                }
            }
        }
    }

    // Kelas untuk menangani setiap satu pembeli (One Thread per Client)
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
                
                // Tambahkan client ini ke daftar online
                allClients.add(out);
                
                // 1. Kirim Data Produk saat pertama kali connect
                out.println("DATA_PRODUK:" + adminUI.getProductDataString());
                
                // 2. SINKRONISASI TIMER UNTUK USER BARU
                // Jika Flash Sale sedang berlangsung, kirim waktu sisanya ke user yang baru masuk
                if (flashSaleEndTime > System.currentTimeMillis()) {
                    out.println("SYNC_TIMER:" + flashSaleEndTime);
                }

                // Loop mendengarkan pesan dari Client
                String request;
                while ((request = in.readLine()) != null) {
                    
                    // Client Login
                    if (request.startsWith("LOGIN:")) {
                        adminUI.handleLogin(request.substring(6));
                    } 
                    
                    // Client Checkout/Order
                    else if (request.startsWith("ORDER:")) {
                        String orderData = request.substring(6);
                        adminUI.handleOrder(orderData); // Log ke admin
                        
                        // Parse nota untuk mengurangi stok
                        if (orderData.contains("Nota: ")) {
                            String nota = orderData.substring(orderData.indexOf("Nota: ") + 6);
                            
                            // Kurangi stok di Admin
                            adminUI.reduceStock(nota); 
                            
                            // Broadcast stok baru ke SEMUA user
                            SellerVer.broadcastUpdate(); 
                        }
                    }
                }
            } catch (Exception e) {
                // Client disconnect diam-diam
            } finally {
                // Bersihkan data jika client putus
                if (out != null) allClients.remove(out);
                try { socket.close(); } catch (IOException e) {}
            }
        }
    }
}