<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.*,model.Usuario"%>
<%
String ctx = request.getContextPath();
List<Usuario> lista = (List<Usuario>) request.getAttribute("lista");
String msg = (String) request.getAttribute("msg");
String error = (String) request.getAttribute("error");

// Parámetros de búsqueda
String qNombres    = request.getParameter("nombres")     != null ? request.getParameter("nombres")     : "";
String qApellidos  = request.getParameter("apellidos")   != null ? request.getParameter("apellidos")   : "";
String qLote       = request.getParameter("lote")        != null ? request.getParameter("lote")        : "";
String qNumeroCasa = request.getParameter("numeroCasa")  != null ? request.getParameter("numeroCasa")  : "";

// Catálogos desde el Controller
List<String> lotesList = (List<String>) request.getAttribute("lotes");
List<String> casasList = (List<String>) request.getAttribute("casas");

// Fallback por si llegan nulos
if (lotesList == null || casasList == null) {
    lotesList = (List<String>) session.getAttribute("lotes");
    casasList = (List<String>) session.getAttribute("casas");
}
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

  <script>
    function validarDir(e){
      const nombres   = document.getElementById('nombres').value.trim();
      const apellidos = document.getElementById('apellidos').value.trim();
      const lote      = document.getElementById('lote').value.trim();
      const numero    = document.getElementById('numeroCasa').value.trim();
      const alfa = /^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9 .-]{0,60}$/;

      if (nombres && !alfa.test(nombres)){
        showMsg('Solo caracteres alfanuméricos en Nombres.', 'warning');
        e.preventDefault(); return false;
      }
      if (apellidos && !alfa.test(apellidos)){
        showMsg('Solo caracteres alfanuméricos en Apellidos.', 'warning');
        e.preventDefault(); return false;
      }
      const parcial = (lote && !numero) || (!lote && numero);
      if (parcial){
        showMsg('Debe seleccionar lote y número de casa, o ninguno.', 'danger');
        e.preventDefault(); return false;
      }
      return true;
    }

    function showMsg(text, type){
      const box = document.getElementById('msgFront');
      if (!text){ box.className=''; box.innerHTML=''; return; }
      const cls = (type==='danger'?'alert-danger': type==='warning'?'alert-warning':'alert-info');
      box.className = 'alert '+cls;
      box.innerHTML = '<i class="bi bi-info-circle me-2"></i>'+text;
    }
  </script>
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

    <% if (error != null) { %>
      <div class="alert alert-danger"><i class="bi bi-exclamation-triangle me-2"></i><%=error%></div>
    <% } %>
    <% if (msg != null) { %>
      <div class="alert alert-info"><i class="bi bi-info-circle me-2"></i><%=msg%></div>
    <% } %>

    <div id="msgFront"></div>

    <form class="row g-3 mb-4" method="get" action="<%=ctx%>/directorio" onsubmit="return validarDir(event);">
      <div class="col-md-6">
        <label class="form-label">Nombres del residente</label>
        <input id="nombres" class="form-control" type="text" name="nombres"
               value="<%=qNombres%>" maxlength="60" autocomplete="off">
      </div>

      <div class="col-md-6">
        <label class="form-label">Apellidos del residente</label>
        <input id="apellidos" class="form-control" type="text" name="apellidos"
               value="<%=qApellidos%>" maxlength="60" autocomplete="off">
      </div>

      <div class="col-md-3">
        <label class="form-label">Lote</label>
        <select id="lote" class="form-select" name="lote">
          <option value="">(Sin lote)</option>
          <% if (lotesList != null) for (String l : lotesList) { 
               String sel = l.equalsIgnoreCase(qLote) ? "selected" : ""; %>
            <option value="<%=l%>" <%=sel%>><%=l%></option>
          <% } %>
        </select>
      </div>

      <div class="col-md-3">
        <label class="form-label">Número de casa</label>
        <select id="numeroCasa" class="form-select" name="numeroCasa">
          <option value="">(Sin número)</option>
          <% if (casasList != null) for (String c : casasList) { 
               String sel = c.equalsIgnoreCase(qNumeroCasa) ? "selected" : ""; %>
            <option value="<%=c%>" <%=sel%>><%=c%></option>
          <% } %>
        </select>
      </div>

      <div class="col-12 d-flex gap-2">
        <button class="btn btn-brand" type="submit">
          <i class="bi bi-search me-1"></i>Buscar
        </button>
        <a class="btn btn-outline-secondary" href="<%=ctx%>/directorio?op=limpiar">
          <i class="bi bi-eraser me-1"></i>Limpiar
        </a>
      </div>
    </form>

    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light">
          <tr>
            <th>Nombre completo</th>
            <th>Número de casa</th>
            <th>Correo</th>
          </tr>
        </thead>
        <tbody>
        <%
          if (lista == null) {
        %>
          <tr><td colspan="3" class="text-muted">Use los filtros y presione “Buscar”.</td></tr>
        <%
          } else if (lista.isEmpty()) {
        %>
          <tr><td colspan="3">No se encontró ningún usuario con los datos ingresados.</td></tr>
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
