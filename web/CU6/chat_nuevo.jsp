<%-- 
    Document   : chat_nuevo
    Created on : 2/10/2025, 11:14:24 AM
    Author     : abrah
--%>
<%@page contentType="text/html;charset=UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="service.ConversacionService.UsuarioMin"%>
<!DOCTYPE html>
<html>
<head>
  <title>Nueva conversación</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
</head>
<body>
<div class="container py-4">
  <h4 class="mb-3">Selecciona un guardia activo</h4>

  <% String error = (String) request.getAttribute("error");
     if (error != null) { %>
     <div class="alert alert-warning"><%= error %></div>
  <% } %>

  <form method="post">
    <div class="row g-3">
      <%
        List<UsuarioMin> guardias = (List<UsuarioMin>) request.getAttribute("guardias");
        for (UsuarioMin g : guardias) {
      %>
        <div class="col-12 col-md-6">
          <div class="card shadow-sm">
            <div class="card-body d-flex align-items-center">
              <input class="form-check-input me-2" type="radio" name="guardiaId" value="<%=g.id%>" required>
              <div>
                <div class="fw-semibold"><%=g.nombre%></div>
                <div class="text-muted small"><%=g.correo%></div>
              </div>
            </div>
          </div>
        </div>
      <% } %>
    </div>

    <div class="d-flex gap-2 mt-4">
      <button type="submit" class="btn btn-primary">Guardar</button>
      <a href="${pageContext.request.contextPath}/comunicacion" class="btn btn-secondary">Cancelar</a>
    </div>
  </form>
</div>
</body>
</html>

