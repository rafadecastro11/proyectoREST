package com.gimnasio.controller;

import com.gimnasio.model.Reserva;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping
public class ReservaController {

    private static final String URL = "jdbc:postgresql://localhost:5432/dit";
    private static final String USER = "dit";
    private static final String PASS = "dit";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    @PostMapping("/reservas")
    public ResponseEntity<String> crearReserva(@RequestBody Reserva reserva) {
        String sql = "INSERT INTO reservas (id_clase, socio, fecha) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, reserva.getIdClase());
            stmt.setString(2, reserva.getSocio());
            stmt.setDate(3, Date.valueOf(reserva.getFecha()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body("Reserva creada con ID: " + rs.getInt(1));
                }
            }
            return ResponseEntity.status(HttpStatus.CREATED).body("Reserva creada");

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error al crear reserva: " + e.getMessage());
        }
    }

    @DeleteMapping("/reservas/{id}")
    public ResponseEntity<String> borrarReserva(@PathVariable int id) {
        String sql = "DELETE FROM reservas WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affected = stmt.executeUpdate();

            if (affected > 0) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body("Reserva con ID " + id + " eliminada");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No se encontro reserva con ID: " + id);
            }

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error al borrar reserva: " + e.getMessage());
        }
    }

    @GetMapping("/clases/{id}/reservas")
    public ResponseEntity<String> listarSocios(@PathVariable int id) {
        String sql = "SELECT socio, fecha FROM reservas WHERE id_clase = ? ORDER BY socio";
        List<String> socios = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    socios.add("Socio: " + rs.getString("socio")
                            + ", Fecha: " + rs.getDate("fecha"));
                }
            }

            if (socios.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No hay reservas para la clase con ID: " + id);
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(String.join("\n", socios));

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error al consultar reservas: " + e.getMessage());
        }
    }
}
