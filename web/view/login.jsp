<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
  String ctx = request.getContextPath();

  // Mensaje de error (desde el servlet o por ?err=1)
  String error = (String) request.getAttribute("error");
  if (error == null) {
    String e = request.getParameter("err");
    if ("1".equals(e)) error = "Usuario o contraseña incorrectos.";
  }

  // Prellenado del campo usuario: 1) atributo 'correo' del servlet, 2) parámetro 'user'
  String lastUser = (String) request.getAttribute("correo");
  if (lastUser == null) lastUser = request.getParameter("user");
  if (lastUser == null) lastUser = "";
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
  <style>.auth-card{max-width:520px;}</style>
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
        <div><%= error %></div>
      </div>
    <% } %>

    <!-- Envío POR POST  -->
    <form method="post" action="<%=ctx%>/login" autocomplete="off" spellcheck="false">
      <div class="mb-3">
        <label class="form-label">Usuario o correo</label>
        <input class="form-control"
               name="user"
               autocomplete="username"
               autocapitalize="off"
               value="<%= lastUser %>"
               required
               autofocus>
      </div>
      <div class="mb-4">
        <label class="form-label">Contraseña</label>
        <input class="form-control"
               type="password"
               name="pass"
               autocomplete="off"
               autocapitalize="off"
               spellcheck="false"
               required>
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
