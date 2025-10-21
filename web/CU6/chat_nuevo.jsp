<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page isELIgnored="false"%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Consulta General (Chat)</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <!-- Usa CDN como en las otras páginas -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
  <style>.card + .card { margin-top: 1rem; }</style>
</head>
<body>
<%
  HttpSession s = request.getSession(false);
  Integer rol = (s == null) ? null : (Integer) s.getAttribute("rol");
  String ctx = request.getContextPath();
  String panelUrl = ctx + "/index.jsp";
  if (rol != null) {
    if (rol == 1) panelUrl = ctx + "/view/admin/dashboard.jsp";
    else if (rol == 2) panelUrl = ctx + "/view/guardia/control.jsp";
    else if (rol == 3) panelUrl = ctx + "/view/residente/qr.jsp";
  }
%>

<div class="container my-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h2 class="mb-0">Comunicación Interna &raquo; Consulta General</h2>
    <div class="btn-group">
      <a href="<%=ctx%>/comunicacion" class="btn btn-outline-secondary btn-sm">Volver</a>
      <a href="<%=panelUrl%>" class="btn btn-outline-primary btn-sm">Panel principal</a>
    </div>
  </div>

  <div class="row g-4">
    <div class="col-lg-6">
      <div class="card shadow-sm">
        <div class="card-body">
          <h5 class="card-title mb-3">Mis conversaciones activas</h5>
          <%
            java.util.List list = (java.util.List) request.getAttribute("convs");
            if (list == null || list.isEmpty()) {
          %>
            <p class="text-muted">No tienes conversaciones activas.</p>
          <%
            } else {
          %>
            <div class="list-group">
              <%
                for (Object o : list) {
                  model.Conversacion c = (model.Conversacion) o;
              %>
                <a class="list-group-item list-group-item-action d-flex justify-content-between align-items-center"
                   href="<%=ctx%>/chat?id=<%= c.getId() %>">
                  <span>ID #<%= c.getId() %> — Estado: <strong><%= c.getEstado() %></strong></span>
                  <small class="text-muted">
                    Último: <%= c.getFechaUltimoMensaje() != null ? c.getFechaUltimoMensaje() : c.getFechaCreacion() %>
                  </small>
                </a>
              <%
                }
              %>
            </div>
          <%
            }
          %>
        </div>
      </div>
    </div>

    <div class="col-lg-6">
      <div class="card shadow-sm">
        <div class="card-body">
          <h5 class="card-title mb-3">Crear nueva conversación</h5>

          <%
            Boolean puedeCrear = (Boolean) request.getAttribute("puedeCrear");
            if (puedeCrear != null && puedeCrear.booleanValue()) {
          %>
            <form method="post" action="<%=ctx%>/chat/nuevo" id="formCrear">
              <div class="mb-3">
                <label for="guardiaId" class="form-label">Selecciona un guardia</label>
                <select class="form-select" id="guardiaId" name="guardiaId" required>
                  <option value="">-- Seleccionar --</option>
                  <%
                    java.util.List g = (java.util.List) request.getAttribute("guardias");
                    if (g != null) {
                      for (Object x : g) {
                        service.ConversacionService.UsuarioMin u =
                            (service.ConversacionService.UsuarioMin) x;
                  %>
                    <option value="<%= u.id %>">
                      <%= u.nombre %><%= (u.correo != null && !u.correo.trim().isEmpty()) ? " ("+u.correo+")" : "" %>
                    </option>
                  <%
                      }
                    }
                  %>
                </select>
              </div>

              <div class="d-flex gap-2">
                <button type="submit" class="btn btn-primary" id="btnGuardar" disabled>Guardar</button>
                <a href="<%=ctx%>/comunicacion" class="btn btn-outline-secondary">Cancelar</a>
              </div>
            </form>
          <%
            } else {
          %>
            <p class="text-muted">No tienes permisos para crear nuevas conversaciones.</p>
          <%
            }
          %>
        </div>
      </div>
    </div>
  </div>
</div>

<script>
(function(){
  var sel = document.getElementById('guardiaId');
  var btn = document.getElementById('btnGuardar');
  if (!sel || !btn) return;
  var toggle = function(){ btn.disabled = (sel.value.trim() === ""); };
  sel.addEventListener('change', toggle);
  toggle();
})();
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
