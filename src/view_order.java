
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import javax.swing.*;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import javax.swing.table.TableModel;
import java.io.File;
import java.awt.Desktop;
import javax.swing.JOptionPane;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Color;
import java.awt.Frame;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import javax.swing.UIManager;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
/**
 *
 * @author SUBHANSHU
 */
public class view_order extends javax.swing.JFrame {

    /**
     * Creates new form view_order
     */




    public view_order() {
         UIManager.put("Table.selectionInactiveBackground", Color.BLACK);
        UIManager.put("Table.selectionInactiveForeground", Color.WHITE);

        initComponents();
        loadCustomers();
jTable1.setSelectionBackground(Color.BLACK);
jTable2.setSelectionForeground(Color.WHITE);
    }

    private void loadCustomerOrders(String customerName) {

        DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
        model.setRowCount(0);

        String sql = "SELECT Cart_id, Product_name, Quantity, Category, Total_price "
                + "FROM cart WHERE Customer = ?";

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/project", "root", "stcgs@12"); PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, customerName);
            ResultSet rs = pst.executeQuery();

            boolean found = false;

            while (rs.next()) {
                found = true;

                Object[] rowData = {
                    rs.getInt("Cart_id"),
                    rs.getString("Product_name"),
                    rs.getInt("Quantity"),
                    rs.getString("Category"),
                    rs.getDouble("Total_price")
                };

                model.addRow(rowData);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + ex.getMessage());
        }
    }

    private void loadCustomers() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/project", "root", "stcgs@12"); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM customer")) {

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
            JOptionPane.showMessageDialog(this, "❌ Error loading customers: " + e.getMessage());
        }
    }

    public void generateSingleOrderPDF(String customerName, String mobile, int rowIndex) {
        try {
            DefaultTableModel model = (DefaultTableModel) jTable2.getModel();

            if (rowIndex < 0) {
                JOptionPane.showMessageDialog(this, "Please select an order.");
                return;
            }

            String filePath = customerName + "_Order_" + System.currentTimeMillis() + ".pdf";

            Document doc = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter.getInstance(doc, new FileOutputStream(filePath));
            doc.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.WHITE);
            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font bold = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
            Font normal = new Font(Font.FontFamily.HELVETICA, 10);
            Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            Font totalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            PdfPCell hc = new PdfPCell(new Phrase("UNITED INDIA Ltd.", titleFont));
            hc.setBackgroundColor(new BaseColor(220, 0, 0));
            hc.setPadding(10);
            hc.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.addCell(hc);
            doc.add(header);

            doc.add(new Paragraph("Rps Auria sec - 88, Haryana, India | +91 9871945542 | GSTIN: 07AABCU9603R1ZV", normal));
            doc.add(Chunk.NEWLINE);

            Paragraph rcp = new Paragraph("Order Receipt", subtitleFont);
            rcp.setAlignment(Element.ALIGN_CENTER);
            doc.add(rcp);
            doc.add(Chunk.NEWLINE);

            doc.add(new Paragraph("Customer: " + customerName, normal));
            doc.add(new Paragraph("Mobile: " + mobile, normal));
            doc.add(new Paragraph("Date: " + new java.util.Date(), normal));
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2.5f, 1f, 1.3f, 1.3f});

            String[] headers = {"Cart ID", "Product", "Qty", "Category", "Price"};
            BaseColor headerColor = new BaseColor(220, 0, 0);

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, tableHeaderFont));
                cell.setBackgroundColor(headerColor);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(6);
                table.addCell(cell);
            }

            table.addCell(model.getValueAt(rowIndex, 0).toString());
            table.addCell(model.getValueAt(rowIndex, 1).toString());
            table.addCell(model.getValueAt(rowIndex, 2).toString());
            table.addCell(model.getValueAt(rowIndex, 3).toString());
            table.addCell(model.getValueAt(rowIndex, 4).toString());

            double total = Double.parseDouble(model.getValueAt(rowIndex, 4).toString());

            doc.add(table);
            doc.add(Chunk.NEWLINE);

            PdfPTable totalBox = new PdfPTable(1);
            totalBox.setWidthPercentage(40);
            PdfPCell tc = new PdfPCell(new Phrase("Total Amount: ₹ " + String.format("%.2f", total), totalFont));
            tc.setPadding(8);
            tc.setHorizontalAlignment(Element.ALIGN_LEFT);
            totalBox.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalBox.addCell(tc);
            doc.add(totalBox);

            doc.add(Chunk.NEWLINE);
            doc.add(new Paragraph("Thank you for your order!", bold));
            doc.close();

                   // ---------- OPEN PDF AUTOMATICALLY ----------
        java.awt.Desktop.getDesktop().open(new java.io.File(filePath));


        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating PDF: " + e.getMessage());
        }
    }


    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton2 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        jLabel5.setText("jLabel5");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setFont(new java.awt.Font("Segoe UI Black", 1, 36)); // NOI18N
        jLabel1.setText("View Order");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(590, 0, -1, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        jLabel2.setText("Custome List");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 70, -1, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        jLabel3.setText("Oder List");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1040, 80, -1, -1));

        jButton1.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/close.png"))); // NOI18N
        jButton1.setText("Close");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 720, 1340, 30));

        jTable1.setBackground(new java.awt.Color(0, 0, 0));
        jTable1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTable1.setForeground(new java.awt.Color(255, 255, 255));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Id", "Name", "Mobile No.", "Email"
            }
        ));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jTable1);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 106, 710, 570));

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
                "cart Id", "product Name ", "quantity", "category", "total amount"
            }
        ));
        jTable2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable2MouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(jTable2);

        getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(820, 110, 510, 570));

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/icons8-search-bar-40.png"))); // NOI18N
        jButton2.setText(" ");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        getContentPane().add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(680, 80, 30, 30));

        jLabel6.setFont(new java.awt.Font("Segoe UI Black", 1, 12)); // NOI18N
        jLabel6.setText("Select Customer");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 80, 100, 20));

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/order.png"))); // NOI18N
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1340, 750));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
 Store_Menu reg = new Store_Menu();
        reg.setVisible(true);
        reg.setLocationRelativeTo(this);
        reg.setAlwaysOnTop(true);
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        int row = jTable1.getSelectedRow();
        if (row < 0) {
            return;
        }

        String customerName = jTable1.getValueAt(row, 1).toString().trim();
        loadCustomerOrders(customerName);    }//GEN-LAST:event_jTable1MouseClicked

    private void jTable2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked
        int cRow = jTable1.getSelectedRow();   // selected customer
        int oRow = jTable2.getSelectedRow();   // selected order

        if (cRow < 0 || oRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a customer and order.");
            return;
        }

        String customerName = jTable1.getValueAt(cRow, 1).toString();
        String mobile = jTable1.getValueAt(cRow, 3).toString();   // Mobile column index 3
        generateSingleOrderPDF(customerName, mobile, oRow);

    }//GEN-LAST:event_jTable2MouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
  String name = JOptionPane.showInputDialog(this, "Enter customer name (full or partial):");
