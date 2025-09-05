<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
  // Roles: 1=ADMIN, 2=RESIDENTE, 3=GUARDIA
  Integer _mrol = (Integer) session.getAttribute("rol");
%>
<nav class="navbar navbar-expand-lg navbar-light bg-light mb-3">
  <div class="container-fluid">
    <a class="navbar-brand" href="${pageContext.request.contextPath}/">MiCasitaSegura</a>
    <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navMain">
      <span class="navbar-toggler-icon"></span>
    </button>

    <div class="collapse navbar-collapse" id="navMain">
      <ul class="navbar-nav me-auto mb-2 mb-lg-0">

        <%-- Admin: Directorio + lista visitantes + registrar + escanear --%>
        <% if (_mrol != null && _mrol == 1) { %>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/directorio">Directorio</a>
          </li>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/visitantes">Visitantes</a></li>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/visitantes?op=new">Registrar visitante</a></li>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/view/guardia/scan.jsp">
              <i class="bi bi-qr-code-scan me-1"></i>Escanear QR
            </a>
          </li>
        <% } %>

        <%-- Guardia: solo registrar y escanear (no lista ni directorio) --%>
        <% if (_mrol != null && _mrol == 3) { %>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/visitantes?op=new">Registrar visitante</a></li>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/view/guardia/scan.jsp">
              <i class="bi bi-qr-code-scan me-1"></i>Escanear QR
            </a>
          </li>
        <% } %>

        <%-- Residente: solo Mi QR --%>
        <% if (_mrol != null && _mrol == 2) { %>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/view/residente/qr.jsp">
              <i class="bi bi-qr-code me-1"></i>Mi QR
            </a>
          </li>
        <% } %>
      </ul>

      <ul class="navbar-nav">
        <li class="nav-item">
          <a class="nav-link" href="${pageContext.request.contextPath}/logout">
            <i class="bi bi-box-arrow-right me-1"></i>Salir
          </a>
        </li>
      </ul>
    </div>
  </div>
</nav>
