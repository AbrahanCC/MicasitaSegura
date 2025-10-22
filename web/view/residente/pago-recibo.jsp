<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="model.Pago, model.MetodoPago, model.Usuario, java.time.format.DateTimeFormatter, java.time.format.TextStyle, java.time.*, java.util.Locale"%>

<%
  // Datos desde el controlador
  Pago pago = (Pago) request.getAttribute("pago");
  MetodoPago metodo = (MetodoPago) request.getAttribute("metodoPago"); // puede venir null
  Usuario usr = (Usuario) session.getAttribute("usuario");

  if (pago == null) {
%>
  <div class="container mt-4">
    <div class="alert alert-danger">No se encontró la información del pago.</div>
    <a class="btn btn-secondary" href="<%=request.getContextPath()%>/residente/pagos?action=listar">Volver</a>
  </div>
  <% return; } %>

<!DOCTYPE html>
<html>
<head>
  <title>Recibo de pago</title>
  <meta charset="utf-8"/>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css" rel="stylesheet">
  <style>
    .invoice-card { max-width: 920px; margin: auto; }
    .invoice-title { letter-spacing: .3px; }
    .kv { font-size: .95rem; color: #6c757d; }
    .kv strong { color: #212529; }
    .totals-row th { background:#f8f9fa; }
    @media print {
      .no-print { display: none !important; }
      body { background: #fff; }
      .card, .invoice-card { box-shadow: none !important; border: none !important; }
    }
  </style>
</head>
<body class="bg-light">

<jsp:include page="/view/_menu.jsp"/>

<%
  Locale es = new Locale("es", "GT");

  String fechaPagoTxt = (pago.getFechaPago() != null)
      ? pago.getFechaPago().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
      : "";

  String mesAPagarTxt = "";
  if (pago.getMesAPagar() != null) {
    YearMonth ym = YearMonth.from(pago.getMesAPagar());
    mesAPagarTxt = ym.getMonth().getDisplayName(TextStyle.FULL, es) + " " + ym.getYear();
  }

  String nombreResidente = (usr != null)
      ? ( (usr.getNombre()==null?"":usr.getNombre()) + " " + (usr.getApellidos()==null?"":usr.getApellidos()) ).trim()
      : "Residente";

  String casa = (usr != null && usr.getNumeroCasa()!=null) ? usr.getNumeroCasa() : "";
  String lote = (usr != null && usr.getLote()!=null) ? usr.getLote() : "";

  String tarjetaTxt = (metodo != null)
      ? ( (metodo.getMarca()==null?"CARD":metodo.getMarca())
          + " **** **** **** "
          + (metodo.getUltimos4()==null?"0000":metodo.getUltimos4())
          + " (exp " + metodo.getMesExpiracion() + "/" + metodo.getAnioExpiracion() + ")" )
      : "Tarjeta (no registrada)";
%>

<div class="container my-4 invoice-card">
  <!-- Encabezado + acciones -->
  <div class="d-flex align-items-center justify-content-between mb-3 no-print">
    <h3 class="mb-0 invoice-title">Recibo de pago</h3>
    <div class="d-flex gap-2">
      <a class="btn btn-outline-secondary btn-sm"
         href="<%=request.getContextPath()%>/residente/pagos?action=listar">
        <i class="bi bi-arrow-left"></i> Volver
      </a>
      <button class="btn btn-primary btn-sm" onclick="window.print()">
        <i class="bi bi-printer"></i> Imprimir
      </button>
    </div>
  </div>

  <div class="card shadow-sm">
    <div class="card-body p-4">

      <!-- Header del recibo -->
      <div class="row mb-4">
        <div class="col">
          <h4 class="mb-0">MiCasitaSegura</h4>
          <div class="text-muted">Comprobante de pago</div>
        </div>
        <div class="col text-end">
          <span class="badge bg-success px-3 py-2">PAGADO</span>
          <div class="small text-muted mt-2">Recibo N.º</div>
          <div class="fs-5 fw-semibold">#<%=pago.getId()%></div>
        </div>
      </div>

      <!-- Datos del residente y detalles -->
      <div class="row g-3 mb-4">
        <div class="col-md-6">
          <div class="kv">Residente</div>
          <div class="fw-semibold"><%=nombreResidente%></div>
          <div class="text-muted">
            <% if (!casa.isEmpty()) { %> Casa: <%=casa%><% } %>
            <% if (!lote.isEmpty()) { %> &nbsp;&middot;&nbsp; Lote: <%=lote%><% } %>
            <% if (usr != null && usr.getCorreo()!=null && !usr.getCorreo().isEmpty()) { %>
              <br/>Correo: <%=usr.getCorreo()%>
            <% } %>
          </div>
        </div>
        <div class="col-md-6 text-md-end">
          <div class="kv">Fecha de pago</div>
          <div class="fw-semibold"><%=fechaPagoTxt%></div>
          <div class="kv mt-2">Estado</div>
          <div class="fw-semibold"><%=pago.getStatus()%></div>
        </div>
      </div>

      <!-- Tabla de conceptos -->
      <div class="table-responsive">
        <table class="table table-sm align-middle">
          <thead class="table-light">
            <tr>
              <th style="width:50%">Concepto</th>
              <th class="text-center" style="width:25%">Mes/Período</th>
              <th class="text-end" style="width:25%">Monto (Q)</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><strong><%=pago.getTipoNombre()%></strong></td>
              <td class="text-center text-capitalize"><%=mesAPagarTxt%></td>
              <td class="text-end"><%=String.format("Q%.2f", pago.getMontoBase())%></td>
            </tr>
            <tr>
              <td class="text-muted">Recargo por mora</td>
              <td class="text-center">—</td>
              <td class="text-end"><%=String.format("Q%.2f", pago.getRecargo())%></td>
            </tr>
            <tr class="totals-row">
              <th colspan="2" class="text-end">Total</th>
              <th class="text-end fs-5"><%=String.format("Q%.2f", pago.getTotal())%></th>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Método de pago y observaciones -->
      <div class="row g-3">
        <div class="col-md-6">
          <div class="kv">Método de pago</div>
          <div class="fw-semibold"><%=tarjetaTxt%></div>
        </div>
        <div class="col-md-6">
          <div class="kv">Observaciones</div>
          <div><%= (pago.getObservaciones()==null || pago.getObservaciones().isEmpty()) ? "—" : pago.getObservaciones() %></div>
        </div>
      </div>

      <hr class="my-4"/>
      <div class="d-flex justify-content-between">
        <small class="text-muted">Este comprobante es válido como constancia interna de pago.</small>
        <small class="text-muted">Emitido por MiCasitaSegura</small>
      </div>

    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
