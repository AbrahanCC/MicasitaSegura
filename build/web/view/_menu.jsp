<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
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
        <% if (_mrol != null && (_mrol == 1 || _mrol == 2 || _mrol == 3)) { %>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/directorio">Directorio</a></li>
        <% } %>
        <% if (_mrol != null && (_mrol == 1 || _mrol == 3)) { %>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/visitantes">Visitantes</a></li>
          <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/visitantes?op=new">Registrar visitante</a></li>
        <% } %>
      </ul>
      <ul class="navbar-nav">
        <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/logout">Salir</a></li>
      </ul>
    </div>
  </div>
</nav>
