<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="javax.servlet.http.HttpSession" %>
<%
  // ---- Guardia autenticado (rol=2) o redirige a /login ----
  HttpSession s = request.getSession(false);
  Integer rol   = (s == null) ? null : (Integer) s.getAttribute("rol");
  String uname  = (s == null) ? ""   : (String)  s.getAttribute("uname");
  if (rol == null || rol != 2) {
    response.sendRedirect(request.getContextPath() + "/login");
    return;
  }
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Guardia | Panel principal</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="${pageContext.request.contextPath}/assets/css/app.css" rel="stylesheet">
</head>
<body>

<jsp:include page="/view/_menu.jsp" />

<div class="container py-4 d-flex justify-content-center" style="min-height:100vh;">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:700px;">

    <div class="text-center mb-4">
      <div class="brand-badge mx-auto mb-3"><i class="bi bi-shield-lock"></i></div>
      <h4 class="mb-1">Mi Casita Segura</h4>
      <small class="text-muted">Panel principal - Guardia</small>
    </div>

    <div class="mb-3">
      <strong>Bienvenido Guardia:</strong> <%= uname %>
    </div>

    <div class="d-grid gap-3">
      <a class="btn btn-outline-secondary btn-lg" href="${pageContext.request.contextPath}/directorio">
        <i class="bi bi-book me-2"></i>Ver Directorio
      </a>

      <a class="btn btn-brand btn-lg" href="${pageContext.request.contextPath}/view/guardia/scan.jsp?auto=1">
        <i class="bi bi-qr-code-scan me-2"></i>Escanear QR
      </a>

      <a class="btn btn-outline-warning btn-lg" href="${pageContext.request.contextPath}/guardia/avisos">
        <i class="bi bi-megaphone me-2"></i>Mandar avisos
      </a>
        
      <a class="btn btn-outline-warning btn-lg" href="${pageContext.request.contextPath}/CU6/chat.jsp?auto=1">
        <i class="bi bi-megaphone me-2"></i>Chat general
      </a>
    </div>

    <hr class="my-4">

    <div class="text-end">
      <a class="btn btn-link" href="${pageContext.request.contextPath}/logout">
        <i class="bi bi-box-arrow-right me-1"></i>Cerrar sesión
      </a>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
