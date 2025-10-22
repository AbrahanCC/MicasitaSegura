<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*,model.Usuario" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
  String ctx = request.getContextPath();
  List<Usuario> data = (List<Usuario>) request.getAttribute("data");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Usuarios • Mi Casita Segura</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="<%=ctx%>/assets/css/app.css" rel="stylesheet">
</head>
<body>
<jsp:include page="/view/_menu.jsp" />

<div class="container py-4 d-flex justify-content-center">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:1080px;">
    <div class="d-flex align-items-center mb-4">
      <div class="brand-badge me-3"><i class="bi bi-people"></i></div>
      <div>
        <h4 class="mb-0">Usuarios registrados</h4>
        <small class="text-muted">Listado general del sistema</small>
      </div>
      <div class="ms-auto">
        <!-- FA1 paso 1: Crear Usuario -->
        <a class="btn btn-brand" href="<%=ctx%>/usuarios?op=new">
          <i class="bi bi-person-plus me-1"></i>Crear usuario
        </a>
      </div>
    </div>

    <!-- Mensajes del servidor -->
    <c:if test="${not empty sessionScope.flashOk}">
      <div class="alert alert-success">${sessionScope.flashOk}</div>
      <c:remove var="flashOk" scope="session" />
    </c:if>
    <c:if test="${not empty sessionScope.flashErr}">
      <div class="alert alert-danger">${sessionScope.flashErr}</div>
      <c:remove var="flashErr" scope="session" />
    </c:if>

    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light">
          <tr>
            <th>DPI Usuario</th>
            <th>Nombre</th>
            <th>Apellidos</th>
            <th>Correo</th>
            <th>Número de casa</th>
            <th>Rol</th>
            <th class="text-center">Acciones</th>
          </tr>
        </thead>
        <tbody>
        <%
          if (data == null || data.isEmpty()) {
        %>
          <tr><td colspan="7" class="text-center text-muted">No hay usuarios registrados.</td></tr>
        <%
          } else {
            for (Usuario u : data) {
        %>
          <tr>
            <td><%= u.getDpi() != null ? u.getDpi() : "-" %></td>
            <td><%= u.getNombre() %></td>
            <td><%= u.getApellidos() %></td>
            <td><%= u.getCorreo() %></td>
            <td>
              <span class="badge text-bg-secondary">
                <%= (u.getLote() != null ? u.getLote()+"-" : "") %><%= (u.getNumeroCasa() != null ? u.getNumeroCasa() : "-") %>
              </span>
            </td>
            <td><%= (u.getRolNombre() != null && !u.getRolNombre().isEmpty()) ? u.getRolNombre() : "-" %></td>
            <td class="text-center">
              <a href="<%=ctx%>/usuarios?op=edit&id=<%=u.getId()%>" class="btn btn-sm btn-outline-primary me-1" title="Editar">
                <i class="bi bi-pencil-square"></i>
              </a>
              <!-- FA2: confirmar eliminar -->
              <a href="<%=ctx%>/usuarios?op=del&id=<%=u.getId()%>"
                 class="btn btn-sm btn-outline-danger"
                 onclick="return confirm('¿Está seguro de eliminar el usuario <%=u.getNombre()%>?')">
                <i class="bi bi-trash"></i>
              </a>
            </td>
          </tr>
        <%
            }
          }
        %>
        </tbody>
      </table>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
