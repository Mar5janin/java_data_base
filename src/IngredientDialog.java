import javax.swing.*;
import java.awt.*;

public class IngredientDialog extends JDialog {

    private final JTextField nameField = new JTextField(20);
    private final JComboBox<String> unitCombo = new JComboBox<>(new String[]{
        "g", "kg", "ml", "l", "szt.", "łyżka", "łyżeczka", "szklanka", "szczypta", "ząbek", "plaster", "pęczek"
    });
    private boolean saved = false;
    private Ingredient ingredient;

    public IngredientDialog(Frame parent, Ingredient ingredient) {
        super(parent, ingredient == null ? "Nowy składnik" : "Edytuj składnik", true);
        this.ingredient = ingredient;

        if (ingredient != null) {
            nameField.setText(ingredient.getName());
            unitCombo.setSelectedItem(ingredient.getUnit());
        }

        buildUI();
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Nazwa:"), gbc);
        gbc.gridx = 1;
        form.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Jednostka:"), gbc);
        gbc.gridx = 1;
        form.add(unitCombo, gbc);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Zapisz");
        JButton cancelBtn = new JButton("Anuluj");

        saveBtn.setBackground(new Color(76, 153, 0));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);

        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());

        buttons.add(saveBtn);
        buttons.add(cancelBtn);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void save() {
        String name = nameField.getText().trim();
        String unit = (String) unitCombo.getSelectedItem();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nazwa składnika nie może być pusta.", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (ingredient == null) {
            ingredient = new Ingredient(name, unit);
            DatabaseHelper.insertIngredient(ingredient);
        } else {
            ingredient.setName(name);
            ingredient.setUnit(unit);
            DatabaseHelper.updateIngredient(ingredient);
        }
        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
}
