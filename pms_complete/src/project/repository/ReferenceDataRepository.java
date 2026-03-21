package project.repository;

import project.config.DbConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ReferenceDataRepository {


    public List<String> getAllPositions() {
        List<String> positions = new ArrayList<>();
        String sql = "SELECT position FROM position_salary_rules ORDER BY position";
        
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                positions.add(rs.getString("position"));
            }
        } catch (SQLException e) {
            System.err.println("[RefDataRepo] getAllPositions: " + e.getMessage());
        }
        
        return positions;
    }


    public List<String> getAllDepartments() {
        List<String> departments = new ArrayList<>();
        String sql = "SELECT department_name FROM departments ORDER BY department_name";
        
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                departments.add(rs.getString("department_name"));
            }
        } catch (SQLException e) {
            System.err.println("[RefDataRepo] getAllDepartments: " + e.getMessage());
        }
        
        return departments;
    }


    public boolean positionExists(String position) {
        String sql = "SELECT 1 FROM position_salary_rules WHERE position = ?";
        
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setString(1, position);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("[RefDataRepo] positionExists: " + e.getMessage());
        }
        
        return false;
    }


    public boolean departmentExists(String department) {
        String sql = "SELECT 1 FROM departments WHERE department_name = ?";
        
        try (Connection c = DbConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setString(1, department);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("[RefDataRepo] departmentExists: " + e.getMessage());
        }
        
        return false;
    }
}
