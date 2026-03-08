import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RecipeDialog extends JDialog {

    private final JTextField nameField              = new JTextField(25);
    private final JComboBox<Category> categoryCombo = new JComboBox<>();
    private final JTextField prepTimeField          = new JTextField(6);
    private final JTextField portionsField          = new JTextField(6);
    private final JTextArea descriptionArea         = new JTextArea(4, 25);

    private final JComboBox<Ingredient> ingredientCombo = new JComboBox<>();
    private final JTextField quantityField          = new JTextField(6);
    private final JComboBox<String> unitCombo       = new JComboBox<>(new String[]{
            "g", "kg", "ml", "l", "szt.", "lyzka", "lyzeczka", "szklanka", "szczypta", "zabek", "plaster", "peczek"
    });

    private final DefaultTableModel ingredientTableModel = new DefaultTableModel(
            new String[]{"Skladnik", "Ilosc", "Jednostka"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable ingredientTable = new JTable(ingredientTableModel);

    private JButton addIngBtn;
    private JButton updateIngBtn;
    private JButton removeIngBtn;

    private final List<RecipeIngredient> recipeIngredients = new ArrayList<>();
    private boolean saved = false;
    private Recipe recipe;

    public RecipeDialog(Frame parent, Recipe recipe) {
        super(parent, recipe == null ? "Nowy przepis" : "Edytuj przepis", true);
        this.recipe = recipe;

        loadCategories();
        loadIngredients();
        buildUI();

        if (recipe != null) {
            fillForm(recipe);
        }

        setSize(640, 680);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void loadCategories() {
        categoryCombo.removeAllItems();
        DatabaseHelper.getAllCategories().forEach(categoryCombo::addItem);
    }

    private void loadIngredients() {
        ingredientCombo.removeAllItems();
        DatabaseHelper.getAllIngredients().forEach(ingredientCombo::addItem);
    }

    private void fillForm(Recipe r) {
        nameField.setText(r.getName());
        prepTimeField.setText(r.getPrepTime() > 0 ? String.valueOf(r.getPrepTime()) : "");
        portionsField.setText(r.getPortions() > 0 ? String.valueOf(r.getPortions()) : "");
        descriptionArea.setText(r.getDescription() != null ? r.getDescription() : "");

        if (r.getCategory() != null) {
            for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                if (categoryCombo.getItemAt(i).getId() == r.getCategory().getId()) {
                    categoryCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        recipeIngredients.clear();
        ingredientTableModel.setRowCount(0);
        for (RecipeIngredient ri : DatabaseHelper.getRecipeIngredients(r.getId())) {
            recipeIngredients.add(ri);
            ingredientTableModel.addRow(new Object[]{
                    ri.getIngredient().getName(),
                    formatQty(ri.getQuantity()),
                    ri.getUnit()
            });
        }
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Informacje o przepisie"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Nazwa:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3;
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Kategoria:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3;
        formPanel.add(categoryCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Czas (min):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.3; gbc.gridwidth = 1;
        formPanel.add(prepTimeField, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        formPanel.add(new JLabel("Porcje:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.3;
        formPanel.add(portionsField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Opis:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(descriptionArea), gbc);

        JPanel ingPanel = new JPanel(new BorderLayout(5, 5));
        ingPanel.setBorder(BorderFactory.createTitledBorder("Skladniki przepisu"));

        ingredientTable.setRowHeight(24);
        ingredientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(ingredientTable);
        tableScroll.setPreferredSize(new Dimension(0, 130));

        ingredientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelected();
        });

        JPanel formRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        formRow.add(new JLabel("Skladnik:"));
        ingredientCombo.setPreferredSize(new Dimension(150, 25));
        formRow.add(ingredientCombo);
        formRow.add(new JLabel("Ilosc:"));
        quantityField.setPreferredSize(new Dimension(55, 25));
        formRow.add(quantityField);
        formRow.add(new JLabel("Jedn.:"));
        formRow.add(unitCombo);

        addIngBtn    = makeIngButton("+ Dodaj",    new Color(0, 120, 215));
        updateIngBtn = makeIngButton("Aktualizuj", new Color(160, 100, 0));
        removeIngBtn = makeIngButton("Usun",       new Color(200, 50, 50));

        updateIngBtn.setEnabled(false);
        removeIngBtn.setEnabled(false);

        addIngBtn.addActionListener(e -> addIngredient());
        updateIngBtn.addActionListener(e -> updateIngredient());
        removeIngBtn.addActionListener(e -> removeIngredient());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        btnRow.add(addIngBtn);
        btnRow.add(updateIngBtn);
        btnRow.add(removeIngBtn);

        JLabel hint = new JLabel("<html><i>Kliknij wiersz aby zaladowac do edycji. Mozna zmienic skladnik, ilosc, jednostke i kliknac Aktualizuj.</i></html>");
        hint.setFont(hint.getFont().deriveFont(10f));
        hint.setForeground(Color.GRAY);

        JPanel southIng = new JPanel(new BorderLayout(3, 3));
        southIng.add(formRow, BorderLayout.NORTH);
        southIng.add(btnRow,  BorderLayout.CENTER);
        southIng.add(hint,    BorderLayout.SOUTH);

        ingPanel.add(tableScroll, BorderLayout.CENTER);
        ingPanel.add(southIng,    BorderLayout.SOUTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn   = new JButton("Zapisz przepis");
        JButton cancelBtn = new JButton("Anuluj");
        saveBtn.setBackground(new Color(76, 153, 0));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setPreferredSize(new Dimension(130, 30));
        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());
        buttons.add(saveBtn);
        buttons.add(cancelBtn);

        JPanel topHalf = new JPanel(new BorderLayout(5, 5));
        topHalf.add(formPanel, BorderLayout.NORTH);
        topHalf.add(ingPanel,  BorderLayout.CENTER);

        root.add(topHalf, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        add(root);
    }

    private void onRowSelected() {
        int row = ingredientTable.getSelectedRow();
        boolean sel = row >= 0;
        updateIngBtn.setEnabled(sel);
        removeIngBtn.setEnabled(sel);
        if (!sel) return;

        RecipeIngredient ri = recipeIngredients.get(row);
        for (int i = 0; i < ingredientCombo.getItemCount(); i++) {
            if (ingredientCombo.getItemAt(i).getId() == ri.getIngredient().getId()) {
                ingredientCombo.setSelectedIndex(i);
                break;
            }
        }
        quantityField.setText(formatQty(ri.getQuantity()));
        unitCombo.setSelectedItem(ri.getUnit());
    }

    private void addIngredient() {
        Ingredient ing = (Ingredient) ingredientCombo.getSelectedItem();
        if (ing == null) return;

        double qty = parseQty();
        if (Double.isNaN(qty)) return;

        for (RecipeIngredient ri : recipeIngredients) {
            if (ri.getIngredient().getId() == ing.getId()) {
                JOptionPane.showMessageDialog(this, "Ten skladnik juz jest na liscie.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        String unit = (String) unitCombo.getSelectedItem();
        recipeIngredients.add(new RecipeIngredient(ing, qty, unit));
        ingredientTableModel.addRow(new Object[]{ing.getName(), formatQty(qty), unit});
        quantityField.setText("");
        ingredientTable.clearSelection();
    }

    private void updateIngredient() {
        int row = ingredientTable.getSelectedRow();
        if (row < 0) return;

        Ingredient ing = (Ingredient) ingredientCombo.getSelectedItem();
        if (ing == null) return;

        double qty = parseQty();
        if (Double.isNaN(qty)) return;

        for (int i = 0; i < recipeIngredients.size(); i++) {
            if (i != row && recipeIngredients.get(i).getIngredient().getId() == ing.getId()) {
                JOptionPane.showMessageDialog(this, "Ten skladnik juz jest na liscie.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        String unit = (String) unitCombo.getSelectedItem();
        RecipeIngredient ri = recipeIngredients.get(row);
        ri.setIngredient(ing);
        ri.setQuantity(qty);
        ri.setUnit(unit);

        ingredientTableModel.setValueAt(ing.getName(), row, 0);
        ingredientTableModel.setValueAt(formatQty(qty), row, 1);
        ingredientTableModel.setValueAt(unit, row, 2);

        ingredientTable.clearSelection();
        quantityField.setText("");
    }

    private void removeIngredient() {
        int row = ingredientTable.getSelectedRow();
        if (row < 0) return;
        recipeIngredients.remove(row);
        ingredientTableModel.removeRow(row);
        updateIngBtn.setEnabled(false);
        removeIngBtn.setEnabled(false);
        quantityField.setText("");
    }

    private double parseQty() {
        String text = quantityField.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Podaj ilosc skladnika.", "Blad", JOptionPane.ERROR_MESSAGE);
            return Double.NaN;
        }
        try {
            return Double.parseDouble(text.replace(",", "."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ilosc musi byc liczba.", "Blad", JOptionPane.ERROR_MESSAGE);
            return Double.NaN;
        }
    }

    private void save() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nazwa przepisu nie moze byc pusta.", "Blad", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int prepTime = 0, portions = 0;
        try {
            if (!prepTimeField.getText().trim().isEmpty())
                prepTime = Integer.parseInt(prepTimeField.getText().trim());
            if (!portionsField.getText().trim().isEmpty())
                portions = Integer.parseInt(portionsField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Czas i porcje musza byc liczbami calkowitymi.", "Blad", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (recipe == null) recipe = new Recipe();
        recipe.setName(name);
        recipe.setDescription(descriptionArea.getText().trim());
        recipe.setPrepTime(prepTime);
        recipe.setPortions(portions);
        recipe.setCategory((Category) categoryCombo.getSelectedItem());
        recipe.setIngredients(recipeIngredients);

        if (recipe.getId() == 0) DatabaseHelper.insertRecipe(recipe);
        else                     DatabaseHelper.updateRecipe(recipe);

        saved = true;
        dispose();
    }

    private JButton makeIngButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private String formatQty(double qty) {
        return qty % 1 == 0 ? String.valueOf((int) qty) : String.valueOf(qty);
    }

    public boolean isSaved() { return saved; }
}