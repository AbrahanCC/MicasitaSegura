<%-- 
    Document   : comunicacion
    Permitir acceso a ADMIN (1) y RESIDENTE (2)
--%>
<%@page contentType="text/html;charset=UTF-8"%>
<%
  // --- Control de acceso básico en la vista ---
  HttpSession s = request.getSession(false);
  Integer rol = (s == null) ? null : (Integer) s.getAttribute("rol");
  if (rol == null) { response.sendRedirect(request.getContextPath() + "/login"); return; }
  if (rol != 1 && rol != 2) { response.sendError(403); return; } // AQUÍ ESTÁ EL ARREGLO: Admin(1) y Residente(2)
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
  <h3 class="mb-4">Comunicación Interna</h3>

  <div class="row g-3">
    <div class="col-12 col-md-6">
      <div class="card shadow-sm h-100">
        <div class="card-body">
          <h5 class="card-title">Consulta General (Chat)</h5>
          <p class="card-text">Inicia una conversación con un guardia de seguridad.</p>
          <!-- AQUÍ ESTÁ EL ARREGLO: navegar SIEMPRE al servlet /chat/nuevo -->
          <a class="btn btn-primary w-100" href="${pageContext.request.contextPath}/chat/nuevo">
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
          <a class="btn btn-danger w-100" href="${pageContext.request.contextPath}/incidente">
            Reportar incidente
          </a>
        </div>
      </div>
    </div>
  </div>

  <%
    String msg = (String) session.getAttribute("msgExito");
    if (msg != null) { session.removeAttribute("msgExito"); %>
      <div class="alert alert-success mt-3"><%= msg %></div>
  <% } %>
</div>
</body>
</html>
