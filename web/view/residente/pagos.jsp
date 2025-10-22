<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.Pago,java.util.*"%>

<!DOCTYPE html>
<html>
<head>
  <title>Pagos</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body class="bg-light">

<jsp:include page="/view/_menu.jsp"/>

<div class="container my-4">
  <h3 class="mb-3">Pagos</h3>

  <%
    List<Pago> pagos = (List<Pago>) request.getAttribute("pagos");
    String flashOk = (String) session.getAttribute("flash_ok"); 
    if (flashOk != null) { session.removeAttribute("flash_ok"); }
    String flashErr = (String) session.getAttribute("flash_err"); 
    if (flashErr != null) { session.removeAttribute("flash_err"); }
  %>

  <% if (flashOk != null) { %>
    <div class="alert alert-success"><%=flashOk%></div>
  <% } %>
  <% if (flashErr != null) { %>
    <div class="alert alert-danger"><%=flashErr%></div>
  <% } %>

  <div class="card shadow-sm">
    <div class="card-body table-responsive">
      <table class="table table-sm align-middle">
        <thead class="table-light">
          <tr>
            <th>No.</th>
            <th>Tipo Pago</th>
            <th>Cantidad (Q)</th>
            <th>Fecha de pago</th>
            <th>Observaciones</th>
            <th>Estado</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
        <%
          int i = 1;
          if (pagos != null && !pagos.isEmpty()) {
            for (Pago p : pagos) {
        %>
          <tr>
            <td><%= i++ %></td>
            <td><%= p.getTipoNombre() %></td>
            <td>Q<%= String.format("%.2f", p.getTotal()) %></td>
            <td><%= p.getFechaPago() %></td>
            <td><%= (p.getObservaciones() == null) ? "" : p.getObservaciones() %></td>
            <td>
              <span class="badge bg-success"><%= p.getStatus() %></span>
            </td>
            <td>
              <a class="btn btn-outline-secondary btn-sm"
                 href="${pageContext.request.contextPath}/residente/pagos?action=recibo&id=<%=p.getId()%>">
                 <i class="bi bi-receipt-cutoff"></i> Recibo
              </a>
            </td>
          </tr>
        <% 
            }
          } else { 
        %>
          <tr>
            <td colspan="7" class="text-center text-muted">No hay pagos registrados aún.</td>
          </tr>
        <% } %>
        </tbody>
      </table>
    </div>
  </div>

  <div class="mt-3">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/residente/pagos?action=form">
  <i class="bi bi-plus-circle me-1"></i> Pagar servicio
</a>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
