<%@page contentType="text/html;charset=UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.Mensaje,model.Conversacion"%>

<%
  HttpSession s = request.getSession(false);
  Integer rol = (s == null) ? null : (Integer) s.getAttribute("rol");
  if (rol == null) { response.sendRedirect(request.getContextPath() + "/login"); return; }
  if (rol != 1 && rol != 2 && rol != 3) { response.sendError(403); return; }

  Conversacion c = (Conversacion) request.getAttribute("conv");
  List<Mensaje> mensajes = (List<Mensaje>) request.getAttribute("mensajes");
  Integer yoId = (s == null) ? null : (Integer) s.getAttribute("uid");
  if (c == null || mensajes == null || yoId == null) {
    response.sendRedirect(request.getContextPath() + "/comunicacion");
    return;
  }

  String ctx = request.getContextPath();
  String panelUrl = ctx + "/index.jsp";
  if (rol == 1) panelUrl = ctx + "/view/admin/dashboard.jsp";
  else if (rol == 2) panelUrl = ctx + "/view/guardia/control.jsp";
  else if (rol == 3) panelUrl = ctx + "/view/residente/qr.jsp";
%>

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Chat</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
  <style>
    .chat-box    { height: calc(100vh - 260px); overflow-y: auto; background:#f7f7f9; }
    .bubble-me   { background:#d1e7dd; border-radius:12px; padding:8px 12px; display:inline-block; }
    .bubble-them { background:#e2e3e5; border-radius:12px; padding:8px 12px; display:inline-block; }
  </style>
</head>
<body>
<div class="container py-3">

  <div class="d-flex justify-content-between align-items-center mb-2">
    <div class="btn-group">
      <a href="<%=ctx%>/comunicacion" class="btn btn-outline-secondary btn-sm">Volver</a>
      <a href="<%=panelUrl%>" class="btn btn-outline-primary btn-sm">Panel principal</a>
    </div>
    <div>
      <span class="badge <%= "ACTIVA".equalsIgnoreCase(c.getEstado()) ? "bg-success" : "bg-secondary" %>">
        <%= c.getEstado() %>
      </span>
      <%
        if ("ACTIVA".equalsIgnoreCase(c.getEstado())) {
      %>
      <form method="post" action="<%=ctx%>/chat" class="d-inline">
        <input type="hidden" name="idConversacion" value="<%= c.getId() %>">
        <input type="hidden" name="op" value="close">
        <button class="btn btn-outline-danger btn-sm">Cerrar</button>
      </form>
      <% } %>
    </div>
  </div>

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

  <form method="post" action="<%=ctx%>/chat">
    <input type="hidden" name="idConversacion" value="<%= c.getId() %>">
    <div class="input-group">
      <input name="contenido" maxlength="200" class="form-control"
             placeholder="Escribe un mensaje..."
             <%= "ACTIVA".equalsIgnoreCase(c.getEstado()) ? "" : "disabled" %> required>
      <button class="btn btn-primary" type="submit"
              <%= "ACTIVA".equalsIgnoreCase(c.getEstado()) ? "" : "disabled" %>>
        Enviar
      </button>
    </div>
  </form>

</div>
</body>
</html>
