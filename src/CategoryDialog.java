import javax.swing.*;
import java.awt.*;

public class CategoryDialog extends JDialog {

    private final JTextField nameField = new JTextField(20);
    private boolean saved = false;
    private Category category;

    public CategoryDialog(Frame parent, Category category) {
        super(parent, category == null ? "Nowa kategoria" : "Edytuj kategorię", true);
        this.category = category;

        if (category != null) {
            nameField.setText(category.getName());
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
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Nazwa kategorii:"), gbc);
        gbc.gridx = 1;
        form.add(nameField, gbc);

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
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nazwa kategorii nie może być pusta.", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (category == null) {
            category = new Category(name);
            DatabaseHelper.insertCategory(category);
        } else {
            category.setName(name);
            DatabaseHelper.updateCategory(category);
        }
        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
}
