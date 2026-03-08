import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RecipeDialog extends JDialog {

    private final JTextField nameField        = new JTextField(25);
    private final JComboBox<Category> categoryCombo = new JComboBox<>();
    private final JTextField prepTimeField    = new JTextField(6);
    private final JTextField portionsField    = new JTextField(6);
    private final JTextArea descriptionArea   = new JTextArea(4, 25);

    private final JComboBox<Ingredient> ingredientCombo = new JComboBox<>();
    private final JTextField quantityField    = new JTextField(6);
    private final JComboBox<String> unitCombo = new JComboBox<>(new String[]{
        "g", "kg", "ml", "l", "szt.", "lyzka", "lyzeczka", "szklanka", "szczypta", "zabek", "plaster", "peczek"
    });

    private final DefaultTableModel ingredientTableModel = new DefaultTableModel(
        new String[]{"Skladnik", "Ilosc", "Jednostka"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable ingredientTable = new JTable(ingredientTableModel);

    // Lista skladnikow w tym przepisie (robocza kopia)
    private final List<RecipeIngredient> recipeIngredients = new ArrayList<>();
    private boolean saved = false;
    private Recipe recipe;

    public RecipeDialog(Frame parent, Recipe recipe) {
        super(parent, recipe == null ? "Nowy przepis" : "Edytuj przepis", true);
        this.recipe = recipe;

        loadCategories();
        loadIngredients();

        // WAZNE: najpierw zaladuj dane, potem buduj UI
        if (recipe != null) {
            fillForm(recipe);
        }

        buildUI();
        setSize(600, 640);
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

        // Ustaw kategorie
        if (r.getCategory() != null) {
            for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                if (categoryCombo.getItemAt(i).getId() == r.getCategory().getId()) {
                    categoryCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Zaladuj skladniki z bazy (swieze pobieranie!)
        recipeIngredients.clear();
        ingredientTableModel.setRowCount(0);
        List<RecipeIngredient> fromDb = DatabaseHelper.getRecipeIngredients(r.getId());
        for (RecipeIngredient ri : fromDb) {
            recipeIngredients.add(ri);
            String qty = ri.getQuantity() % 1 == 0
                ? String.valueOf((int) ri.getQuantity())
                : String.valueOf(ri.getQuantity());
            ingredientTableModel.addRow(new Object[]{
                ri.getIngredient().getName(), qty, ri.getUnit()
            });
        }
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ---- FORMULARZ GORNY ----
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

        // ---- PANEL SKLADNIKOW ----
        JPanel ingPanel = new JPanel(new BorderLayout(5, 5));
        ingPanel.setBorder(BorderFactory.createTitledBorder("Skladniki przepisu"));

        ingredientTable.setRowHeight(24);
        ingredientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(ingredientTable);
        tableScroll.setPreferredSize(new Dimension(0, 140));

        // Wiersz dodawania skladnika
        JPanel addIngRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 3));
        addIngRow.add(new JLabel("Skladnik:"));
        ingredientCombo.setPreferredSize(new Dimension(155, 25));
        addIngRow.add(ingredientCombo);
        addIngRow.add(new JLabel("Ilosc:"));
        quantityField.setPreferredSize(new Dimension(55, 25));
        addIngRow.add(quantityField);
        addIngRow.add(new JLabel("Jedn.:"));
        addIngRow.add(unitCombo);

        JButton addIngBtn = new JButton("+ Dodaj");
        addIngBtn.setBackground(new Color(0, 120, 215));
        addIngBtn.setForeground(Color.WHITE);
        addIngBtn.setFocusPainted(false);
        addIngBtn.addActionListener(e -> addIngredientToList());
        addIngRow.add(addIngBtn);

        JButton removeIngBtn = new JButton("Usun");
        removeIngBtn.setBackground(new Color(200, 50, 50));
        removeIngBtn.setForeground(Color.WHITE);
        removeIngBtn.setFocusPainted(false);
        removeIngBtn.addActionListener(e -> removeIngredientFromList());
        addIngRow.add(removeIngBtn);

        ingPanel.add(tableScroll, BorderLayout.CENTER);
        ingPanel.add(addIngRow,   BorderLayout.SOUTH);

        // ---- PRZYCISKI DOLNE ----
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn   = new JButton("Zapisz");
        JButton cancelBtn = new JButton("Anuluj");
        saveBtn.setBackground(new Color(76, 153, 0));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setPreferredSize(new Dimension(100, 30));
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

    private void addIngredientToList() {
        Ingredient ing = (Ingredient) ingredientCombo.getSelectedItem();
        if (ing == null) return;

        String qtyText = quantityField.getText().trim();
        if (qtyText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Podaj ilosc skladnika.", "Blad", JOptionPane.ERROR_MESSAGE);
            return;
        }
        double qty;
        try {
            qty = Double.parseDouble(qtyText.replace(",", "."));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ilosc musi byc liczba.", "Blad", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Sprawdz czy skladnik juz jest na liscie
        for (RecipeIngredient ri : recipeIngredients) {
            if (ri.getIngredient().getId() == ing.getId()) {
                JOptionPane.showMessageDialog(this, "Ten skladnik juz jest na liscie.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        String unit = (String) unitCombo.getSelectedItem();
        recipeIngredients.add(new RecipeIngredient(ing, qty, unit));
        String qtyStr = qty % 1 == 0 ? String.valueOf((int) qty) : String.valueOf(qty);
        ingredientTableModel.addRow(new Object[]{ing.getName(), qtyStr, unit});
        quantityField.setText("");
    }

    private void removeIngredientFromList() {
        int row = ingredientTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Zaznacz skladnik do usuniecia.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        recipeIngredients.remove(row);
        ingredientTableModel.removeRow(row);
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

    public boolean isSaved() { return saved; }
}
