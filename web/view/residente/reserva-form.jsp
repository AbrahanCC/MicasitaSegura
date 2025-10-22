<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.AreaComun" %>
<%@ page import="model.Usuario" %>
<%
  String ctx = request.getContextPath();
  List<AreaComun> areas = (List<AreaComun>) request.getAttribute("areas");
  if (areas == null) { response.sendRedirect(ctx + "/residente/reservas"); return; } // pasar por controller
  Usuario usuario = (Usuario) session.getAttribute("usuario");
  boolean hayAreas = !areas.isEmpty();

  String flashOk  = (String) session.getAttribute("flash_ok");
  String flashErr = (String) session.getAttribute("flash_err");
  if (flashOk  != null) session.removeAttribute("flash_ok");
  if (flashErr != null) session.removeAttribute("flash_err");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <title>Registro de reservas</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body>
  <jsp:include page="/view/_menu.jsp" />

  <div class="container my-4" style="max-width: 720px;">

    <% if (flashOk != null) { %>
      <div class="alert alert-success alert-dismissible fade show" role="alert">
        <i class="bi bi-check-circle me-1"></i> <%= flashOk %>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
      </div>
    <% } %>
    <% if (flashErr != null) { %>
      <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <i class="bi bi-exclamation-triangle me-1"></i> <%= flashErr %>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
      </div>
    <% } %>

    <div class="card shadow-sm">
      <div class="card-header bg-white d-flex justify-content-between align-items-center">
        <h5 class="m-0"><i class="bi bi-calendar-plus me-1"></i> Registro de reservas</h5>
        <!-- Ver mis reservas (lista) -->
        <a class="btn btn-link" href="<%= ctx %>/residente/reservas?action=listar">
          <i class="bi bi-list-ul me-1"></i> Ver mis reservas
        </a>
      </div>

      <div class="card-body">
        <% if (!hayAreas) { %>
          <div class="alert alert-warning" role="alert">
            <i class="bi bi-info-circle me-1"></i> No hay áreas activas para reservar.
          </div>
        <% } %>

        <form method="post" action="<%= ctx %>/residente/reservas" class="row g-3">
          <input type="hidden" name="action" value="crear"/>

          <!-- RN1: Salón para reservar -->
          <div class="col-12">
            <label class="form-label">Area para reservar</label>
            <select name="area_id" class="form-select" required <%= hayAreas ? "" : "disabled" %>>
              <option value="">-- Seleccionar --</option>
              <% for (AreaComun a : areas) { %>
                <option value="<%= a.getId() %>"><%= a.getNombre() %></option>
              <% } %>
            </select>
          </div>

          <!-- RN2: Persona que reserva -->
          <div class="col-md-6">
            <label class="form-label">Persona que reserva</label>
            <div class="input-group">
              <span class="input-group-text"><i class="bi bi-person"></i></span>
              <input type="text" class="form-control"
                     value="<%= (usuario != null ? usuario.getNombre() : "") %>" readonly/>
            </div>
          </div>

          <div class="col-md-6">
            <label class="form-label">Fecha *</label>
            <input type="date" name="fecha" class="form-control" required <%= hayAreas ? "" : "disabled" %>/>
          </div>

          <div class="col-md-6">
            <label class="form-label">Hora Inicio *</label>
            <input type="time" name="hora_inicio" class="form-control" required <%= hayAreas ? "" : "disabled" %>/>
          </div>

          <div class="col-md-6">
            <label class="form-label">Hora Fin *</label>
            <input type="time" name="hora_fin" class="form-control" required <%= hayAreas ? "" : "disabled" %>/>
          </div>

          <div class="col-12 d-flex gap-2">
            <button type="submit" class="btn btn-primary" <%= hayAreas ? "" : "disabled" %>>
              <i class="bi bi-check2-circle me-1"></i> Registrar Reserva
            </button>
            <!-- Cancelar: te lleva a Mi QR -->
            <a href="<%= ctx %>/view/residente/qr.jsp" class="btn btn-outline-secondary">
              <i class="bi bi-x-lg me-1"></i> Cancelar
            </a>
          </div>
        </form>
      </div>
    </div>
  </div>

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
