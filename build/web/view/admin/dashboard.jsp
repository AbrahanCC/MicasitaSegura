<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String ctx = request.getContextPath();
    HttpSession s = request.getSession(false);
    Integer rol = (s == null) ? null : (Integer) s.getAttribute("rol");
    String uname = (s == null) ? "" : (String) s.getAttribute("uname");
    if (rol == null || rol != 1) { response.sendRedirect(ctx + "/login"); return; }
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Admin | Panel principal</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="<%=ctx%>/assets/css/app.css" rel="stylesheet">
</head>
<body>

<%@ include file="/view/_menu.jsp" %>

<div class="container py-4 d-flex justify-content-center" style="min-height:100vh;">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:700px;">
    <div class="text-center mb-4">
      <div class="brand-badge mx-auto mb-3"><i class="bi bi-house-door"></i></div>
      <h4 class="mb-1">Mi Casita Segura</h4>
      <small class="text-muted">Panel principal</small>
    </div>

    <div class="mb-3">
      <strong>Bienvenido Admin:</strong> <%= (uname == null ? "" : uname) %>
    </div>

    <div class="d-grid gap-3">
      <a class="btn btn-brand btn-lg" href="<%=ctx%>/usuarios">
        <i class="bi bi-people me-2"></i>Gestión de Usuarios
      </a>
      <a class="btn btn-outline-secondary btn-lg" href="<%=ctx%>/pingdb">
        <i class="bi bi-database-check me-2"></i>Probar conexión a BD
      </a>
    </div>

    <hr class="my-4">

    <div class="text-end">
      <a class="btn btn-link" href="<%=ctx%>/logout"><i class="bi bi-box-arrow-right me-1"></i>Cerrar sesión</a>
    </div>
  </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
