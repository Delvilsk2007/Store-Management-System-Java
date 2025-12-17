// ======= CLEAN IMPORTS =======

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import java.sql.*;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;

import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class order extends javax.swing.JFrame {

    int customerPk = 0;
    int productPk = 0;
    int finalTotalPrice = 0;
    int orderPK = 0;
    private TableRowSorter<DefaultTableModel> sorter;

    public order() {
        initComponents();
        loadCustomers();
       // setExtendedState(JFrame.MAXIMIZED_BOTH);
        loadProducts();
        loadCart();
        loadCart();
        calculateAndDisplayTotal();
    }

    private void formWindowOpened(java.awt.event.WindowEvent evt) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        sorter = new TableRowSorter<>(model);
        jTable1.setRowSorter(sorter);
    }
private PdfPCell makeSummaryCell(String text, Font font) {
    PdfPCell cell = new PdfPCell(new Phrase(text, font));
    cell.setPadding(6);
    cell.setBorder(PdfPCell.NO_BORDER);
    return cell;
}

    // STEP 1 - LOAD CUSTOMERS TABLE
    private void loadCustomers() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/project", "root", "stcgs@12"); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM customer")) {

            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                int cId = rs.getInt("Id");
                String cname = rs.getString("Name");
                String cemail = rs.getString("Email");
                String cmobile = rs.getString("Mobile_no");
                model.addRow(new Object[]{cId, cname, cemail, cmobile});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error loading customers: " + e.getMessage());
        }
    }

    // STEP 2 - LOAD PRODUCTS TABLE
    private void loadProducts() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/project", "root", "stcgs@12"); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM product")) {

            DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                int s_no = rs.getInt("S_no");
                String pName = rs.getString("P_Name");
                double price = rs.getDouble("Price");
                String category = rs.getString("Category");
                int quantity = rs.getInt("Quantity");

                model.addRow(new Object[]{s_no, pName, price, category, quantity});
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error loading products: " + e.getMessage());
        }
    }

    // STEP 3 - LOAD CART TABLE
    private void loadCart() {
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/project", "root", "stcgs@12");
            stmt = con.createStatement();

            String sql = "SELECT * FROM cart"; // your cart table
            rs = stmt.executeQuery(sql);

            DefaultTableModel model = (DefaultTableModel) jTable3.getModel();
            model.setRowCount(0); // clear table before reloading

            while (rs.next()) {
                int cartId = rs.getInt("Cart_id");
                String cname = rs.getString("Customer");
                String pname = rs.getString("Product_name");
                int qty = rs.getInt("Quantity");
                String category = rs.getString("Category");
                double totalPrice = rs.getDouble("Total_price");

                model.addRow(new Object[]{cartId, cname, pname, qty, category, totalPrice});
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error loading cart: " + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
            }
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (Exception e) {
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
            }
        }

    }

    private double calculateAndDisplayTotal() {
    double grandTotal = 0.0;

    javax.swing.table.DefaultTableModel cartModel = (javax.swing.table.DefaultTableModel) jTable3.getModel();

    int TOTAL_PRICE_COLUMN_INDEX = 5;

    for (int i = 0; i < cartModel.getRowCount(); i++) {
        try {
            Object totalObj = cartModel.getValueAt(i, TOTAL_PRICE_COLUMN_INDEX);
            double rowTotal = Double.parseDouble(totalObj.toString());
            grandTotal += rowTotal;
        } catch (Exception e) {
            System.err.println("Error reading row total: " + e.getMessage());
        }
    }

    String formattedTotal = String.format("%,.2f", grandTotal);
    jLabel15.setText(formattedTotal); // show in label

    return grandTotal; // return value for PDF
}


      

    public void generateCustomerBillPDF(JTable cartTable) {

    try {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a customer row first.");
            return;
        }

        String customerName = cartTable.getValueAt(selectedRow, 1).toString();
        String customerMobile = cartTable.getValueAt(selectedRow, 3).toString();
        String customerEmail = cartTable.getValueAt(selectedRow, 4).toString();
        String addressLine = "Dheeraj Nagar, Faridabad  "
                                 + " Haryana - 121003";
        
                              

        String filePath = "Customer_" + customerName + "_Receipt_" + System.currentTimeMillis() + ".pdf";

        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // FONT STYLES
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.WHITE);
        Font invoiceFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        Font blackBold = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        Font normal = new Font(Font.FontFamily.HELVETICA, 10);
        Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
        Font totalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

        // ======== HEADER WITH RED BACKGROUND =========
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell hc = new PdfPCell(new Phrase("UNITED INDIA Ltd.", titleFont));
        hc.setBackgroundColor(new BaseColor(220, 0, 0));
        hc.setPadding(10);
        hc.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.addCell(hc);
        document.add(header);

        document.add(new Paragraph("Rps Auria sec - 88, Haryana, India | +91 9871945542 | GSTIN: 07AABCU9603R1ZV", normal));
        document.add(Chunk.NEWLINE);

        // Invoice Title Row
        PdfPTable invoiceRow = new PdfPTable(2);
        invoiceRow.setWidthPercentage(100);

        PdfPCell left = new PdfPCell(new Phrase("TAX INVOICE", invoiceFont));
        left.setBorder(Rectangle.NO_BORDER);
        invoiceRow.addCell(left);

        String invoiceNo = "INV-" + (int)(Math.random()*900000+100000);
        PdfPCell right = new PdfPCell(new Phrase("Invoice No: " + invoiceNo + "\nDate: " + new java.util.Date(), normal));
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        right.setBorder(Rectangle.NO_BORDER);
        invoiceRow.addCell(right);

        document.add(invoiceRow);
        document.add(Chunk.NEWLINE);

        // Bill To & Ship To Layout
        PdfPTable customerGrid = new PdfPTable(2);
        customerGrid.setWidthPercentage(100);

        PdfPCell billTo = new PdfPCell();
        billTo.addElement(new Phrase("BILL TO", blackBold));
        billTo.addElement(new Phrase(customerName, normal));
        billTo.addElement(new Phrase("Mobile: " + customerMobile, normal));
        billTo.addElement(new Phrase("Email: " + customerEmail, normal));
        billTo.setPadding(6);
        customerGrid.addCell(billTo);

        PdfPCell shipTo = new PdfPCell();
        shipTo.addElement(new Phrase("SHIP TO", blackBold));
        shipTo.addElement(new Phrase(customerName, normal));
        shipTo.addElement(new Phrase("Address: " + addressLine, normal));
        shipTo.setPadding(6);
        customerGrid.addCell(shipTo);

        document.add(customerGrid);
        document.add(Chunk.NEWLINE);

        // ======= PRODUCT TABLE =======
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.0f, 2.5f, 1f, 1.3f, 1.4f});

        String[] headers = {"Cart Id", "Product", "Qty", "Category", "Price"};
        BaseColor headerColor = new BaseColor(220, 0, 0);

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
            cell.setBackgroundColor(headerColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            table.addCell(cell);
        }

        double grandTotal = 0;

        for (int i = 0; i < cartTable.getRowCount(); i++) {
            if (cartTable.getValueAt(i, 1).toString().equalsIgnoreCase(customerName)) {

                table.addCell(cartTable.getValueAt(i, 0).toString());
                table.addCell(cartTable.getValueAt(i, 2).toString());
                table.addCell(cartTable.getValueAt(i, 3).toString());
                table.addCell(cartTable.getValueAt(i, 4).toString());
                table.addCell(cartTable.getValueAt(i, 5).toString());

                grandTotal += Double.parseDouble(cartTable.getValueAt(i, 5).toString());
            }
        }

        document.add(table);
        document.add(Chunk.NEWLINE);

        // ======= TOTAL BOX =======
        PdfPTable totalBox = new PdfPTable(1);
        totalBox.setWidthPercentage(40);
        PdfPCell tc = new PdfPCell(new Phrase("Total Amount: ₹ " + String.format("%.2f", grandTotal), totalFont));
        tc.setPadding(8);
        tc.setHorizontalAlignment(Element.ALIGN_LEFT);
        totalBox.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalBox.addCell(tc);
        document.add(totalBox);

        document.add(Chunk.NEWLINE);

        // Bank Details + Footer
        document.add(new Paragraph("Bank Details", blackBold));
        document.add(new Paragraph("Account Holder: UNITED INDIA Ltd.", normal));
        document.add(new Paragraph("Account No: 1245787896321 | IFSC: SBIN003456 | Bank: SBI New Delhi", normal));
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph("Notes:", blackBold));
        document.add(new Paragraph("1. Goods once sold are not returnable.", normal));
        document.add(new Paragraph("2. Payment due within 15 days.", normal));
        document.add(Chunk.NEWLINE);

        Paragraph signature = new Paragraph("Authorized Signatory ____________________", blackBold);
        signature.setAlignment(Element.ALIGN_RIGHT);
        document.add(signature);

        document.close();

        // ---------- OPEN PDF AUTOMATICALLY ----------
        java.awt.Desktop.getDesktop().open(new java.io.File(filePath));
        
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
    }
}

