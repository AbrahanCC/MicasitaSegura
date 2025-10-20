<%-- CU6/chat.jsp --%>
<%@page contentType="text/html;charset=UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.Mensaje,model.Conversacion"%>

<%
  // ---- Control de acceso ----
  HttpSession s = request.getSession(false);
  Integer rol = (s == null) ? null : (Integer) s.getAttribute("rol");
  if (rol == null) { response.sendRedirect(request.getContextPath() + "/login"); return; }
  // Para pruebas: permiten los 3 roles (1,2,3)
  if (rol != 1 && rol != 2 && rol != 3) { response.sendError(403); return; }

  // Atributos puestos por el servlet ChatServlet
  Conversacion c = (Conversacion) request.getAttribute("conv");
  List<Mensaje> mensajes = (List<Mensaje>) request.getAttribute("mensajes");

  Integer yoId = (Integer) s.getAttribute("uid");
  if (c == null || mensajes == null || yoId == null) {
    response.sendRedirect(request.getContextPath() + "/comunicacion");
    return;
  }
%>

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Chat</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
  <style>
    .chat-box    { height: calc(100vh - 220px); overflow-y: auto; background:#f7f7f9; }
    .bubble-me   { background:#d1e7dd; border-radius:12px; padding:8px 12px; display:inline-block; }
    .bubble-them { background:#e2e3e5; border-radius:12px; padding:8px 12px; display:inline-block; }
  </style>
</head>
<body>
<div class="container py-3">

  <div class="chat-box p-3 mb-3 border rounded">
  <%
    for (Mensaje m : mensajes) {
      int emisor = m.getIdEmisor();
      boolean mine = (yoId != null && emisor == yoId.intValue());
      String alignClass  = mine ? "justify-content-end" : "justify-content-start";
      String bubbleClass = mine ? "bubble-me"            : "bubble-them";
  %>
    <div class="d-flex <%= alignClass %> mb-2">
      <div class="<%= bubbleClass %>"><%= m.getContenido() %></div>
    </div>
  <% } %>
  </div>

  <% String error = (String) request.getAttribute("error");
     if (error != null) { %>
     <div class="alert alert-warning"><%= error %></div>
  <% } %>

  <form method="post" action="${pageContext.request.contextPath}/chat">
    <input type="hidden" name="idConversacion" value="<%= c.getId() %>">
    <input type="hidden" name="correoDest" value="">
    <div class="input-group">
      <input name="contenido" maxlength="200" class="form-control" placeholder="Escribe un mensaje..." required>
      <button class="btn btn-primary" type="submit">Enviar</button>
    </div>
  </form>

</div>
</body>
</html>
