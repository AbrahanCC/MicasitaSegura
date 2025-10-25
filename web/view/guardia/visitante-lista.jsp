<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="java.util.*,model.Visitante"%>
<%
  String ctx = request.getContextPath();
  List<Visitante> data = (List<Visitante>) request.getAttribute("data");
%>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <title>Registros de visitantes • Mi Casita Segura</title>
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <link href="<%=ctx%>/assets/css/app.css" rel="stylesheet">
</head>
<body>
<jsp:include page="/view/_menu.jsp" />

<div class="container py-4 d-flex justify-content-center">
  <div class="glass p-4 p-sm-5 w-100" style="max-width:1080px;">

    <div class="d-flex align-items-center mb-4">
      <div class="brand-badge me-3"><i class="bi bi-people"></i></div>
      <div>
        <h4 class="mb-0">Registros de visitantes</h4>
        <small class="text-muted">Listado general</small>
      </div>
      <div class="ms-auto">
        <a class="btn btn-brand" href="<%=ctx%>/visitantes?op=new">
          <i class="bi bi-person-plus me-1"></i>Registrar visitante
        </a>
      </div>
    </div>

    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light">
          <tr>
            <th>Fecha registro</th>
            <th>Nombre del visitante</th>
            <th>DPI del visitante</th>
            <th>Correo visitante</th> 
            <th>Tipo de visita</th>
            <th>Destino</th>
            <th>Estado</th>
            <th class="text-center">Acciones</th>
          </tr>
        </thead>
        <tbody>
        <%
          if (data == null || data.isEmpty()) {
        %>
          <tr><td colspan="8" class="text-center text-muted">No hay registros.</td></tr>
        <%
          } else {
            for (Visitante v : data) {
              String lote = v.getLote() != null ? v.getLote() : "";
              String casa = v.getCasa() != null ? v.getCasa() : "";
        %>
          <tr>
            <td><%= v.getCreadoEn() != null ? v.getCreadoEn() : "" %></td>
            <td><%= v.getNombre() %></td>
            <td><%= v.getDpi() != null ? v.getDpi() : "" %></td>
            <td><%= v.getCorreo() != null ? v.getCorreo() : "" %></td> <!-- NUEVO -->
            <td><%= v.getTipoVisita() != null ? v.getTipoVisita() : "-" %></td>
            <td><%= (lote.isEmpty() && casa.isEmpty()) ? "-" : (lote + "-" + casa) %></td>
            <td>
              <span class="badge 
                <%= "activo".equalsIgnoreCase(v.getEstado()) ? "text-bg-success" :
                    "emitido".equalsIgnoreCase(v.getEstado()) ? "text-bg-info" :
                    "cancelado".equalsIgnoreCase(v.getEstado()) ? "text-bg-danger" :
                    "consumido".equalsIgnoreCase(v.getEstado()) ? "text-bg-secondary" :
                    "text-bg-light" %>">
                <%= v.getEstado() %>
              </span>
            </td>
            <td class="text-center">
              <button type="button"
                      class="btn btn-sm btn-outline-primary me-1 btn-qr"
                      title="Ver/descargar QR"
                      data-token="<%=v.getToken()%>"
                      data-bs-toggle="modal"
                      data-bs-target="#qrModal">
                <i class="bi bi-qr-code"></i>
              </button>
              <a href="<%=ctx%>/visitantes?op=cancel&id=<%=v.getId()%>"
                 class="btn btn-sm btn-outline-danger"
                 onclick="return confirm('¿Desea cancelar la visita de <%=v.getNombre()%>?');">
                <i class="bi bi-x-circle"></i>
              </a>
            </td>
          </tr>
        <%
            }
          }
        %>
        </tbody>
      </table>
    </div>
  </div>
</div>

<div class="modal fade" id="qrModal" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header">
        <h6 class="modal-title mb-0"><i class="bi bi-qr-code me-2"></i>QR del visitante</h6>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Cerrar"></button>
      </div>
      <div class="modal-body text-center">
        <img id="qrImg" src="" alt="QR" class="img-fluid" style="max-width: 320px;">
      </div>
      <div class="modal-footer">
        <a id="qrDownload" href="#" class="btn btn-brand" download="pase-qr.png">
          <i class="bi bi-download me-1"></i>Descargar
        </a>
        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">
          <i class="bi bi-x-circle me-1"></i>Cerrar
        </button>
      </div>
    </div>
  </div>
</div>

<script>
  const CTX = '<%=ctx%>';
  const qrImg = document.getElementById('qrImg');
  const qrDownload = document.getElementById('qrDownload');

  document.querySelectorAll('.btn-qr').forEach(btn => {
    btn.addEventListener('click', () => {
      const token = btn.getAttribute('data-token');
      const url = CTX + '/api/qr?token=' + encodeURIComponent(token);
      qrImg.src = url;
      qrDownload.href = url;
    });
  });
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