// ... rest of your code (constructor, initComponents) ...
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        Save_Oder_Details_btn = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI Black", 1, 36)); // NOI18N
        jLabel1.setText(" Orders");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(630, 0, -1, -1));

        jTable1.setBackground(new java.awt.Color(0, 0, 0));
        jTable1.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        jTable1.setForeground(new java.awt.Color(255, 255, 255));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Id", "Name", "Email", "Mobile_no"
            }
        ));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(26, 117, 380, 250));

        jTable2.setBackground(new java.awt.Color(0, 0, 0));
        jTable2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTable2.setForeground(new java.awt.Color(255, 255, 255));
        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "S_no", "Product_Name", "Price", "Category", "Quantity"
            }
        ));
        jTable2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable2MouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTable2);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(463, 117, 420, 250));

        jTable3.setBackground(new java.awt.Color(0, 0, 0));
        jTable3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTable3.setForeground(new java.awt.Color(255, 255, 255));
        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Cart Id", "Customer Name", "Product Name", "Quantity", "Category", "Total Price"
            }
        ));
        jTable3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable3MouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(jTable3);

        getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 120, 440, 250));

        jLabel2.setFont(new java.awt.Font("Segoe UI Black", 1, 17)); // NOI18N
        jLabel2.setText("Customer List");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 80, -1, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI Black", 1, 17)); // NOI18N
        jLabel3.setText("Product List");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(637, 75, -1, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI Black", 1, 17)); // NOI18N
        jLabel4.setText("Cart");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(1155, 77, -1, 20));

        jLabel5.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        jLabel5.setText("Selected Customer");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(26, 385, 143, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Name");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(26, 435, -1, -1));

        jTextField1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });
        getContentPane().add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(26, 467, 350, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Email");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(26, 505, -1, -1));

        jTextField2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 540, 350, -1));

        jTextField3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 620, 350, -1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setText("Moblie_No.");
        getContentPane().add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(26, 587, -1, -1));

        jLabel9.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        jLabel9.setText("Selected Product");
        getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(463, 385, -1, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setText("Product Name");
        getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(463, 429, -1, -1));

        jTextField4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        getContentPane().add(jTextField4, new org.netbeans.lib.awtextra.AbsoluteConstraints(463, 463, 452, -1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setText("Price");
        getContentPane().add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(463, 508, 36, -1));

        jTextField5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        getContentPane().add(jTextField5, new org.netbeans.lib.awtextra.AbsoluteConstraints(463, 540, 452, -1));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setText("Category");
        getContentPane().add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(463, 585, -1, -1));

        jTextField6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        getContentPane().add(jTextField6, new org.netbeans.lib.awtextra.AbsoluteConstraints(463, 619, 452, -1));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setText("Quantity");
        getContentPane().add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(463, 652, -1, -1));

        jTextField7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTextField7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField7ActionPerformed(evt);
            }
        });
        getContentPane().add(jTextField7, new org.netbeans.lib.awtextra.AbsoluteConstraints(463, 690, 452, -1));

        jButton1.setFont(new java.awt.Font("Segoe UI Black", 1, 16)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/login.png"))); // NOI18N
        jButton1.setText("ADD TO CART");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(463, 734, 452, -1));

        jLabel14.setFont(new java.awt.Font("Segoe UI Black", 1, 18)); // NOI18N
        jLabel14.setText("Total Amount RS:");
        getContentPane().add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 395, -1, -1));

        jLabel15.setFont(new java.awt.Font("Segoe UI Black", 1, 18)); // NOI18N
        jLabel15.setText("00000");
        getContentPane().add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(1238, 395, -1, -1));

        Save_Oder_Details_btn.setFont(new java.awt.Font("Segoe UI Black", 1, 16)); // NOI18N
        Save_Oder_Details_btn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/save.png"))); // NOI18N
        Save_Oder_Details_btn.setText("Save Oder Details");
        Save_Oder_Details_btn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Save_Oder_Details_btnActionPerformed(evt);
            }
        });
        getContentPane().add(Save_Oder_Details_btn, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 460, 350, 30));

        jButton3.setFont(new java.awt.Font("Segoe UI Black", 1, 16)); // NOI18N
        jButton3.setText("Reset");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 540, 350, -1));

        jButton4.setFont(new java.awt.Font("Segoe UI Black", 1, 16)); // NOI18N
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/close.png"))); // NOI18N
        jButton4.setText("Close");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 610, 360, -1));

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/icons8-search-bar-40.png"))); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 390, 40, 30));

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/icons8-search-client-30.png"))); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 380, 30, 30));

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/order.png"))); // NOI18N
        getContentPane().add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1340, 770));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField7ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        Store_Menu reg = new Store_Menu();
        reg.setVisible(true);
        reg.setLocationRelativeTo(this);
        reg.setAlwaysOnTop(true);
        this.dispose();


    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        jTextField5.setText("");
        jTextField6.setText("");
        jTextField7.setText("");

        // Reset internal variables (optional but useful)
        customerPk = 0;
        productPk = 0;
        loadCustomers();
        loadProducts();
        loadCart();
        loadCart();
        calculateAndDisplayTotal();
        JOptionPane.showMessageDialog(this, "All fields have been cleared.");
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked

        // STEP 2 - SELECT CUSTOMER BY MOUSE
        int index = jTable1.getSelectedRow();
        TableModel model = jTable1.getModel();

        customerPk = Integer.parseInt(model.getValueAt(index, 0).toString());
        jTextField1.setText(model.getValueAt(index, 1).toString()); // Name
        jTextField2.setText(model.getValueAt(index, 2).toString()); // Email
        jTextField3.setText(model.getValueAt(index, 3).toString()); // Mobile

    }//GEN-LAST:event_jTable1MouseClicked

    private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked
        int index = jTable2.getSelectedRow();
        TableModel model = jTable2.getModel();

        productPk = Integer.parseInt(model.getValueAt(index, 0).toString());
        jTextField4.setText(model.getValueAt(index, 1).toString()); // Product name
        jTextField5.setText(model.getValueAt(index, 2).toString()); // Price
        jTextField6.setText(model.getValueAt(index, 3).toString()); // Category        // TODO add your handling code here:
    }//GEN-LAST:event_jTable2MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String cname = jTextField1.getText();
        String pname = jTextField4.getText();
        String category = jTextField6.getText();
        String qtyText = jTextField7.getText();
        String priceText = jTextField5.getText();

        // Basic validation
        if (cname.isEmpty() || pname.isEmpty() || qtyText.isEmpty() || priceText.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Please select a customer, product, and enter quantity.");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyText);
            double price = Double.parseDouble(priceText);
            double total = qty * price;

            // --- Use try-with-resources for automatic resource closing ---
            try (java.sql.Connection con = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/project", "root", "stcgs@12")) {

                // 1️⃣ Insert into cart table
                // **FIXED ERROR:** Changed 'Customer_name' to 'Customer'
                String sql = "INSERT INTO cart (Customer, Product_name, Quantity, Category, Total_price) VALUES (?, ?, ?, ?, ?)";
                try (java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setString(1, cname);
                    ps.setString(2, pname);
                    ps.setInt(3, qty);
                    ps.setString(4, category);
                    ps.setDouble(5, total);

                    ps.executeUpdate();
                }

                // 2️⃣ Reduce quantity in product table
                String updateSql = "UPDATE product SET Quantity = Quantity - ? WHERE P_Name = ?";
                try (java.sql.PreparedStatement psUpdate = con.prepareStatement(updateSql)) {

                    psUpdate.setInt(1, qty);
                    psUpdate.setString(2, pname);

                    psUpdate.executeUpdate();
                }

                // 3️⃣ Refresh tables (assuming loadProducts() and loadCart() are methods defined in your class)
                loadProducts();
                loadCart();
// 2b. Refresh jTable3 (Cart) 
                loadCart(); // Assuming loadCart() updates the JTable

                // 3. **NEW STEP**: Update the total amount
                calculateAndDisplayTotal();
                javax.swing.JOptionPane.showMessageDialog(this, "Item added to cart successfully!");

                // 4️⃣ Clear input fields
                jTextField4.setText("");
                jTextField5.setText("");
                jTextField6.setText("");
                jTextField7.setText("");

            } // Connection 'con' is closed automatically here

        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Invalid quantity or price.");
        } catch (java.sql.SQLException e) {
            // This will now catch the error if the table names or other columns are wrong
            javax.swing.JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }

    }//GEN-LAST:event_jButton1ActionPerformed

    private void Save_Oder_Details_btnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Save_Oder_Details_btnActionPerformed

    try {
        // DEFAULT SAVE LOCATION (Desktop)
        String userHome = System.getProperty("user.home");
        String filePath = userHome + "\\Desktop\\Sales_Report.pdf";

        JTable tableData = jTable3;
        int orderId = 1; 
        String OwnerName = "Sk"; 
        double totalAmount = calculateAndDisplayTotal(); 

        Document doc = new Document(PageSize.A4, 25, 25, 25, 25);
        PdfWriter.getInstance(doc, new FileOutputStream(filePath));
        doc.open();

        // ---------- FONTS ----------
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.WHITE);
        Font infoTitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font infoFont = new Font(Font.FontFamily.HELVETICA, 11);
        Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
        Font tableFont = new Font(Font.FontFamily.HELVETICA, 10);
        Font totalFont = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD);

        // ---------- COMPANY HEADER ----------
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell hCell = new PdfPCell(new Phrase("UNITED INDIA Ltd.", titleFont));
        hCell.setPadding(12);
        hCell.setBackgroundColor(new BaseColor(0, 84, 168));
        hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        hCell.setBorder(PdfPCell.NO_BORDER);
        header.addCell(hCell);
        doc.add(header);

        doc.add(new Paragraph("Corporate Office: Rps Auria sec - 88, Haryana, India", infoFont));
        doc.add(new Paragraph("Contact: +91 9871945542 | GSTIN: 07AABCU9603R1ZV", infoFont));
        doc.add(Chunk.NEWLINE);

        // ---------- REPORT TITLE ----------
        Paragraph p = new Paragraph("Monthly / Yearly Sales Report", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
        doc.add(Chunk.NEWLINE);

        // ---------- SUMMARY BOX ----------
        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(100);
        summary.addCell(makeSummaryCell("Owner :", infoTitleFont));
        summary.addCell(makeSummaryCell(OwnerName, infoFont));
        summary.addCell(makeSummaryCell("Report Date:", infoTitleFont));
        summary.addCell(makeSummaryCell(new SimpleDateFormat("dd MMM yyyy hh:mm a").format(new Date()), infoFont));
        summary.addCell(makeSummaryCell("Total Items:", infoTitleFont));
        summary.addCell(makeSummaryCell(String.valueOf(tableData.getRowCount()), infoFont));
        doc.add(summary);

        // ---------- SALES TABLE ----------
        PdfPTable pdfTable = new PdfPTable(tableData.getColumnCount());
        pdfTable.setWidthPercentage(100);

        BaseColor headerColor = new BaseColor(0, 84, 168);
        BaseColor altWhite = BaseColor.WHITE;
        BaseColor altGray = new BaseColor(235, 235, 235);

        for (int i = 0; i < tableData.getColumnCount(); i++) {
            PdfPCell cell = new PdfPCell(new Phrase(tableData.getColumnName(i), tableHeaderFont));
            cell.setBackgroundColor(headerColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(7);
            pdfTable.addCell(cell);
        }

        for (int r = 0; r < tableData.getRowCount(); r++) {
            BaseColor rowColor = (r % 2 == 0) ? altWhite : altGray;

            for (int c = 0; c < tableData.getColumnCount(); c++) {
                Object val = tableData.getValueAt(r, c);
                PdfPCell cell = new PdfPCell(new Phrase(val != null ? val.toString() : "", tableFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                cell.setBackgroundColor(rowColor);
                pdfTable.addCell(cell);
            }
        }

        doc.add(pdfTable);
        doc.add(Chunk.NEWLINE);

        // ---------- TOTAL ----------
        Paragraph totalText = new Paragraph("Total Revenue: ₹ " + String.format("%.2f", totalAmount), totalFont);
        totalText.setAlignment(Element.ALIGN_RIGHT);
        doc.add(totalText);

        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);

        // ---------- FOOTER ----------
        Paragraph sign = new Paragraph("Authorized Signature ____________________", infoFont);
        sign.setAlignment(Element.ALIGN_LEFT);
        doc.add(sign);

        doc.close();

        JOptionPane.showMessageDialog(this, "PDF generated on Desktop.");

        // ---------- OPEN PDF AUTOMATICALLY ----------
        java.awt.Desktop.getDesktop().open(new java.io.File(filePath));

    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        e.printStackTrace();
    }



    }//GEN-LAST:event_Save_Oder_Details_btnActionPerformed

    private void jTable3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable3MouseClicked
        generateCustomerBillPDF(jTable3);

    }//GEN-LAST:event_jTable3MouseClicked

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased

        String search = jTextField1.getText().trim();
        if (search.length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + search));
        }


    }//GEN-LAST:event_jTextField1KeyReleased

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        String name = JOptionPane.showInputDialog(this, "Enter customer name (full or partial):");
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/project", "root", "stcgs@12"); PreparedStatement pstmt = con.prepareStatement(
                "SELECT * FROM customer WHERE LOWER(Name) LIKE ?")) {

            pstmt.setString(1, "%" + name.toLowerCase() + "%");

            ResultSet rs = pstmt.executeQuery();
            DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                int cId = rs.getInt("Id");
                String cname = rs.getString("Name");
                String cemail = rs.getString("Mobile_no");
                String cmobile = rs.getString("Email");
                model.addRow(new Object[]{cId, cname, cemail, cmobile});
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error searching customer: " + e.getMessage());
        }


    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/project", "root", "stcgs@12");
            Statement stmt = con.createStatement();

            String[] options = {
                "Search by Name",
                "Search by Category",
                "Search by Price Range + Category + Name"
            };

            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Choose a search option:",
                    "Search Options",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            String sql = "";

            // 1. Search by name
            if (choice == 0) {
                String name = JOptionPane.showInputDialog(this, "Enter product name (full or partial):");
                if (name == null || name.trim().isEmpty()) {
                    return;
                }

                sql = "SELECT * FROM product WHERE P_Name LIKE ?";
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, "%" + name + "%");
            } // 2. Search by category
            else if (choice == 1) {
                String[] categories = {"Electronics", "Sports", "Spare Parts"};
                JComboBox<String> categoryBox = new JComboBox<>(categories);

                int ans = JOptionPane.showConfirmDialog(
                        this,
                        categoryBox,
                        "Select Category",
                        JOptionPane.OK_CANCEL_OPTION
                );
                if (ans != JOptionPane.OK_OPTION) {
                    return;
                }

                String category = categoryBox.getSelectedItem().toString();

                sql = "SELECT * FROM product WHERE Category = ?";
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, category);
            } // 3. Search by price + category + name
            else if (choice == 2) {

                String minR = JOptionPane.showInputDialog(this, "Enter minimum price:");
                if (minR == null) {
                    return;
                }
                double minVal = Double.parseDouble(minR);

                String maxR = JOptionPane.showInputDialog(this, "Enter maximum price:");
                if (maxR == null) {
                    return;
                }
                double maxVal = Double.parseDouble(maxR);

                String[] categories = {"Electronics", "Sports", "Spare Parts"};
                JComboBox<String> categoryBox = new JComboBox<>(categories);

                int catAns = JOptionPane.showConfirmDialog(
                        this,
                        categoryBox,
                        "Select Category",
                        JOptionPane.OK_CANCEL_OPTION
                );
                if (catAns != JOptionPane.OK_OPTION) {
                    return;
                }

                String category = categoryBox.getSelectedItem().toString();

                String nameFilter = JOptionPane.showInputDialog(this, "Enter part of product name (optional):");
                if (nameFilter == null) {
                    nameFilter = "";
                }

                sql = "SELECT * FROM product WHERE Price BETWEEN ? AND ? AND Category = ? AND P_Name LIKE ?";
                pstmt = con.prepareStatement(sql);
                pstmt.setDouble(1, minVal);
                pstmt.setDouble(2, maxVal);
                pstmt.setString(3, category);
                pstmt.setString(4, "%" + nameFilter + "%");
            } else {
                return;
            }

            rs = pstmt.executeQuery();

            // Load JTable2 only
            DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
            model.setRowCount(0);

            int count = 0;

            while (rs.next()) {
                int s = rs.getInt("S_no");
                String name = rs.getString("P_Name");
                double price = rs.getDouble("Price");
                String cat = rs.getString("Category");
                int qty = rs.getInt("Quantity");

                model.addRow(new Object[]{s, name, price, cat, qty});
                count++;
            }

            if (count == 0) {
                JOptionPane.showMessageDialog(this, "No records found.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (Exception e) {
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception e) {
            }
        }


    }//GEN-LAST:event_jButton5ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(order.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(order.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(order.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(order.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new order().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton Save_Oder_Details_btn;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    // End of variables declaration//GEN-END:variables
}
