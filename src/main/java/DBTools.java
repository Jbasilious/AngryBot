import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class DBTools {
    private static Connection connection;


    protected static void openConnection() throws SQLException {
        try {
            connection = DriverManager.getConnection(Config.DB_URL(), Config.DB_USER(), Config.DB_PASS());
            System.out.println("VALID: " + connection.isValid(5));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            closeConnection();
        }
    }

    protected static void closeConnection() throws SQLException {
        System.out.println("Closing Connection...");
        connection.close();
        System.out.println("VALID: " + connection.isValid(5));

    }

    protected static void insertGUILD_USER(String GUILD, String UID) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT IGNORE INTO GUILD_USER(GUILD, UID,BANANA_TOTAL,BANANA_CURRENT,GUNKED,GUNKS,TIMEOUT,HOOKER,STD) VALUES (?,?,?,?,?,?,?,?,?)")) {
            statement.setString(1, GUILD);
            statement.setString(2, UID);
            statement.setInt(3, 10);
            statement.setInt(4, 10);
            statement.setInt(5, 0);
            statement.setInt(6, 0);
            statement.setInt(7, 0);
            statement.setInt(8, 0);
            statement.setString(9, "");
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    protected static int insertCard(String cardStyle, String title, String attribute, String level, String type, String description, String atk, String defe, int cost, String image_label, boolean shoppable) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO cards(card_style, title,attribute,level,type,description,atk,defe,cost,image_label,shoppable) VALUES (?,?,?,?,?,?,?,?,?,?,?)")) {
            statement.setString(1, cardStyle);
            statement.setString(2, title);
            statement.setString(3, attribute);
            statement.setString(4, level);
            statement.setString(5, type);
            statement.setString(6, description);
            statement.setString(7, atk);
            statement.setString(8, defe);
            statement.setInt(9, cost);
            statement.setString(10, image_label);
            statement.setBoolean(11, shoppable);
            return statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    protected static int getLatestCardId() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT max(id) from cards")) {
            var res = statement.executeQuery();
            res.next();
            return res.getInt(1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    protected static Set<String> getCardTitles() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT title from cards")) {
            var res = statement.executeQuery();
            Set<String> titles = new HashSet<String>();
            while (res.next()) {
                titles.add(res.getString(1));
            }
            return titles;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return new HashSet<>();
    }

    protected static int getCardIdForTitle(String cardTitle) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id from cards where title = ?")) {
            statement.setString(1, cardTitle);
            var res = statement.executeQuery();
            res.next();
            return res.getInt(1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }


    protected static void updateGUILD_USER(String guild, String uid, Integer bananaTotal, Integer bananaCurrent, Integer gunked, Integer gunks,Integer timeout,Integer hooker,String std) {
        try (PreparedStatement statement = connection.prepareStatement(buildUpdateQuery(bananaTotal, bananaCurrent, gunked, gunks, timeout, hooker, std))) {
            int parameterIndex = 1;

            // Set values based on non-null parameters
            if (bananaTotal != null) {
                statement.setInt(parameterIndex++, bananaTotal);
            }
            if (bananaCurrent != null) {
                statement.setInt(parameterIndex++, bananaCurrent);
            }
            if (gunked != null) {
                statement.setInt(parameterIndex++, gunked);
            }
            if (gunks != null) {
                statement.setInt(parameterIndex++, gunks);
            }
            if (timeout != null) {
                statement.setInt(parameterIndex++, timeout);
            }
            if (hooker != null) {
                statement.setInt(parameterIndex++, hooker);
            }
            if (std != null) {
                statement.setString(parameterIndex++, std);
            }

            // Set key parameters
            statement.setString(parameterIndex++, guild);
            statement.setString(parameterIndex, uid);
            System.out.println("Executing SQL: " + statement.toString());
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    protected static void modBanana(String GUILD, String UID, Integer delta) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE GUILD_USER SET BANANA_CURRENT=BANANA_CURRENT+? WHERE GUILD = ? AND UID = ?")) {
            statement.setInt(1, delta);
            statement.setString(2, GUILD);
            statement.setString(3, UID);
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        if (delta > 0 ){
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE GUILD_USER SET BANANA_TOTAL=BANANA_TOTAL+? WHERE GUILD = ? AND UID = ?")) {
                statement.setInt(1, delta);
                statement.setString(2, GUILD);
                statement.setString(3, UID);
                statement.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    // Helper method to dynamically build the update query
    private static String buildUpdateQuery(Integer bananaTotal, Integer bananaCurrent, Integer gunked, Integer gunks,Integer timeout,Integer hooker,String std) {
        StringBuilder queryBuilder = new StringBuilder("UPDATE GUILD_USER SET ");

        // Append non-null parameters to the query
        appendToQuery(queryBuilder, "BANANA_TOTAL", bananaTotal);
        appendToQuery(queryBuilder, "BANANA_CURRENT", bananaCurrent);
        appendToQuery(queryBuilder, "GUNKED", gunked);
        appendToQuery(queryBuilder, "GUNKS", gunks);
        appendToQuery(queryBuilder, "TIMEOUT", timeout);
        appendToQuery(queryBuilder, "HOOKER", hooker);
        appendToQuery(queryBuilder, "STD", std);

        // Remove the trailing comma, if any
        if (queryBuilder.lastIndexOf(",") == queryBuilder.length() - 2) {
            queryBuilder.deleteCharAt(queryBuilder.length() - 2);
        }

        // Append the WHERE clause
        queryBuilder.append(" WHERE GUILD = ? AND UID = ?");

        return queryBuilder.toString();
    }

    // Helper method to append column names to the query if the value is non-null
    private static void appendToQuery(StringBuilder queryBuilder, String columnName, Object value) {
        if (value != null) {
            queryBuilder.append(columnName).append(" = ?, ");
        }
    }



    protected static ResultSet selectGUILD_USER(String GUILD, String UID) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM GUILD_USER WHERE GUILD = ? AND UID = ?")) {
            statement.setString(1, GUILD);
            statement.setString(2, UID);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet;
            } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    protected static int selectJACKPOT(String GUILD) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT BANANA_JACKPOT FROM GAMBLE where GUILD=?");
            statement.setString(1,GUILD);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("BANANA_JACKPOT");
            } else {
                // Handle the case where no result is found (return a default value or throw an exception)
                return 0; // Replace with appropriate handling
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return 0; // Replace with appropriate handling
        }
    }

    protected static void insertJACKPOT(String GUILD) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO GAMBLE(GUILD) VALUES (?)")) {
            statement.setString(1, GUILD);
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    protected static ResultSet  getCOMMAND_KEYWORD(String CONDITION) throws SQLException{
        try (PreparedStatement statement = connection.prepareStatement(
                "select KEYWORD as KEYWORD from COMMAND_KEYWORD where COMMAND=?")) {
            statement.setString(1, CONDITION);
            System.out.println(statement);
            return statement.executeQuery();
            } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }

    }

    protected static void updateCOMMAND_KEYWORD(String COMMAND, String KEYWORD) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE COMMAND_KEYWORD set KEYWORD=? WHERE COMMAND=? ")) {
            statement.setString(1, KEYWORD);
            statement.setString(2, COMMAND);
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    protected static void updateJACKPOT(int bananaJackpot) {
        try {
            PreparedStatement statement = connection.prepareStatement(buildUpdateBananaJackpotQuery());
            statement.setInt(1, bananaJackpot);
            System.out.println("Executing SQL: " + statement.toString());
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    protected static void resetJACKPOT(int baseBananaJackpot) {
        try {
            // Update the jackpot value to the base value
            PreparedStatement statement = connection.prepareStatement(buildUpdateBananaJackpotQuery());
            statement.setInt(1, baseBananaJackpot);
        
            System.out.println("Executing SQL: " + statement.toString());
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    
    
    
    private static String buildUpdateBananaJackpotQuery() {
        return "UPDATE GAMBLE SET BANANA_JACKPOT = ?";
    }

}


