import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI; // IMPORT PENTING UNTUK MEMPERBAIKI WARNA

public class FullEcommerceApps extends JFrame {
    
    // =================================================================================
    // 1. KONFIGURASI DAN VARIABEL
    // =================================================================================

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    
    private CardLayout profileCard = new CardLayout();
    private JPanel profileContainer = new JPanel(profileCard);
    private JPanel gridDash, gridSale, cartPanel, paymentPanel;
    private JPanel historyList;

    private String currentUser;
    private JLabel lblUserProfile;
    
    private Map<String, CartItem> cartMap = new LinkedHashMap<>();
    private java.util.List<HistoryItem> history = new ArrayList<>();
    
    private String userPhone = "0812-3456-7890";
    private String userAddress = "Jl. Fashion No. 123, Jakarta Selatan, DKI Jakarta";
    
    private final String IP = "localhost";
    private final int PORT = 5000;
    private PrintWriter out;

    // --- WARNA ---
    private final Color COLOR_RED = new Color(214, 48, 49);
    private final Color COLOR_WHITE = Color.WHITE;
    private final Color COLOR_BG = new Color(245, 246, 250);
    private final Color COLOR_GREEN = new Color(46, 204, 113); // Warna tombol default (Hijau)

    // --- VARIABEL LOGIN (UAS) ---
    private boolean isSellerMode = false; 
    private JTextField tLoginEmail;
    private JPasswordField tLoginPass;
    private JTextField tSignEmail, tSignUser, tSignAddr;
    private JPasswordField tSignPass;
    private JLabel lblSignUser, lblSignAddr;

    private final Color COL_PRIMARY = new Color(229, 57, 53);    
    private final Color COL_ORANGE  = new Color(243, 156, 18);   
    private final Color COL_BLUE    = new Color(66, 133, 244);   
    private final Color COL_TEXT    = new Color(33, 37, 41);
    