if (name == null || name.trim().isEmpty()) {
    return;
}

try (Connection con = DriverManager.getConnection(
        "jdbc:mysql://localhost:3306/project", "root", "stcgs@12");
     PreparedStatement pstmt = con.prepareStatement(
        "SELECT Id, Name, Mobile_no, Email FROM customer WHERE LOWER(Name) LIKE LOWER(?)")) {

    pstmt.setString(1, "%" + name.trim() + "%");

    ResultSet rs = pstmt.executeQuery();
    DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0);

    boolean found = false;

    while (rs.next()) {
        found = true;
        int cId = rs.getInt("Id");
        String cname = rs.getString("Name");
        String cmobile = rs.getString("Mobile_no");
        String cemail = rs.getString("Email");

        model.addRow(new Object[]{cId, cname, cmobile, cemail});
    }

    if (!found) {
        JOptionPane.showMessageDialog(this, "No customer found.");
    }

} catch (Exception e) {
    JOptionPane.showMessageDialog(this, "Error searching customer:\n" + e.getMessage());
}

    }//GEN-LAST:event_jButton2ActionPerformed

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
            java.util.logging.Logger.getLogger(view_order.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(view_order.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(view_order.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(view_order.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new view_order().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    // End of variables declaration//GEN-END:variables
}
