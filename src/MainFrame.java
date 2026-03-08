import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MainFrame extends JFrame {

    private final DefaultTableModel recipeTableModel = new DefaultTableModel(
        new String[]{"ID", "Nazwa", "Kategoria", "Czas (min)", "Porcje"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable recipeTable = new JTable(recipeTableModel);
    private final JTextField searchField = new JTextField(18);
    private final JComboBox<Object> categoryFilterCombo = new JComboBox<>();

    private final JLabel detailNameLabel = new JLabel(" ");
    private final JLabel detailInfoLabel = new JLabel(" ");
    private final JTextArea detailDescArea = new JTextArea(3, 30);
    private final DefaultTableModel detailIngModel = new DefaultTableModel(
        new String[]{"Składnik", "Ilość", "Jednostka"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable detailIngTable = new JTable(detailIngModel);

    private final DefaultTableModel ingTableModel = new DefaultTableModel(
        new String[]{"ID", "Nazwa", "Jednostka"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable ingTable = new JTable(ingTableModel);

    private final DefaultTableModel catTableModel = new DefaultTableModel(
        new String[]{"ID", "Nazwa"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable catTable = new JTable(catTableModel);

    public MainFrame() {
        setTitle("Zbior Przepisow Kulinarnych");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 660);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null);

        applyTheme();
        buildUI();

        loadRecipes();
        loadIngredients();
        loadCategories();
        refreshCategoryFilter();
    }

    private void buildUI() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Przepisy", buildRecipePanel());
        tabs.addTab("Skladniki", buildIngredientPanel());
        tabs.addTab("Kategorie", buildCategoryPanel());
        add(tabs);
    }

    private JPanel buildRecipePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchBar.add(new JLabel("Szukaj:"));
        searchBar.add(searchField);

        JButton searchBtn = new JButton("Szukaj");
        JButton resetBtn  = new JButton("Reset");
        searchBtn.setFocusPainted(false);
        resetBtn.setFocusPainted(false);

        searchBtn.addActionListener(e -> searchRecipes());
        searchField.addActionListener(e -> searchRecipes());
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            categoryFilterCombo.setSelectedIndex(0);
            loadRecipes();
        });

        searchBar.add(searchBtn);
        searchBar.add(resetBtn);
        searchBar.add(new JLabel("  Kategoria:"));
        categoryFilterCombo.setPreferredSize(new Dimension(160, 25));
        searchBar.add(categoryFilterCombo);

        recipeTable.setRowHeight(26);
        recipeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hideColumn(recipeTable, 0); // ukryj kolumnę ID
        recipeTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JPanel detailPanel = buildDetailPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            new JScrollPane(recipeTable), detailPanel);
        splitPane.setDividerLocation(480);
        splitPane.setResizeWeight(0.55);

        recipeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showRecipeDetails();
        });

        JButton addBtn    = makeButton("+ Dodaj",  new Color(60, 140, 60));
        JButton editBtn   = makeButton("Edytuj",   new Color(30, 100, 180));
        JButton deleteBtn = makeButton("Usun",     new Color(190, 50, 50));
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        recipeTable.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = recipeTable.getSelectedRow() >= 0;
            editBtn.setEnabled(sel);
            deleteBtn.setEnabled(sel);
        });

        addBtn.addActionListener(e -> {
            RecipeDialog dlg = new RecipeDialog(this, null);
            dlg.setVisible(true);
            if (dlg.isSaved()) { loadRecipes(); refreshCategoryFilter(); clearDetails(); }
        });

        editBtn.addActionListener(e -> {
            int id = getSelectedId(recipeTable);
            if (id < 0) return;
            Recipe r = DatabaseHelper.getRecipeById(id);
            RecipeDialog dlg = new RecipeDialog(this, r);
            dlg.setVisible(true);
            if (dlg.isSaved()) { loadRecipes(); showRecipeDetails(); }
        });

        deleteBtn.addActionListener(e -> {
            int id = getSelectedId(recipeTable);
            if (id < 0) return;
            int confirm = JOptionPane.showConfirmDialog(this,
                "Czy na pewno chcesz usunac ten przepis?", "Potwierdzenie",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                DatabaseHelper.deleteRecipe(id);
                loadRecipes();
                clearDetails();
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        btnPanel.add(addBtn); btnPanel.add(editBtn); btnPanel.add(deleteBtn);

        panel.add(searchBar,  BorderLayout.NORTH);
        panel.add(splitPane,  BorderLayout.CENTER);
        panel.add(btnPanel,   BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Szczegoly przepisu"));

        detailNameLabel.setFont(detailNameLabel.getFont().deriveFont(Font.BOLD, 15f));
        detailDescArea.setEditable(false);
        detailDescArea.setLineWrap(true);
        detailDescArea.setWrapStyleWord(true);
        detailDescArea.setBackground(panel.getBackground());
        detailIngTable.setRowHeight(22);

        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.add(detailNameLabel,  BorderLayout.NORTH);
        top.add(detailInfoLabel,  BorderLayout.CENTER);
        top.add(new JScrollPane(detailDescArea), BorderLayout.SOUTH);

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(detailIngTable), BorderLayout.CENTER);

        return panel;
    }

    private void showRecipeDetails() {
        int id = getSelectedId(recipeTable);
        if (id < 0) { clearDetails(); return; }

        Recipe r = DatabaseHelper.getRecipeById(id);
        if (r == null) return;

        detailNameLabel.setText(r.getName());
        detailInfoLabel.setText(
            (r.getCategory() != null ? r.getCategory().getName() : "-") +
            "   |   Czas: " + r.getPrepTime() + " min" +
            "   |   Porcje: " + r.getPortions()
        );
        detailDescArea.setText(r.getDescription());

        detailIngModel.setRowCount(0);
        for (RecipeIngredient ri : r.getIngredients()) {
            String qty = ri.getQuantity() % 1 == 0
                ? String.valueOf((int) ri.getQuantity())
                : String.valueOf(ri.getQuantity());
            detailIngModel.addRow(new Object[]{ri.getIngredient().getName(), qty, ri.getUnit()});
        }
    }

    private void clearDetails() {
        detailNameLabel.setText(" ");
        detailInfoLabel.setText(" ");
        detailDescArea.setText("");
        detailIngModel.setRowCount(0);
    }

    // ─────────────────────────── INGREDIENT PANEL ───────────────────────────

    private JPanel buildIngredientPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ingTable.setRowHeight(26);
        ingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hideColumn(ingTable, 0);

        JButton addBtn    = makeButton("+ Dodaj", new Color(60, 140, 60));
        JButton editBtn   = makeButton("Edytuj",  new Color(30, 100, 180));
        JButton deleteBtn = makeButton("Usun",    new Color(190, 50, 50));
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        ingTable.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = ingTable.getSelectedRow() >= 0;
            editBtn.setEnabled(sel); deleteBtn.setEnabled(sel);
        });

        addBtn.addActionListener(e -> {
            IngredientDialog dlg = new IngredientDialog(this, null);
            dlg.setVisible(true);
            if (dlg.isSaved()) loadIngredients();
        });
        editBtn.addActionListener(e -> {
            int id = getSelectedId(ingTable);
            if (id < 0) return;
            IngredientDialog dlg = new IngredientDialog(this, DatabaseHelper.getIngredientById(id));
            dlg.setVisible(true);
            if (dlg.isSaved()) loadIngredients();
        });
        deleteBtn.addActionListener(e -> {
            int id = getSelectedId(ingTable);
            if (id < 0) return;
            int c = JOptionPane.showConfirmDialog(this, "Usunac ten skladnik?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) { DatabaseHelper.deleteIngredient(id); loadIngredients(); }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        btnPanel.add(addBtn); btnPanel.add(editBtn); btnPanel.add(deleteBtn);

        panel.add(new JScrollPane(ingTable), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ─────────────────────────── CATEGORY PANEL ───────────────────────────

    private JPanel buildCategoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        catTable.setRowHeight(26);
        catTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hideColumn(catTable, 0);

        JButton addBtn    = makeButton("+ Dodaj", new Color(60, 140, 60));
        JButton editBtn   = makeButton("Edytuj",  new Color(30, 100, 180));
        JButton deleteBtn = makeButton("Usun",    new Color(190, 50, 50));
        editBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        catTable.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = catTable.getSelectedRow() >= 0;
            editBtn.setEnabled(sel); deleteBtn.setEnabled(sel);
        });

        addBtn.addActionListener(e -> {
            CategoryDialog dlg = new CategoryDialog(this, null);
            dlg.setVisible(true);
            if (dlg.isSaved()) { loadCategories(); refreshCategoryFilter(); }
        });
        editBtn.addActionListener(e -> {
            int id = getSelectedId(catTable);
            if (id < 0) return;
            CategoryDialog dlg = new CategoryDialog(this, DatabaseHelper.getCategoryById(id));
            dlg.setVisible(true);
            if (dlg.isSaved()) { loadCategories(); refreshCategoryFilter(); }
        });
        deleteBtn.addActionListener(e -> {
            int id = getSelectedId(catTable);
            if (id < 0) return;
            int c = JOptionPane.showConfirmDialog(this, "Usunac te kategorie?", "Potwierdzenie", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) { DatabaseHelper.deleteCategory(id); loadCategories(); refreshCategoryFilter(); }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        btnPanel.add(addBtn); btnPanel.add(editBtn); btnPanel.add(deleteBtn);

        panel.add(new JScrollPane(catTable), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ─────────────────────────── DATA LOADING ───────────────────────────

    private void loadRecipes() {
        recipeTableModel.setRowCount(0);
        for (Recipe r : DatabaseHelper.getAllRecipes()) {
            recipeTableModel.addRow(new Object[]{
                r.getId(), r.getName(),
                r.getCategory() != null ? r.getCategory().getName() : "-",
                r.getPrepTime() > 0 ? r.getPrepTime() : "-",
                r.getPortions() > 0 ? r.getPortions() : "-"
            });
        }
    }

    private void loadIngredients() {
        ingTableModel.setRowCount(0);
        for (Ingredient i : DatabaseHelper.getAllIngredients())
            ingTableModel.addRow(new Object[]{i.getId(), i.getName(), i.getUnit()});
    }

    private void loadCategories() {
        catTableModel.setRowCount(0);
        for (Category c : DatabaseHelper.getAllCategories())
            catTableModel.addRow(new Object[]{c.getId(), c.getName()});
    }

    private void refreshCategoryFilter() {
        Object selected = categoryFilterCombo.getSelectedItem();
        categoryFilterCombo.removeAllItems();
        categoryFilterCombo.addItem("Wszystkie kategorie");
        for (Category c : DatabaseHelper.getAllCategories()) categoryFilterCombo.addItem(c);
        if (selected instanceof Category) categoryFilterCombo.setSelectedItem(selected);
        else categoryFilterCombo.setSelectedIndex(0);
    }

    private void searchRecipes() {
        String keyword = searchField.getText().trim();
        Object sel = categoryFilterCombo.getSelectedItem();
        int catId = (sel instanceof Category) ? ((Category) sel).getId() : 0;

        recipeTableModel.setRowCount(0);
        for (Recipe r : DatabaseHelper.searchRecipes(keyword, catId)) {
            recipeTableModel.addRow(new Object[]{
                r.getId(), r.getName(),
                r.getCategory() != null ? r.getCategory().getName() : "-",
                r.getPrepTime() > 0 ? r.getPrepTime() : "-",
                r.getPortions() > 0 ? r.getPortions() : "-"
            });
        }
        clearDetails();
    }

    // ─────────────────────────── HELPERS ───────────────────────────

    private void hideColumn(JTable table, int col) {
        table.getColumnModel().getColumn(col).setMaxWidth(0);
        table.getColumnModel().getColumn(col).setMinWidth(0);
        table.getColumnModel().getColumn(col).setWidth(0);
    }

    private int getSelectedId(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) return -1;
        return (int) table.getModel().getValueAt(row, 0);
    }

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(110, 30));
        return btn;
    }

    private void applyTheme() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}
    }
}