    private final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 32);
    private final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 15);
    private final Font FONT_INPUT = new Font("SansSerif", Font.PLAIN, 14);
    private final Font FONT_BTN   = new Font("SansSerif", Font.BOLD, 16);

    // --- CLASS DATA ---
    class CartItem {
        int qty, price, maxStock; // Tambahkan maxStock
        String imgName;
        boolean selected = true;
        CartItem(int q, int p, String img, int max) { 
            this.qty = q; 
            this.price = p; 
            this.imgName = img; 
            this.maxStock = max; // Inisialisasi maxStock
        }
    }

    class HistoryItem {
        String nama, imagePath, tanggal, metode;
        int harga, qty;
        HistoryItem(String n, String i, int h, int q, String t, String m) {
            this.nama=n; this.imagePath=i; this.harga=h; this.qty=q; this.tanggal=t; this.metode=m;
        }
    }

    public FullEcommerceApps() {
        initUI();
    }

    private void initUI() {
        setTitle("FashionHub");
        setSize(420, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        // LOGIN (UAS)
        mainPanel.add(createLoginFlowPanel(), "LOGIN_FLOW");

        // DASHBOARD (TUBES)
        gridDash = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
        gridDash.setBackground(COLOR_BG);
        gridDash.setBorder(new EmptyBorder(0, 0, 100, 0));
        gridSale = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 15));
        gridSale.setBackground(COLOR_BG);
        gridSale.setBorder(new EmptyBorder(0, 0, 100, 0));
        
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
        cardLayout.show(mainPanel, "LOGIN_FLOW");
        setVisible(true);
    }

    // =================================================================================
    // 2. BAGIAN GUI LOGIN (UAS)
    // =================================================================================

    private JPanel createLoginFlowPanel() {
        CardLayout flow = new CardLayout();
        JPanel p = new JPanel(flow);
        p.setBackground(Color.WHITE);

        p.add(createWelcomePanel(flow, p), "WELCOME");
        p.add(createLoginFormPanel(flow, p), "LOGIN_FORM");
        p.add(createSignupPanel(flow, p), "SIGNUP_FORM");

        flow.show(p, "WELCOME");
        return p;
    }

    private void doLoginSuccess() {
        if(lblUserProfile != null) lblUserProfile.setText("User: " + currentUser);
        connect(); 
        cardLayout.show(mainPanel, "DASHBOARD");
    }

    private JPanel createWelcomePanel(CardLayout layout, JPanel container) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 40, 0, 40);

        JLabel logo = new JLabel("FashionHub");
        logo.setFont(new Font("SansSerif", Font.BOLD, 42));
        logo.setForeground(COL_PRIMARY);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.weighty = 1.0; gbc.anchor = GridBagConstraints.CENTER;
        p.add(logo, gbc);

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        btnPanel.setBackground(Color.WHITE);
        
        JButton btnStart = createRoundedButton("Get Started", COL_ORANGE, Color.WHITE);
        JButton btnLogin = createRoundedButton("Login", COL_PRIMARY, Color.WHITE);

        btnStart.addActionListener(e -> layout.show(container, "SIGNUP_FORM"));
        btnLogin.addActionListener(e -> layout.show(container, "LOGIN_FORM"));

        btnPanel.add(btnStart);
        btnPanel.add(btnLogin);

        gbc.gridy = 1; gbc.weighty = 0.2; gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(0, 40, 60, 40);
        p.add(btnPanel, gbc);

        return p;
    }

    private JPanel createSignupPanel(CardLayout layout, JPanel container) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int margin = 35; 

        JLabel title = new JLabel("Signup");
        title.setFont(FONT_TITLE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        
        gbc.gridx = 0; gbc.gridy = 0; 
        gbc.insets = new Insets(40, margin, 20, margin);
        p.add(title, gbc);

        JPanel togglePanel = new JPanel(new GridLayout(1, 2, 0, 0));
        togglePanel.setPreferredSize(new Dimension(300, 40));
        
        JButton btnCust = createToggleButton("Customer", true);
        JButton btnSell = createToggleButton("Seller", false);

        togglePanel.add(btnCust);
        togglePanel.add(btnSell);

        gbc.gridy = 1; 
        gbc.insets = new Insets(0, margin + 10, 20, margin + 10);
        p.add(togglePanel, gbc);

        gbc.insets = new Insets(5, margin, 5, margin);
        gbc.gridy = 2; p.add(createLabel("Email"), gbc);
        gbc.gridy = 3; 
        tSignEmail = createStyledTextField("Enter email");
        gbc.insets = new Insets(0, margin, 15, margin);
        p.add(tSignEmail, gbc);

        lblSignUser = createLabel("Nama Toko");
        tSignUser = createStyledTextField("Enter Username");
        
        gbc.insets = new Insets(5, margin, 5, margin);
        gbc.gridy = 4; p.add(lblSignUser, gbc);
        gbc.gridy = 5; 
        gbc.insets = new Insets(0, margin, 15, margin);
        p.add(tSignUser, gbc);

        gbc.insets = new Insets(5, margin, 5, margin);
        gbc.gridy = 6; p.add(createLabel("Password"), gbc);
        gbc.gridy = 7; 
        tSignPass = createStyledPasswordField("Enter password");
        gbc.insets = new Insets(0, margin, 15, margin);
        p.add(tSignPass, gbc);

        lblSignAddr = createLabel("Alamat");
        tSignAddr = createStyledTextField("Enter address");

        gbc.insets = new Insets(5, margin, 5, margin);
        gbc.gridy = 8; p.add(lblSignAddr, gbc);
        gbc.gridy = 9; 
        gbc.insets = new Insets(0, margin, 30, margin);
        p.add(tSignAddr, gbc);

        btnCust.addActionListener(e -> { 
            isSellerMode = false;
            updateToggleVisual(btnCust, btnSell, true); 
            toggleSellerFields(false); 
            p.revalidate(); p.repaint();
        });

        btnSell.addActionListener(e -> { 
            isSellerMode = true;
            updateToggleVisual(btnCust, btnSell, false); 
            toggleSellerFields(true); 
            p.revalidate(); p.repaint();
        });

        toggleSellerFields(false);

        JButton btnSignup = createRoundedButton("Singup", COL_PRIMARY, Color.WHITE);
        btnSignup.addActionListener(e -> {
            boolean emailOk = !tSignEmail.getText().equals("Enter email") && !tSignEmail.getText().isEmpty();
            boolean passOk = tSignPass.getPassword().length > 0;

            if (isSellerMode) {
                String u = tSignUser.getText();
                String addr = tSignAddr.getText();
                if(emailOk && passOk && !u.equals("Enter Username") && !addr.equals("Enter address")) {
                    currentUser = u;
                    userAddress = addr;
                    doLoginSuccess();
                } else {
                    JOptionPane.showMessageDialog(this, "Seller wajib lengkapi semua data!");
                }
            } else {
                if(emailOk && passOk) {
                    currentUser = tSignEmail.getText().split("@")[0];
                    doLoginSuccess();
                } else {
                    JOptionPane.showMessageDialog(this, "Email & Password harus diisi!");
                }
            }
        });

        gbc.gridy = 10;
        gbc.insets = new Insets(0, margin, 20, margin);
        p.add(btnSignup, gbc);

        p.add(createFooterLink("Sudah punya akun?", "Login", e -> layout.show(container, "LOGIN_FORM")), 
              createFooterConstraints(11));

        return p;
    }

    private void toggleSellerFields(boolean show) {
        lblSignUser.setVisible(show);
        tSignUser.setVisible(show);
        lblSignAddr.setVisible(show);
        tSignAddr.setVisible(show);
    }

    private JPanel createLoginFormPanel(CardLayout layout, JPanel container) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int margin = 35;

        JLabel title = new JLabel("Log in");
        title.setFont(FONT_TITLE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        
        gbc.gridx = 0; gbc.gridy = 0; 
        gbc.insets = new Insets(60, margin, 40, margin);
        p.add(title, gbc);

        gbc.insets = new Insets(5, margin, 5, margin);
        gbc.gridy = 1; p.add(createLabel("Email"), gbc);
        gbc.gridy = 2; 
        tLoginEmail = createStyledTextField("Your email");
        gbc.insets = new Insets(0, margin, 20, margin);
        p.add(tLoginEmail, gbc);

        gbc.insets = new Insets(5, margin, 5, margin);
        gbc.gridy = 3; p.add(createLabel("Password"), gbc);
        gbc.gridy = 4; 
        tLoginPass = createStyledPasswordField("Your password");
        gbc.insets = new Insets(0, margin, 40, margin);
        p.add(tLoginPass, gbc);

        JButton btnLogin = createRoundedButton("Login", COL_PRIMARY, Color.WHITE);
        btnLogin.addActionListener(e -> {
            String email = tLoginEmail.getText();
            if(email != null && !email.contains("Your") && !email.isEmpty()) {
                currentUser = email;
                doLoginSuccess();
            } else {
                JOptionPane.showMessageDialog(this, "Isi email valid!");
            }
        });
        
        gbc.gridy = 5; 
        gbc.insets = new Insets(0, margin, 20, margin);
        p.add(btnLogin, gbc);

        p.add(createFooterLink("Belum punya akun?", "Sign Up", e -> layout.show(container, "SIGNUP_FORM")), 
              createFooterConstraints(6));

        GridBagConstraints spacer = new GridBagConstraints();
        spacer.gridy = 10; spacer.weighty = 1.0;
        p.add(Box.createVerticalGlue(), spacer);

        return p;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(FONT_LABEL); l.setForeground(COL_TEXT); return l;
    }

    private JButton createToggleButton(String text, boolean isActive) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createLineBorder(COL_PRIMARY, 1));
        b.setPreferredSize(new Dimension(0, 40));
        updateToggleColor(b, isActive);
        return b;
    }

    private void updateToggleVisual(JButton btnCust, JButton btnSell, boolean isCustActive) {
        updateToggleColor(btnCust, isCustActive);
        updateToggleColor(btnSell, !isCustActive);
    }

    private void updateToggleColor(JButton b, boolean isActive) {
        if(isActive) { b.setBackground(COL_PRIMARY); b.setForeground(Color.WHITE); b.setOpaque(true); } 
        else { b.setBackground(Color.WHITE); b.setForeground(COL_PRIMARY); b.setOpaque(true); }
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField tf = new JTextField(placeholder); styleField(tf, placeholder); return tf;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField tf = new JPasswordField(placeholder); tf.setEchoChar((char)0); styleField(tf, placeholder);
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if(new String(tf.getPassword()).equals(placeholder)) { tf.setText(""); tf.setForeground(Color.BLACK); tf.setEchoChar('•'); } }
            public void focusLost(FocusEvent e) { if(tf.getPassword().length == 0) { tf.setText(placeholder); tf.setForeground(Color.GRAY); tf.setEchoChar((char)0); } }
        });
        return tf;
    }

    private void styleField(JTextField tf, String ph) {
        tf.setFont(FONT_INPUT); tf.setForeground(Color.GRAY); tf.setPreferredSize(new Dimension(0, 45));
        tf.setBorder(BorderFactory.createCompoundBorder(new RoundedBorder(15, COL_BLUE), BorderFactory.createEmptyBorder(0, 15, 0, 15)));
        if(!(tf instanceof JPasswordField)) {
            tf.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { if(tf.getText().equals(ph)) { tf.setText(""); tf.setForeground(Color.BLACK); } }
                public void focusLost(FocusEvent e) { if(tf.getText().isEmpty()) { tf.setText(ph); tf.setForeground(Color.GRAY); } }
            });
        }
    }

    private JButton createRoundedButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40); g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(FONT_BTN); b.setForeground(fg); b.setFocusPainted(false); b.setBorderPainted(false);
        b.setContentAreaFilled(false); b.setPreferredSize(new Dimension(300, 50)); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JPanel createFooterLink(String normalText, String linkText, ActionListener action) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0)); p.setBackground(Color.WHITE);
        JLabel l1 = new JLabel(normalText); l1.setFont(new Font("SansSerif", Font.PLAIN, 14)); l1.setForeground(COL_BLUE); 
        JLabel l2 = new JLabel(linkText); l2.setFont(new Font("SansSerif", Font.BOLD, 14)); l2.setForeground(COL_PRIMARY); l2.setCursor(new Cursor(Cursor.HAND_CURSOR));
        l2.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { action.actionPerformed(null); } });
        p.add(l1); p.add(l2); return p;
    }

    private GridBagConstraints createFooterConstraints(int y) {
        GridBagConstraints gbc = new GridBagConstraints(); gbc.gridx = 0; gbc.gridy = y; gbc.insets = new Insets(0, 0, 30, 0); return gbc;
    }

    // =================================================================================
    // 3. LOGIKA UTAMA (TUBES)
    // =================================================================================

    private JPanel createCard(String imgName, String name, String priceStr, int stock) {
        int priceInt = Integer.parseInt(priceStr.replace(".", ""));
        JPanel c = new JPanel(new BorderLayout());
        c.setPreferredSize(new Dimension(175, 300)); // Tinggi disesuaikan
        c.setBackground(Color.WHITE);
        c.setBorder(new LineBorder(new Color(230, 230, 230), 1, true));

        JLabel lblImg = new JLabel(); // Variabel ini sekarang sudah didefinisikan
        lblImg.setHorizontalAlignment(JLabel.CENTER);
        try {
            ImageIcon icon = new ImageIcon("images/" + imgName);
            Image img = icon.getImage().getScaledInstance(150, 130, Image.SCALE_SMOOTH);
            lblImg.setIcon(new ImageIcon(img));
        } catch (Exception e) { lblImg.setText("No Image"); }

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(Color.WHITE);
        
        // Menampilkan Nama, Harga, dan Stok
        JLabel lblInfo = new JLabel("<html><center>" + name + "<br><b color='red'>Rp " + priceStr + 
                                    "</b><br><small>Stok: " + stock + "</small></center></html>", JLabel.CENTER);
        
        JButton btn = new JButton(stock > 0 ? "TAMBAH" : "HABIS");
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);

        if (stock <= 0) {
            btn.setBackground(Color.GRAY);
            btn.setForeground(Color.WHITE); // Sekarang akan terlihat karena tombol tetap "Enabled"
            btn.setEnabled(true); // Biarkan true agar warna teks tidak dipaksa pudar oleh sistem
        } else {
            btn.setBackground(COLOR_RED);
            btn.setForeground(Color.WHITE);
            btn.setEnabled(true);
        }

        btn.addActionListener(e -> {
            // Validasi stok di dalam sini
            if (stock <= 0) {
                JOptionPane.showMessageDialog(this, "Maaf, stok barang ini sudah habis!");
                return; // Keluar dari fungsi, tidak masuk ke keranjang
            }

            int qtyInCart = cartMap.containsKey(name) ? cartMap.get(name).qty : 0;
            if (qtyInCart < stock) {
                if(cartMap.containsKey(name)) cartMap.get(name).qty++;
                else cartMap.put(name, new CartItem(1, priceInt, imgName, stock));
                JOptionPane.showMessageDialog(this, name + " masuk keranjang!");
            } else {
                JOptionPane.showMessageDialog(this, "Stok tidak mencukupi!");
            }
        });

        bottom.add(lblInfo, BorderLayout.CENTER); 
        bottom.add(btn, BorderLayout.SOUTH);
        c.add(lblImg, BorderLayout.CENTER); 
        c.add(bottom, BorderLayout.SOUTH);
        return c;
    }

    // 2. PERBAIKAN METHOD createHomeCard (Menyesuaikan argumen)
    private JPanel createHomeCard(String imgName, String name, String priceStr, boolean isSale, int stock) {
        JPanel card = createCard(imgName, name, priceStr, stock); // Sekarang mengirim 4 argumen

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
                btnPlus.addActionListener(e -> { 
                    if(item.qty < item.maxStock) {
                        item.qty++; 
                        updateCartUI(); 
                    } else {
                        JOptionPane.showMessageDialog(this, "Batas stok tercapai!");
                    }
                });
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
            
            // --- PERBAIKAN TOMBOL CHECKOUT (HAPUS OVERLAY) ---
            JButton btnPay = new JButton("Checkout");
            // INI BARIS AJAIBNYA: Menghapus style Windows yang bikin tombol jadi putih
            btnPay.setUI(new BasicButtonUI()); 
            
            btnPay.setPreferredSize(new Dimension(100, 30));
            btnPay.setBackground(COLOR_GREEN); // Default Hijau
            btnPay.setForeground(COLOR_WHITE);
            btnPay.setFocusPainted(false); 
            
            btnPay.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btnPay.setBackground(COLOR_RED); } // Hover Merah
                public void mouseExited(MouseEvent e) { btnPay.setBackground(COLOR_GREEN); } // Balik Hijau
            });

            final long fTotal = totalAll;
            btnPay.addActionListener(e -> { if(fTotal > 0) showPaymentPage(fTotal); });

            footer.add(btnPay, BorderLayout.EAST);
            cartPanel.add(Box.createVerticalGlue());
            cartPanel.add(footer);
        }
        cartPanel.revalidate(); cartPanel.repaint();
    }

    private void showPaymentPage(long total) {
        paymentPanel.removeAll();
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(COLOR_BG);
        container.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. HEADER ALAMAT
        JPanel addrBox = new JPanel();
        addrBox.setLayout(new BoxLayout(addrBox, BoxLayout.Y_AXIS));
        addrBox.setBackground(COLOR_WHITE);
        addrBox.setBorder(new CompoundBorder(new LineBorder(new Color(220,220,220)), new EmptyBorder(15,15,15,15)));
        addrBox.setMaximumSize(new Dimension(380, 130)); 
        addrBox.setAlignmentX(Component.CENTER_ALIGNMENT); 

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

        // 3. OPSI PEMBAYARAN
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

        // --- PERBAIKAN TOMBOL BAYAR SEKARANG (HAPUS OVERLAY) ---
        JButton btnFinal = new JButton("BAYAR SEKARANG");
        // INI BARIS AJAIBNYA: Menghapus style Windows yang bikin tombol jadi putih
        btnFinal.setUI(new BasicButtonUI());
        
        btnFinal.setBackground(COLOR_GREEN); // Default Hijau
        btnFinal.setForeground(COLOR_WHITE);
        btnFinal.setPreferredSize(new Dimension(160, 40));
        btnFinal.setFocusPainted(false);
        
        btnFinal.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnFinal.setBackground(COLOR_RED); } // Hover Merah
            public void mouseExited(MouseEvent e) { btnFinal.setBackground(COLOR_GREEN); } // Balik Hijau
        });

        btnFinal.addActionListener(e -> {
            String method = rb1.isSelected() ? "Bank" : rb2.isSelected() ? "E-Wallet" : "COD";
            String nota = "";
            for(String k : cartMap.keySet()) if(cartMap.get(k).selected) nota += k + " (x" + cartMap.get(k).qty + "), ";
            
            if(out != null) out.println("ORDER:" + currentUser + " | Metode: " + method + " | Nota: " + nota);
            
            String tanggal = new SimpleDateFormat("dd MMM").format(new Date());
            for (String k : cartMap.keySet()) {
                CartItem item = cartMap.get(k);
                if (item.selected) {
                    history.add(new HistoryItem(k, "images/" + item.imgName, item.price, item.qty, tanggal, method));
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

    // 3. PERBAIKAN METHOD parseProducts (Mengambil data stok dari server)
    private void parseProducts(String raw) {
        gridDash.removeAll(); 
        gridSale.removeAll();
        for(String item : raw.split(";")) {
            String[] d = item.split(",");
            if(d.length < 5) continue;

            int stock = Integer.parseInt(d[2]); // Kolom indeks 2 adalah stok
            boolean isSale = d[4].equals("SALE");

            JPanel card = createHomeCard(
                d[0], // image
                d[1], // nama
                d[3], // harga
                isSale,
                stock // Sekarang mengirim stok ke card
            );

            gridDash.add(card);
            if (isSale) gridSale.add(card);
        }
        gridDash.add(Box.createVerticalStrut(80)); 
        gridSale.add(Box.createVerticalStrut(80));

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
        b2.addActionListener(e -> { refreshHistoryUI(); profileCard.show(profileContainer, "HAL_RIWAYAT"); });

        p.add(av); p.add(lblUserProfile); p.add(Box.createVerticalStrut(20)); p.add(b2);
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
            BorderFactory.createLineBorder(new Color(220,220,220)), new EmptyBorder(10,10,10,10)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        ImageIcon icon = new ImageIcon(item.imagePath);
        Image img = icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
        JLabel imgLabel = new JLabel(new ImageIcon(img));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(Color.WHITE);

        JLabel nama = new JLabel(item.nama);
        nama.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel harga = new JLabel("Rp " + item.harga);
        JLabel qty = new JLabel("Qty: " + item.qty);
        JLabel meta = new JLabel(item.tanggal + " | " + item.metode);
        meta.setFont(new Font("SansSerif", Font.PLAIN, 11)); meta.setForeground(Color.GRAY);

        info.add(nama); info.add(harga); info.add(qty); info.add(Box.createVerticalStrut(5)); info.add(meta);
        card.add(imgLabel, BorderLayout.WEST); card.add(info, BorderLayout.CENTER);
        return card;
    }

    private void refreshHistoryUI() {
        historyList.removeAll();
        if (history.isEmpty()) historyList.add(new JLabel("Belum ada riwayat pembelian."));
        else {
            for (HistoryItem item : history) {
                historyList.add(createHistoryCard(item));
                historyList.add(Box.createVerticalStrut(10));
            }
        }
        historyList.revalidate(); historyList.repaint();
    }

    private JButton createMenuButton(String t, Color bg, Color fg) {
        JButton b = new JButton(t); b.setMaximumSize(new Dimension(350, 50)); b.setBackground(bg); b.setForeground(fg); b.setAlignmentX(0.5f); return b;
    }

    private static class RoundedBorder implements Border {
        private int r; private Color c;
        RoundedBorder(int r, Color c) { this.r = r; this.c = c; }
        public Insets getBorderInsets(Component cmp) { return new Insets(1, 1, 1, 1); }
        public boolean isBorderOpaque() { return true; }
        public void paintBorder(Component cmp, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c); g2.setStroke(new BasicStroke(1)); g2.drawRoundRect(x, y, w-1, h-1, r, r); g2.dispose();
        }
    }

    public static void main(String[] args) { 
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception e) {}
        new FullEcommerceApps(); 
    }
}