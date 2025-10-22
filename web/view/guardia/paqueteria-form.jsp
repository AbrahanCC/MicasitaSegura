<%-- 
    Document   : paqueteria-form
    Created on : 21/10/2025, 10:22:29 PM
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
  <title>Registrar paquetería • Mi Casita Segura</title>
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
  <div class="glass p-4 p-sm-5 w-100" style="max-width:880px;">
    <div class="d-flex align-items-center mb-4">
      <div class="brand-badge me-3"><i class="bi bi-box-seam"></i></div>
      <div>
        <h4 class="mb-0">Registrar paquetería</h4>
        <small class="text-muted">Ingresa la guía y selecciona al residente destinatario</small>
      </div>
    </div>

    <% if (flashOk != null) { %>
      <div class="alert alert-success"><%= flashOk %></div>
    <% } %>
    <% if (flashErr != null) { %>
      <div class="alert alert-danger"><%= flashErr %></div>
    <% } %>

    <!-- Espera: request.setAttribute("residentes", List<model.Usuario>) en el controller op=new -->
    <form id="frmPaqueteria" class="row g-3" method="post" action="${pageContext.request.contextPath}/paqueteria">
      <input type="hidden" name="op" value="save"/>

      <div class="col-md-4">
        <label class="form-label">Número de guía</label>
        <input class="form-control" name="numero_guia" maxlength="64" required>
      </div>

      <div class="col-md-8">
        <label class="form-label">Destinatario (residente activo)</label>
        <select id="selResidente" class="form-select" name="destinatario_id" required>
          <option value="">Seleccione…</option>
          <c:forEach items="${residentes}" var="u">
            <option value="${u.id}"
                    data-casa="${u.numeroCasa}"
                    data-lote="${u.lote}">
              ${u.apellidos}, ${u.nombre}
              <c:if test="${not empty u.numeroCasa}">
                (Casa ${u.numeroCasa}<c:if test='${not empty u.lote}'> • Lote ${u.lote}</c:if>)
              </c:if>
            </option>
          </c:forEach>
        </select>
      </div>

      <div class="col-sm-3">
        <label class="form-label">Casa</label>
        <input id="txtCasa" class="form-control" disabled>
      </div>
      <div class="col-sm-3">
        <label class="form-label">Lote</label>
        <input id="txtLote" class="form-control" disabled>
      </div>

      <div class="col-sm-6">
        <label class="form-label">Observaciones (opcional)</label>
        <input class="form-control" name="observaciones" maxlength="255" placeholder="Mensajería, color caja, etc.">
      </div>

      <div class="col-12 d-flex gap-2 mt-2">
        <button class="btn btn-brand" type="submit">
          <i class="bi bi-save me-1"></i>Guardar
        </button>
        <button class="btn btn-outline-secondary" type="reset" id="btnLimpiar">
          <i class="bi bi-eraser me-1"></i>Limpiar
        </button>
        <a class="btn btn-outline-dark ms-auto" href="<%=ctx%>/paqueteria?op=list">
          <i class="bi bi-list-ul me-1"></i>Ver lista
        </a>
      </div>
    </form>
  </div>
</div>

<script>
(function(){
  const sel = document.getElementById('selResidente');
  const casa = document.getElementById('txtCasa');
  const lote = document.getElementById('txtLote');
  const limpiar = document.getElementById('btnLimpiar');

  function pintar(){
    const opt = sel.options[sel.selectedIndex];
    casa.value = opt && opt.dataset.casa ? opt.dataset.casa : '';
    lote.value = opt && opt.dataset.lote ? opt.dataset.lote : '';
  }
  sel && sel.addEventListener('change', pintar);
  limpiar && limpiar.addEventListener('click', () => setTimeout(()=>{ sel.selectedIndex=0; pintar(); },0));
})();
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>