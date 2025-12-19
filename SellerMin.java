import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

public class SellerMin {
    private JFrame frame;
    private CardLayout cardLayout;
    private JPanel mainContent, gridPanel;
    private DefaultTableModel tableModel;
    private JTextArea logArea;

    // --- KONFIGURASI FILE & GAMBAR ---
    private final String FILE_STOK = "stok_barang.txt";
    private final String IMAGE_DIR = "images/";

    // --- WARNA & STYLE ---
    private final Color COL_PRIMARY = new Color(229, 57, 53);    
    private final Color COL_BLUE    = new Color(66, 133, 244);   
    private final Color COL_TEXT    = new Color(33, 37, 41);     
    private final Color COL_BG      = new Color(248, 249, 250);  
    private final Color COL_WHITE   = Color.WHITE;

    private final Font FONT_LABEL = new Font("SansSerif", Font.BOLD, 15);
    private final Font FONT_INPUT = new Font("SansSerif", Font.PLAIN, 14);
    private final Font FONT_BTN   = new Font("SansSerif", Font.BOLD, 16);

    // --- VARIABEL FORM ---
    private JTextField tNama, tStok, tHarga, tDesc;
    
    // [BARU] Field untuk input durasi timer Flash Sale
    private JTextField tDurasi; 
    
    private JLabel lblFile;
    private JCheckBox cSale;
    private String currentFileName = "default.jpg";
    private int editingRowIndex = -1;

    public SellerMin() {
        // Buat folder gambar jika belum ada
        new File(IMAGE_DIR).mkdirs();
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception e) {}
        
