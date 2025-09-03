<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Registrar visitante • Mi Casita Segura</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="<%=ctx%>/assets/css/app.css" rel="stylesheet">
</head>
<body>

<jsp:include page="/view/_menu.jsp" />

<div class="container py-4 d-flex justify-content-center">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:880px;">

    <div class="d-flex align-items-center mb-4">
      <div class="brand-badge me-3"><i class="bi bi-person-plus"></i></div>
      <div>
        <h4 class="mb-0">Registrar visitante</h4>
        <small class="text-muted">Completa los datos y guarda</small>
      </div>
    </div>

    <!-- Mensajes -->
    <c:if test="${not empty msg}">
      <div class="alert alert-success alert-dismissible fade show" role="alert">
        ${msg}
        <c:if test="${not empty token}">
          <div class="mt-1"><strong>Token:</strong> ${token}</div>
        </c:if>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
      </div>
    </c:if>
    <c:if test="${not empty error}">
      <div class="alert alert-danger alert-dismissible fade show" role="alert">
        ${error}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
      </div>
    </c:if>

    <form class="row g-3" method="post" action="${pageContext.request.contextPath}/visitantes">
      <!-- Nombre -->
      <div class="col-md-6">
        <label class="form-label">Nombre</label>
        <input class="form-control" name="nombre" required>
      </div>

      <!-- Motivo -->
      <div class="col-md-6">
        <label class="form-label">Motivo</label>
        <input class="form-control" name="motivo" placeholder="Ej. entrega, visita, servicio" required>
      </div>

      <!-- Lote / Número de casa -->
      <div class="col-sm-3">
        <label class="form-label">Lote</label>
        <select class="form-select" name="lote" required>
          <option value="">(Sin lote)</option>
          <%
            String[] lotes = "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z".split(",");
            for (String l : lotes) { %>
              <option value="<%=l%>"><%=l%></option>
          <% } %>
        </select>
      </div>
      <div class="col-sm-3">
        <label class="form-label">Número de casa</label>
        <input class="form-control" type="number" name="numeroCasa" min="1" max="999" required>
      </div>

      <!-- Email (opcional) -->
      <div class="col-sm-6">
        <label class="form-label">Email del visitante (opcional)</label>
        <input class="form-control" type="email" name="email" placeholder="alguien@correo.com">
        <div class="form-text">Si lo completas, el QR se enviará por correo.</div>
      </div>

      <!-- Info de reglas -->
      <div class="col-12">
        <div class="alert alert-info d-flex align-items-start">
          <div>
            <strong>Regla del pase:</strong> se genera automáticamente, es de <strong>1 solo uso</strong> y
            <strong>vence en 10 minutos</strong>. Al guardar se abrirá la cámara para validarlo.
          </div>
        </div>
      </div>

      <div class="col-12 d-flex gap-2 mt-2">
        <button class="btn btn-brand" type="submit"><i class="bi bi-save me-1"></i>Guardar</button>
        <a class="btn btn-outline-secondary" href="<%=ctx%>/visitantes"><i class="bi bi-arrow-left me-1"></i>Volver</a>
      </div>
    </form>

  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
