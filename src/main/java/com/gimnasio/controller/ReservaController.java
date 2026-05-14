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
        String sqlPlazas = "SELECT plazas FROM clases WHERE id = ?";
        String sqlUpdatePlazas = "UPDATE clases SET plazas = plazas - 1 WHERE id = ? AND plazas > 0";
        String sqlInsert = "INSERT INTO reservas (id_clase, socio, fecha) VALUES (?, ?, ?)";

        try (Connection conn = getConnection()) {

            int idClase = reserva.getIdClase();

            try (PreparedStatement stmtPlazas = conn.prepareStatement(sqlPlazas)) {
                stmtPlazas.setInt(1, idClase);
                try (ResultSet rs = stmtPlazas.executeQuery()) {
                    if (!rs.next()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("La clase con ID " + idClase + " no existe");
                    }
                    int plazas = rs.getInt("plazas");

                    if (plazas <= 0) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Aforo completo para la clase con ID " + idClase);
                    }
                }
            }

            try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdatePlazas)) {
                stmtUpdate.setInt(1, idClase);
                int actualizadas = stmtUpdate.executeUpdate();

                if (actualizadas == 0) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Aforo completo para la clase con ID " + idClase);
                }
            }

            try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                stmtInsert.setInt(1, idClase);
                stmtInsert.setString(2, reserva.getSocio());
                stmtInsert.setDate(3, Date.valueOf(reserva.getFecha()));
                stmtInsert.executeUpdate();

                try (ResultSet rsInsert = stmtInsert.getGeneratedKeys()) {
                    if (rsInsert.next()) {
                        return ResponseEntity.status(HttpStatus.CREATED)
                                .body("Reserva creada con ID: " + rsInsert.getInt(1));
                    }
                }
                return ResponseEntity.status(HttpStatus.CREATED).body("Reserva creada");
            }

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear reserva: " + e.getMessage());
        }
    }

    @DeleteMapping("/reservas/{id}")
    public ResponseEntity<String> borrarReserva(@PathVariable int id) {
        String sqlReserva = "SELECT id_clase FROM reservas WHERE id = ?";
        String sqlDelete = "DELETE FROM reservas WHERE id = ?";
        String sqlUpdatePlazas = "UPDATE clases SET plazas = plazas + 1 WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmtReserva = conn.prepareStatement(sqlReserva)) {

            stmtReserva.setInt(1, id);
            int idClase;

            try (ResultSet rs = stmtReserva.executeQuery()) {
                if (!rs.next()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("No se encontro reserva con ID: " + id);
                }
                idClase = rs.getInt("id_clase");
            }

            try (PreparedStatement stmtDelete = conn.prepareStatement(sqlDelete)) {
                stmtDelete.setInt(1, id);
                stmtDelete.executeUpdate();
            }

            try (PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdatePlazas)) {
                stmtUpdate.setInt(1, idClase);
                stmtUpdate.executeUpdate();
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body("Reserva con ID " + id + " eliminada, plaza liberada");

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error al borrar reserva: " + e.getMessage());
        }
    }

    @GetMapping("/clases/{id}/reservas")
    public ResponseEntity<String> listarSocios(@PathVariable int id) {
        String sql = "SELECT id, socio, fecha FROM reservas WHERE id_clase = ? ORDER BY id";
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
