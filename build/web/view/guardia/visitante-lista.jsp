<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.*,model.Visitante"%>
<%
String ctx = request.getContextPath();
List<Visitante> data = (List<Visitante>) request.getAttribute("data");
String msg   = (String) request.getAttribute("msg");
String error = (String) request.getAttribute("error");

String qDesde   = request.getParameter("desde")             != null ? request.getParameter("desde")             : "";
String qHasta   = request.getParameter("hasta")             != null ? request.getParameter("hasta")             : "";
String qDestino = request.getParameter("destinoNumeroCasa") != null ? request.getParameter("destinoNumeroCasa") : "";
String qDpi     = request.getParameter("dpi")               != null ? request.getParameter("dpi")               : "";
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
        <h4 class="mb-0">Visitantes</h4>
        <small class="text-muted">Filtra por fecha, destino o DPI</small>
      </div>
      <div class="ms-auto">
        <a class="btn btn-brand" href="<%=ctx%>/visitantes?op=new">
          <i class="bi bi-person-plus me-1"></i>Registrar visitante
        </a>
      </div>
    </div>

    <% if (error != null) { %>
      <div class="alert alert-danger"><i class="bi bi-exclamation-triangle me-2"></i><%=error%></div>
    <% } %>
    <% if (msg != null) { %>
      <div class="alert alert-info"><i class="bi bi-info-circle me-2"></i><%=msg%></div>
    <% } %>

    <form class="row g-3 mb-4" method="get" action="<%=ctx%>/visitantes">
      <div class="col-sm-3">
        <label class="form-label">Desde</label>
        <input class="form-control" type="date" name="desde" value="<%=qDesde%>">
      </div>
      <div class="col-sm-3">
        <label class="form-label">Hasta</label>
        <input class="form-control" type="date" name="hasta" value="<%=qHasta%>">
      </div>
      <div class="col-sm-3">
        <label class="form-label">Destino (A-12)</label>
        <input class="form-control" name="destinoNumeroCasa" value="<%=qDestino%>">
      </div>
      <div class="col-sm-3">
        <label class="form-label">DPI</label>
        <input class="form-control" name="dpi" value="<%=qDpi%>">
      </div>
      <div class="col-12 d-flex gap-2">
        <button class="btn btn-brand" type="submit"><i class="bi bi-funnel me-1"></i>Filtrar</button>
        <a class="btn btn-outline-secondary" href="<%=ctx%>/visitantes"><i class="bi bi-x-circle me-1"></i>Limpiar</a>
      </div>
    </form>

    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light">
          <tr>
            <th>Fecha/Hora</th>
            <th>Nombre</th>
            <th>DPI</th>
            <th>Motivo</th>
            <th>Destino</th>
            <th>Guardia</th>
          </tr>
        </thead>
        <tbody>
        <%
          if (data == null || data.isEmpty()) {
        %>
          <tr><td colspan="6">Sin registros.</td></tr>
        <%
          } else {
            for (Visitante v : data) {
        %>
          <tr>
            <td><%= v.getFechaHora() %></td>
            <td><%= v.getNombre() %></td>
            <td><%= v.getDpi() %></td>
            <td><%= v.getMotivo() %></td>
            <td><span class="badge text-bg-secondary"><%= v.getDestinoNumeroCasa() %></span></td>
            <td><%= v.getCreadoPorGuardiaId() %></td>
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
