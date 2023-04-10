package presentacion;

import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import negocio.VentaControl;

public class FrmVenta extends javax.swing.JInternalFrame {

    private final VentaControl CONTROL;
    private String accion;
    private String nombreAnt;

    private int totalPorPagina = 10;
    private int numPagina = 1;
    private boolean primeraCarga = true;
    private int totalRegistro;

    public DefaultTableModel modeloDetalles;
    public JFrame contenedor;

    public FrmVenta(JFrame frmP) {

        initComponents();
        this.contenedor = frmP;
        this.CONTROL = new VentaControl();
        this.paginar();
        this.listar("", false);
        this.primeraCarga = false;
        tabGeneral.setEnabledAt(1, false);
        this.accion = "guardar";
        this.crearDetalle();
    }

    private void paginar() {
        int toalPaginas;

        this.totalRegistro = this.CONTROL.total();
        this.totalPorPagina = Integer.parseInt((String) cboTotalPagina.getSelectedItem());
        toalPaginas = (int) (Math.ceil((double) this.totalRegistro / this.totalPorPagina));
        if (toalPaginas == 0) {
            toalPaginas = 1;
        }
        cboNumPagina.removeAllItems();
        for (int i = 1; i <= toalPaginas; i++) {
            cboNumPagina.addItem(Integer.toString(i));
        }
        cboNumPagina.setSelectedIndex(0);
    }

    private void listar(String texto, boolean paginar) {
        this.totalPorPagina = Integer.parseInt((String) cboTotalPagina.getSelectedItem());
        if ((String) cboNumPagina.getSelectedItem() != null) {
            this.numPagina = Integer.parseInt((String) cboNumPagina.getSelectedItem());
        }

        if (paginar == true) {
            tablaListado.setModel(this.CONTROL.listar(texto, this.totalPorPagina, this.numPagina));
        } else {
            tablaListado.setModel(this.CONTROL.listar(texto, this.totalPorPagina, 1));
        }

        TableRowSorter orden = new TableRowSorter(tablaListado.getModel());
        tablaListado.setRowSorter(orden);
        this.ocultarColumnas();
        lblTotalRegistros.setText("Mostrando " + this.CONTROL.totalMostrados()
                + " de un total de " + this.CONTROL.total() + " registros");
    }

    private void ocultarColumnas() {
        tablaListado.getColumnModel().getColumn(1).setMaxWidth(0);
        tablaListado.getColumnModel().getColumn(1).setMinWidth(0);
        tablaListado.getTableHeader().getColumnModel().getColumn(1).setMaxWidth(0);
        tablaListado.getTableHeader().getColumnModel().getColumn(1).setMinWidth(0);

        tablaListado.getColumnModel().getColumn(3).setMaxWidth(0);
        tablaListado.getColumnModel().getColumn(3).setMinWidth(0);
        tablaListado.getTableHeader().getColumnModel().getColumn(3).setMaxWidth(0);
        tablaListado.getTableHeader().getColumnModel().getColumn(3).setMinWidth(0);
    }

    private void crearDetalle() {
        modeloDetalles = new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int fila, int columna) {
                if (columna == 4) {
                    return columna == 4;
                }
                if (columna == 5) {
                    return columna == 5;
                }
                if (columna == 6) {
                    return columna == 6;
                }
                return columna == 4;
            }

            @Override
            public Object getValueAt(int fila, int columna) {
                if (columna == 7) {
                    Double cantidad;
                    try {
                        cantidad = Double.parseDouble((String) getValueAt(fila, 4));
                    } catch (Exception e) {
                        cantidad = 1.0;
                    }
                    Double precio = Double.parseDouble((String) getValueAt(fila, 5));
                    Double descuento = Double.parseDouble((String) getValueAt(fila, 6));
                    if (cantidad != null && precio != null && descuento != null) {
                        return String.format("%.2f", (cantidad * precio) - descuento);
                    } else {
                        return 0;
                    }
                }

                return super.getValueAt(fila, columna);
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column);
                try {
                    int cantidad = Integer.parseInt((String) getValueAt(row, 4));
                    int stock = Integer.parseInt((String) getValueAt(row, 3));
                    if (cantidad > stock) {
                        super.setValueAt(stock, row, 4);
                        mensajeError("La cantidad que desea vender no puede superar al Stock, como maximo puede:" + stock);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                calcularTotales();
                fireTableDataChanged();
            }

        };

