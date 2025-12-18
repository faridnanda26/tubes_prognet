import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class FullEcommerceApps extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    
    private CardLayout profileCard = new CardLayout();
    private JPanel profileContainer = new JPanel(profileCard);

    private String currentUser;
    private JLabel lblUserProfile;
    private JPanel gridDash, gridSale, cartPanel, paymentPanel;
    
    private Map<String, CartItem> cartMap = new LinkedHashMap<>();
    private java.util.List<HistoryItem> history = new ArrayList<>();
    private JPanel historyList;
    
    private String userPhone = "0812-3456-7890";
    private String userAddress = "Jl. Fashion No. 123, Jakarta Selatan, DKI Jakarta";
    
    private final String IP = "localhost";
    private final int PORT = 5000;
    private PrintWriter out;

    private final Color COLOR_RED = new Color(214, 48, 49);
    private final Color COLOR_WHITE = Color.WHITE;
    private final Color COLOR_BG = new Color(245, 246, 250);

    class CartItem {
        int qty, price;
        String imgName;
        boolean selected = true;
        CartItem(int q, int p, String img) { this.qty = q; this.price = p; this.imgName = img; }
    }

    public FullEcommerceApps() {
        initUI();
    }

    private void initUI() {
        setTitle("FashionHub");
        setSize(420, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel.add(createLoginPage(), "LOGIN_PAGE");

        gridDash = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
        gridDash.setBackground(COLOR_BG);
        gridSale = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
        gridSale.setBackground(COLOR_BG);
        
        cartPanel = new JPanel(); 
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));

        paymentPanel = new JPanel(new BorderLayout());
        paymentPanel.setBackground(COLOR_BG);

        setupProfilePages();

        mainPanel.add(createNavWrapper(gridDash, "BERANDA"), "DASHBOARD");
        mainPanel.add(createNavWrapper(gridSale, "FLASH SALE ⚡"), "FLASH");
        mainPanel.add(createNavWrapper(cartPanel, "KERANJANG SAYA"), "CART");
        mainPanel.add(createNavWrapper(profileContainer, "PROFIL"), "PROFILE");
        mainPanel.add(createNavWrapper(paymentPanel, "CHECKOUT PESANAN"), "PAYMENT");

        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN_PAGE");
        setVisible(true);
    }

    // ================= HALAMAN LOGIN =================
    private JPanel createLoginPage() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(COLOR_RED);
        RoundedPanel box = new RoundedPanel(40, COLOR_WHITE);
        box.setPreferredSize(new Dimension(320, 480));
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        JLabel logo = new JLabel("FashionHub");
        logo.setFont(new Font("SansSerif", Font.BOLD, 28));
        logo.setForeground(COLOR_RED); logo.setAlignmentX(0.5f);

        JTextField tUser = new JTextField();
        tUser.setMaximumSize(new Dimension(260, 55));
        tUser.setBorder(BorderFactory.createTitledBorder("Username"));

        JPasswordField tPass = new JPasswordField();
        tPass.setMaximumSize(new Dimension(260, 55));
        tPass.setBorder(BorderFactory.createTitledBorder("Password"));

        JButton btnLogin = new JButton("MASUK SEKARANG");
        btnLogin.setBackground(COLOR_RED); btnLogin.setForeground(COLOR_WHITE);
        btnLogin.setAlignmentX(0.5f);
        btnLogin.addActionListener(e -> {
            if(!tUser.getText().isEmpty() && tPass.getPassword().length > 0) {
                currentUser = tUser.getText();

                if (lblUserProfile != null) {
                    lblUserProfile.setText("User: " + currentUser);
                }

                connect();
                cardLayout.show(mainPanel, "DASHBOARD");
            }
        });

        box.add(logo); box.add(Box.createVerticalStrut(40));
        box.add(tUser); box.add(Box.createVerticalStrut(15));
        box.add(tPass); box.add(Box.createVerticalStrut(30));
        box.add(btnLogin);
        p.add(box);
        return p;
    }

    private JPanel createCard(String imgName, String name, String priceStr) {
        int priceInt = Integer.parseInt(priceStr.replace(".", ""));
        JPanel c = new JPanel(new BorderLayout());
        c.setPreferredSize(new Dimension(175, 270));
        c.setBackground(Color.WHITE);
        c.setBorder(new LineBorder(new Color(230, 230, 230), 1, true));

        JLabel lblImg = new JLabel();
        lblImg.setHorizontalAlignment(JLabel.CENTER);
        try {
            ImageIcon icon = new ImageIcon("images/" + imgName);
            Image img = icon.getImage().getScaledInstance(150, 130, Image.SCALE_SMOOTH);
            lblImg.setIcon(new ImageIcon(img));
        } catch (Exception e) { lblImg.setText("No Image"); }

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        JLabel lblInfo = new JLabel("<html><center>" + name + "<br><b color='red'>Rp " + priceStr + "</b></center></html>", JLabel.CENTER);
        
        JButton btn = new JButton("TAMBAH");
        btn.setBackground(COLOR_RED);
        btn.setForeground(COLOR_WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.addActionListener(e -> {
            if(cartMap.containsKey(name)) cartMap.get(name).qty++;
            else cartMap.put(name, new CartItem(1, priceInt, imgName));
            JOptionPane.showMessageDialog(this, name + " masuk keranjang!");
        });

        bottom.add(lblInfo, BorderLayout.CENTER); bottom.add(btn, BorderLayout.SOUTH);
        c.add(lblImg, BorderLayout.CENTER); c.add(bottom, BorderLayout.SOUTH);
        return c;
    }

    private JPanel createHomeCard(String imgName, String name, String priceStr, boolean isSale) {
        JPanel card = createCard(imgName, name, priceStr);

        if (isSale) {
            JLabel badge = new JLabel("FLASH SALE");
            badge.setOpaque(true);
            badge.setBackground(COLOR_RED);
            badge.setForeground(Color.WHITE);
            badge.setFont(new Font("SansSerif", Font.BOLD, 10));
            badge.setBorder(new EmptyBorder(3,6,3,6));

            JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            badgeWrap.setOpaque(false);
            badgeWrap.add(badge);

            card.add(badgeWrap, BorderLayout.NORTH);
        }

        return card;
    }

    private void updateCartUI() {
        cartPanel.removeAll();
        cartPanel.setBackground(COLOR_BG);
        cartPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        if (cartMap.isEmpty()) {
            cartPanel.add(new JLabel("Keranjang kosong."));
        } else {
            long totalAll = 0;
            for (String key : cartMap.keySet()) {
                CartItem item = cartMap.get(key);
                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setBackground(COLOR_WHITE);
                row.setMaximumSize(new Dimension(400, 90));
                row.setBorder(new CompoundBorder(new LineBorder(new Color(230,230,230)), new EmptyBorder(10,10,10,10)));

                JCheckBox cb = new JCheckBox();
                cb.setSelected(item.selected);
                cb.setBackground(COLOR_WHITE);
                cb.addActionListener(e -> { item.selected = cb.isSelected(); updateCartUI(); });

                JLabel lblThumb = new JLabel();
                try {
                    ImageIcon icon = new ImageIcon("images/" + item.imgName);
                    Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                    lblThumb.setIcon(new ImageIcon(img));
                } catch (Exception e) {}

                JLabel info = new JLabel("<html><b>" + key + "</b><br><small>Rp " + item.price + "</small></html>");
                
                JPanel qtyBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                qtyBox.setBackground(COLOR_WHITE);
                JButton btnMin = new JButton("-");
                JLabel lblQty = new JLabel(String.valueOf(item.qty));
                JButton btnPlus = new JButton("+");
                btnMin.addActionListener(e -> { if(item.qty > 1) item.qty--; else cartMap.remove(key); updateCartUI(); });
                btnPlus.addActionListener(e -> { item.qty++; updateCartUI(); });
                qtyBox.add(btnMin); qtyBox.add(lblQty); qtyBox.add(btnPlus);

                JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                westPanel.setBackground(COLOR_WHITE);
                westPanel.add(cb); westPanel.add(lblThumb);

                row.add(westPanel, BorderLayout.WEST); row.add(info, BorderLayout.CENTER); row.add(qtyBox, BorderLayout.EAST);
                cartPanel.add(row); cartPanel.add(Box.createVerticalStrut(10));
                if(item.selected) totalAll += (long) item.price * item.qty;
            }

            JPanel footer = new JPanel(new BorderLayout());
            footer.setBackground(COLOR_WHITE); footer.setBorder(new EmptyBorder(10,15,10,15));
            footer.setMaximumSize(new Dimension(400, 60));
            footer.add(new JLabel("Total: Rp " + totalAll), BorderLayout.WEST);
            
            JButton btnPay = new JButton("Checkout");
            btnPay.setPreferredSize(new Dimension(100, 30));
            btnPay.setBackground(new Color(46, 204, 113)); btnPay.setForeground(COLOR_WHITE);
            final long fTotal = totalAll;
            btnPay.addActionListener(e -> { if(fTotal > 0) showPaymentPage(fTotal); });

            footer.add(btnPay, BorderLayout.EAST);
            cartPanel.add(Box.createVerticalGlue());
            cartPanel.add(footer);
        }
        cartPanel.revalidate(); cartPanel.repaint();
    }

    // ================= HALAMAN PAYMENT (KOTAK CENTER, TEKS KIRI) =================
    private void showPaymentPage(long total) {
        paymentPanel.removeAll();
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(COLOR_BG);
        container.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. HEADER ALAMAT (Box Center, Teks Left)
        JPanel addrBox = new JPanel();
        addrBox.setLayout(new BoxLayout(addrBox, BoxLayout.Y_AXIS));
        addrBox.setBackground(COLOR_WHITE);
        addrBox.setBorder(new CompoundBorder(new LineBorder(new Color(220,220,220)), new EmptyBorder(15,15,15,15)));
        addrBox.setMaximumSize(new Dimension(380, 130)); // Kotak proporsional di tengah
        addrBox.setAlignmentX(Component.CENTER_ALIGNMENT); // Kotaknya Center

        JLabel lblIcon = new JLabel("📍 Alamat Pengiriman");
        lblIcon.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel lblUser = new JLabel(currentUser + " | " + userPhone);
        lblUser.setFont(new Font("SansSerif", Font.BOLD, 12));
        JLabel lblAddress = new JLabel("<html>" + userAddress + "</html>");
        lblAddress.setForeground(Color.GRAY);

        addrBox.add(lblIcon); addrBox.add(Box.createVerticalStrut(8));
        addrBox.add(lblUser); addrBox.add(Box.createVerticalStrut(5));
        addrBox.add(lblAddress);
        container.add(addrBox); container.add(Box.createVerticalStrut(20));

        // 2. DAFTAR PRODUK
        JLabel lblTitleProd = new JLabel("Ringkasan Belanja");
        lblTitleProd.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTitleProd.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(lblTitleProd); container.add(Box.createVerticalStrut(10));

        for (String key : cartMap.keySet()) {
            if (cartMap.get(key).selected) {
                CartItem item = cartMap.get(key);
                JPanel prodRow = new JPanel(new BorderLayout(15, 0));
                prodRow.setBackground(COLOR_WHITE);
                prodRow.setBorder(new EmptyBorder(10, 10, 10, 10));
                prodRow.setMaximumSize(new Dimension(380, 80));
                prodRow.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel thumb = new JLabel();
                try {
                    ImageIcon icon = new ImageIcon("images/" + item.imgName);
                    Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                    thumb.setIcon(new ImageIcon(img));
                } catch (Exception e) {}

                JLabel desc = new JLabel("<html><b>" + key + "</b><br>x" + item.qty + "</html>");
                JLabel price = new JLabel("Rp " + (item.price * item.qty));
                price.setForeground(COLOR_RED);

                prodRow.add(thumb, BorderLayout.WEST);
                prodRow.add(desc, BorderLayout.CENTER);
                prodRow.add(price, BorderLayout.EAST);

                container.add(prodRow); container.add(Box.createVerticalStrut(5));
            }
        }
        container.add(Box.createVerticalStrut(20));

        // 3. OPSI PEMBAYARAN (Box Center, Teks Left)
        JPanel payBox = new JPanel();
        payBox.setLayout(new BoxLayout(payBox, BoxLayout.Y_AXIS));
        payBox.setBackground(COLOR_WHITE);
        payBox.setBorder(new CompoundBorder(new LineBorder(new Color(220,220,220)), new EmptyBorder(15,15,15,15)));
        payBox.setMaximumSize(new Dimension(380, 160));
        payBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPayTitle = new JLabel("Metode Pembayaran");
        lblPayTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        payBox.add(lblPayTitle); payBox.add(Box.createVerticalStrut(10));

        JRadioButton rb1 = new JRadioButton("Transfer Bank");
        JRadioButton rb2 = new JRadioButton("E-Wallet");
        JRadioButton rb3 = new JRadioButton("COD (Bayar di Tempat)");
        rb1.setBackground(COLOR_WHITE); rb2.setBackground(COLOR_WHITE); rb3.setBackground(COLOR_WHITE);
        rb1.setSelected(true);

        ButtonGroup group = new ButtonGroup();
        group.add(rb1); group.add(rb2); group.add(rb3);

        payBox.add(rb1); payBox.add(rb2); payBox.add(rb3);
        container.add(payBox); container.add(Box.createVerticalStrut(20));

        // 4. FOOTER
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(COLOR_WHITE);
        footer.setBorder(new EmptyBorder(15, 15, 15, 15));
        footer.setMaximumSize(new Dimension(380, 80));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel totalFinal = new JLabel("Total: Rp " + total);
        totalFinal.setFont(new Font("SansSerif", Font.BOLD, 16));
        totalFinal.setForeground(COLOR_RED);

        JButton btnFinal = new JButton("BAYAR SEKARANG");
        btnFinal.setBackground(new Color(46, 204, 113));
        btnFinal.setForeground(COLOR_WHITE);
        btnFinal.setPreferredSize(new Dimension(160, 40));

        btnFinal.addActionListener(e -> {
            String method = rb1.isSelected() ? "Bank" : rb2.isSelected() ? "E-Wallet" : "COD";
            String nota = "";
            for(String k : cartMap.keySet()) if(cartMap.get(k).selected) nota += k + " (x" + cartMap.get(k).qty + "), ";
            
            if(out != null) out.println("ORDER:" + currentUser + " | Metode: " + method + " | Nota: " + nota);
            
            String tanggal = new SimpleDateFormat("dd MMM").format(new Date());

            for (String k : cartMap.keySet()) {
                CartItem item = cartMap.get(k);
                if (item.selected) {
                    history.add(new HistoryItem(
                        k,
                        "images/" + item.imgName,
                        item.price,
                        item.qty,
                        tanggal,
                        method
                    ));
                }
            }

            cartMap.entrySet().removeIf(entry -> entry.getValue().selected);
            JOptionPane.showMessageDialog(this, "Berhasil! Silakan cek menu profil.");
            cardLayout.show(mainPanel, "DASHBOARD");
        });

        footer.add(totalFinal, BorderLayout.WEST);
        footer.add(btnFinal, BorderLayout.EAST);
        container.add(Box.createVerticalGlue());
        container.add(footer);

        paymentPanel.add(new JScrollPane(container), BorderLayout.CENTER);
        cardLayout.show(mainPanel, "PAYMENT");
    }

    // ================= SISTEM NAVIGASI & KONEKSI (TIDAK BERUBAH) =================
    private JPanel createNavWrapper(JPanel content, String title) {
        JPanel w = new JPanel(new BorderLayout());
        JPanel h = new JPanel(new BorderLayout()); h.setBackground(COLOR_RED); h.setPreferredSize(new Dimension(0, 50));
        JLabel t = new JLabel("  " + title); t.setForeground(COLOR_WHITE); h.add(t, BorderLayout.WEST);
        w.add(h, BorderLayout.NORTH); w.add(new JScrollPane(content), BorderLayout.CENTER);
        w.add(createNav(), BorderLayout.SOUTH); return w;
    }

    private JPanel createNav() {
        JPanel n = new JPanel(new GridLayout(1, 4)); n.setPreferredSize(new Dimension(0, 60)); n.setBackground(COLOR_RED);
        String[] icons = {"🏠", "⚡", "🛒", "👤"}; String[] targets = {"DASHBOARD", "FLASH", "CART", "PROFILE"};
        for(int i=0; i<4; i++) {
            final String target = targets[i]; JButton b = new JButton(icons[i]); b.setFont(new Font("Segoe UI Emoji", 0, 20));
            b.setForeground(Color.WHITE); b.setContentAreaFilled(false); b.setBorderPainted(false);
            b.addActionListener(e -> { if(target.equals("CART")) updateCartUI(); cardLayout.show(mainPanel, target); });
            n.add(b);
        }
        return n;
    }

    private void connect() {
        try {
            Socket s = new Socket(IP, PORT);
            out = new PrintWriter(s.getOutputStream(), true);
            out.println("LOGIN:" + currentUser);
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    while (true) {
                        String line = in.readLine();
                        if(line != null && line.startsWith("DATA_PRODUK:")) SwingUtilities.invokeLater(() -> parseProducts(line.substring(12)));
                    }
                } catch (Exception e) {}
            }).start();
        } catch (Exception e) {}
    }

    private void parseProducts(String raw) {
        gridDash.removeAll(); 
        gridSale.removeAll();

        for(String item : raw.split(";")) {
            String[] d = item.split(",");
            if(d.length < 5) continue;

            boolean isSale = d[4].equals("SALE");

            JPanel card = createHomeCard(
                d[0], // image
                d[1], // nama
                d[3], // harga
                isSale
            );

            // 👉 SEMUA MASUK BERANDA
            gridDash.add(card);

            // 👉 FLASH SALE TETAP MASUK MENU FLASH
            if (isSale) gridSale.add(card);
        }

        gridDash.revalidate();
        gridSale.revalidate();
        repaint();
    }

    private void setupProfilePages() {
        profileContainer.add(createProfileMenu(), "MENU_UTAMA");
        profileContainer.add(createHistoryPage(), "HAL_RIWAYAT");
    }

    private JPanel createProfileMenu() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(COLOR_BG);
        p.setBorder(new EmptyBorder(30,20,20,20));

        JLabel av = new JLabel("👤", JLabel.CENTER);
        av.setFont(new Font("Sans", Font.PLAIN, 80));
        av.setAlignmentX(0.5f);

        lblUserProfile = new JLabel("User: -");
        lblUserProfile.setAlignmentX(0.5f);

        JButton b2 = createMenuButton("📜 Riwayat Pesanan", COLOR_WHITE, Color.BLACK);
        b2.addActionListener(e -> {
            refreshHistoryUI();
            profileCard.show(profileContainer, "HAL_RIWAYAT");
        });

        p.add(av);
        p.add(lblUserProfile);
        p.add(Box.createVerticalStrut(20));
        p.add(b2);

        return p;
    }

    private JPanel createHistoryPage() {
        JPanel p = new JPanel(new BorderLayout());

        JButton b = new JButton("← Kembali");
        b.addActionListener(e -> profileCard.show(profileContainer, "MENU_UTAMA"));

        historyList = new JPanel();
        historyList.setLayout(new BoxLayout(historyList, BoxLayout.Y_AXIS));
        historyList.setBackground(COLOR_BG);

        p.add(b, BorderLayout.NORTH);
        p.add(new JScrollPane(historyList), BorderLayout.CENTER);

        return p;
    }

    private JPanel createHistoryCard(HistoryItem item) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,220,220)),
            new EmptyBorder(10,10,10,10)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Gambar
        ImageIcon icon = new ImageIcon(item.imagePath);
        Image img = icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
        JLabel imgLabel = new JLabel(new ImageIcon(img));

        // Info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(Color.WHITE);

        JLabel nama = new JLabel(item.nama);
        nama.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel harga = new JLabel("Rp " + item.harga);
        JLabel qty = new JLabel("Qty: " + item.qty);

        JLabel meta = new JLabel(item.tanggal + " | " + item.metode);
        meta.setFont(new Font("SansSerif", Font.PLAIN, 11));
        meta.setForeground(Color.GRAY);

        info.add(nama);
        info.add(harga);
        info.add(qty);
        info.add(Box.createVerticalStrut(5));
        info.add(meta);

        card.add(imgLabel, BorderLayout.WEST);
        card.add(info, BorderLayout.CENTER);

        return card;
    }

    private void refreshHistoryUI() {
        historyList.removeAll();

        if (history.isEmpty()) {
            historyList.add(new JLabel("Belum ada riwayat pembelian."));
        } else {
            for (HistoryItem item : history) {
                historyList.add(createHistoryCard(item));
                historyList.add(Box.createVerticalStrut(10));
            }
        }

        historyList.revalidate();
        historyList.repaint();
    }

    private JButton createMenuButton(String t, Color bg, Color fg) {
        JButton b = new JButton(t); b.setMaximumSize(new Dimension(350, 50)); b.setBackground(bg); b.setForeground(fg); b.setAlignmentX(0.5f); return b;
    }

    class RoundedPanel extends JPanel {
        private int r; Color c; public RoundedPanel(int r, Color c){this.r=r;this.c=c;setOpaque(false);}
        protected void paintComponent(Graphics g){ Graphics2D g2=(Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(c); g2.fillRoundRect(0,0,getWidth(),getHeight(),r,r); }
    }

    public static void main(String[] args) { 
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception e) {}
        new FullEcommerceApps(); 
    }
}