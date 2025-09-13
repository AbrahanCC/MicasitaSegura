<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    HttpSession s = request.getSession(false);
    if (s != null) {
        Integer rol = (Integer) s.getAttribute("rol");
        String ctx = request.getContextPath();
        if (rol != null) {
            if (rol == 1) { response.sendRedirect(ctx + "/view/admin/dashboard.jsp"); return; }
            if (rol == 2) { response.sendRedirect(ctx + "/view/residente/qr.jsp"); return; }
            if (rol == 3) { response.sendRedirect(ctx + "/view/guardia/control.jsp"); return; }
        }
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <title>Mi Casita Segura</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="<%=request.getContextPath()%>/assets/css/app.css" rel="stylesheet">
</head>
<body>
<div class="container py-4 d-flex justify-content-center" style="min-height:100vh;">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:700px;">
    <div class="text-center mb-4">
      <div class="brand-badge mx-auto mb-3"><i class="bi bi-house-door"></i></div>
      <h4 class="mb-1">Mi Casita Segura</h4>
      <small class="text-muted">Bienvenido</small>
    </div>

    <div class="d-grid">
      <a class="btn btn-brand btn-lg" href="<%=request.getContextPath()%>/login">
        <i class="bi bi-box-arrow-in-right me-2"></i>Iniciar sesión
      </a>
    </div>
  </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
