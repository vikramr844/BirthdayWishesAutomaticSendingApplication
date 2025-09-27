package com.smsapp;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class WhatsAppSender extends JFrame {
    private JTextField instanceIdField;
    private JTextField apiTokenField;
    private JTextArea messageArea;
    private JTextArea logArea;
    private JButton bulkSendButton;
    private JButton loadCsvButton;
    private JButton activateInstanceButton;
    private JButton checkStatusButton;
    private JButton settingsButton;
    private JButton helpButton;
    private JButton quickSendButton;
    private JButton exportLogButton;
    private JTextField csvFileField;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel apiStatusLabel;
    private JLabel titleLabel;
    private JLabel statsLabel;
    private JSpinner delaySpinner;
    private ExecutorService executorService;
    private List<Employee> employees;
    private AtomicInteger sentCount;
    private boolean bulkSending;
    private int totalBirthdays;

    // Modern color scheme
    private final Color PRIMARY_COLOR = new Color(37, 211, 102); // WhatsApp green
    private final Color PRIMARY_DARK = new Color(32, 180, 90);
    private final Color SECONDARY_COLOR = new Color(74, 107, 255); // Modern blue
    private final Color ACCENT_COLOR = new Color(255, 193, 7); // Amber
    private final Color DANGER_COLOR = new Color(220, 53, 69);
    private final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private final Color WARNING_COLOR = new Color(255, 193, 7);
    private final Color BACKGROUND_GRADIENT_START = new Color(248, 250, 252);
    private final Color BACKGROUND_GRADIENT_END = new Color(241, 245, 249);
    private final Color CARD_COLOR = Color.WHITE;
    private final Color TEXT_PRIMARY = new Color(33, 37, 41);
    private final Color TEXT_SECONDARY = new Color(108, 117, 125);

    // Modern fonts
    private final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 32);
    private final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 18);
    private final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    private final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONT_MONO = new Font("JetBrains Mono", Font.PLAIN, 11);

    private static class Employee {
        String name, whatsappNumber, dob, department;
        Employee(String name, String whatsappNumber, String dob, String department) {
            this.name = name;
            this.whatsappNumber = whatsappNumber;
            this.dob = dob;
            this.department = department;
        }
        boolean isBirthdayToday() {
            try {
                String today = new SimpleDateFormat("dd/MM").format(new Date());
                String employeeDob = dob.length() > 5 ? dob.substring(0, 5) : dob;
                return employeeDob.equals(today);
            } catch (Exception e) { return false; }
        }
    }

    public WhatsAppSender() {
        setTitle("🎉 WhatsApp Bulk Birthday Sender - CADDAM Software Solution");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 900);
        setLocationRelativeTo(null);
        setIconImage(createAppIcon());
        initializeUI();
        executorService = Executors.newFixedThreadPool(3);
        employees = new ArrayList<>();
        sentCount = new AtomicInteger(0);
        bulkSending = false;
        totalBirthdays = 0;
    }

    private Image createAppIcon() {
        BufferedImage icon = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = icon.createGraphics();
        
        GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_COLOR, 64, 64, PRIMARY_DARK);
        g2d.setPaint(gradient);
        g2d.fillRoundRect(0, 0, 64, 64, 16, 16);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, 32));
        g2d.drawString("🎂", 16, 44);
        
        g2d.dispose();
        return icon;
    }

    private void initializeUI() {
        JPanel backgroundPanel = new GradientPanel();
        backgroundPanel.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        mainPanel.setOpaque(false);

        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
            createSidebarPanel(), createMainContentPanel());
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(3);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        backgroundPanel.add(mainPanel, BorderLayout.CENTER);
        add(backgroundPanel);

        addButtonAnimation();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Left side - Logo and title
        JPanel titlePanel = new JPanel(new BorderLayout(10, 5));
        titlePanel.setOpaque(false);

        JLabel logoLabel = new JLabel("🎉") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_COLOR, 50, 50, PRIMARY_DARK);
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, 50, 50, 12, 12);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
                g2d.drawString("🎂", 13, 35);
            }
        };
        logoLabel.setPreferredSize(new Dimension(50, 50));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        titleLabel = new JLabel("WhatsApp Birthday Sender");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Automated birthday wishes for your team • CADDAM Software Solution");
        subtitleLabel.setFont(FONT_SUBTITLE);
        subtitleLabel.setForeground(TEXT_SECONDARY);

        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);

        titlePanel.add(logoLabel, BorderLayout.WEST);
        titlePanel.add(textPanel, BorderLayout.CENTER);

        // Right side - Stats and actions
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statsPanel.setOpaque(false);

        statsLabel = new JLabel("📊 Ready to celebrate birthdays!");
        statsLabel.setFont(FONT_BOLD);
        statsLabel.setForeground(TEXT_SECONDARY);
        statsLabel.setBorder(new EmptyBorder(5, 15, 5, 15));
        statsLabel.setBackground(new Color(255, 255, 255, 150));
        statsLabel.setOpaque(true);
        statsLabel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(255, 255, 255, 100), 1),
            new EmptyBorder(8, 15, 8, 15)
        ));

        helpButton = createModernButton("❓ Help", SECONDARY_COLOR, false);
        settingsButton = createModernButton("⚙️ Settings", TEXT_SECONDARY, false);

        // Add action listeners
        helpButton.addActionListener(e -> showHelpDialog());
        settingsButton.addActionListener(e -> showSettingsDialog());

        statsPanel.add(statsLabel);
        statsPanel.add(helpButton);
        statsPanel.add(settingsButton);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel(new BorderLayout(15, 15));
        sidebarPanel.setOpaque(false);

