<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Reserva" %>
<%
  String ctx = request.getContextPath();
  List<Reserva> reservas = (List<Reserva>) request.getAttribute("reservas");

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
  <title>Mis reservas</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body>
  <jsp:include page="/view/_menu.jsp" />

  <div class="container my-4">
    <div class="d-flex align-items-center justify-content-between mb-3">
      <h3 class="m-0">Mis reservas</h3>
      <a class="btn btn-outline-secondary" href="<%= ctx %>/residente/reservas">
        <i class="bi bi-arrow-left"></i> Volver al formulario
      </a>
    </div>

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
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Área</th>
                <th>Fecha</th>
                <th>Inicio</th>
                <th>Fin</th>
                <th>Estado</th>
                <th class="text-center">Acciones</th>
              </tr>
            </thead>
            <tbody>
            <% if (reservas != null && !reservas.isEmpty()) {
                 for (Reserva r : reservas) {
                     String badgeClass = "text-bg-primary";
                     if ("CANCELADA".equalsIgnoreCase(r.getEstado())) badgeClass = "text-bg-danger";
                     else if ("CREADA".equalsIgnoreCase(r.getEstado())) badgeClass = "text-bg-success";
            %>
              <tr>
                <td class="fw-medium"><i class="bi bi-building me-1"></i><%= r.getAreaNombre() %></td>
                <td><span class="badge text-bg-secondary"><%= r.getFecha() %></span></td>
                <td><%= r.getHoraInicio() %></td>
                <td><%= r.getHoraFin() %></td>
                <td><span class="badge <%= badgeClass %>"><%= r.getEstado() %></span></td>
                <td class="text-center">
                  <% if (!"CANCELADA".equalsIgnoreCase(r.getEstado())) { %>
                    <form method="post" action="<%= ctx %>/residente/reservas"
                          onsubmit="return confirm('¿Desea cancelar la reserva?');" class="d-inline">
                      <input type="hidden" name="action" value="cancelar"/>
                      <input type="hidden" name="id" value="<%= r.getId() %>"/>
                      <button type="submit" class="btn btn-outline-danger btn-sm">
                        <i class="bi bi-x-circle me-1"></i> Cancelar
                      </button>
                    </form>
                  <% } else { %>
                    <span class="text-muted">—</span>
                  <% } %>
                </td>
              </tr>
            <% } } else { %>
              <tr>
                <td colspan="6" class="text-center text-muted py-4">
                  <i class="bi bi-calendar2-x me-1"></i> No hay reservas.
                </td>
              </tr>
            <% } %>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
