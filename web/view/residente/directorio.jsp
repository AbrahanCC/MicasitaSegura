<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.*,model.Usuario"%>
<%
String ctx = request.getContextPath();
List<Usuario> lista = (List<Usuario>) request.getAttribute("lista");
String msg = (String) request.getAttribute("msg");
String error = (String) request.getAttribute("error");

String qNombres    = request.getParameter("nombres")     != null ? request.getParameter("nombres")     : "";
String qApellidos  = request.getParameter("apellidos")   != null ? request.getParameter("apellidos")   : "";
String qLote       = request.getParameter("lote")        != null ? request.getParameter("lote")        : "";
String qNumeroCasa = request.getParameter("numeroCasa")  != null ? request.getParameter("numeroCasa")  : "";

// Catálogos enviados por el Controller
List<String> lotesList = (List<String>) request.getAttribute("lotes");
List<String> casasList = (List<String>) request.getAttribute("casas");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Directorio • Mi Casita Segura</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="<%=ctx%>/assets/css/app.css" rel="stylesheet">
</head>
<body>

<%@ include file="/view/_menu.jsp" %>

<div class="container py-4 d-flex justify-content-center">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:980px;">
    <div class="d-flex align-items-center mb-4">
      <div class="brand-badge me-3"><i class="bi bi-people"></i></div>
      <div>
        <h4 class="mb-0">Directorio Residencial</h4>
        <small class="text-muted">Consulta por nombre o por número de casa</small>
      </div>
    </div>

    <!-- Muestra mensajes de error o información -->
    <% if (error != null) { %>
      <div class="alert alert-danger"><i class="bi bi-exclamation-triangle me-2"></i><%=error%></div>
    <% } %>
    <% if (msg != null) { %>
      <div class="alert alert-info"><i class="bi bi-info-circle me-2"></i><%=msg%></div>
    <% } %>

    <!-- Campos opcionales de búsqueda -->
    <!-- Botones Buscar y Limpiar -->
    <form class="row g-3 mb-4" method="get" action="<%=ctx%>/directorio">
      <div class="col-md-6">
        <label class="form-label">Nombres del residente</label>
        <!-- Restricción: solo letras y espacios -->
        <input class="form-control" type="text" name="nombres" 
               value="<%=qNombres%>" pattern="[A-Za-zÁÉÍÓÚáéíóúÑñ ]+" 
               title="Solo se permiten letras y espacios">
      </div>
      <div class="col-md-6">
        <label class="form-label">Apellidos del residente</label>
        <!-- Restricción: solo letras y espacios -->
        <input class="form-control" type="text" name="apellidos" 
               value="<%=qApellidos%>" pattern="[A-Za-zÁÉÍÓÚáéíóúÑñ ]+" 
               title="Solo se permiten letras y espacios">
      </div>

      <div class="col-md-3">
        <label class="form-label">Lote</label>
        <select class="form-select" name="lote">
          <option value="">(Sin lote)</option>
          <% if (lotesList != null) {
               for (String l : lotesList) {
                 String sel = l.equalsIgnoreCase(qLote) ? "selected" : "";
          %>
            <option value="<%=l%>" <%=sel%>><%=l%></option>
          <%   }
             } %>
        </select>
      </div>

      <div class="col-md-3">
        <label class="form-label">Número de casa</label>
        <select class="form-select" name="numeroCasa">
          <option value="">(Sin número)</option>
          <% if (casasList != null) {
               for (String c : casasList) {
                 String sel = c.equalsIgnoreCase(qNumeroCasa) ? "selected" : "";
          %>
            <option value="<%=c%>" <%=sel%>><%=c%></option>
          <%   }
             } %>
        </select>
      </div>

      <div class="col-12 d-flex gap-2">
        <button class="btn btn-brand" type="submit"><i class="bi bi-search me-1"></i>Buscar</button>
        <!-- Botón Limpiar formulario -->
        <a class="btn btn-outline-secondary" href="<%=ctx%>/directorio?op=limpiar">
          <i class="bi bi-eraser me-1"></i>Limpiar
        </a>
      </div>
    </form>

    <!-- Resultados del residente consultado -->
    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light">
          <tr>
            <th>Nombre completo del Usuario</th>
            <th>Número de casa</th>
            <th>Correo electrónico</th>
          </tr>
        </thead>
        <tbody>
        <%
          if (lista == null) {
            // Esperando acción de búsqueda
        %>
          <tr><td colspan="3" class="text-muted">Use los filtros y presione “Buscar”.</td></tr>
        <%
          } else if (lista.isEmpty()) {
            // No se encontró información
        %>
          <tr><td colspan="3">Sin resultados.</td></tr>
        <%
          } else {
            for (Usuario u : lista) {
        %>
          <tr>
            <td><%= u.getApellidos() + ", " + u.getNombre() %></td>
            <td>
              <span class="badge text-bg-secondary">
                <%= (u.getLote() != null ? u.getLote() + "-" : "") %><%= (u.getNumeroCasa() != null ? u.getNumeroCasa() : "") %>
              </span>
            </td>
            <td><%= u.getCorreo() %></td>
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
