<%-- 
    Document   : mantenimiento
    Created on : 21/10/2025, 11:04:03 PM
    Author     : abrah
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Reporte de Mantenimiento • Mi Casita Segura</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="<%=ctx%>/assets/css/app.css" rel="stylesheet">
</head>
<body>
<jsp:include page="/view/_menu.jsp"/>

<div class="container py-4 d-flex justify-content-center">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:880px;">
    <div class="d-flex align-items-center mb-4">
      <div class="brand-badge me-3"><i class="bi bi-wrench-adjustable-circle"></i></div>
      <div>
        <h4 class="mb-0">Reporte de Mantenimiento</h4>
        <small class="text-muted">Informe un problema o error detectado en el sistema</small>
      </div>
    </div>

    <c:if test="${not empty mensaje}">
      <div class="alert alert-success">${mensaje}</div>
    </c:if>
    <c:if test="${not empty error}">
      <div class="alert alert-danger">${error}</div>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/residente/mantenimiento">
      <div class="mb-3">
        <label for="tipoInconveniente" class="form-label">Tipo de inconveniente</label>
        <select id="tipoInconveniente" name="tipoInconveniente" class="form-select" required>
          <option value="">Seleccione...</option>
          <option value="Lentitud en el sistema">Lentitud en el sistema</option>
          <option value="Error al realizar una acción">Error al realizar una acción</option>
          <option value="Error al acceder a una opción">Error al acceder a una opción</option>
          <option value="Error de visualización">Error de visualización</option>
          <option value="Otros">Otros</option>
        </select>
      </div>

      <div class="mb-3">
        <label for="descripcion" class="form-label">Descripción del problema</label>
        <textarea id="descripcion" name="descripcion" class="form-control" rows="4"
                  placeholder="Describa brevemente el problema..." required></textarea>
      </div>

      <div class="d-flex gap-2">
        <button type="submit" class="btn btn-brand">
          <i class="bi bi-send me-1"></i> Enviar reporte
        </button>
        <button type="reset" class="btn btn-outline-secondary">
          <i class="bi bi-eraser me-1"></i> Limpiar
        </button>
      </div>
    </form>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>

