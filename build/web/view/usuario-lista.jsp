<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.*,model.Usuario"%>
<%
  List<Usuario> data = (List<Usuario>) request.getAttribute("data");
  String ctx = request.getContextPath();

  HttpSession s = request.getSession(false);
  Integer rol = (s == null) ? null : (Integer) s.getAttribute("rol");
  String destinoInicio = ctx + "/index.jsp";
  if (rol != null) {
      if (rol == 1) destinoInicio = ctx + "/view/admin/dashboard.jsp";
      else if (rol == 2) destinoInicio = ctx + "/view/residente/qr.jsp";
      else if (rol == 3) destinoInicio = ctx + "/view/guardia/control.jsp";
  }
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Mantenimiento de Usuarios</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="<%=ctx%>/assets/css/app.css" rel="stylesheet">
</head>
<body>
<div class="container py-4 d-flex justify-content-center" style="min-height:100vh;">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:1100px;">
    <div class="d-flex align-items-center mb-3">
      <div class="brand-badge me-3"><i class="bi bi-people"></i></div>
      <div>
        <h4 class="mb-0">Mantenimiento de Usuarios</h4>
        <small class="text-muted">Listado de usuarios activos</small>
      </div>
      <div class="ms-auto">
        <a href="<%=destinoInicio%>" class="btn btn-outline-secondary me-2">
          <i class="bi bi-house-door"></i> Inicio
        </a>
        <a href="<%=ctx%>/usuarios?op=new" class="btn btn-brand">
          <i class="bi bi-person-plus"></i> Nuevo usuario
        </a>
      </div>
    </div>

    <div class="table-responsive">
      <table class="table table-hover align-middle">
        <thead class="table-light">
          <tr>
            <th>DPI usuario</th>
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
          <tr>
            <td colspan="7" class="text-center text-muted py-4">No hay usuarios activos.</td>
          </tr>
        <%
          } else {
            for (Usuario u : data) {
        %>
          <tr>
            <td><%= u.getDpi() %></td>
            <td><%= u.getNombre() %></td>
            <td><%= u.getApellidos() %></td>
            <td><%= u.getCorreo() %></td>
            <td><%= u.getNumeroCasa() %></td>
            <td><span class="badge text-bg-secondary"><%= u.getRolId() %></span></td>
            <td class="text-center">
              <a href="<%=ctx%>/usuarios?op=edit&id=<%=u.getId()%>" class="btn btn-sm btn-outline-primary me-1">
                <i class="bi bi-pencil-square"></i>
              </a>
              <a href="<%=ctx%>/usuarios?op=del&id=<%=u.getId()%>"
                 onclick="return confirm('¿Eliminar a <%=u.getNombre()%>?')"
                 class="btn btn-sm btn-outline-danger">
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