<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.*,model.Visitante"%>
<%
  String ctx = request.getContextPath();
  List<Visitante> data = (List<Visitante>) request.getAttribute("data");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Visitantes • Mi Casita Segura</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="<%=ctx%>/assets/css/app.css" rel="stylesheet">
</head>
<body>
<%@ include file="/view/_menu.jsp" %>

<div class="container py-4 d-flex justify-content-center">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:1080px;">

    <div class="d-flex align-items-center mb-4">
      <div class="brand-badge me-3"><i class="bi bi-people"></i></div>
      <div>
        <h4 class="mb-0">Registros de visitantes</h4>
        <small class="text-muted">Listado general</small>
      </div>
      <div class="ms-auto">
        <a class="btn btn-brand" href="<%=ctx%>/visitantes?op=new">
          <i class="bi bi-person-plus me-1"></i>Registrar visitante
        </a>
      </div>
    </div>

    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light">
          <tr>
            <th>Fecha registro</th>
            <th>Nombre visitante</th>
            <th>DPI</th>
            <th>Tipo de visita</th>
            <th>Destino</th>
            <th>Estado</th>
            <th class="text-center">Acciones</th>
          </tr>
        </thead>
        <tbody>
        <%
          if (data == null || data.isEmpty()) {
        %>
          <tr><td colspan="7" class="text-center text-muted">No hay registros.</td></tr>
        <%
          } else {
            for (Visitante v : data) {
        %>
          <tr>
            <td><%= v.getCreadoEn() != null ? v.getCreadoEn() : "" %></td>
            <td><%= v.getNombre() %></td>
            <td><%= v.getDpi() %></td>
            <td><%= v.getVisitType() != null ? v.getVisitType() : "-" %></td>
            <td><%= v.getDestinoNumeroCasa() %></td>
            <td>
              <span class="badge 
                <%= "activo".equals(v.getEstado()) ? "text-bg-success" :
                    "emitido".equals(v.getEstado()) ? "text-bg-info" :
                    "cancelado".equals(v.getEstado()) ? "text-bg-danger" :
                    "text-bg-secondary" %>">
                <%= v.getEstado() %>
              </span>
            </td>
            <td class="text-center">
              <a href="<%=ctx%>/api/qr?token=<%=v.getToken()%>" class="btn btn-sm btn-outline-primary me-1" title="Descargar QR">
                <i class="bi bi-qr-code"></i>
              </a>
              <a href="<%=ctx%>/visitantes?op=cancel&id=<%=v.getId()%>" 
                 class="btn btn-sm btn-outline-danger" 
                 onclick="return confirm('¿Desea cancelar la visita de <%=v.getNombre()%>?')">
                <i class="bi bi-x-circle"></i>
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