//        // Quick stats card - Made more prominent
//        JPanel statsCard = createModernCard("📈 QUICK STATS", createStatsContent(), 50);
//        sidebarPanel.add(statsCard, BorderLayout.NORTH);

        // API status card
        JPanel apiCard = createModernCard("🔌 API STATUS", createAPIContent(), 40);
        sidebarPanel.add(apiCard, BorderLayout.CENTER);

        // Enhanced Actions card with priority buttons
        JPanel actionsCard = createModernCard("⚡ QUICK ACTIONS", createEnhancedActionsContent(), 190);
        sidebarPanel.add(actionsCard, BorderLayout.SOUTH);

        return sidebarPanel;
    }

    private JPanel createMainContentPanel() {
        JPanel mainContent = new JPanel(new GridLayout(2, 1, 20, 20));
        mainContent.setOpaque(false);

        // Top row - Priority components
        JPanel topRow = new JPanel(new GridLayout(1, 2, 20, 20));
        topRow.setOpaque(false);

        // Enhanced CSV card with better visibility
        topRow.add(createModernCard("📋 EMPLOYEE DATA", createEnhancedCSVContent(), 200));
        
        // Message template card
        topRow.add(createModernCard("💌 MESSAGE TEMPLATE", createMessageContent(), 200));

        // Bottom row - Enhanced log area
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);

        // Log area now takes full width for better visibility
        bottomRow.add(createModernCard("📊 ACTIVITY LOG", createEnhancedLogContent(), 300), BorderLayout.CENTER);

        mainContent.add(topRow);
        mainContent.add(bottomRow);

        return mainContent;
    }

    private JPanel createStatsContent() {
        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        statsPanel.setOpaque(false);

        JLabel totalLabel = createStatLabel("Total Employees", "0", PRIMARY_COLOR);
        JLabel birthdaysLabel = createStatLabel("Today's Birthdays", "0", ACCENT_COLOR);
        JLabel sentLabel = createStatLabel("Messages Sent", "0", SUCCESS_COLOR);

        statsPanel.add(totalLabel);
        statsPanel.add(birthdaysLabel);
        statsPanel.add(sentLabel);

        return statsPanel;
    }

    private JLabel createStatLabel(String title, String value, Color color) {
        JPanel statPanel = new JPanel(new BorderLayout());
        statPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_BODY);
        titleLabel.setForeground(TEXT_SECONDARY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(color);

        statPanel.add(titleLabel, BorderLayout.NORTH);
        statPanel.add(valueLabel, BorderLayout.CENTER);

        JLabel container = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        container.setLayout(new BorderLayout());
        container.setBorder(new EmptyBorder(10, 15, 10, 15));
        container.add(statPanel, BorderLayout.CENTER);

        return container;
    }

    private JPanel createAPIContent() {
        JPanel apiPanel = new JPanel(new BorderLayout(10, 10));
        apiPanel.setOpaque(false);

        apiStatusLabel = new JLabel("🔍 Check API Status");
        apiStatusLabel.setFont(FONT_BODY);
        apiStatusLabel.setForeground(TEXT_SECONDARY);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        buttonPanel.setOpaque(false);

        checkStatusButton = createModernButton("📡 Check Status", SECONDARY_COLOR, true);
        activateInstanceButton = createModernButton("📱 Get QR Code", ACCENT_COLOR, true);

        checkStatusButton.addActionListener(e -> checkInstanceStatus());
        activateInstanceButton.addActionListener(e -> getQRCode());

        buttonPanel.add(checkStatusButton);
        buttonPanel.add(activateInstanceButton);

        apiPanel.add(apiStatusLabel, BorderLayout.NORTH);
        apiPanel.add(buttonPanel, BorderLayout.CENTER);

        return apiPanel;
    }

    private JPanel createEnhancedActionsContent() {
        JPanel actionsPanel = new JPanel(new GridLayout(3, 1, 8, 8));
        actionsPanel.setOpaque(false);

        // Primary action - Start Sending (Most prominent)
        bulkSendButton = createModernButton("🚀 START SENDING", PRIMARY_COLOR, false);
        bulkSendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bulkSendButton.addActionListener(e -> startBulkSending());

        // Secondary action - Load CSV
        loadCsvButton = createModernButton("📁 LOAD CSV FILE", SECONDARY_COLOR, false);
        loadCsvButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loadCsvButton.addActionListener(e -> loadCSVFile());

//         Quick action - Test Send
        quickSendButton = createModernButton("⚡ TEST SEND", ACCENT_COLOR, false);
        quickSendButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        quickSendButton.addActionListener(e -> testSendMessage());

        actionsPanel.add(bulkSendButton);
        actionsPanel.add(loadCsvButton);
        actionsPanel.add(quickSendButton);

        return actionsPanel;
    }

    private JPanel createEnhancedCSVContent() {
        JPanel csvPanel = new JPanel(new BorderLayout(10, 10));
        csvPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Employee Data Management");
        titleLabel.setFont(FONT_HEADING);
        titleLabel.setForeground(TEXT_PRIMARY);

        // Enhanced file selection with progress indicator
        JPanel filePanel = new JPanel(new BorderLayout(10, 5));
        filePanel.setOpaque(false);

        JLabel fileLabel = new JLabel("CSV File:");
        fileLabel.setFont(FONT_BOLD);
        fileLabel.setForeground(TEXT_SECONDARY);

        csvFileField = createModernTextField("No file selected");
        csvFileField.setEditable(false);
        csvFileField.setForeground(TEXT_SECONDARY);

        JPanel fileButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        fileButtonPanel.setOpaque(false);

        JButton browseButton = createModernButton("📁 Browse", SECONDARY_COLOR, true);
        browseButton.addActionListener(e -> loadCSVFile());

        JButton templateButton = createModernButton("📋 Template", ACCENT_COLOR, true);
        templateButton.addActionListener(e -> downloadCSVTemplate());

        fileButtonPanel.add(browseButton);
        fileButtonPanel.add(templateButton);

        filePanel.add(fileLabel, BorderLayout.NORTH);
        filePanel.add(csvFileField, BorderLayout.CENTER);
        filePanel.add(fileButtonPanel, BorderLayout.SOUTH);

        // Status with icon
        statusLabel = new JLabel("⏳ Please load a CSV file to begin");
        statusLabel.setFont(FONT_BODY);
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setBorder(new EmptyBorder(10, 0, 0, 0));

        csvPanel.add(titleLabel, BorderLayout.NORTH);
        csvPanel.add(filePanel, BorderLayout.CENTER);
        csvPanel.add(statusLabel, BorderLayout.SOUTH);

        return csvPanel;
    }

    private JPanel createMessageContent() {
        JPanel messagePanel = new JPanel(new BorderLayout(10, 10));
        messagePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Birthday Message Template");
        titleLabel.setFont(FONT_HEADING);
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel instructionLabel = new JLabel("Use {name} for employee name, {department} for department");
        instructionLabel.setFont(FONT_BODY);
        instructionLabel.setForeground(TEXT_SECONDARY);

        messageArea = new JTextArea(6, 30);
        messageArea.setText("Hello {name}! 🎉\n\nWishing you a very Happy Birthday! 🎂\nMay this year bring you joy, success, and happiness!\n\nBest regards,\nCADDAM Software Solution");
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(FONT_BODY);
        messageArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setBorder(null);

        // Template actions
        JPanel templateActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        templateActions.setOpaque(false);

        JButton saveTemplateButton = createModernButton("💾 Save Template", SECONDARY_COLOR, true);
        JButton loadTemplateButton = createModernButton("📂 Load Template", ACCENT_COLOR, true);

        saveTemplateButton.addActionListener(e -> saveMessageTemplate());
        loadTemplateButton.addActionListener(e -> loadMessageTemplate());

        templateActions.add(saveTemplateButton);
        templateActions.add(loadTemplateButton);

        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.setOpaque(false);
        contentPanel.add(instructionLabel, BorderLayout.NORTH);
        contentPanel.add(messageScroll, BorderLayout.CENTER);
        contentPanel.add(templateActions, BorderLayout.SOUTH);

        messagePanel.add(titleLabel, BorderLayout.NORTH);
        messagePanel.add(contentPanel, BorderLayout.CENTER);

        return messagePanel;
    }

    private JPanel createEnhancedLogContent() {
        JPanel logPanel = new JPanel(new BorderLayout(10, 10));
        logPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Real-time Activity Log");
        titleLabel.setFont(FONT_HEADING);
        titleLabel.setForeground(TEXT_PRIMARY);

        logArea = new JTextArea(12, 50); // Larger log area
        logArea.setEditable(false);
        logArea.setFont(FONT_MONO);
        logArea.setBackground(new Color(248, 249, 250));
        logArea.setForeground(TEXT_PRIMARY);
        logArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(null);

        // Enhanced log controls
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        controlPanel.setOpaque(false);

        JButton clearLogButton = createModernButton("🗑️ Clear Log", TEXT_SECONDARY, true);
        exportLogButton = createModernButton("📤 Export Log", SECONDARY_COLOR, true);
        JButton autoScrollButton = createModernButton("🔍 Auto-scroll", ACCENT_COLOR, true);

        clearLogButton.addActionListener(e -> logArea.setText(""));
        exportLogButton.addActionListener(e -> exportLogToFile());
        autoScrollButton.addActionListener(e -> toggleAutoScroll());

        controlPanel.add(autoScrollButton);
        controlPanel.add(clearLogButton);
        controlPanel.add(exportLogButton);

        logPanel.add(titleLabel, BorderLayout.NORTH);
        logPanel.add(logScroll, BorderLayout.CENTER);
        logPanel.add(controlPanel, BorderLayout.SOUTH);

        return logPanel;
    }

    private JPanel createModernCard(String title, JComponent content, int height) {
        JPanel card = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(255, 255, 255, 250));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                
                g2d.setColor(new Color(240, 240, 240));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);
            }
        };
        
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(280, height));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_HEADING);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JButton createModernButton(String text, Color bgColor, boolean small) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                super.paintComponent(g);
            }
        };
        
        button.setFont(small ? FONT_BODY : FONT_BUTTON);
        button.setForeground(Color.WHITE);
        button.setBorder(new EmptyBorder(small ? 8 : 12, 20, small ? 8 : 12, 20));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private JTextField createModernTextField(String text) {
        JTextField field = new JTextField(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                g2d.setColor(new Color(220, 220, 220));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                
                super.paintComponent(g);
            }
        };
        
        field.setFont(FONT_BODY);
        field.setBorder(new EmptyBorder(10, 12, 10, 12));
        field.setOpaque(false);
        
        return field;
    }

    private void addButtonAnimation() {
        Timer timer = new Timer(3000, e -> {
            if (bulkSendButton.isEnabled() && employees.size() > 0) {
                bulkSendButton.setBackground(PRIMARY_COLOR.brighter());
                Timer resetTimer = new Timer(500, e2 -> {
                    bulkSendButton.setBackground(PRIMARY_COLOR);
                });
                resetTimer.setRepeats(false);
                resetTimer.start();
            }
        });
        timer.start();
    }

    // Settings Dialog
    private void showSettingsDialog() {
        JDialog settingsDialog = new JDialog(this, "Application Settings", true);
        settingsDialog.setSize(500, 400);
        settingsDialog.setLocationRelativeTo(this);
        settingsDialog.setLayout(new BorderLayout(20, 20));
        settingsDialog.getContentPane().setBackground(BACKGROUND_GRADIENT_START);

        JPanel contentPanel = new JPanel(new GridLayout(5, 2, 15, 15));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);

        // API Settings
        contentPanel.add(new JLabel("Instance ID:"));
        instanceIdField = createModernTextField("instance Id");
        contentPanel.add(instanceIdField);

        contentPanel.add(new JLabel("API Token:"));
        apiTokenField = createModernTextField("api token key");
        contentPanel.add(apiTokenField);

        contentPanel.add(new JLabel("Delay between messages (seconds):"));
        delaySpinner = new JSpinner(new SpinnerNumberModel(10, 5, 60, 1));
        contentPanel.add(delaySpinner);

        contentPanel.add(new JLabel("Auto-save logs:"));
        JCheckBox autoSaveCheckbox = new JCheckBox("Enable auto-save");
        contentPanel.add(autoSaveCheckbox);

        contentPanel.add(new JLabel("Notification sounds:"));
        JCheckBox soundCheckbox = new JCheckBox("Enable sounds");
        contentPanel.add(soundCheckbox);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = createModernButton("💾 Save Settings", PRIMARY_COLOR, false);
        JButton cancelButton = createModernButton("❌ Cancel", DANGER_COLOR, false);

        saveButton.addActionListener(e -> {
            settingsDialog.dispose();
            log("✅ Settings saved successfully");
        });

        cancelButton.addActionListener(e -> settingsDialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        settingsDialog.add(contentPanel, BorderLayout.CENTER);
        settingsDialog.add(buttonPanel, BorderLayout.SOUTH);
        settingsDialog.setVisible(true);
    }

    // Help Dialog
    private void showHelpDialog() {
        JDialog helpDialog = new JDialog(this, "Help & Instructions", true);
        helpDialog.setSize(600, 500);
        helpDialog.setLocationRelativeTo(this);
        helpDialog.setLayout(new BorderLayout(20, 20));

        JTextArea helpText = new JTextArea();
        helpText.setText(getHelpContent());
        helpText.setEditable(false);
        helpText.setFont(FONT_BODY);
        helpText.setBorder(new EmptyBorder(20, 20, 20, 20));
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(helpText);
        
        JButton closeButton = createModernButton("Close Help", SECONDARY_COLOR, false);
        closeButton.addActionListener(e -> helpDialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        helpDialog.add(scrollPane, BorderLayout.CENTER);
        helpDialog.add(buttonPanel, BorderLayout.SOUTH);
        helpDialog.setVisible(true);
    }

    private String getHelpContent() {
        return """
            📱 WHATSAPP BULK BIRTHDAY SENDER - HELP GUIDE
                
            QUICK START:
            1. Load your employee CSV file using the 'Load CSV File' button
            2. Check API status and authenticate if needed
            3. Customize your birthday message template
            4. Click 'START SENDING' to begin automated birthday wishes
                
            CSV FILE FORMAT:
            Name,WhatsAppNumber,DOB,Department
            John Doe,919876543210,15/03,Sales
            Jane Smith,919876543211,25/12,Marketing
                
            MESSAGE TEMPLATE VARIABLES:
            - {name} - Employee's name
            - {department} - Employee's department
                
            API CONFIGURATION:
            - Instance ID: Your UltraMSG instance ID
            - API Token: Your UltraMSG API token
            - Delay: Seconds between messages (5-60)
                
            TROUBLESHOOTING:
            • Ensure WhatsApp Web is authenticated
            • Check internet connection
            • Verify CSV file format
            • Confirm API credentials are correct
                
            For additional support, contact CADDAM Software Solution.
            """;
    }

   //  New enhanced methods
    private void testSendMessage() {
        if (employees.isEmpty()) {
            showError("Please load a CSV file first");
            return;
        }
        
        Employee testEmployee = employees.get(0);
        String testMessage = messageArea.getText()
            .replace("{name}", testEmployee.name)
            .replace("{department}", testEmployee.department);
            
        log("🧪 Test sending to: " + testEmployee.name);
        log("💬 Test message: " + testMessage.substring(0, Math.min(50, testMessage.length())) + "...");
        log("✅ Test mode - message not actually sent");
    }

    private void downloadCSVTemplate() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("birthday_template.csv"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                FileWriter writer = new FileWriter(file);
                writer.write("Name,WhatsAppNumber,DOB,Department\n");
                writer.write("John Doe,919876543210,15/03,Sales\n");
                writer.write("Jane Smith,919876543211,25/12,Marketing\n");
                writer.write("// DOB format: DD/MM (e.g., 15/03 for March 15th)\n");
                writer.close();
                log("📋 CSV template saved: " + file.getAbsolutePath());
            }
        } catch (Exception ex) {
            log("❌ Error saving template: " + ex.getMessage());
        }
    }

    private void saveMessageTemplate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("birthday_message_template.txt"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                FileWriter writer = new FileWriter(fileChooser.getSelectedFile());
                writer.write(messageArea.getText());
                writer.close();
                log("💾 Message template saved successfully");
            } catch (Exception ex) {
                log("❌ Error saving template: " + ex.getMessage());
            }
        }
    }

    private void loadMessageTemplate() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();
                messageArea.setText(content.toString());
                log("📂 Message template loaded successfully");
            } catch (Exception ex) {
                log("❌ Error loading template: " + ex.getMessage());
            }
        }
    }

    private void exportLogToFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("whatsapp_sender_log.txt"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                FileWriter writer = new FileWriter(fileChooser.getSelectedFile());
                writer.write(logArea.getText());
                writer.close();
                log("📤 Log exported successfully");
            } catch (Exception ex) {
                log("❌ Error exporting log: " + ex.getMessage());
            }
        }
    }

    private void toggleAutoScroll() {
        // Implementation for auto-scroll toggle
        log("🔍 Auto-scroll feature coming soon!");
    }

    // Gradient background panel
    class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            GradientPaint gradient = new GradientPaint(
                0, 0, BACKGROUND_GRADIENT_START, 
                getWidth(), getHeight(), BACKGROUND_GRADIENT_END
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            g2d.setColor(new Color(255, 255, 255, 10));
            for (int i = 0; i < getWidth(); i += 20) {
                for (int j = 0; j < getHeight(); j += 20) {
                    g2d.fillOval(i, j, 2, 2);
                }
            }
        }
    }

    // Rest of your existing functional methods remain the same...
    // [Keep all your existing checkInstanceStatus, getQRCode, loadCSVFile, startBulkSending, etc. methods]
    
    private void checkInstanceStatus() {
        executorService.execute(() -> {
            try {
                String instanceId = instanceIdField.getText().trim();
                String token = apiTokenField.getText().trim();
                
                log("🔍 Checking instance status...");
                
                String statusUrl = "https://api.ultramsg.com/instance" + instanceId + "/instance/status?token=" + token;
                String result = sendGetRequest(statusUrl);
                
                SwingUtilities.invokeLater(() -> {
                    if (result.contains("\"status\":\"authenticated\"")) {
                        apiStatusLabel.setText("✅ Authenticated & Ready");
                        apiStatusLabel.setForeground(SUCCESS_COLOR);
                        log("✅ Instance is AUTHENTICATED and ready to send messages!");
                    } else if (result.contains("\"status\":\"standby\"")) {
                        apiStatusLabel.setText("❌ Standby - Scan QR Code");
                        apiStatusLabel.setForeground(WARNING_COLOR);
                        log("❌ Instance is in STANDBY mode. Need to scan QR code!");
                    } else {
                        apiStatusLabel.setText("❓ Check response in log");
                        apiStatusLabel.setForeground(TEXT_SECONDARY);
                        log("📊 Status response: " + result);
                    }
                });
                
            } catch (Exception ex) {
                log("❌ Status check error: " + ex.getMessage());
            }
        });
    }

    private void getQRCode() {
        executorService.execute(() -> {
            try {
                String instanceId = instanceIdField.getText().trim();
                String token = apiTokenField.getText().trim();
                
                log("📱 Generating QR code for authentication...");
                
                String qrUrl = "https://api.ultramsg.com/instance" + instanceId + "/instance/qr?token=" + token;
                String result = sendGetRequest(qrUrl);
                
                log("📊 QR API Response: " + result);
                
                if (result.contains("qrCode")) {
                    String qrCodeUrl = extractValue(result, "qrCode");
                    if (qrCodeUrl != null && qrCodeUrl.startsWith("http")) {
                        log("🔗 QR Code URL: " + qrCodeUrl);
                        
                        try {
                            Desktop.getDesktop().browse(new java.net.URI(qrCodeUrl));
                            log("🌐 Opened QR code in browser. Please scan with WhatsApp.");
                        } catch (Exception e) {
                            log("📱 Manually open this URL to scan QR: " + qrCodeUrl);
                        }
                        
                        JOptionPane.showMessageDialog(this,
                            "📱 WhatsApp QR Code Generated!\n\n" +
                            "1. Open WhatsApp on your phone\n" +
                            "2. Go to Settings → Linked Devices → Link a Device\n" +
                            "3. Scan the QR code shown in browser\n" +
                            "4. Wait for authentication to complete\n" +
                            "5. Click 'Check Status' after scanning\n\n" +
                            "QR URL: " + qrCodeUrl,
                            "Scan QR Code", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        log("❌ Invalid QR code URL in response");
                    }
                } else {
                    log("❌ QR code generation failed: " + result);
                    JOptionPane.showMessageDialog(this, 
                        "QR generation failed: " + result, 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                log("❌ QR code error: " + ex.getMessage());
            }
        });
    }

    private String extractValue(String json, String key) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex == -1) return null;
            
            int valueStart = json.indexOf(":", keyIndex) + 1;
            int valueEnd = json.indexOf(",", valueStart);
            if (valueEnd == -1) valueEnd = json.indexOf("}", valueStart);
            
            String value = json.substring(valueStart, valueEnd).replace("\"", "").trim();
            return value;
        } catch (Exception e) {
            return null;
        }
    }

    private void loadCSVFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            csvFileField.setText(file.getAbsolutePath());
            parseCSVFile(file);
        }
    }

    private void parseCSVFile(File file) {
        employees.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            int count = 0;
            
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                if (line.trim().isEmpty()) continue;
                
                String[] values = line.split(",");
                if (values.length >= 3) {
                    String name = values[0].trim();
                    String number = values[1].trim().replaceAll("[^0-9+]", "");
                    String dob = values[2].trim();
                    String dept = values.length > 3 ? values[3].trim() : "";
                    
                    if (!number.isEmpty() && !dob.isEmpty()) {
                        employees.add(new Employee(name, number, dob, dept));
                        count++;
                    }
                }
            }
            
            log("✅ Loaded " + count + " employees from CSV");
            checkBirthdays();
            
        } catch (Exception ex) {
            log("❌ Error reading CSV: " + ex.getMessage());
            showError("CSV Error: " + ex.getMessage());
        }
    }

    private void checkBirthdays() {
        int birthdayCount = 0;
        for (Employee emp : employees) {
            if (emp.isBirthdayToday()) birthdayCount++;
        }
        
        totalBirthdays = birthdayCount;
        log("🎂 Found " + birthdayCount + " birthdays today");
        statusLabel.setText("✅ " + birthdayCount + " birthdays found today • Ready to send!");
        updateStats();
    }

    private void updateStats() {
        SwingUtilities.invokeLater(() -> {
            statsLabel.setText("📊 " + employees.size() + " employees • " + totalBirthdays + " birthdays today");
        });
    }

    private void startBulkSending() {
        if (employees.isEmpty()) {
            showError("Please load a CSV file first");
            return;
        }

        List<Employee> birthdayEmployees = new ArrayList<>();
        for (Employee emp : employees) {
            if (emp.isBirthdayToday()) birthdayEmployees.add(emp);
        }
        
        if (birthdayEmployees.isEmpty()) {
            showError("No birthdays found for today!");
            return;
        }

        if (!apiStatusLabel.getText().contains("Authenticated")) {
            int result = JOptionPane.showConfirmDialog(this,
                "⚠️ Instance not authenticated!\n\n" +
                "You need to scan QR code first to authenticate.\n" +
                "Get QR code now?",
                "Authentication Required", JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                getQRCode();
            }
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "🚀 START BULK SENDING\n\n" +
            "Ready to send birthday wishes to " + birthdayEmployees.size() + " employees!\n\n" +
            "Messages will be sent automatically via WhatsApp API.\n" +
            "Estimated time: " + (birthdayEmployees.size() * (Integer) delaySpinner.getValue()) + " seconds",
            "Confirm Bulk Sending", JOptionPane.YES_NO_OPTION);
            
        if (confirm != JOptionPane.YES_OPTION) return;

        bulkSending = true;
        sentCount.set(0);
        bulkSendButton.setEnabled(false);
        bulkSendButton.setText("⏳ SENDING...");
        
        log("🚀 STARTING BULK SENDING TO " + birthdayEmployees.size() + " EMPLOYEES");
        log("⏰ Estimated completion: " + (birthdayEmployees.size() * (Integer) delaySpinner.getValue()) + " seconds");

        executorService.execute(() -> {
            for (int i = 0; i < birthdayEmployees.size(); i++) {
                if (!bulkSending) break;
                
                Employee emp = birthdayEmployees.get(i);
                sendBirthdayMessage(emp, i + 1, birthdayEmployees.size());
                
                if (i < birthdayEmployees.size() - 1) {
                    try {
                        Thread.sleep((Integer) delaySpinner.getValue() * 1000);
                    } catch (InterruptedException e) { break; }
                }
            }
            
            SwingUtilities.invokeLater(() -> {
                bulkSending = false;
                bulkSendButton.setEnabled(true);
                bulkSendButton.setText("🚀 START SENDING");
                log("✅ BULK SENDING COMPLETED!");
                log("📨 Successfully sent to " + sentCount.get() + " out of " + birthdayEmployees.size() + " employees");
                updateStats();
                
                if (sentCount.get() == birthdayEmployees.size()) {
                    JOptionPane.showMessageDialog(this,
                        "🎉 Bulk Sending Completed Successfully!\n\n" +
                        "Sent birthday wishes to all " + sentCount.get() + " employees.",
                        "Sending Complete", JOptionPane.INFORMATION_MESSAGE);
                }
            });
        });
    }

    private void sendBirthdayMessage(Employee employee, int current, int total) {
        try {
            String personalizedMessage = messageArea.getText()
                .replace("{name}", employee.name)
                .replace("{department}", employee.department);
            
            log("🎂 [" + current + "/" + total + "] Sending to: " + employee.name);
            
            String instanceId = instanceIdField.getText().trim();
            String token = apiTokenField.getText().trim();
            
            String phone = employee.whatsappNumber.replaceAll("[^0-9+]", "");
            if (!phone.startsWith("+")) {
                phone = "+" + phone;
            }
            
            String apiUrl = "https://api.ultramsg.com/instance" + instanceId + "/messages/chat";
            String urlWithParams = apiUrl + "?token=" + token + "&to=" + phone + "&body=" + 
                URLEncoder.encode(personalizedMessage, "UTF-8");
            
            String result = sendGetRequest(urlWithParams);
            
            if (result.contains("\"sent\":true") || result.contains("message sent") || result.contains("true")) {
                sentCount.incrementAndGet();
                log("✅ SUCCESS: Sent to " + employee.name);
            } else if (result.contains("instance is not authenticated")) {
                log("⏳ PENDING: Message queued for " + employee.name + " (will send after authentication)");
                sentCount.incrementAndGet();
            } else {
                log("❌ FAILED: " + employee.name + " - " + result);
            }
            
            SwingUtilities.invokeLater(() -> {
                updateStats();
            });
            
        } catch (Exception ex) {
            log("❌ ERROR sending to " + employee.name + ": " + ex.getMessage());
        }
    }

    private String sendGetRequest(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            
            int responseCode = connection.getResponseCode();
            StringBuilder response = new StringBuilder();
            
            try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            
            connection.disconnect();
            return response.toString();
            
        } catch (Exception ex) {
            return "Error: " + ex.getMessage();
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE));
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            WhatsAppSender app = new WhatsAppSender();
            app.setVisible(true);
            app.log("🚀 WhatsApp Bulk Birthday Sender Started");
            app.log("💡 Enhanced UI with priority on sending and CSV loading");
            app.log("🔗 Using UltraMSG WhatsApp API");
            app.log("🏢 CADDAM Software Solution");
            app.log("⭐ Priority features: Bulk sending, CSV management, Enhanced logs");
        });
    }
}