<%-- 
    Document   : paqueteria-lista
    Created on : 21/10/2025, 10:22:54 PM
    Author     : abrah
--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Lista de paquetería • Mi Casita Segura</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="<%=ctx%>/assets/css/app.css" rel="stylesheet">
</head>
<body>
<jsp:include page="/view/_menu.jsp" />

<%
  javax.servlet.http.HttpSession s = request.getSession(false);
  String flashOk  = (s!=null && s.getAttribute("flash_ok")  != null) ? (String) s.getAttribute("flash_ok")  : null;
  String flashErr = (s!=null && s.getAttribute("flash_err") != null) ? (String) s.getAttribute("flash_err") : null;
  if (s != null) { s.removeAttribute("flash_ok"); s.removeAttribute("flash_err"); }
%>

<div class="container py-4 d-flex justify-content-center">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:1100px;">

    <div class="d-flex align-items-center mb-4">
      <div class="brand-badge me-3"><i class="bi bi-list-ul"></i></div>
      <div>
        <h4 class="mb-0">Lista de paquetes (pendientes de entregar)</h4>
        <small class="text-muted">Filtra por guía, nombre, apellidos, casa o lote</small>
      </div>
    </div>

    <% if (flashOk != null) { %>
      <div class="alert alert-success"><%= flashOk %></div>
    <% } %>
    <% if (flashErr != null) { %>
      <div class="alert alert-danger"><%= flashErr %></div>
    <% } %>

    <form class="row g-2 mb-3" method="get" action="${pageContext.request.contextPath}/paqueteria">
      <input type="hidden" name="op" value="list"/>
      <div class="col-md-7">
        <input class="form-control" type="text" name="q" placeholder="Buscar por guía, nombre, apellidos, casa o lote"
               value="${filtro}">
      </div>
      <div class="col-md-5 d-flex gap-2">
        <button class="btn btn-outline-primary" type="submit">
          <i class="bi bi-search me-1"></i>Buscar
        </button>
        <a class="btn btn-outline-secondary" href="<%=ctx%>/paqueteria?op=list">Limpiar</a>
        <a class="btn btn-dark ms-auto" href="<%=ctx%>/paqueteria?op=new">
          <i class="bi bi-plus-lg me-1"></i>Registrar paquetería
        </a>
      </div>
    </form>

    <c:choose>
      <c:when test="${empty data}">
        <!-- RN04 -->
        <div class="alert alert-info mb-0">
          <i class="bi bi-info-circle me-1"></i>No hay paquetería pendiente de entregar.
        </div>
      </c:when>
      <c:otherwise>
        <div class="table-responsive">
          <table class="table table-hover align-middle">
            <thead>
              <tr>
                <th>#</th>
                <th>Número de guía</th>
                <th>Casa</th>
                <th>Lote</th>
                <th>Fecha recepción</th>
                <th class="text-end">Acciones</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach items="${data}" var="p" varStatus="st">
                <tr>
                  <td>${st.index + 1}</td>
                  <td><span class="fw-semibold">${p.numeroGuia}</span></td>
                  <td>${empty p.casaDestinatario ? "" : p.casaDestinatario}</td>
                  <td>${empty p.loteDestinatario ? "" : p.loteDestinatario}</td>
                  <td>${p.fechaRecepcion}</td>
                  <td class="text-end">
                    <form method="post" action="${pageContext.request.contextPath}/paqueteria" class="d-inline"
                          onsubmit="return confirmarEntrega();">
                      <input type="hidden" name="op" value="deliver">
                      <input type="hidden" name="id" value="${p.id}">
                      <button type="submit" class="btn btn-success btn-sm">
                        <i class="bi bi-check2-circle me-1"></i>Entregar
                      </button>
                    </form>
                  </td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<script>
  function confirmarEntrega(){
    return confirm('¿Está seguro de realizar la entrega de paquetes?');
  }
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>