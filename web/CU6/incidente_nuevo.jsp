<%@page contentType="text/html;charset=UTF-8"%>
<%
  HttpSession s = request.getSession(false);
  Integer rol = (s == null) ? null : (Integer) s.getAttribute("rol");
  if (rol == null) { response.sendRedirect(request.getContextPath() + "/login"); return; }
  if (rol != 1 && rol != 2 && rol != 3) { response.sendError(403); return; }

  String ctx = request.getContextPath();
  String panelUrl = ctx + "/index.jsp";
  if (rol == 1) panelUrl = ctx + "/view/admin/dashboard.jsp";
  else if (rol == 2) panelUrl = ctx + "/view/guardia/control.jsp";
  else if (rol == 3) panelUrl = ctx + "/view/residente/qr.jsp";

  String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html>
<head>
  <title>Reportar incidente</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
</head>
<body>
<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h4 class="mb-0">Reportar incidente</h4>
    <div class="btn-group">
      <a href="<%=ctx%>/comunicacion" class="btn btn-outline-secondary btn-sm">Volver</a>
      <a href="<%=panelUrl%>" class="btn btn-outline-primary btn-sm">Panel principal</a>
    </div>
  </div>

  <% if (error != null) { %>
    <div class="alert alert-warning"><%= error %></div>
  <% } %>

  <form method="post" id="formInc">
    <div class="mb-3">
      <label class="form-label">Tipo</label>
      <select name="tipo" class="form-select" required>
        <option value="">-- seleccione --</option>
        <option>DISTURBIOS</option>
        <option>RUIDO</option>
        <option>ACCIDENTE_VEHICULAR</option>
        <option>DAÑOS_INMOBILIARIOS</option>
        <option>OTROS</option>
      </select>
    </div>
    <div class="mb-3">
      <label class="form-label">Fecha y hora</label>
      <input type="datetime-local" name="fechaHora" class="form-control" required>
    </div>
    <div class="mb-3">
      <label class="form-label">Descripción (máx 200)</label>
      <textarea name="descripcion" class="form-control" maxlength="200" rows="3" required></textarea>
    </div>

    <div class="d-flex gap-2">
      <button id="btnGuardar" class="btn btn-danger" type="submit" disabled>Guardar</button>
      <a href="<%=ctx%>/comunicacion" class="btn btn-secondary">Regresar</a>
    </div>
  </form>
</div>

<script>
const form = document.getElementById('formInc');
const btn  = document.getElementById('btnGuardar');
function check() {
  const tipo = form.tipo.value.trim();
  const fh   = form.fechaHora.value.trim();
  const d    = form.descripcion.value.trim();
  btn.disabled = !(tipo && fh && d.length>0 && d.length<=200);
}
form.addEventListener('input', check);
check();
</script>
</body>
</html>