        showLogin();
    }

    // ========================================================================
    // 1. HALAMAN LOGIN
    // ========================================================================
    
    private void showLogin() {
        JFrame loginFrame = new JFrame("Admin Login - FashionHub");
        loginFrame.setSize(420, 650);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.getContentPane().setBackground(COL_WHITE);
        loginFrame.setLayout(new GridBagLayout());
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int margin = 35;

        JLabel logo = new JLabel("FashionHub");
        logo.setFont(new Font("SansSerif", Font.BOLD, 40));
        logo.setForeground(COL_PRIMARY);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        
        gbc.gridx = 0; gbc.gridy = 0; 
        gbc.insets = new Insets(0, margin, 10, margin);
        loginFrame.add(logo, gbc);

        JLabel lblUser = createLabel("Username");
        JTextField tUser = createStyledTextField("Enter username");
        gbc.gridy = 2; gbc.insets = new Insets(5, margin, 5, margin);
        loginFrame.add(lblUser, gbc);
        gbc.gridy = 3; gbc.insets = new Insets(0, margin, 20, margin);
        loginFrame.add(tUser, gbc);

        JLabel lblPass = createLabel("Password");
        JPasswordField tPass = createStyledPasswordField("Enter password");
        gbc.gridy = 4; gbc.insets = new Insets(5, margin, 5, margin);
        loginFrame.add(lblPass, gbc);
        gbc.gridy = 5; gbc.insets = new Insets(0, margin, 40, margin);
        loginFrame.add(tPass, gbc);

        JButton btnLogin = createRoundedButton("Login", COL_PRIMARY, COL_WHITE);
        btnLogin.addActionListener(e -> {
            if(!tUser.getText().equals("Enter username")) {
                loginFrame.dispose();
                initMainDashboard();
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Isi username & password!");
            }
        });

        gbc.gridy = 6; gbc.insets = new Insets(0, margin, 20, margin);
        loginFrame.add(btnLogin, gbc);
        loginFrame.setVisible(true);
    }

    // ========================================================================
    // 2. DASHBOARD UTAMA
    // ========================================================================

    private void initMainDashboard() {
        frame = new JFrame("Seller Center - Dashboard");
        frame.setSize(450, 800);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);

        // A. Panel Dashboard (Grid Produk)
        gridPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        gridPanel.setBackground(COL_BG);
        gridPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel gridContainer = new JPanel(new BorderLayout());
        gridContainer.add(gridPanel, BorderLayout.NORTH);
        gridContainer.setBackground(COL_BG);
        mainContent.add(new JScrollPane(gridContainer), "DASHBOARD");

        // B. Panel Tambah Produk
        mainContent.add(createAddPage(), "TAMBAH");

        // C. Panel Notifikasi (Log)
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(FONT_INPUT);
        logArea.setBorder(new EmptyBorder(10,10,10,10));
        mainContent.add(new JScrollPane(logArea), "NOTIF");

        // D. Panel Order
        mainContent.add(createOrderPage(), "ORDER");

        // Bottom Navigation
        JPanel nav = new JPanel(new GridLayout(1, 4));
        nav.setBackground(COL_PRIMARY);
        nav.setPreferredSize(new Dimension(0, 60));
        
        String[] icons = {"🏠", "➕", "🔔", "🛒"};
        String[] pages = {"DASHBOARD", "TAMBAH", "NOTIF", "ORDER"};

        for (int i = 0; i < icons.length; i++) {
            final String p = pages[i];
            JButton b = new JButton(icons[i]);
            b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
            b.setForeground(COL_WHITE); 
            b.setContentAreaFilled(false); 
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.addActionListener(e -> {
                if (p.equals("DASHBOARD")) updateProductGrid();
                if (p.equals("TAMBAH")) clearForm();
                cardLayout.show(mainContent, p);
            });
            nav.add(b);
        }

        frame.add(mainContent, BorderLayout.CENTER);
        frame.add(nav, BorderLayout.SOUTH);

        // Load Data Awal
        tableModel = new DefaultTableModel(new String[]{"Foto", "Nama", "Stok", "Harga", "Sale", "Desc"}, 0);
        loadData();
        updateProductGrid();
        
        frame.setVisible(true);
    }

    private void updateProductGrid() {
        gridPanel.removeAll();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            final int index = i;
            String img = (String) tableModel.getValueAt(i, 0);
            String name = (String) tableModel.getValueAt(i, 1);
            String price = (String) tableModel.getValueAt(i, 3);

            JPanel card = new JPanel(new BorderLayout());
            card.setBorder(new LineBorder(new Color(230, 230, 230), 1, true));
            card.setBackground(COL_WHITE);

            JLabel imgLbl = new JLabel();
            try {
                ImageIcon ic = new ImageIcon(IMAGE_DIR + img);
                imgLbl.setIcon(new ImageIcon(ic.getImage().getScaledInstance(140, 110, Image.SCALE_SMOOTH)));
                imgLbl.setHorizontalAlignment(JLabel.CENTER);
            } catch (Exception e) { imgLbl.setText("No Img"); }
            imgLbl.setBorder(new EmptyBorder(10, 5, 5, 5));

            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBackground(COL_WHITE);
            bottomPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setBackground(COL_WHITE);
            JLabel lblName = new JLabel(name);
            lblName.setFont(new Font("SansSerif", Font.BOLD, 12));
            JLabel lblPrice = new JLabel("Rp " + price);
            lblPrice.setForeground(COL_PRIMARY);
            lblPrice.setFont(new Font("SansSerif", Font.BOLD, 12));
            textPanel.add(lblName);
            textPanel.add(lblPrice);

            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            actionPanel.setBackground(COL_WHITE);

            JButton btnEdit = createCircleButton("✎", COL_PRIMARY);
            btnEdit.addActionListener(e -> prepareEdit(index));

            JButton btnDel = createCircleButton("✕", Color.GRAY);
            btnDel.addActionListener(e -> {
                if(JOptionPane.showConfirmDialog(frame, "Hapus produk ini?") == 0) {
                    tableModel.removeRow(index);
                    simpan();
                    updateProductGrid();
                    SellerVer.broadcastUpdate();
                }
            });

            actionPanel.add(btnEdit);
            actionPanel.add(btnDel);

            bottomPanel.add(textPanel, BorderLayout.CENTER);
            bottomPanel.add(actionPanel, BorderLayout.SOUTH);

            card.add(imgLbl, BorderLayout.CENTER);
            card.add(bottomPanel, BorderLayout.SOUTH);
            gridPanel.add(card);
        }
        gridPanel.revalidate(); gridPanel.repaint();
    }

    private JButton createCircleButton(String text, Color bg) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillOval(0, 0, getWidth()-1, getHeight()-1);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), (getWidth()-fm.stringWidth(getText()))/2, (getHeight()/2)+5);
                g2.dispose();
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setPreferredSize(new Dimension(30, 30));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ========================================================================
    // 3. HALAMAN TAMBAH PRODUK (DENGAN LOGIKA TIMER)
    // ========================================================================

    private JPanel createAddPage() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Tambah Produk");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(10, 25, 80));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(20));

        // Upload Area
        JLabel lblUploadTitle = new JLabel("Upload Foto");
        lblUploadTitle.setFont(FONT_LABEL);
        lblUploadTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lblUploadTitle);
        p.add(Box.createVerticalStrut(10));

        JPanel uploadArea = new JPanel(new BorderLayout());
        uploadArea.setBackground(new Color(245, 245, 245));
        uploadArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        uploadArea.setPreferredSize(new Dimension(300, 120));
        uploadArea.setBorder(BorderFactory.createDashedBorder(Color.LIGHT_GRAY, 2, 2));
        uploadArea.setCursor(new Cursor(Cursor.HAND_CURSOR));

        lblFile = new JLabel("<html><center>📸<br>Klik untuk pilih foto</center></html>", SwingConstants.CENTER);
        lblFile.setForeground(Color.GRAY);
        uploadArea.add(lblFile, BorderLayout.CENTER);

        uploadArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fc = new JFileChooser();
                if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    currentFileName = f.getName();
                    lblFile.setText("<html><center>✅ " + currentFileName + "</center></html>");
                    try { 
                        Files.copy(f.toPath(), Paths.get(IMAGE_DIR + currentFileName), StandardCopyOption.REPLACE_EXISTING); 
                    } catch (Exception ex) {}
                }
            }
        });
        
        uploadArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(uploadArea);
        p.add(Box.createVerticalStrut(20));

        // Input Fields
        tNama = createStyledTextField("");
        addFormGroup(p, "Nama Produk", tNama);

        tStok = createStyledTextField("0");
        addFormGroup(p, "Stok", tStok);

        tHarga = createStyledTextField("0");
        addFormGroup(p, "Harga", tHarga);

        tDesc = createStyledTextField("Deskripsi singkat..."); 
        addFormGroup(p, "Deskripsi", tDesc);

        // --- PANEL FLASH SALE (INPUT DURASI) ---
        // Ini adalah bagian logika baru agar admin bisa input menit timer
        JPanel salePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        salePanel.setBackground(Color.WHITE);
        salePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        cSale = new JCheckBox("Aktifkan Flash Sale");
        cSale.setBackground(Color.WHITE);
        cSale.setFont(FONT_INPUT);

        // Input Durasi (Hidden by default, muncul kalau checkbox dicentang)
        tDurasi = createStyledTextField("60"); // Default 60 menit
        tDurasi.setPreferredSize(new Dimension(80, 45));
        tDurasi.setVisible(false); 
        
        JLabel lblMenit = new JLabel(" Menit");
        lblMenit.setFont(FONT_INPUT);
        lblMenit.setVisible(false);

        // Listener: Toggle visibility input menit
        cSale.addActionListener(e -> {
            boolean aktif = cSale.isSelected();
            tDurasi.setVisible(aktif);
            lblMenit.setVisible(aktif);
            salePanel.revalidate(); 
        });

        salePanel.add(cSale);
        salePanel.add(Box.createHorizontalStrut(10));
        salePanel.add(tDurasi);
        salePanel.add(lblMenit);
        
        p.add(salePanel);
        p.add(Box.createVerticalStrut(20));

        // Tombol Simpan
        JButton save = new JButton("Simpan Produk");
        save.setFont(FONT_BTN);
        save.setBackground(Color.WHITE); 
        save.setForeground(Color.BLACK);
        save.setFocusPainted(false);
        save.setCursor(new Cursor(Cursor.HAND_CURSOR));
        save.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); 
        save.setBorder(BorderFactory.createCompoundBorder(
             new LineBorder(Color.BLACK, 1, true),
             new EmptyBorder(10, 10, 10, 10)
        ));

        save.addActionListener(e -> {
            // 1. Simpan Data Produk ke Tabel & File
            Object[] row = {currentFileName, tNama.getText(), tStok.getText(), tHarga.getText(), cSale.isSelected() ? "SALE" : "-", tDesc.getText()};
            if (editingRowIndex == -1) tableModel.addRow(row);
            else for(int i=0; i<6; i++) tableModel.setValueAt(row[i], editingRowIndex, i);
            
            simpan();
            updateProductGrid();
            SellerVer.broadcastUpdate();
            
            // 2. LOGIKA KIRIM TIMER KE SERVER
            if (cSale.isSelected()) {
                try {
                    int menit = Integer.parseInt(tDurasi.getText());
                    SellerVer.broadcastTimer(menit); // Panggil method di SellerVer
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Durasi harus angka!");
                }
            }

            clearForm();
            cardLayout.show(mainContent, "DASHBOARD");
            JOptionPane.showMessageDialog(frame, "Data & Timer Tersimpan!");
        });
        
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(save);
        p.add(Box.createVerticalGlue());

        return p;
    }

    private void addFormGroup(JPanel parent, String labelText, JComponent inputField) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(COL_TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputField.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        parent.add(label);
        parent.add(Box.createVerticalStrut(8));
        parent.add(inputField);
        parent.add(Box.createVerticalStrut(15));
    }

    private JPanel createOrderPage() {
        JPanel panel = new JPanel(new BorderLayout());
        JTable table = new JTable(new DefaultTableModel(new String[]{"Produk", "Jumlah", "Harga", "Status"}, 0));
        table.setRowHeight(30);
        table.getTableHeader().setBackground(COL_PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ========================================================================
    // 4. HELPER UI COMPONENTS
    // ========================================================================

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(COL_TEXT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField tf = new JTextField(placeholder);
        tf.setFont(FONT_INPUT);
        tf.setForeground(Color.GRAY);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); 
        tf.setPreferredSize(new Dimension(300, 45));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15, COL_BLUE),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if(tf.getText().equals(placeholder)) { tf.setText(""); tf.setForeground(Color.BLACK); } }
            public void focusLost(FocusEvent e) { if(tf.getText().isEmpty()) { tf.setText(placeholder); tf.setForeground(Color.GRAY); } }
        });
        return tf;
    }

    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField tf = new JPasswordField(placeholder);
        tf.setFont(FONT_INPUT);
        tf.setForeground(Color.GRAY);
        tf.setEchoChar((char)0);
        tf.setPreferredSize(new Dimension(300, 45));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15, COL_BLUE), BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        tf.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { 
                if(new String(tf.getPassword()).equals(placeholder)) { tf.setText(""); tf.setForeground(Color.BLACK); tf.setEchoChar('•'); } 
            }
            public void focusLost(FocusEvent e) { 
                if(tf.getPassword().length == 0) { tf.setText(placeholder); tf.setForeground(Color.GRAY); tf.setEchoChar((char)0); } 
            }
        });
        return tf;
    }

    private JButton createRoundedButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(FONT_BTN);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        b.setPreferredSize(new Dimension(300, 50));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static class RoundedBorder implements Border {
        private int r; private Color c;
        RoundedBorder(int r, Color c) { this.r = r; this.c = c; }
        public Insets getBorderInsets(Component cmp) { return new Insets(1, 1, 1, 1); }
        public boolean isBorderOpaque() { return true; }
        public void paintBorder(Component cmp, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(x, y, w-1, h-1, r, r);
            g2.dispose();
        }
    }

    // ========================================================================
    // 5. HELPER LOGIC (DATA & FILE)
    // ========================================================================

    private void prepareEdit(int rowIndex) {
        editingRowIndex = rowIndex;
        currentFileName = (String) tableModel.getValueAt(rowIndex, 0);
        tNama.setText((String) tableModel.getValueAt(rowIndex, 1));
        tStok.setText((String) tableModel.getValueAt(rowIndex, 2));
        tHarga.setText((String) tableModel.getValueAt(rowIndex, 3));
        cSale.setSelected(tableModel.getValueAt(rowIndex, 4).equals("SALE"));
        tDesc.setText((String) tableModel.getValueAt(rowIndex, 5));
        lblFile.setText(currentFileName);
        
        // Atur field durasi saat mode edit
        tDurasi.setVisible(cSale.isSelected());
        tDurasi.setText("60"); 
        
        cardLayout.show(mainContent, "TAMBAH");
    }

    private void clearForm() {
        editingRowIndex = -1; tNama.setText("Nama Produk"); tStok.setText("0"); 
        tHarga.setText("0"); tDesc.setText("Deskripsi...");
        cSale.setSelected(false); lblFile.setText("Belum ada file");
        tDurasi.setVisible(false);
    }

    private void simpan() {
        try (PrintWriter pw = new PrintWriter(FILE_STOK)) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < 6; j++) pw.print(tableModel.getValueAt(i, j) + ",");
                pw.println();
            }
        } catch (Exception e) {}
    }

    private void loadData() {
        try {
            File f = new File(FILE_STOK);
            if (!f.exists()) return;
            BufferedReader br = new BufferedReader(new FileReader(f));
            String l;
            while ((l = br.readLine()) != null) tableModel.addRow(l.split(","));
            br.close();
        } catch (Exception e) {}
    }
    
    // Method untuk Server: Mengambil data string produk
    public String getProductDataString() {
        StringBuilder sb = new StringBuilder();
        if (tableModel != null) {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                sb.append(tableModel.getValueAt(i, 0)).append(",")
                  .append(tableModel.getValueAt(i, 1)).append(",")
                  .append(tableModel.getValueAt(i, 2)).append(",")
                  .append(tableModel.getValueAt(i, 3)).append(",")
                  .append(tableModel.getValueAt(i, 4)).append(",")
                  .append(tableModel.getValueAt(i, 5)).append(";");
            }
        }
        return sb.toString();
    }

    // Method untuk Server: Update log login
    public void handleLogin(String username) {
        SwingUtilities.invokeLater(() -> {
            if (logArea != null) {
                logArea.append("🟢 [LOGIN] " + username + " terhubung.\n");
            }
        });
    }

    // Method untuk Server: Update log order
    public void handleOrder(String orderDetails) {
        SwingUtilities.invokeLater(() -> {
            if (logArea != null) {
                logArea.append("🛒 [ORDER] Masuk: " + orderDetails + "\n");
            }
        });
    }

    // Method untuk Server: Kurangi stok dan simpan
    public synchronized void reduceStock(String nota) {
        String[] items = nota.split(", ");
        for (String itemStr : items) {
            if (itemStr.contains("(x")) {
                try {
                    String name = itemStr.substring(0, itemStr.indexOf(" (x")).trim();
                    int qtyMinus = Integer.parseInt(itemStr.substring(itemStr.indexOf("(x") + 2, itemStr.indexOf(")")));

                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        if (tableModel.getValueAt(i, 1).toString().equalsIgnoreCase(name)) {
                            int currentStock = Integer.parseInt(tableModel.getValueAt(i, 2).toString());
                            int newStock = Math.max(0, currentStock - qtyMinus);
                            tableModel.setValueAt(String.valueOf(newStock), i, 2);
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Gagal memotong stok untuk: " + itemStr);
                }
            }
        }
        simpan(); 
        updateProductGrid();
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(SellerMin::new); }
}