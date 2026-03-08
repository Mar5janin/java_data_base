public class RecipeIngredient {
    private int recipeId;
    private Ingredient ingredient;
    private double quantity;
    private String unit;

    public RecipeIngredient() {}

    public RecipeIngredient(Ingredient ingredient, double quantity, String unit) {
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
    }

    public RecipeIngredient(int recipeId, Ingredient ingredient, double quantity, String unit) {
        this.recipeId = recipeId;
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit;
    }

    public int getRecipeId() { return recipeId; }
    public void setRecipeId(int recipeId) { this.recipeId = recipeId; }

    public Ingredient getIngredient() { return ingredient; }
    public void setIngredient(Ingredient ingredient) { this.ingredient = ingredient; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
