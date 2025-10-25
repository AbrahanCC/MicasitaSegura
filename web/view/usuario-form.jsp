<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Gestión de Usuarios • Mi Casita Segura</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="${pageContext.request.contextPath}/assets/css/app.css" rel="stylesheet">
</head>
<body>
<jsp:include page="/view/_menu.jsp"/>

<div class="container py-4 d-flex justify-content-center">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:880px;">
    <div class="d-flex align-items-center mb-4">
      <div class="brand-badge me-3"><i class="bi bi-person-lines-fill"></i></div>
      <div>
        <h4 class="mb-0">
          <c:choose>
            <c:when test="${u.id > 0}">Editar usuario</c:when>
            <c:otherwise>Nuevo usuario</c:otherwise>
          </c:choose>
        </h4>
        <small class="text-muted">Complete los datos obligatorios</small>
      </div>
    </div>

    <!-- Mensajes flash -->
    <c:if test="${not empty sessionScope.flashOk}">
      <div class="alert alert-success">${sessionScope.flashOk}</div>
      <c:remove var="flashOk" scope="session"/>
    </c:if>
    <c:if test="${not empty sessionScope.flashError}">
      <div class="alert alert-danger">${sessionScope.flashError}</div>
      <c:remove var="flashError" scope="session"/>
    </c:if>

    <!-- Mensajes del backend -->
    <c:if test="${not empty error}">
      <div class="alert alert-danger">${error}</div>
    </c:if>
    <c:if test="${not empty success}">
      <div class="alert alert-success">${success}</div>
    </c:if>

    <!-- Formulario -->
    <form id="frmUsuario" method="post" action="${pageContext.request.contextPath}/usuarios" novalidate>
      <input type="hidden" name="id" value="${u.id}"/>

      <div class="row g-3">
        <div class="col-md-6">
          <label class="form-label">DPI del residente *</label>
          <input class="form-control" name="dpi" value="${u.dpi}" placeholder="13 dígitos">
        </div>

        <div class="col-md-6">
          <label class="form-label">Nombre *</label>
          <input class="form-control" name="nombre" value="${u.nombre}">
        </div>

        <div class="col-md-6">
          <label class="form-label">Apellidos *</label>
          <input class="form-control" name="apellidos" value="${u.apellidos}">
        </div>

        <div class="col-md-6">
          <label class="form-label">Correo *</label>
          <input class="form-control" type="text" name="correo" value="${u.correo}">
        </div>

        <div class="col-md-6">
          <label class="form-label">Usuario *</label>
          <input class="form-control" name="username" value="${u.username}">
        </div>

        <div class="col-md-6">
          <label class="form-label">
            Contraseña <c:if test="${u.id <= 0}">*</c:if>
          </label>
          <input class="form-control" type="password" name="pass"
                 placeholder="<c:choose><c:when test='${u.id > 0}'>Dejar en blanco para conservar</c:when><c:otherwise>••••••••</c:otherwise></c:choose>">
        </div>

        <div class="col-md-6">
          <label class="form-label">Rol del usuario *</label>
          <select class="form-select" name="rolId" id="rolId">
            <option value="">Seleccione…</option>
            <c:forEach var="r" items="${roles}">
              <option value="${r.id}" <c:if test="${r.id == u.rolId}">selected</c:if>>${r.nombre}</option>
            </c:forEach>
          </select>
        </div>

        <div class="col-sm-3">
          <label class="form-label">Lote</label>
          <select class="form-select" name="lote" id="lote">
            <option value="">Seleccione…</option>
            <c:forEach var="l" items="${lotes}">
              <option value="${l}" <c:if test="${l == u.lote}">selected</c:if>>Lote ${l}</option>
            </c:forEach>
          </select>
        </div>

        <div class="col-sm-3">
          <label class="form-label">Número de casa</label>
          <select class="form-select" name="numeroCasa" id="numeroCasa">
            <option value="">Seleccione…</option>
            <c:forEach var="casa" items="${casas}">
              <option value="${casa}" <c:if test="${casa == u.numeroCasa}">selected</c:if>>Casa ${casa}</option>
            </c:forEach>
          </select>
        </div>

        <div class="col-md-3 d-flex align-items-end">
          <div class="form-check">
            <input class="form-check-input" type="checkbox" name="activo" id="activo"
                   <c:if test="${u.activo}">checked</c:if>>
            <label class="form-check-label" for="activo">Activo</label>
          </div>
        </div>
      </div>

      <div class="mt-4 d-flex gap-2">
        <button class="btn btn-brand" type="submit" id="btnGuardar" disabled>
          <i class="bi bi-save me-1"></i>Guardar usuario
        </button>
        <button class="btn btn-outline-secondary" type="reset" id="btnReset">
          <i class="bi bi-eraser me-1"></i>Limpiar
        </button>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/usuarios">
          <i class="bi bi-arrow-left me-1"></i>Cancelar
        </a>
      </div>
    </form>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
(() => {
  const form   = document.getElementById('frmUsuario');
  const rolSel = document.getElementById('rolId');
  const lote   = document.getElementById('lote');
  const casa   = document.getElementById('numeroCasa');
  const btn    = document.getElementById('btnGuardar');

  const dpi    = form.querySelector('[name="dpi"]');
  const nombre = form.querySelector('[name="nombre"]');
  const ape    = form.querySelector('[name="apellidos"]');
  const correo = form.querySelector('[name="correo"]');
  const user   = form.querySelector('[name="username"]');
  const pass   = form.querySelector('[name="pass"]');

  // ¿Es registro nuevo? (si u.id <= 0)
  const IS_NEW = ${u.id <= 0};

  // Detecta guardia por ID=2 o por texto del rol
  function esGuardia() {
    const id = parseInt((rolSel.value || '').trim(), 10);
    const txt = (rolSel.options[rolSel.selectedIndex]?.text || '').toLowerCase();
    return id === 2 || /guardia|agente.*seguridad|seguridad/.test(txt);
  }

  function toggleResidencial() {
    const g = esGuardia();
    lote.disabled = g;
    casa.disabled = g;
    if (g) { lote.value = ''; casa.value = ''; }
  }

  function noVacio(el){ return (el?.value || '').trim().length > 0; }

  function todoListo() {
    const baseOk = noVacio(dpi) && noVacio(nombre) && noVacio(ape)
                && noVacio(correo) && noVacio(user) && noVacio(rolSel);
    const passOk = IS_NEW ? noVacio(pass) : true;
    const resOk  = esGuardia() ? true : (noVacio(lote) && noVacio(casa));
    return baseOk && passOk && resOk;
  }

  function actualizar() {
    toggleResidencial();
    btn.disabled = !todoListo();
  }

  // Listeners
  form.addEventListener('input', actualizar, true);
  form.addEventListener('change', actualizar, true);
  document.getElementById('btnReset').addEventListener('click', () => {
    setTimeout(actualizar, 0); // tras reset
  });

  form.addEventListener('submit', (e) => {
    actualizar();
    if (btn.disabled) { e.preventDefault(); e.stopPropagation(); }
  });

  // Estado inicial
  actualizar();
})();
</script>
</body>
</html>
