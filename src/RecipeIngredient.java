public class RecipeIngredient {
    private Ingredient ingredient;
    private double quantity;
    private String unit;

    public RecipeIngredient(Ingredient ingredient, double quantity, String unit) {
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
    }

    public Ingredient getIngredient() { return ingredient; }
    public void setIngredient(Ingredient ingredient) { this.ingredient = ingredient; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}