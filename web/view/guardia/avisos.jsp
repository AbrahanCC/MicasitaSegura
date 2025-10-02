<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Enviar avisos | Guardia</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="${pageContext.request.contextPath}/assets/css/app.css" rel="stylesheet">
</head>
<body>

<jsp:include page="/view/_menu.jsp" />

<div class="container py-4 d-flex justify-content-center" style="min-height:100vh;">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:900px;">

    <div class="text-center mb-4">
      <div class="brand-badge mx-auto mb-3"><i class="bi bi-megaphone"></i></div>
      <h4 class="mb-1">Enviar Aviso</h4>
      <small class="text-muted">Redacta y envía una notificación a residentes</small>
    </div>

    <c:if test="${not empty ok}">
      <div class="alert alert-success"><i class="bi bi-check-circle me-1"></i>${ok}</div>
    </c:if>
    <c:if test="${not empty error}">
      <div class="alert alert-danger"><i class="bi bi-exclamation-triangle me-1"></i>${error}</div>
    </c:if>

    <form action="<%=ctx%>/guardia/avisos" method="post" class="needs-validation" novalidate>
      <div class="row g-3">
        <div class="col-md-6">
          <label class="form-label">Enviar a</label>
          <select class="form-select" name="destinoTipo" id="destinoTipo" required>
            <option value="ALL">Todos los residentes</option>
            <option value="UNO">Un correo específico</option>
          </select>
          <div class="invalid-feedback">Selecciona el tipo de destinatario.</div>
        </div>

        <div class="col-md-6" id="grupoEmail" style="display:none;">
          <label for="email" class="form-label">Correo destino</label>
          <select class="form-select" id="email" name="email">
            <option value="">Seleccione un correo…</option>
            <c:forEach var="c" items="${correos}">
              <option value="${c}">${c}</option>
            </c:forEach>
          </select>
          <div class="form-text">Catálogo de residentes activos.</div>
          <div class="invalid-feedback">Selecciona un correo.</div>
        </div>
      </div>

      <div class="row g-3 mt-1">
        <div class="col-12">
          <label for="asunto" class="form-label">Asunto</label>
          <input type="text" class="form-control" id="asunto" name="asunto" required>
          <div class="invalid-feedback">El asunto es obligatorio.</div>
        </div>
      </div>

      <div class="row g-3 mt-1">
        <div class="col-12">
          <label for="mensaje" class="form-label">Mensaje</label>
          <textarea class="form-control" id="mensaje" name="mensaje" rows="5" required></textarea>
          <div class="invalid-feedback">El mensaje es obligatorio.</div>
        </div>
      </div>

      <div class="alert alert-info mt-3">
        <i class="bi bi-info-circle me-1"></i>
        El envío se procesa en segundo plano. Si algún correo falla, la aplicación no se detiene.
      </div>

      <div class="d-flex gap-2 mt-2">
        <button type="submit" class="btn btn-primary">
          <i class="bi bi-send-check me-1"></i>Enviar aviso
        </button>
        <a class="btn btn-secondary" href="<%=ctx%>/guardia/panel">
          <i class="bi bi-arrow-left me-1"></i>Volver
        </a>
      </div>
    </form>

  </div>
</div>

<script>
  (function () {
    const selTipo = document.getElementById('destinoTipo');
    const grp     = document.getElementById('grupoEmail');
    const emailEl = document.getElementById('email');

    function toggleEmail() {
      const uno = selTipo.value === 'UNO';
      grp.style.display = uno ? 'block' : 'none';
      emailEl.required = uno;
      if (!uno) { emailEl.value = ''; emailEl.classList.remove('is-invalid'); }
    }
    selTipo.addEventListener('change', toggleEmail);
    toggleEmail();

    const forms = document.querySelectorAll('.needs-validation');
    Array.prototype.slice.call(forms).forEach(function (form) {
      form.addEventListener('submit', function (event) {
        if (!form.checkValidity()) {
          event.preventDefault();
          event.stopPropagation();
        }
        form.classList.add('was-validated');
      }, false);
    });
  })();
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
