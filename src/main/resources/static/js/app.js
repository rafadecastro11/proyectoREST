const API_BASE = 'http://localhost:8080';

function mostrarAlerta(mensaje, tipo) {
    const alertBox = document.getElementById('alertBox');
    const iconos = {
        success: '<i class="fas fa-check-circle me-2"></i>',
        danger: '<i class="fas fa-exclamation-circle me-2"></i>',
        warning: '<i class="fas fa-exclamation-triangle me-2"></i>',
        info: '<i class="fas fa-info-circle me-2"></i>'
    };
    alertBox.innerHTML = '<div class="alert alert-' + tipo + ' alert-dismissible fade show d-flex align-items-center" role="alert">'
        + (iconos[tipo] || '')
        + mensaje
        + '<button type="button" class="btn-close ms-auto" data-bs-dismiss="alert"></button>'
        + '</div>';
    setTimeout(() => { alertBox.innerHTML = ''; }, 6000);
}

document.getElementById('formReserva').addEventListener('submit', async function (e) {
    e.preventDefault();
    const payload = {
        id_clase: parseInt(document.getElementById('id_clase').value),
        socio: document.getElementById('socio').value.trim(),
        fecha: document.getElementById('fecha').value
    };
    if (!payload.id_clase || !payload.socio || !payload.fecha) {
        mostrarAlerta('Todos los campos son obligatorios.', 'warning');
        return;
    }
    try {
        const res = await fetch(API_BASE + '/reservas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const texto = await res.text();
        if (res.ok) {
            mostrarAlerta(texto, 'success');
            this.reset();
        } else {
            mostrarAlerta(texto, 'danger');
        }
    } catch (err) {
        mostrarAlerta('Error de conexión: ' + err.message, 'danger');
    }
});

let ultimosResultados = [];

async function buscarReservas() {
    const q = document.getElementById('inputBusqueda').value.trim();
    if (!q) {
        mostrarAlerta('Introduce un término de búsqueda.', 'warning');
        return;
    }
    const tbody = document.getElementById('tablaReservas');
    const emptyState = document.getElementById('emptyState');
    tbody.innerHTML = '';
    emptyState.classList.add('d-none');
    try {
        const res = await fetch(API_BASE + '/reservas/buscar?q=' + encodeURIComponent(q));
        if (!res.ok) {
            mostrarAlerta('Error al buscar reservas.', 'danger');
            return;
        }
        const datos = await res.json();
        ultimosResultados = datos;
        if (datos.length === 0) {
            emptyState.classList.remove('d-none');
            return;
        }
        datos.forEach(function (item) {
            const tr = document.createElement('tr');
            tr.innerHTML =
                '<td class="fw-bold text-muted">' + item.id + '</td>' +
                '<td>' + item.socio + '</td>' +
                '<td>' + item.nombre_clase + '</td>' +
                '<td>' + item.fecha + '</td>' +
                '<td class="text-center"><button class="btn btn-warning btn-sm" onclick="editarReserva(' + item.id + ')"><i class="fas fa-edit"></i></button></td>' +
                '<td class="text-center"><button class="btn btn-danger btn-sm" onclick="eliminarReserva(' + item.id + ')"><i class="fas fa-trash"></i></button></td>';
            tbody.appendChild(tr);
        });
    } catch (err) {
        mostrarAlerta('Error de conexión: ' + err.message, 'danger');
    }
}

function editarReserva(id) {
    const item = ultimosResultados.find(function(r) { return r.id === id; });
    if (!item) {
        mostrarAlerta('No se encontraron datos de la reserva.', 'danger');
        return;
    }
    document.getElementById('edit_id').value = item.id;
    document.getElementById('edit_id_clase').value = item.id_clase;
    document.getElementById('edit_socio').value = item.socio;
    document.getElementById('edit_fecha').value = item.fecha;
    const modal = new bootstrap.Modal(document.getElementById('modalEditar'));
    modal.show();
}

document.getElementById('formEditar').addEventListener('submit', async function (e) {
    e.preventDefault();
    const id = parseInt(document.getElementById('edit_id').value);
    const payload = {
        id_clase: parseInt(document.getElementById('edit_id_clase').value),
        socio: document.getElementById('edit_socio').value.trim(),
        fecha: document.getElementById('edit_fecha').value
    };
    if (!payload.id_clase || !payload.socio || !payload.fecha) {
        mostrarAlerta('Todos los campos son obligatorios.', 'warning');
        return;
    }
    try {
        const res = await fetch(API_BASE + '/reservas/' + id, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const texto = await res.text();
        if (res.ok) {
            mostrarAlerta(texto, 'success');
            bootstrap.Modal.getInstance(document.getElementById('modalEditar')).hide();
            buscarReservas();
        } else {
            mostrarAlerta(texto, 'danger');
        }
    } catch (err) {
        mostrarAlerta('Error de conexión: ' + err.message, 'danger');
    }
});

document.getElementById('btnBuscar').addEventListener('click', buscarReservas);
document.getElementById('inputBusqueda').addEventListener('keydown', function (e) {
    if (e.key === 'Enter') buscarReservas();
});

async function eliminarReserva(id) {
    if (!confirm('¿Eliminar la reserva #' + id + '?')) return;
    try {
        const res = await fetch(API_BASE + '/reservas/' + id, { method: 'DELETE' });
        const texto = await res.text();
        if (res.ok) {
            mostrarAlerta(texto, 'success');
            buscarReservas();
        } else {
            mostrarAlerta(texto, 'danger');
        }
    } catch (err) {
        mostrarAlerta('Error de conexión: ' + err.message, 'danger');
    }
}
