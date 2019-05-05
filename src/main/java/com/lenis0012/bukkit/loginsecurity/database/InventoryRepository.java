package com.lenis0012.bukkit.loginsecurity.database;

import com.lenis0012.bukkit.loginsecurity.LoginSecurity;
import com.lenis0012.bukkit.loginsecurity.storage.PlayerInventory;
import org.bukkit.Bukkit;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;
import java.util.function.Consumer;

public class InventoryRepository {
    private final LoginSecurity loginSecurity;
    private final DataSource dataSource;

    public InventoryRepository(LoginSecurity loginSecurity, DataSource dataSource) {
        this.loginSecurity = loginSecurity;
        this.dataSource = dataSource;
    }

    public void insert(PlayerInventory inventory, Consumer<AsyncResult<PlayerInventory>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(loginSecurity, () -> {
            try {
                insertBlocking(inventory);
                resolveResult(callback, inventory);
            } catch (SQLException e) {
                resolveError(callback, e);
            }
        });
    }

    public void insertBlocking(PlayerInventory inventory) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO ls_inventories(helmet, chestplate, leggings, boots, off_hand, contents) VALUES (?,?,?,?,?,?);",
                    Statement.RETURN_GENERATED_KEYS)) {

                statement.setString(1, inventory.getHelmet());
                statement.setString(2, inventory.getChestplate());
                statement.setString(3, inventory.getLeggings());
                statement.setString(4, inventory.getBoots());
                statement.setString(5, inventory.getOffHand());
                statement.setString(6, inventory.getContents());
                statement.executeUpdate();

                try(ResultSet keys = statement.getGeneratedKeys()) {
                    if(!keys.next()) {
                        throw new RuntimeException("No keys were returned after insert");
                    }
                    inventory.setId(keys.getInt(1));
                }
            }
        }
    }

    public void findById(int id, Consumer<AsyncResult<PlayerInventory>> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(loginSecurity, () -> {
            try {
                final PlayerInventory inventory = findByIdBlocking(id);
                resolveResult(callback, inventory);
            } catch (SQLException e) {
                resolveError(callback, e);
            }
        });
    }

    public PlayerInventory findByIdBlocking(int id) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            try(PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM ls_inventories WHERE id=?;")) {
                statement.setInt(1, id);
                try(ResultSet result = statement.executeQuery()) {
                    if(!result.next()) {
                        return null; // Not found
                    }

                    final PlayerInventory inventory = new PlayerInventory();
                    inventory.setId(result.getInt("id"));
                    inventory.setHelmet(result.getString("helmet"));
                    inventory.setChestplate(result.getString("chestplate"));
                    inventory.setLeggings(result.getString("leggings"));
                    inventory.setBoots(result.getString("boots"));
                    inventory.setOffHand(result.getString("off_hand"));
                    inventory.setContents(result.getString("contents"));
                    return inventory;
                }
            }
        }
    }

    private <T> void resolveResult(Consumer<AsyncResult<T>> callback, T result) {
        Bukkit.getScheduler().runTask(loginSecurity, () ->
                callback.accept(new AsyncResult<T>(true, result, null)));
    }

    private <T> void resolveError(Consumer<AsyncResult<T>> callback, Exception error) {
        Bukkit.getScheduler().runTask(loginSecurity, () ->
                callback.accept(new AsyncResult<T>(false, null, error)));
    }
}