        modeloDetalles.setColumnIdentifiers(new Object[]{"ID", "CODIGO", "ARTICULO", "STOCK", "CANTIDAD", "PRECIO", "DESCUENTO", "SUBTOTAL"});
        tablaDetalle.setModel(modeloDetalles);
    }

    public void agregarDetalles(String id, String codigo, String nombre, String stock, String precio, String descuento) {
        String idT;
        boolean existe = false;
        for (int i = 0; i < this.modeloDetalles.getRowCount(); i++) {
            idT = String.valueOf(this.modeloDetalles.getValueAt(i, 0));
            if (idT.equals(id)) {
                existe = true;
            }
        }
        if (existe) {
            this.mensajeError("El articulo ya ha sido agregado");
        } else {
            this.modeloDetalles.addRow(new Object[]{id, codigo, nombre, stock, "1", precio, descuento, precio});
            this.calcularTotales();
        }
    }

    private void calcularTotales() {
        double total = 0;
        double subTotal = 0;
        int items = modeloDetalles.getRowCount();
        if (items == 0) {
            total = 0;
        } else {
            for (int i = 0; i < items; i++) {
                total = total + Double.parseDouble(String.valueOf(modeloDetalles.getValueAt(i, 7)));
            }
        }

        subTotal = total / (1 + Double.parseDouble(txtImpuesto.getText()));

        txtTotal.setText(String.format("%.2f", total));
        txtSubTotal.setText(String.format("%.2f", subTotal));
        txtTotalImpuesto.setText(String.format("%.2f", total - subTotal));
    }

    private void obtenerNumero() {
        String tipoComprobante = (String) cboTipoComprobante.getSelectedItem();
        String serieComprobante = this.CONTROL.ultimoSerie(tipoComprobante);
        String numComprobante = this.CONTROL.ultimoNumero(tipoComprobante, serieComprobante);
        txtSerieComprobante.setText(serieComprobante);
        if (numComprobante.equals("")) {
            txtNumComprobante.setText("");
        } else {
            int num;
            num = Integer.parseInt(numComprobante) + 1;
            txtNumComprobante.setText(Integer.toString(num));
        }
    }

    private void limpiar() {
        txtNombreCliente.setText("");
        txtIdCliente.setText("");
        txtSerieComprobante.setText("");
        txtNumComprobante.setText("");
        txtImpuesto.setText("0.18");

        this.accion = "guardar";
        txtTotal.setText("0.00");
        txtSubTotal.setText("0.00");
        txtTotalImpuesto.setText("0.00");
        this.crearDetalle();
        btnGuardar.setVisible(true);
    }

    private void mensajeError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Sistema", JOptionPane.ERROR_MESSAGE);
    }

    private void mensajeOk(String mensaje) {

        JOptionPane.showMessageDialog(this, mensaje, "Sistema", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabGeneral = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablaListado = new javax.swing.JTable();
        lblTotalRegistros = new javax.swing.JLabel();
        btnNuevo = new javax.swing.JButton();
        btnDesactivar = new javax.swing.JButton();
        cboNumPagina = new javax.swing.JComboBox<>();
        cboTotalPagina = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        btnVerVenta = new javax.swing.JButton();
        btnReporteComprobante = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        btnGuardar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        txtIdCliente = new javax.swing.JTextField();
        txtNombreCliente = new javax.swing.JTextField();
        btnSelecionarCliente = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtImpuesto = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        cboTipoComprobante = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        txtSerieComprobante = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtNumComprobante = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtCodigoBarra = new javax.swing.JTextField();
        btnVerArituculos = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablaDetalle = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txtSubTotal = new javax.swing.JTextField();
        txtTotalImpuesto = new javax.swing.JTextField();
        txtTotal = new javax.swing.JTextField();
        btnQuitar = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Ventas");

        tabGeneral.setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setText("Nombre: ");

        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        tablaListado.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tablaListado);

        lblTotalRegistros.setText("Registros");

        btnNuevo.setText("Nuevo");
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });

        btnDesactivar.setText("Anular");
        btnDesactivar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDesactivarActionPerformed(evt);
            }
        });

        cboNumPagina.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboNumPaginaActionPerformed(evt);
            }
        });

        cboTotalPagina.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "10", "20", "50", "100", "200", "500" }));
        cboTotalPagina.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTotalPaginaActionPerformed(evt);
            }
        });

        jLabel10.setText("# Pagina");

        jLabel11.setText("Total Registro por Pagina");

        btnVerVenta.setText("Ver Venta");
        btnVerVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerVentaActionPerformed(evt);
            }
        });

        btnReporteComprobante.setText("Reporte");
        btnReporteComprobante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReporteComprobanteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addGap(18, 18, 18)
                                .addComponent(cboNumPagina, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnDesactivar, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(192, 192, 192)
                                .addComponent(jLabel11)
                                .addGap(44, 44, 44)
                                .addComponent(cboTotalPagina, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(243, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblTotalRegistros, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(35, 35, 35))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 324, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnBuscar)
                                .addGap(18, 18, 18)
                                .addComponent(btnNuevo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnReporteComprobante, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnVerVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnBuscar)
                    .addComponent(btnNuevo)
                    .addComponent(btnVerVenta)
                    .addComponent(btnReporteComprobante))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboNumPagina, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboTotalPagina, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11))
                .addGap(67, 67, 67)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalRegistros)
                    .addComponent(btnDesactivar))
                .addGap(50, 50, 50))
        );

        tabGeneral.addTab("Listado", jPanel1);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setText("Cliente(*)");
        jPanel2.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(26, 15, -1, -1));

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });
        jPanel2.add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 550, -1, -1));

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });
        jPanel2.add(btnCancelar, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 550, -1, -1));

        jLabel4.setText("(*) Indica que es un campo obligatorio");
        jPanel2.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 520, -1, -1));
        jPanel2.add(txtIdCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 12, 57, -1));
        jPanel2.add(txtNombreCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(175, 12, 190, -1));

        btnSelecionarCliente.setText("...");
        btnSelecionarCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelecionarClienteActionPerformed(evt);
            }
        });
        jPanel2.add(btnSelecionarCliente, new org.netbeans.lib.awtextra.AbsoluteConstraints(383, 11, -1, -1));

        jLabel3.setText("Impuesto(*)");
        jPanel2.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(499, 15, -1, -1));

        txtImpuesto.setText("0.18");
        jPanel2.add(txtImpuesto, new org.netbeans.lib.awtextra.AbsoluteConstraints(576, 12, 41, -1));

        jLabel5.setText("Tipo Comprobante(*)");
        jPanel2.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(26, 56, -1, -1));

        cboTipoComprobante.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "BOLETA", "FACTURA", "TICKET", "GUIA" }));
        cboTipoComprobante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboTipoComprobanteActionPerformed(evt);
            }
        });
        jPanel2.add(cboTipoComprobante, new org.netbeans.lib.awtextra.AbsoluteConstraints(146, 52, 182, -1));

        jLabel6.setText("Serie Comprobante");
        jPanel2.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(346, 56, -1, -1));

        txtSerieComprobante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSerieComprobanteActionPerformed(evt);
            }
        });
        jPanel2.add(txtSerieComprobante, new org.netbeans.lib.awtextra.AbsoluteConstraints(456, 53, 134, -1));

        jLabel7.setText("Numero Comprobante(*)");
        jPanel2.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(608, 56, -1, -1));
        jPanel2.add(txtNumComprobante, new org.netbeans.lib.awtextra.AbsoluteConstraints(745, 53, 139, -1));

        jLabel8.setText("Arituculo");
        jPanel2.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(26, 96, -1, -1));

        txtCodigoBarra.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCodigoBarraKeyReleased(evt);
            }
        });
        jPanel2.add(txtCodigoBarra, new org.netbeans.lib.awtextra.AbsoluteConstraints(86, 93, 258, -1));

        btnVerArituculos.setText("Ver");
        btnVerArituculos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerArituculosActionPerformed(evt);
            }
        });
        jPanel2.add(btnVerArituculos, new org.netbeans.lib.awtextra.AbsoluteConstraints(374, 92, -1, -1));

        tablaDetalle.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(tablaDetalle);

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(26, 133, 858, 308));

        jLabel9.setText("SubTotal");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 460, -1, -1));

        jLabel12.setText("Total Impuesto");
        jPanel2.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 500, -1, -1));

        jLabel13.setText("Total");
        jPanel2.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 540, -1, -1));

        txtSubTotal.setEditable(false);
        jPanel2.add(txtSubTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 460, 94, -1));

        txtTotalImpuesto.setEditable(false);
        jPanel2.add(txtTotalImpuesto, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 500, 94, -1));

        txtTotal.setEditable(false);
        jPanel2.add(txtTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(790, 540, 94, -1));

        btnQuitar.setText("Quitar");
        btnQuitar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnQuitarActionPerformed(evt);
            }
        });
        jPanel2.add(btnQuitar, new org.netbeans.lib.awtextra.AbsoluteConstraints(800, 100, -1, -1));

        tabGeneral.addTab("Mantenimiento", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabGeneral)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabGeneral)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cboTotalPaginaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTotalPaginaActionPerformed
        this.paginar();
    }//GEN-LAST:event_cboTotalPaginaActionPerformed

    private void cboNumPaginaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboNumPaginaActionPerformed
        if (this.primeraCarga == false) {
            this.listar("", true);
        }
    }//GEN-LAST:event_cboNumPaginaActionPerformed

    private void btnDesactivarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDesactivarActionPerformed
        if (tablaListado.getSelectedRowCount() == 1) {
            String id = String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(), 0));
            String comprobante = String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(), 5));
            String serie = String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(), 6));
            String numero = String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(), 7));

            if (JOptionPane.showConfirmDialog(this, "Desea anular el registro " + comprobante + " " + serie + "-" + numero + " ?", "Anular", JOptionPane.YES_NO_OPTION) == 0) {
                String resp = this.CONTROL.Anular(Integer.parseInt(id));
                if (resp.equals("OK")) {
                    this.mensajeOk("Registro Anulado");
                    this.listar("", false);
                } else {
                    this.mensajeError(resp);
                }
            }
        } else {
            this.mensajeError("Seleccione el registro que desee desactivar");
        }
    }//GEN-LAST:event_btnDesactivarActionPerformed

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNuevoActionPerformed
        tabGeneral.setEnabledAt(1, true);
        tabGeneral.setEnabledAt(0, false);
        tabGeneral.setSelectedIndex(1);
        this.accion = "guardar";
        btnGuardar.setText("Guardar");
        this.obtenerNumero();
    }//GEN-LAST:event_btnNuevoActionPerformed

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBuscarActionPerformed
        this.listar(txtBuscar.getText(), false);
    }//GEN-LAST:event_btnBuscarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        tabGeneral.setEnabledAt(0, true);
        tabGeneral.setEnabledAt(1, false);
        tabGeneral.setSelectedIndex(0);
        this.limpiar();
    }//GEN-LAST:event_btnCancelarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed

        if (txtIdCliente.getText().length() == 0) {
            JOptionPane.showMessageDialog(this, "Debe selecionar un proveedor", "Sistema", JOptionPane.WARNING_MESSAGE);
            btnSelecionarCliente.requestFocus();
            return;
        }
        if (txtSerieComprobante.getText().length() > 7) {
            JOptionPane.showMessageDialog(this, "La serie no debe ser mayor a 7 caracteres", "Sistema", JOptionPane.WARNING_MESSAGE);
            txtSerieComprobante.requestFocus();
            return;
        }
        if (txtNumComprobante.getText().length() == 0) {
            JOptionPane.showMessageDialog(this, "Debe ingresar un Comprobante no mayor a 10 caracteres", "Sistema", JOptionPane.WARNING_MESSAGE);
            txtNumComprobante.requestFocus();
            return;
        }
        if (modeloDetalles.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Debe agregar minimo un articulo", "Sistema", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String resp = "";
        resp = this.CONTROL.insertar(Integer.parseInt(txtIdCliente.getText()), (String) cboTipoComprobante.getSelectedItem(), txtSerieComprobante.getText(), txtNumComprobante.getText(), Double.parseDouble(txtImpuesto.getText()), Double.parseDouble(txtTotal.getText()), modeloDetalles);
        if (resp.equals("OK")) {
            this.mensajeOk("Registrado correctamente");
            this.limpiar();
            this.listar("", false);
        } else {
            this.mensajeError(resp);
        }
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void txtSerieComprobanteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSerieComprobanteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSerieComprobanteActionPerformed

    private void btnSelecionarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelecionarClienteActionPerformed
        FrmSelecionarClienteVenta frm = new FrmSelecionarClienteVenta(contenedor, this, true);
        frm.toFront();
    }//GEN-LAST:event_btnSelecionarClienteActionPerformed

    private void btnVerArituculosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerArituculosActionPerformed
        FrmSelecionarArticuloVenta frm = new FrmSelecionarArticuloVenta(contenedor, this, true);
        frm.toFront();
    }//GEN-LAST:event_btnVerArituculosActionPerformed

    private void txtCodigoBarraKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCodigoBarraKeyReleased
        if (txtCodigoBarra.getText().length() > 0) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                entidades.Articulo art;
                art = this.CONTROL.obtenerAticuloCodigoVenta(txtCodigoBarra.getText());
                if (art == null) {
                    this.mensajeError("No existe el articulo con ese codigo");
                } else {
                    this.agregarDetalles(Integer.toString(art.getId()), art.getCodigo(), art.getNombre(), Integer.toString(art.getStock()), Double.toString(art.getPrecioVenta()), "0");
                }
            }
        } else {
            this.mensajeError("Ingrese el codigo a buscar");
        }
    }//GEN-LAST:event_txtCodigoBarraKeyReleased

    private void btnQuitarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQuitarActionPerformed
        if (tablaDetalle.getSelectedRowCount() == 1) {
            this.modeloDetalles.removeRow(tablaDetalle.getSelectedRow());
            this.calcularTotales();
        } else {
            this.mensajeError("SELECIONE EL DETALLE A QUITAR");
        }
    }//GEN-LAST:event_btnQuitarActionPerformed

    private void btnVerVentaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerVentaActionPerformed
        if (tablaListado.getSelectedRowCount() == 1) {
            String id = String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(), 0));
            String idProveedor = String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(), 3));
            String nombreProveedor = String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(), 4));
            String tipoComprobante = String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(), 5));
            String serie = String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(), 6));
            String numero = String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(), 7));
            String impuesto = String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(), 9));

            txtIdCliente.setText(idProveedor);
            txtNombreCliente.setText(nombreProveedor);
            cboTipoComprobante.setSelectedItem(tipoComprobante);
            txtSerieComprobante.setText(serie);
            txtNumComprobante.setText(numero);
            txtImpuesto.setText(impuesto);

            this.modeloDetalles = CONTROL.listarDetalle(Integer.parseInt(id));
            tablaDetalle.setModel(modeloDetalles);
            this.calcularTotales();
            tabGeneral.setEnabledAt(1, true);
            tabGeneral.setEnabledAt(0, false);
            tabGeneral.setSelectedIndex(1);
            btnGuardar.setVisible(false);
        } else {
            this.mensajeError("SELECIONE EL INGRESO A MOSTRAR");
        }
    }//GEN-LAST:event_btnVerVentaActionPerformed

    private void cboTipoComprobanteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboTipoComprobanteActionPerformed
        this.obtenerNumero();
    }//GEN-LAST:event_cboTipoComprobanteActionPerformed

    private void btnReporteComprobanteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReporteComprobanteActionPerformed
        if (tablaListado.getSelectedRowCount() == 1) {
            String id = String.valueOf(tablaListado.getValueAt(tablaListado.getSelectedRow(), 0));
            this.CONTROL.reporteComprobante(id);
        } else {
            this.mensajeError("SELECIONE LA VENTA A MOSTRAR EL REPORTE");
        }
    }//GEN-LAST:event_btnReporteComprobanteActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnDesactivar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnQuitar;
    private javax.swing.JButton btnReporteComprobante;
    private javax.swing.JButton btnSelecionarCliente;
    private javax.swing.JButton btnVerArituculos;
    private javax.swing.JButton btnVerVenta;
    private javax.swing.JComboBox<String> cboNumPagina;
    private javax.swing.JComboBox<String> cboTipoComprobante;
    private javax.swing.JComboBox<String> cboTotalPagina;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblTotalRegistros;
    private javax.swing.JTabbedPane tabGeneral;
    private javax.swing.JTable tablaDetalle;
    private javax.swing.JTable tablaListado;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCodigoBarra;
    public javax.swing.JTextField txtIdCliente;
    private javax.swing.JTextField txtImpuesto;
    public javax.swing.JTextField txtNombreCliente;
    private javax.swing.JTextField txtNumComprobante;
    private javax.swing.JTextField txtSerieComprobante;
    private javax.swing.JTextField txtSubTotal;
    private javax.swing.JTextField txtTotal;
    private javax.swing.JTextField txtTotalImpuesto;
    // End of variables declaration//GEN-END:variables
}
