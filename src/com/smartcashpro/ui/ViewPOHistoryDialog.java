package com.smartcashpro.ui;

import com.smartcashpro.db.PurchaseOrderDAO;
import com.smartcashpro.model.PurchaseOrder;
import com.smartcashpro.model.PurchaseOrderItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ViewPOHistoryDialog extends JDialog {

    private JTable poHeaderTable;
    private DefaultTableModel poHeaderTableModel;
    private JTable poItemTable;
    private DefaultTableModel poItemTableModel;
    private JButton closeButton;

    private PurchaseOrderDAO poDAO;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    public ViewPOHistoryDialog(Window owner) {
        super(owner, "Purchase Order History", ModalityType.APPLICATION_MODAL);
        this.poDAO = new PurchaseOrderDAO();

        initComponents();
        loadPOHeaders();
        setSize(900, 600); // Adjusted size
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // --- PO Header Table ---
        String[] headerColumns = {"PO ID", "Date", "Supplier", "Status", "Total Cost", "Expected Delivery", "Placed By"};
        poHeaderTableModel = new DefaultTableModel(headerColumns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        poHeaderTable = new JTable(poHeaderTableModel);
        poHeaderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Add listener to load items when a PO header is selected
        poHeaderTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // Single click to load items
                    int selectedRow = poHeaderTable.getSelectedRow();
                    if (selectedRow != -1) {
                        int poId = (Integer) poHeaderTableModel.getValueAt(selectedRow, 0);
                        try {
							loadPOItems(poId);
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
                    }
                }
            }
        });


        // --- PO Item Table ---
        String[] itemColumns = {"Item ID", "Product Name", "Qty Ordered", "Cost/Unit", "Qty Received", "Line Total"};
        poItemTableModel = new DefaultTableModel(itemColumns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        poItemTable = new JTable(poItemTableModel);

        // --- Layout using SplitPane ---
        JScrollPane headerScrollPane = new JScrollPane(poHeaderTable);
        headerScrollPane.setBorder(BorderFactory.createTitledBorder("Purchase Orders"));

        JScrollPane itemScrollPane = new JScrollPane(poItemTable);
        itemScrollPane.setBorder(BorderFactory.createTitledBorder("Selected PO Items"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, headerScrollPane, itemScrollPane);
        splitPane.setDividerLocation(250); // Initial position of the divider

        // --- Bottom Panel: Close Button ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);

        add(splitPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // Padding for dialog
    }

    private void loadPOHeaders() {
        poHeaderTableModel.setRowCount(0); // Clear existing
        List<PurchaseOrder> poList = poDAO.getAllPurchaseOrders();
        if (poList == null) {
             JOptionPane.showMessageDialog(this, "Error loading PO History. Check database connection.", "Load Error", JOptionPane.ERROR_MESSAGE);
             return;
        }
        for (PurchaseOrder po : poList) {
            poHeaderTableModel.addRow(new Object[]{
                    po.getPurchaseOrderId(),
                    po.getPoDate() != null ? po.getPoDate().format(dateTimeFormatter) : "N/A",
                    po.getSupplierName(), // Use getter for name
                    po.getStatus(),
                    po.getTotalCost() != null ? po.getTotalCost().setScale(2, RoundingMode.HALF_UP) : "N/A",
                    po.getExpectedDeliveryDate() != null ? po.getExpectedDeliveryDate().format(dateFormatter) : "N/A",
                    po.getPlacedByUserName() // Use getter for name
            });
        }
    }

    private void loadPOItems(int purchaseOrderId) throws SQLException {
        poItemTableModel.setRowCount(0); // Clear existing
        List<PurchaseOrderItem> items = poDAO.getPurchaseOrderItems(purchaseOrderId);
        if (items == null) {
             JOptionPane.showMessageDialog(this, "Error loading items for PO " + purchaseOrderId, "Load Error", JOptionPane.ERROR_MESSAGE);
             return;
        }
        for (PurchaseOrderItem item : items) {
            BigDecimal lineTotal = item.getCostPricePerUnit().multiply(new BigDecimal(item.getQuantityOrdered()));
            poItemTableModel.addRow(new Object[]{
                    item.getPurchaseOrderItemId(),
                    item.getProductName(),
                    item.getQuantityOrdered(),
                    item.getCostPricePerUnit().setScale(2, RoundingMode.HALF_UP),
                    item.getQuantityReceived(),
                    lineTotal.setScale(2, RoundingMode.HALF_UP)
            });
        }
    }
}