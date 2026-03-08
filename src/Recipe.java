import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private int id;
    private String name;
    private String description;
    private int prepTime;
    private int portions;
    private Category category;
    private List<RecipeIngredient> ingredients;

    public Recipe() {
        this.ingredients = new ArrayList<>();
    }

    public Recipe(int id, String name, String description, int prepTime, int portions, Category category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.prepTime = prepTime;
        this.portions = portions;
        this.category = category;
        this.ingredients = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPrepTime() { return prepTime; }
    public void setPrepTime(int prepTime) { this.prepTime = prepTime; }

    public int getPortions() { return portions; }
    public void setPortions(int portions) { this.portions = portions; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }

    @Override
    public String toString() { return name; }
}
