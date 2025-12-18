import java.awt.*;
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

    private final String FILE_STOK = "stok_barang.txt";
    private final String IMAGE_DIR = "images/";

    private final Color COLOR_RED = new Color(214, 48, 49);
    private final Color COLOR_WHITE = Color.WHITE;
    private final Color COLOR_BG = new Color(245, 246, 250);

    private JTextField tNama, tStok, tHarga, tDesc;
    private JLabel lblFile;
    private JCheckBox cSale;
    private String currentFileName = "default.jpg";
    private int editingRowIndex = -1;

    public SellerMin() {
        new File(IMAGE_DIR).mkdirs();
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception e) {}
        showLogin();
    }

    // ================= LOGIN =================
    private void showLogin() {
        JFrame loginFrame = new JFrame("Admin Login");
        loginFrame.setSize(400, 600);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.getContentPane().setBackground(COLOR_RED);
        loginFrame.setLayout(new GridBagLayout());

        RoundedPanel box = new RoundedPanel(30, COLOR_WHITE);
        box.setPreferredSize(new Dimension(300, 360));
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel logo = new JLabel("FashionHub");
        logo.setForeground(COLOR_RED);
        logo.setFont(new Font("SansSerif", Font.BOLD, 24));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField user = new JTextField();
        user.setBorder(BorderFactory.createTitledBorder("Username"));
        JPasswordField pass = new JPasswordField();
        pass.setBorder(BorderFactory.createTitledBorder("Password"));

        JButton login = new JButton("MASUK");
        login.setBackground(COLOR_RED); login.setForeground(COLOR_WHITE);
        login.setFocusPainted(false);
        login.addActionListener(e -> { loginFrame.dispose(); initMainDashboard(); });

        box.add(logo); box.add(Box.createVerticalStrut(25));
        box.add(user); box.add(Box.createVerticalStrut(10));
        box.add(pass); box.add(Box.createVerticalStrut(20));
        box.add(login);

        loginFrame.add(box);
        loginFrame.setVisible(true);
    }

    // ================= MAIN UI =================
    private void initMainDashboard() {
        frame = new JFrame("Seller Center");
        frame.setSize(420, 750);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);

        // Dashboard
        gridPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        gridPanel.setBackground(COLOR_BG);
        gridPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JPanel gridContainer = new JPanel(new BorderLayout());
        gridContainer.add(gridPanel, BorderLayout.NORTH);
        gridContainer.setBackground(COLOR_BG);
        mainContent.add(new JScrollPane(gridContainer), "DASHBOARD");

        mainContent.add(createAddPage(), "TAMBAH");

        logArea = new JTextArea();
        logArea.setEditable(false);
        mainContent.add(new JScrollPane(logArea), "NOTIF");

        mainContent.add(createOrderPage(), "ORDER");

        // Bottom Nav
        JPanel nav = new JPanel(new GridLayout(1, 4));
        nav.setBackground(COLOR_RED);
        nav.setPreferredSize(new Dimension(0, 60));
        String[] icons = {"🏠", "➕", "🔔", "🛒"};
        String[] pages = {"DASHBOARD", "TAMBAH", "NOTIF", "ORDER"};

        for (int i = 0; i < icons.length; i++) {
            final String p = pages[i];
            JButton b = new JButton(icons[i]);
            b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            b.setForeground(COLOR_WHITE); b.setContentAreaFilled(false); b.setBorderPainted(false);
            b.addActionListener(e -> {
                if (p.equals("DASHBOARD")) updateProductGrid();
                if (p.equals("TAMBAH")) clearForm();
                cardLayout.show(mainContent, p);
            });
            nav.add(b);
        }

        frame.add(mainContent, BorderLayout.CENTER);
        frame.add(nav, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(new String[]{"Foto", "Nama", "Stok", "Harga", "Sale", "Desc"}, 0);
        loadData();
        updateProductGrid();
        frame.setVisible(true);
    }

    // ================= GRID VIEW (TOMBOL DI BAWAH) =================
    private void updateProductGrid() {
        gridPanel.removeAll();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            final int index = i;
            String img = (String) tableModel.getValueAt(i, 0);
            String name = (String) tableModel.getValueAt(i, 1);
            String price = (String) tableModel.getValueAt(i, 3);

            JPanel card = new JPanel(new BorderLayout());
            card.setBorder(new LineBorder(new Color(235, 235, 235), 1, true));
            card.setBackground(COLOR_WHITE);

            // 1. Gambar (Tengah)
            JLabel imgLbl = new JLabel();
            try {
                ImageIcon ic = new ImageIcon(IMAGE_DIR + img);
                imgLbl.setIcon(new ImageIcon(ic.getImage().getScaledInstance(140, 110, Image.SCALE_SMOOTH)));
                imgLbl.setHorizontalAlignment(JLabel.CENTER);
            } catch (Exception e) {}
            imgLbl.setBorder(new EmptyBorder(10, 5, 5, 5));

            // 2. Info & Tombol (Bawah)
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBackground(COLOR_WHITE);
            bottomPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

            // Detail Nama & Harga
            JPanel textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setBackground(COLOR_WHITE);
            JLabel lblName = new JLabel(name);
            lblName.setFont(new Font("SansSerif", Font.BOLD, 12));
            JLabel lblPrice = new JLabel("Rp " + price);
            lblPrice.setForeground(COLOR_RED);
            textPanel.add(lblName);
            textPanel.add(lblPrice);

            // Tombol Aksi Kanan (Bulat)
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            actionPanel.setBackground(COLOR_WHITE);

            JButton btnEdit = createCircleButton("✎", COLOR_RED);
            btnEdit.setPreferredSize(new Dimension(30, 30));
            btnEdit.addActionListener(e -> prepareEdit(index));

            JButton btnDel = createCircleButton("✕", new Color(200, 200, 200));
            btnDel.setPreferredSize(new Dimension(30, 30));
            btnDel.addActionListener(e -> {
                if(JOptionPane.showConfirmDialog(frame, "Hapus?") == 0) {
                    tableModel.removeRow(index);
                    simpan();
                    updateProductGrid();
                }
            });

            actionPanel.add(btnEdit);
            actionPanel.add(btnDel);

            bottomPanel.add(textPanel, BorderLayout.WEST);
            bottomPanel.add(actionPanel, BorderLayout.EAST);

            card.add(imgLbl, BorderLayout.CENTER);
            card.add(bottomPanel, BorderLayout.SOUTH);
            gridPanel.add(card);
        }
        gridPanel.revalidate(); gridPanel.repaint();
    }

    private JButton createCircleButton(String text, Color bg) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillOval(0, 0, getWidth()-1, getHeight()-1);
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        return b;
    }

    // ================= KODE CRUD & UTIL LAINNYA =================
    private void prepareEdit(int rowIndex) {
        editingRowIndex = rowIndex;
        currentFileName = (String) tableModel.getValueAt(rowIndex, 0);
        tNama.setText((String) tableModel.getValueAt(rowIndex, 1));
        tStok.setText((String) tableModel.getValueAt(rowIndex, 2));
        tHarga.setText((String) tableModel.getValueAt(rowIndex, 3));
        cSale.setSelected(tableModel.getValueAt(rowIndex, 4).equals("SALE"));
        tDesc.setText((String) tableModel.getValueAt(rowIndex, 5));
        lblFile.setText(currentFileName);
        cardLayout.show(mainContent, "TAMBAH");
    }

    public String getProductDataString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            for (int j = 0; j < 6; j++) sb.append(tableModel.getValueAt(i, j)).append(",");
            sb.append(";");
        }
        return sb.toString();
    }

    public void handleLogin(String user) { SwingUtilities.invokeLater(() -> logArea.append("👤 LOGIN: " + user + "\n")); }
    public void handleOrder(String detail) { 
        SwingUtilities.invokeLater(() -> {
            logArea.append("🛒 ORDER: " + detail + "\n");
            JOptionPane.showMessageDialog(frame, "Ada Pesanan Baru!");
        });
    }

    private JPanel createAddPage() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(COLOR_WHITE);
        p.setBorder(new EmptyBorder(20, 40, 20, 40));

        tNama = createInput("Nama Produk");
        tStok = createInput("Stok");
        tHarga = createInput("Harga");
        tDesc = createInput("Deskripsi");
        lblFile = new JLabel("Pilih Foto", SwingConstants.CENTER);

        JButton upload = new JButton("📸 UPLOAD");
        upload.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                currentFileName = f.getName();
                lblFile.setText(currentFileName);
                try { Files.copy(f.toPath(), Paths.get(IMAGE_DIR + currentFileName), StandardCopyOption.REPLACE_EXISTING); } catch (Exception ex) {}
            }
        });

        cSale = new JCheckBox("Flash Sale");
        cSale.setBackground(COLOR_WHITE);

        JButton save = new JButton("SIMPAN");
        save.setBackground(COLOR_RED); save.setForeground(COLOR_WHITE);
        save.addActionListener(e -> {
            Object[] row = {currentFileName, tNama.getText(), tStok.getText(), tHarga.getText(), cSale.isSelected() ? "SALE" : "-", tDesc.getText()};
            if (editingRowIndex == -1) tableModel.addRow(row);
            else for(int i=0; i<6; i++) tableModel.setValueAt(row[i], editingRowIndex, i);
            simpan();
            updateProductGrid();
            cardLayout.show(mainContent, "DASHBOARD");
        });

        p.add(tNama); p.add(tStok); p.add(tHarga); p.add(tDesc);
        p.add(upload); p.add(lblFile); p.add(cSale); p.add(save);
        return p;
    }

    private JPanel createOrderPage() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(new JTable(new DefaultTableModel(new String[]{"Produk", "Jumlah", "Harga", "Status"}, 0))), BorderLayout.CENTER);
        return panel;
    }

    private JTextField createInput(String title) {
        JTextField f = new JTextField();
        f.setBorder(BorderFactory.createTitledBorder(title));
        return f;
    }

    private void clearForm() {
        editingRowIndex = -1; tNama.setText(""); tStok.setText(""); tHarga.setText(""); tDesc.setText("");
        cSale.setSelected(false); lblFile.setText("Pilih Foto");
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

    class RoundedPanel extends JPanel {
        int r; Color c;
        RoundedPanel(int r, Color c) { this.r = r; this.c = c; setOpaque(false); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c); g2.fillRoundRect(0, 0, getWidth(), getHeight(), r, r);
        }
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(SellerMin::new); }
}