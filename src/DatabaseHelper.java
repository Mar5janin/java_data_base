import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/przepisy", "root", "");
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS categories (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name TEXT NOT NULL UNIQUE
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ingredients (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    unit TEXT NOT NULL
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS recipes (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name TEXT NOT NULL,
                    description TEXT,
                    prep_time INT,
                    portions INT,
                    category_id INT,
                    FOREIGN KEY (category_id) REFERENCES categories(id)
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS recipe_ingredients (
                    recipe_id INT,
                    ingredient_id INT,
                    quantity REAL,
                    unit TEXT,
                    PRIMARY KEY (recipe_id, ingredient_id),
                    FOREIGN KEY (recipe_id) REFERENCES recipes(id),
                    FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)
                )
            """);

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM categories");
            if (rs.next() && rs.getInt(1) == 0) {
                insertSampleData(conn);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertSampleData(Connection conn) throws SQLException {
        conn.createStatement().execute(
                "INSERT INTO categories (name) VALUES ('Zupy'),('Desery'),('Obiady'),('Sniadania'),('Salatki')"
        );

        conn.createStatement().execute(
                "INSERT INTO ingredients (name, unit) VALUES " +
                        "('Maka pszenna', 'g')," +
                        "('Jajko', 'szt.')," +
                        "('Mleko', 'ml')," +
                        "('Maslo', 'g')," +
                        "('Cukier', 'g')," +
                        "('Sol', 'szczypta')," +
                        "('Pomidory', 'szt.')," +
                        "('Cebula', 'szt.')," +
                        "('Czosnek', 'zabek')," +
                        "('Ser zolty', 'g')," +
                        "('Schab wieprzowy', 'g')," +
                        "('Bulka tarta', 'g')," +
                        "('Olej', 'lyzka')," +
                        "('Twarog', 'g')," +
                        "('Smietana', 'g')," +
                        "('Koncentrat pomidorowy', 'lyzka')," +
                        "('Wywar wolowy', 'ml')," +
                        "('Makaron', 'g')," +
                        "('Ziemniaki', 'g')," +
                        "('Pieprz', 'szczypta')"
        );

        conn.createStatement().execute(
                "INSERT INTO recipes (name, description, prep_time, portions, category_id) VALUES " +
                        "('Zupa pomidorowa', 'Klasyczna polska zupa pomidorowa gotowana na wywarze wolowym z koncentratem pomidorowym. Podawana z makaronem lub ryzem.', 45, 4, 1)," +
                        "('Sernik klasyczny', 'Puszysty sernik pieczony na kruchym spodzie z twarogu. Idealny deser na kazda okazje. Podawac schlodzony.', 90, 8, 2)," +
                        "('Kotlet schabowy', 'Tradycyjny kotlet schabowy panierowany w bulce tartej i jajku, smazony na zloty kolor. Podawany z ziemniakami i surówka.', 30, 2, 3)"
        );

        conn.createStatement().execute(
                "INSERT INTO recipe_ingredients VALUES " +
                        "(1,17,1000,'ml'),(1,7,3,'szt.'),(1,16,2,'lyzka'),(1,8,1,'szt.'),(1,18,100,'g'),(1,6,1,'szczypta'),(1,20,1,'szczypta')," +
                        "(2,14,500,'g'),(2,2,4,'szt.'),(2,5,150,'g'),(2,4,100,'g'),(2,1,50,'g'),(2,15,100,'g')," +
                        "(3,11,300,'g'),(3,2,2,'szt.'),(3,12,100,'g'),(3,6,1,'szczypta'),(3,20,1,'szczypta'),(3,13,3,'lyzka')"
        );
    }

    public static List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        try (Connection conn = getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT id, name FROM categories ORDER BY name")) {
            while (rs.next()) list.add(new Category(rs.getInt("id"), rs.getString("name")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static Category getCategoryById(int id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, name FROM categories WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Category(rs.getInt("id"), rs.getString("name"));
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static void insertCategory(Category c) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO categories (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) c.setId(rs.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void updateCategory(Category c) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE categories SET name = ? WHERE id = ?")) {
            ps.setString(1, c.getName()); ps.setInt(2, c.getId()); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void deleteCategory(int id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM categories WHERE id = ?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<Ingredient> getAllIngredients() {
        List<Ingredient> list = new ArrayList<>();
        try (Connection conn = getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT id, name, unit FROM ingredients ORDER BY name")) {
            while (rs.next()) list.add(new Ingredient(rs.getInt("id"), rs.getString("name"), rs.getString("unit")));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static Ingredient getIngredientById(int id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, name, unit FROM ingredients WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new Ingredient(rs.getInt("id"), rs.getString("name"), rs.getString("unit"));
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static void insertIngredient(Ingredient i) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO ingredients (name, unit) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, i.getName()); ps.setString(2, i.getUnit()); ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) i.setId(rs.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void updateIngredient(Ingredient i) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE ingredients SET name = ?, unit = ? WHERE id = ?")) {
            ps.setString(1, i.getName()); ps.setString(2, i.getUnit()); ps.setInt(3, i.getId()); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void deleteIngredient(int id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM ingredients WHERE id = ?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<Recipe> getAllRecipes() {
        return searchRecipes("", 0);
    }

    public static List<Recipe> searchRecipes(String keyword, int categoryId) {
        List<Recipe> list = new ArrayList<>();
        String sql = """
            SELECT id, name, description, prep_time, portions, category_id
            FROM recipes
            WHERE (? = '' OR LOWER(name) LIKE LOWER(?))
              AND (? = 0 OR category_id = ?)
            ORDER BY name
        """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, keyword); ps.setString(2, "%" + keyword + "%");
            ps.setInt(3, categoryId); ps.setInt(4, categoryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Category cat = getCategoryById(rs.getInt("category_id"));
                list.add(new Recipe(rs.getInt("id"), rs.getString("name"),
                        rs.getString("description"), rs.getInt("prep_time"), rs.getInt("portions"), cat));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public static Recipe getRecipeById(int id) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM recipes WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Category cat = getCategoryById(rs.getInt("category_id"));
                Recipe r = new Recipe(rs.getInt("id"), rs.getString("name"),
                        rs.getString("description"), rs.getInt("prep_time"), rs.getInt("portions"), cat);
                r.setIngredients(getRecipeIngredients(id));
                return r;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static void insertRecipe(Recipe r) {
        String sql = "INSERT INTO recipes (name, description, prep_time, portions, category_id) VALUES (?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getName()); ps.setString(2, r.getDescription());
            ps.setInt(3, r.getPrepTime()); ps.setInt(4, r.getPortions());
            ps.setInt(5, r.getCategory() != null ? r.getCategory().getId() : 0);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) { r.setId(rs.getInt(1)); saveRecipeIngredients(r); }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void updateRecipe(Recipe r) {
        String sql = "UPDATE recipes SET name=?, description=?, prep_time=?, portions=?, category_id=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getName()); ps.setString(2, r.getDescription());
            ps.setInt(3, r.getPrepTime()); ps.setInt(4, r.getPortions());
            ps.setInt(5, r.getCategory() != null ? r.getCategory().getId() : 0);
            ps.setInt(6, r.getId());
            ps.executeUpdate();
            deleteRecipeIngredients(r.getId());
            saveRecipeIngredients(r);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void deleteRecipe(int id) {
        deleteRecipeIngredients(id);
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM recipes WHERE id = ?")) {
            ps.setInt(1, id); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<RecipeIngredient> getRecipeIngredients(int recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        String sql = """
            SELECT ri.ingredient_id, ri.quantity, ri.unit, i.name, i.unit AS i_unit
            FROM recipe_ingredients ri
            JOIN ingredients i ON ri.ingredient_id = i.id
            WHERE ri.recipe_id = ?
        """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, recipeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Ingredient ing = new Ingredient(rs.getInt("ingredient_id"), rs.getString("name"), rs.getString("i_unit"));
                list.add(new RecipeIngredient(recipeId, ing, rs.getDouble("quantity"), rs.getString("unit")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private static void saveRecipeIngredients(Recipe r) {
        String sql = "INSERT INTO recipe_ingredients (recipe_id, ingredient_id, quantity, unit) VALUES (?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (RecipeIngredient ri : r.getIngredients()) {
                ps.setInt(1, r.getId()); ps.setInt(2, ri.getIngredient().getId());
                ps.setDouble(3, ri.getQuantity()); ps.setString(4, ri.getUnit());
                ps.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void deleteRecipeIngredients(int recipeId) {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM recipe_ingredients WHERE recipe_id = ?")) {
            ps.setInt(1, recipeId); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
