import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class DbFunctions {
    int userId;

    public Connection connect_to_db(String dbname,String user,String pass){
        Connection conn=null;
        try{
            Class.forName("org.postgresql.Driver");
            conn= DriverManager.getConnection("jdbc:postgresql://localhost:5432/"+dbname,user,pass);
            if(conn!=null){
                System.out.println("Connection Established");
            }
            else{
                System.out.println("Connection Failed");
            }

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public boolean readLogin(Connection conn, String login, String pass){
        try {
            String query = String.format("SELECT * FROM users WHERE login = '%s' AND pass = '%s'", login, pass);
            Statement statement = conn.createStatement();
            ResultSet rs=statement.executeQuery(query);
            if (rs.next()) {
                userId = rs.getInt("rowid");
                return true;
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return false;
    }

    public void insertUser(Connection conn,String login,String pass){
        try {
            String query=String.format("insert into users(login,pass) values('%s','%s');",login,pass);
            Statement statement = conn.createStatement();
            statement.executeUpdate(query);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public Integer getId(Connection conn,String login,String pass) {
        Integer id = null;
        try {
            String query = String.format("SELECT * FROM users WHERE login = '%s' AND pass = '%s'", login, pass);
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);

            if (rs.next()) {
                id = rs.getInt("rowid");
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return id;
    }

    public ArrayList readFavorites(Connection conn, Integer id){
        Statement statement;
        ResultSet rs;
        ArrayList <String> result = new ArrayList<>();
        try {
            String query = String.format("select * from favorites where users_rowid= %s",id);
            statement=conn.createStatement();
            rs=statement.executeQuery(query);
            while(rs.next()){
                result.add(readDrugFavorites(conn, rs.getString("drugs_rowid")));
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return result;
    }

    public String readDrugFavorites(Connection conn, String id) {
        Statement statement;
        ResultSet rs;
        StringBuilder result= new StringBuilder();
        try {
            String query = String.format("select * from drugs where rowid= %s",id);
            statement=conn.createStatement();
            rs=statement.executeQuery(query);
            while(rs.next()){
                result.append(rs.getString("rowname"));
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return result.toString();
    }

    public ArrayList readCategories(Connection conn) {
        Statement statement;
        ResultSet rs;
        ArrayList <String> result = new ArrayList<>();
        try {
            String query = String.format("select * from categories");
            statement=conn.createStatement();
            rs=statement.executeQuery(query);
            while(rs.next()){
                result.add(rs.getString("rowname"));
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return result;
    }

    public ArrayList readDrugs(Connection conn, String category) {
        int categoriesId = searchIdCategory(conn, category);
        Statement statement;
        ResultSet rs;
        ArrayList <String> result = new ArrayList<>();
        try {
            String query = String.format("select * from drugs where categories_id= %s",categoriesId);
            statement=conn.createStatement();
            rs=statement.executeQuery(query);
            while(rs.next()){
                result.add(rs.getString("rowname"));
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return result;
    }

    public int searchIdCategory(Connection conn, String category) {
        Statement statement;
        ResultSet rs;
        int result=-10;
        try {
            String query = String.format("select * from categories where rowname='%s'", category);
            statement=conn.createStatement();
            rs=statement.executeQuery(query);
            while(rs.next()){
                result = (rs.getInt("rowid"));
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return result;
    }

    public ArrayList readDrug(Connection conn, String drug){
        Statement statement;
        ResultSet rs;
        ArrayList <String> result = new ArrayList<>();
        try {
            String query = String.format("select * from drugs where rowname= '%s'",drug);
            statement=conn.createStatement();
            rs=statement.executeQuery(query);
            while(rs.next()){
                result.add("Название: "+rs.getString("rowname"));
                result.add("Описание: "+rs.getString("description"));
                result.add("Противопоказания: "+rs.getString("contraindications"));
                result.add("Побочные эффекты: "+rs.getString("sideeffects"));
                result.add("Инструкция по применению: "+rs.getString("instruction"));
                result.add("Форма выпуска: "+rs.getString("form"));
                result.add("Срок годности: "+rs.getString("expirationdate"));
                result.add("Условия хранения: "+rs.getString("storageconditions"));
                result.add(rs.getString("image"));
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return result;
    }

    public boolean addFavorites(Connection conn, String drug){
        Statement statement;
        if (!drugAdded(conn, searchIdDrug(conn,drug))) {
            try {
                String query=String.format("insert into favorites(drugs_rowid,users_rowid) values('%s','%s');",searchIdDrug(conn,drug),userId);statement=conn.createStatement();
                statement.executeUpdate(query);
                return true;
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
        return false;
    }

    public boolean drugAdded(Connection conn, int drugID){
        Statement statement;
        ResultSet rs;
        try {
            String query = String.format("select * from favorites where drugs_rowid= '%s' AND users_rowid= '%s'",drugID,userId) ;
            statement=conn.createStatement();
            rs=statement.executeQuery(query);
            if(rs.next()){
                return true;
            }
        }
        catch (Exception e){
            userId = 0;
            System.out.println(e.getMessage());
        }
        return false;
    }

    public int searchIdDrug(Connection conn, String drug) {
        Statement statement;
        ResultSet rs;
        int result=-10;
        try {
            String query = String.format("select * from drugs where rowname='%s'", drug);
            statement=conn.createStatement();
            rs=statement.executeQuery(query);
            while(rs.next()){
                result = (rs.getInt("rowid"));
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return result;
    }

    public ArrayList searchDrug(Connection conn, String queryMessage){
        Statement statement;
        ResultSet rs;
        ArrayList <String> result = new ArrayList<>();
        try {
            String query = String.format("select * from drugs where LOWER(rowname) like LOWER('%%%s%%')", queryMessage);
            statement=conn.createStatement();
            rs=statement.executeQuery(query);
            while(rs.next()){
                result.add(rs.getString("rowname"));
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return result;
    }

    public void deleteDrug(Connection conn, String drug){
        Statement statement;
            try {
                String query=String.format("DELETE FROM favorites WHERE drugs_rowid = '%s' AND  users_rowid= '%s'",searchIdDrug(conn,drug),userId);
                statement=conn.createStatement();
                statement.executeUpdate(query);
            }catch (Exception e){
                System.out.println(e.getMessage());
        }
    }

    public boolean addCategory(Connection conn, String category){
        Statement statement;
        if (!categoryAdded(conn, category)) {
            try {
                String query=String.format("insert into categories(rowname) values('%s');",category);
                statement=conn.createStatement();
                statement.executeUpdate(query);
                return true;
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
        return false;
    }

    public boolean categoryAdded(Connection conn, String category){
        Statement statement;
        ResultSet rs;
        try {
            String query = String.format("select * from categories where rowname = '%s'",category) ;
            statement=conn.createStatement();
            rs=statement.executeQuery(query);
            if(rs.next()){
                return true;
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean addDrug(Connection conn, String[] drugData) {
        Statement statement;
        if (!drugAdded(conn, drugData[0])) {
            try {
                String query=String.format("insert into drugs(rowname, description, contraindications, sideeffects, instruction, form, expirationdate, storageconditions, image, categories_id) values('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')",drugData[0],drugData[1],drugData[2],drugData[3],drugData[4],drugData[5],drugData[6],drugData[7],drugData[8],drugData[9]);
                statement=conn.createStatement();
                statement.executeUpdate(query);
                return true;
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
        return false;
    }

    public boolean drugAdded(Connection conn, String drugName){
        Statement statement;
        ResultSet rs;
        try {
            String query = String.format("select * from drugs where rowname = '%s'",drugName) ;
            statement=conn.createStatement();
            rs=statement.executeQuery(query);
            if(rs.next()){
                return true;
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean categoryEdit(Connection conn, String oldValue, String newValue){
        Statement statement;
        try {
            String query = String.format("UPDATE categories SET rowname = '%s' WHERE rowname = '%s'",newValue,oldValue) ;
            statement = conn.createStatement();
            int rowsAffected = statement.executeUpdate(query);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean categoryDel(Connection conn, String category) {
        Statement statement;
        try {
            String query = String.format("DELETE FROM categories WHERE rowid = '%s'", searchIdCategory(conn, category));
            statement = conn.createStatement();
            int rowsAffected = statement.executeUpdate(query);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean drugDel(Connection conn, String drug) {
        Statement statement;
        try {
            String query = String.format("DELETE FROM drugs WHERE rowid = '%s'", searchIdDrug(conn, drug));
            statement = conn.createStatement();
            int rowsAffected = statement.executeUpdate(query);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    public boolean drugEdit(Connection conn, String drug, String item, String newValue){
        Statement statement;
        try {
            String column="";
            switch (Integer.parseInt(item)) {
                case 1 -> column = "rowname" ;
                case 2 -> column = "description" ;
                case 3-> column = "contraindications" ;
                case 4 -> column = "sideeffects" ;
                case 5 -> column = "instruction" ;
                case 6 -> column = "form" ;
                case 7 -> column = "expirationdate" ;
                case 8 -> column = "storageconditions" ;
                case 9 -> column = "image" ;
                case 10 -> column = "categories_id" ;
            }
            String query = String.format("UPDATE drugs SET %s = '%s' WHERE rowname = '%s'",column,newValue,drug) ;
            statement = conn.createStatement();
            int rowsAffected = statement.executeUpdate(query);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}
