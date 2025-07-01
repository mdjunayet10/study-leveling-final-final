package ui;

import com.google.firebase.database.*;
import util.ColorPalette;
import util.FontManager;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GlobalLeaderboardScreen extends JFrame {
    private final FontManager fontManager = FontManager.getInstance();

    public GlobalLeaderboardScreen() {
        setTitle("Global Leaderboard");
        setSize(750, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ColorPalette.SL_BACKGROUND);

        // Header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main leaderboard panel
        JPanel leaderboardPanel = createLeaderboardPanel();
        add(leaderboardPanel, BorderLayout.CENTER);

        // Footer panel
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorPalette.SL_PRIMARY_DARK);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("GLOBAL RANKINGS");
        titleLabel.setFont(fontManager.getHeaderFont());
        titleLabel.setForeground(ColorPalette.SL_TEXT_PRIMARY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        headerPanel.add(titleLabel, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel createLeaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(ColorPalette.SL_BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create table model
        String[] columns = {"RANK", "USERNAME", "LEVEL", "XP", "TASKS"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        // Create table with Solo Leveling style
        JTable leaderboardTable = new JTable(tableModel);
        leaderboardTable.setFont(fontManager.getBodyFont());
        leaderboardTable.setRowHeight(32);
        leaderboardTable.setShowGrid(false);
        leaderboardTable.setIntercellSpacing(new Dimension(0, 0));
        leaderboardTable.setFillsViewportHeight(true);

        // Table colors
        leaderboardTable.setBackground(ColorPalette.SL_CARD);
        leaderboardTable.setForeground(ColorPalette.SL_TEXT_PRIMARY);
        leaderboardTable.setSelectionBackground(ColorPalette.SL_PRIMARY_LIGHT);
        leaderboardTable.setSelectionForeground(ColorPalette.SL_TEXT_PRIMARY);

        // Table header styling
        JTableHeader header = leaderboardTable.getTableHeader();
        header.setFont(fontManager.getSubheaderFont());
        header.setBackground(ColorPalette.SL_PRIMARY);
        header.setForeground(ColorPalette.SL_TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ColorPalette.SL_ACCENT));
        header.setReorderingAllowed(false);

        // Center-align all cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                         boolean isSelected, boolean hasFocus,
                                                         int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Special styling for top 3 ranks
                if (column == 0 && row < 3) {
                    switch (row) {
                        case 0: // Gold
                            setForeground(ColorPalette.SL_GOLD);
                            setFont(fontManager.getSubheaderFont());
                            break;
                        case 1: // Silver
                            setForeground(ColorPalette.SL_SILVER);
                            setFont(fontManager.getSubheaderFont());
                            break;
                        case 2: // Bronze
                            setForeground(ColorPalette.SL_BRONZE);
                            setFont(fontManager.getSubheaderFont());
                            break;
                    }
                } else if (isSelected) {
                    setForeground(ColorPalette.SL_ACCENT);
                } else {
                    setForeground(ColorPalette.SL_TEXT_PRIMARY);
                    setFont(fontManager.getBodyFont());
                }

                // Add subtle row striping
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? ColorPalette.SL_CARD : ColorPalette.SL_PRIMARY_DARK);
                }

                return c;
            }
        };

        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < leaderboardTable.getColumnCount(); i++) {
            leaderboardTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setBackground(ColorPalette.SL_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(ColorPalette.SL_PRIMARY_LIGHT, 1));
        scrollPane.getViewport().setBackground(ColorPalette.SL_CARD);

        panel.add(scrollPane, BorderLayout.CENTER);

        // Load data
        loadLeaderboardData(tableModel);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(ColorPalette.SL_PRIMARY_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton closeButton = createStyledButton("RETURN");
        closeButton.addActionListener(e -> dispose());

        panel.add(closeButton);

        return panel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(fontManager.getBodyFont());
        button.setForeground(ColorPalette.SL_TEXT_PRIMARY);
        button.setBackground(ColorPalette.SL_PRIMARY);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ColorPalette.SL_PRIMARY_LIGHT);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ColorPalette.SL_ACCENT, 1),
                    BorderFactory.createEmptyBorder(7, 14, 7, 14)
                ));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(ColorPalette.SL_PRIMARY);
                button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            }
        });

        return button;
    }

    private void loadLeaderboardData(DefaultTableModel tableModel) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("leaderboard");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<UserData> users = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    String username = child.getKey();
                    Integer level = child.child("level").getValue(Integer.class);
                    Integer xp = child.child("xp").getValue(Integer.class);
                    Integer completedTasks = child.child("completedTasks").getValue(Integer.class);

                    if (username != null && level != null && xp != null && completedTasks != null) {
                        users.add(new UserData(username, level, xp, completedTasks));
                    }
                }

                // Sort by level, then XP
                users.sort((u1, u2) -> {
                    if (u2.getLevel() != u1.getLevel()) {
                        return Integer.compare(u2.getLevel(), u1.getLevel());
                    } else {
                        return Integer.compare(u2.getXp(), u1.getXp());
                    }
                });

                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0); // clear table
                    int rank = 1;
                    for (UserData user : users) {
                        // Display medals for the top 3 ranks
                        String rankDisplay;
                        switch (rank) {
                            case 1:
                                rankDisplay = "🥇 1";  // Gold medal
                                break;
                            case 2:
                                rankDisplay = "🥈 2";  // Silver medal
                                break;
                            case 3:
                                rankDisplay = "🥉 3";  // Bronze medal
                                break;
                            default:
                                rankDisplay = String.valueOf(rank);
                                break;
                        }

                        tableModel.addRow(new Object[]{
                                rankDisplay, user.getUsername(), user.getLevel(), user.getXp(), user.getCompletedTasks()
                        });
                        rank++;
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                JOptionPane.showMessageDialog(GlobalLeaderboardScreen.this,
                        "Failed to load leaderboard data.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }


    // Helper class
    private static class UserData {
        private final String username;
        private final int level;
        private final int xp;
        private final int completedTasks;

        public UserData(String username, int level, int xp, int completedTasks) {
            this.username = username;
            this.level = level;
            this.xp = xp;
            this.completedTasks = completedTasks;
        }

        public String getUsername() { return username; }
        public int getLevel() { return level; }
        public int getXp() { return xp; }
        public int getCompletedTasks() { return completedTasks; }
    }
}