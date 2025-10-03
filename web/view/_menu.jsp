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

        <%-- Admin --%>
        <% if (_mrol != null && _mrol == 1) { %>
          <li class="nav-item">
            <!-- aquí está el arreglo del 404 de /directorio: apuntamos al servlet /directorio -->
            <a class="nav-link" href="${pageContext.request.contextPath}/directorio">Directorio</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/visitantes">Visitantes</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/visitantes?op=new">
              <i class="bi bi-person-plus me-1"></i>Registrar visitante
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/view/guardia/scan.jsp">
              <i class="bi bi-qr-code-scan me-1"></i>Escanear QR
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/guardia/avisos">
              <i class="bi bi-megaphone me-1"></i>Enviar avisos
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/comunicacion">
                <i class="bi bi-chat-dots me-1"></i>Comunicación Interna
            </a>
          </li>

        <% } %>

        <%-- Guardia --%>
        <% if (_mrol != null && _mrol == 3) { %>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/visitantes?op=new">
              <i class="bi bi-person-plus me-1"></i>Registrar visitante
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/view/guardia/scan.jsp">
              <i class="bi bi-qr-code-scan me-1"></i>Escanear QR
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/guardia/avisos">
              <i class="bi bi-megaphone me-1"></i>Enviar avisos
            </a>
          </li>
        <% } %>

        <%-- Residente --%>
        <% if (_mrol != null && _mrol == 2) { %>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/view/residente/qr.jsp">
              <i class="bi bi-qr-code me-1"></i>Mi QR
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/directorio">
              <i class="bi bi-people me-1"></i>Directorio
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="${pageContext.request.contextPath}/visitantes?op=new">
              <i class="bi bi-person-plus me-1"></i>Registrar visitante
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
