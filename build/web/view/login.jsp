<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
  String ctx   = request.getContextPath();
  String error = (String) request.getAttribute("error");
  if (error == null) {
    String e = request.getParameter("err");
    if ("1".equals(e)) error = "Usuario o contraseña incorrectos.";
  }
  String lastUser = request.getParameter("user") != null ? request.getParameter("user") : "";
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <title>Login • Mi Casita Segura</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="<%=ctx%>/assets/css/app.css" rel="stylesheet">
  <style>
    .auth-card{max-width:520px;}
  </style>
</head>
<body style="min-height:100vh;">

<div class="container py-4 d-flex justify-content-center align-items-center" style="min-height:100vh;">
  <div class="glass p-4 p-sm-5 w-100 auth-card">
    <div class="text-center mb-3">
      <div class="brand-badge mx-auto mb-3"><i class="bi bi-house-door"></i></div>
      <h4 class="mb-1">Mi Casita Segura</h4>
      <small class="text-muted">Iniciar sesión</small>
    </div>

    <% if (error != null) { %>
      <div class="alert alert-danger d-flex align-items-center" role="alert">
        <i class="bi bi-exclamation-triangle-fill me-2"></i>
        <div><%=error%></div>
      </div>
    <% } %>

    <form method="post" action="<%=ctx%>/login">
      <div class="mb-3">
        <label class="form-label">Usuario o correo</label>
        <input class="form-control" name="user" autocomplete="username" value="<%=lastUser%>" required>
      </div>
      <div class="mb-4">
        <label class="form-label">Contraseña</label>
        <input class="form-control" type="password" name="pass" autocomplete="current-password" required>
      </div>
      <div class="d-grid">
        <button class="btn btn-brand" type="submit">
          <i class="bi bi-box-arrow-in-right me-2"></i>Entrar
        </button>
      </div>
    </form>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
