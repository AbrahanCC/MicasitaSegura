<%-- 
    Document   : reserva-form
    Created on : 20/10/2025, 08:09:28 PM
    Author     : abrah
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.AreaComun" %>
<%@ page import="model.Usuario" %>
<%
    List<AreaComun> areas = (List<AreaComun>) request.getAttribute("areas");
    Usuario usuario = (Usuario) session.getAttribute("usuario");
%>
<html>
<head><title>Registrar reserva</title></head>
<body>
<h2>Registro de reservas</h2>

<form method="post" action="<%= request.getContextPath() %>/residente/reservas">
  <input type="hidden" name="action" value="crear"/>

  <div>
    <label>Salón para reservar *</label>
    <select name="area_id" required>
      <option value="">-- Seleccionar --</option>
      <% if (areas != null) { for (AreaComun a : areas) { %>
        <option value="<%= a.getId() %>"><%= a.getNombre() %></option>
      <% } } %>
    </select>
  </div>

  <div>
    <label>Persona que reserva</label>
    <input type="text" value="<%= (usuario != null ? usuario.getNombre() : "") %>" readonly/>
  </div>

  <div>
    <label>Fecha *</label>
    <input type="date" name="fecha" required/>
  </div>

  <div>
    <label>Hora Inicio *</label>
    <input type="time" name="hora_inicio" required/>
  </div>

  <div>
    <label>Hora Fin *</label>
    <input type="time" name="hora_fin" required/>
  </div>

  <p>
    <button type="submit">Registrar Reserva</button>
    <a href="<%= request.getContextPath() %>/residente/reservas">Cancelar</a>
  </p>
</form>

</body>
</html>
