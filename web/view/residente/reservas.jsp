<%-- 
    Document   : reservas-lista
    Created on : 20/10/2025, 08:09:06 PM
    Author     : abrah
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Reserva" %>
<%
    List<Reserva> reservas = (List<Reserva>) request.getAttribute("reservas");
    String flashOk = (String) session.getAttribute("flash_ok");
    String flashErr = (String) session.getAttribute("flash_err");
    if (flashOk != null) { session.removeAttribute("flash_ok"); }
    if (flashErr != null) { session.removeAttribute("flash_err"); }
%>
<html>
<head><title>Gestionar reservas</title></head>
<body>
<h2>Gestionar reservas</h2>

<% if (flashOk != null) { %><div style="color:green;"><%= flashOk %></div><% } %>
<% if (flashErr != null) { %><div style="color:red;"><%= flashErr %></div><% } %>

<p>
  <a href="<%= request.getContextPath() %>/residente/reservas?action=nuevo">Crear reserva</a>
</p>

<table border="1" cellpadding="5" cellspacing="0">
  <thead>
    <tr>
      <th>Salón reservado</th>
      <th>Fecha Reservada</th>
      <th>Hora Inicio</th>
      <th>Hora Fin</th>
      <th>Acciones</th>
    </tr>
  </thead>
  <tbody>
  <% if (reservas != null && !reservas.isEmpty()) {
       for (Reserva r : reservas) { %>
    <tr>
      <td><%= r.getAreaNombre() %></td>
      <td><%= r.getFecha() %></td>
      <td><%= r.getHoraInicio() %></td>
      <td><%= r.getHoraFin() %></td>
      <td>
        <form method="post" action="<%= request.getContextPath() %>/residente/reservas" onsubmit="return confirm('¿Desea cancelar la reserva?');">
          <input type="hidden" name="action" value="cancelar"/>
          <input type="hidden" name="id" value="<%= r.getId() %>"/>
          <button type="submit">Cancelar Reserva</button>
        </form>
      </td>
    </tr>
  <% } } else { %>
    <tr><td colspan="5">No hay reservas.</td></tr>
  <% } %>
  </tbody>
</table>
</body>
</html>
