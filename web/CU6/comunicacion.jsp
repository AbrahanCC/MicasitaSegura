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

  String msg = (String) session.getAttribute("msgExito");
  if (msg != null) session.removeAttribute("msgExito");
%>
<!DOCTYPE html>
<html>
<head>
  <title>Comunicación Interna</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
</head>
<body class="bg-light">
<div class="container py-4">
  <div class="d-flex justify-content-between align-items-center mb-3">
    <h3 class="mb-0">Comunicación Interna</h3>
    <a class="btn btn-outline-primary btn-sm" href="<%=panelUrl%>">Panel principal</a>
  </div>

  <div class="row g-3">
    <div class="col-12 col-md-6">
      <div class="card shadow-sm h-100">
        <div class="card-body">
          <h5 class="card-title">Consulta General (Chat)</h5>
          <p class="card-text">Inicia una conversación con un guardia de seguridad.</p>
          <a class="btn btn-primary w-100" href="<%=ctx%>/chat/nuevo">
            Crear nueva conversación
          </a>
        </div>
      </div>
    </div>

    <div class="col-12 col-md-6">
      <div class="card shadow-sm h-100">
        <div class="card-body">
          <h5 class="card-title">Reportar incidente</h5>
          <p class="card-text">Notifica un incidente a los guardias activos.</p>
          <a class="btn btn-danger w-100" href="<%=ctx%>/incidente">
            Reportar incidente
          </a>
        </div>
      </div>
    </div>
  </div>

  <% if (msg != null) { %>
    <div class="alert alert-success mt-3"><%= msg %></div>
  <% } %>
</div>
</body>
</html>
