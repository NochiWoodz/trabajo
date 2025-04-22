import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger; // Para IDs de transacción
import java.util.stream.Collectors;

public class Exp3_S6_Santiago_Vivanco { 

    // --- Definición del Teatro ---
    // Mapa de Asientos: Clave = ID Asiento (ej "A1"), Valor = Estado ("Disponible", "Reservado", "Vendido")
    private final Map<String, String> mapaAsientos;
    // Precios por sección 
    private final Map<Character, Integer> preciosPorSeccion;
    //Filas por sección y Asientos por fila
    private final Map<Character, int[]> layoutTeatro; // num_filas, asientos_por_fila

    // --- Clase Interna para Transacciones ---
    static class Transaccion {
        private static final AtomicInteger contadorId = new AtomicInteger(1);
        final int idTransaccion;
        final List<String> asientos; // Lista de IDs de asiento (ej "A1", "A2")
        String status; // "Reservada", "Vendida"
        final String tipoCliente; // Aplicado a toda la transacción
        final int precioTotalCalculado;
        

        public Transaccion(List<String> asientos, String status, String tipoCliente, int precioTotal) {
            this.idTransaccion = contadorId.getAndIncrement();
            this.asientos = new ArrayList<>(asientos); // Copiar lista
            this.status = status;
            this.tipoCliente = tipoCliente;
            this.precioTotalCalculado = precioTotal;
        }

        // Getters
        public int getIdTransaccion() { return idTransaccion; }
        public List<String> getAsientos() { return asientos; }
        public String getStatus() { return status; }
        public String getTipoCliente() { return tipoCliente; }
        public int getPrecioTotalCalculado() { return precioTotalCalculado; }

        // solo para status si se convierte reserva a venta
        public void setStatus(String status) { this.status = status; }

        @Override
        public String toString() {
            return "ID: " + idTransaccion + ", Estado: " + status + ", Cliente: " + tipoCliente +
                ", Asientos: " + String.join(", ", asientos) + 
                ", Precio Total: $" + precioTotalCalculado;
        }
    }

    // --- Variables Globales y de Instancia ---
    private final Scanner sc;
    private final List<Transaccion> listaTransacciones; // Almacena Reservas y Ventas

    // Contadores globales (estáticos)
    static int asientosVendidosGlobal = 0;
    static int ingresosTotalesGlobal = 0;

    // Descuentos (estáticos)
    final static double DESCUENTO_ESTUDIANTE = 0.10; // 10% según nuevo requisito
    final static double DESCUENTO_TERCERA_EDAD = 0.15; // 15% según nuevo requisito

    // Constructor
    public Exp3_S6_Santiago_Vivanco() {
        sc = new Scanner(System.in);
        listaTransacciones = new ArrayList<>();
        preciosPorSeccion = new HashMap<>();
        layoutTeatro = new HashMap<>();
        mapaAsientos = new HashMap<>();
        inicializarTeatro(); // precios e inicializa asientos
    }

    // --- Inicialización ---
    private void inicializarTeatro() {
        // Definir A=VIP, B=Platea, C=General
        // filas 1, 2, 3.
        layoutTeatro.put('A', new int[]{3, 5}); // 3 filas, 5 asientos -> A1-1 .. A3-5
        layoutTeatro.put('B', new int[]{5, 8}); // 5 filas, 8 asientos -> B1-1 .. B5-8
        layoutTeatro.put('C', new int[]{8, 10});// 8 filas, 10 asientos -> C1-1 .. C8-10

        // Definir Precios por Sección
        preciosPorSeccion.put('A', 50000); // VIP
        preciosPorSeccion.put('B', 30000); // Platea
        preciosPorSeccion.put('C', 15000); // General

        // Crear e inicializar todos los asientos como "Disponible"
        for (Map.Entry<Character, int[]> entry : layoutTeatro.entrySet()) {
            char seccion = entry.getKey();
            int filas = entry.getValue()[0];
            int asientosPorFila = entry.getValue()[1];
            for (int f = 1; f <= filas; f++) {
                for (int a = 1; a <= asientosPorFila; a++) {
                    String idAsiento = seccion + "" + f + "-" + a; // Ej: A1-1, B3-5, C8-10
                    mapaAsientos.put(idAsiento, "Disponible");
                }
            }
        }
        System.out.println("Teatro inicializado con " + mapaAsientos.size() + " asientos.");
    }

    // --- Método Principal ---
    public static void main(String[] args) {
        Exp3_S6_Santiago_Vivanco teatro = new Exp3_S6_Santiago_Vivanco();
        teatro.iniciarSistema();//breackpoint
        teatro.cerrarScanner();
    }

    // --- Lógica Principal del Sistema ---
    public void iniciarSistema() {
        System.out.println("Bienvenido al Sistema de Reservas y Ventas del Teatro");
        boolean seguirSistema = true;
        while (seguirSistema) {
            mostrarMenuPrincipal();//breakpoint
            int opcion = leerOpcionNumerica(1, 5); // 5 opciones 
            switch (opcion) {
                case 1 -> reservarEntradas();
                case 2 -> comprarEntradas();
                case 3 -> modificarVenta();
                case 4 -> imprimirBoleta();
                case 5 -> seguirSistema = false; // Salir
            }
            if (seguirSistema) {
                 pausaParaContinuar(); // Pausa antes de volver al menú
            }
        }
        mostrarResumenSalida();
    }

    // --- Métodos del Menú y Acciones ---

    private void mostrarMenuPrincipal() {
        limpiarPantalla();
        System.out.println("------------------------------------");
        System.out.println("      MENU PRINCIPAL TEATRO");
        System.out.println("------------------------------------");
        System.out.println("1. Reservar Entradas");
        System.out.println("2. Comprar Entradas / Convertir Reserva");
        System.out.println("3. Modificar Venta / Cancelar Reserva");
        System.out.println("4. Imprimir Boleta (por ID)");
        System.out.println("5. Salir");
        System.out.println("------------------------------------");
    }

    // ---  Manejo de Entradas ---

    private void mostrarMapaAsientos() {
        limpiarPantalla();
        System.out.println("--- Mapa de Asientos --- (D=Disponible, R=Reservado, V=Vendido)");//breakpoint
        // Itera sobre el layout definido para mostrar en orden
        layoutTeatro.forEach((seccion, dims) -> {
            System.out.println("\n--- Sección " + seccion + " (Precio Base: $" + preciosPorSeccion.get(seccion) + ") ---");
            int filas = dims[0];
            int asientosPorFila = dims[1];
            for (int f = 1; f <= filas; f++) {
                System.out.print("Fila " + f + ": ");
                for (int a = 1; a <= asientosPorFila; a++) {
                    String idAsiento = seccion + "" + f + "-" + a;
                    String estado = mapaAsientos.getOrDefault(idAsiento, "Error"); 
                    char siglaEstado = switch (estado) {
                        case "Reservado" -> 'R';
                        case "Vendido" -> 'V';
                        default -> 'D'; // Disponible
                    };
                    // Mostrar ID corto (ej A1) y estado
                    System.out.printf("[%s%d:%c] ", seccion, a, siglaEstado); // Muestra ej [A5:D]
                }
                System.out.println(); // Salto de línea al final de la fila
            }
        });
        System.out.println("------------------------");
    }

    private List<String> seleccionarAsientosDisponibles() {
        List<String> asientosSeleccionados = new ArrayList<>();
        boolean seguirSeleccionando = true;
        while (seguirSeleccionando) {// breakpoint
            System.out.print("Ingrese ID de asiento (ej. A1-5) o 'fin' para terminar: ");
            String input = sc.nextLine().toUpperCase().trim();
            if (input.equals("FIN")) {
                seguirSeleccionando = false;
                continue;
            }

            // Validar formato y existencia
            if (!mapaAsientos.containsKey(input)) {
                System.out.println("Error: ID de asiento inválido o no existe.");
                continue;
            }

            // Validar disponibilidad
            String estadoActual = mapaAsientos.get(input);
            if (!estadoActual.equals("Disponible")) {
                System.out.println("Error: Asiento '" + input + "' no está disponible (Estado: " + estadoActual + ").");
                continue;
            }

             // Validar si ya lo seleccionó en esta tanda
            if (asientosSeleccionados.contains(input)) {
                System.out.println("Info: Ya ha seleccionado el asiento '" + input + "'.");
                continue;
            }


            System.out.println("Asiento '" + input + "' agregado.");
            asientosSeleccionados.add(input);
        }
        return asientosSeleccionados;
    }

    private int calcularPrecioAsiento(String idAsiento) {
        if (idAsiento == null || idAsiento.isEmpty()) return 0;
        char seccion = idAsiento.charAt(0); // La primera letra es la sección
        return preciosPorSeccion.getOrDefault(seccion, 0);
    }

    private int calcularPrecioTotal(List<String> asientos, String tipoCliente) {
        int precioBruto = 0;// breakpoint
        for (String idAsiento : asientos) {
            precioBruto += calcularPrecioAsiento(idAsiento);
        }

        double descuento = switch (tipoCliente) {
            case "Estudiante" -> DESCUENTO_ESTUDIANTE;
            case "Tercera Edad" -> DESCUENTO_TERCERA_EDAD;
            default -> 0.0; 
        };

        return (int) (precioBruto * (1.0 - descuento));
    }

    private String seleccionarTipoCliente() {
        System.out.println("\nSeleccione tipo de cliente:");
        System.out.println("1. Estudiante (" + (int)(DESCUENTO_ESTUDIANTE * 100) + "% dto.)");
        System.out.println("2. Tercera Edad (" + (int)(DESCUENTO_TERCERA_EDAD * 100) + "% dto.)");
        System.out.println("3. General");
        int opcion = leerOpcionNumerica(1, 3);
        return switch (opcion) {
            case 1 -> "Estudiante";
            case 2 -> "Tercera Edad";
            default -> "General";
        };
    }


    private void reservarEntradas() {
        mostrarMapaAsientos();// breakpoint
        System.out.println("\n--- Nueva Reserva ---");
        List<String> asientos = seleccionarAsientosDisponibles();

        if (asientos.isEmpty()) {
            System.out.println("No se seleccionaron asientos. Reserva cancelada.");
            return;
        }

        String tipoCliente = seleccionarTipoCliente();
        int precioTotal = calcularPrecioTotal(asientos, tipoCliente);// breakpoint

        // Cambiar estado en el mapa y crear transacción
        for (String idAsiento : asientos) {
            mapaAsientos.put(idAsiento, "Reservado");
        }
        Transaccion nuevaReserva = new Transaccion(asientos, "Reservada", tipoCliente, precioTotal);
        listaTransacciones.add(nuevaReserva);

        System.out.println("\n--- Reserva Exitosa ---");
        System.out.println(nuevaReserva);
        System.out.println("-----------------------");
    }

    private void comprarEntradas() {
        limpiarPantalla();
        System.out.println("--- Comprar Entradas ---");
        System.out.println("1. Comprar asientos nuevos");
        System.out.println("2. Convertir una reserva existente a compra");
        System.out.println("3. Volver al menú");
        int opcion = leerOpcionNumerica(1, 3);

        switch (opcion) {
            case 1: // Compra directa
                comprarAsientosNuevos();
                break;
            case 2: // Convertir reserva
                convertirReservaACompra();
                break;
            case 3: // Volver
                System.out.println("Volviendo al menú principal...");
                break;
        }
    }

    private void comprarAsientosNuevos() {
        mostrarMapaAsientos();
        System.out.println("\n--- Nueva Compra Directa ---");
        List<String> asientos = seleccionarAsientosDisponibles();

        if (asientos.isEmpty()) {
            System.out.println("No se seleccionaron asientos. Compra cancelada.");
            return;
        }

        String tipoCliente = seleccionarTipoCliente();
        int precioTotal = calcularPrecioTotal(asientos, tipoCliente);

        // Cambiar estado a "Vendido" y crear transacción
        for (String idAsiento : asientos) {
            mapaAsientos.put(idAsiento, "Vendido");// breakpoint
        }
        Transaccion nuevaVenta = new Transaccion(asientos, "Vendida", tipoCliente, precioTotal);
        listaTransacciones.add(nuevaVenta);

        // Actualizar globales
        asientosVendidosGlobal += asientos.size();
        ingresosTotalesGlobal += precioTotal;

        System.out.println("\n--- Compra Exitosa ---");
        System.out.println(nuevaVenta);
        System.out.println("----------------------");
    }

    private void convertirReservaACompra() {
        System.out.print("Ingrese el ID de la reserva a convertir: ");
        int idReserva = leerEntero(); // Asume que leerEntero maneja la entrada y el newline

        Optional<Transaccion> transaccionOpt = buscarTransaccionPorId(idReserva);

        if (transaccionOpt.isPresent()) {
            Transaccion tx = transaccionOpt.get();
            if (tx.getStatus().equals("Reservada")) {

                // --- Inicio: Proceso de conversión con posible reversión ---
                boolean conversionExitosa = true; // Asumimos éxito inicial
                // Lista para rastrear los asientos que SÍ cambiamos a Vendido en este intento
                List<String> asientosCambiadosEnEsteIntento = new ArrayList<>();

                // cambiar el estado de todos los asientos en el mapa
                for (String idAsiento : tx.getAsientos()) {
                    // Verifica el estado actual del asiento en el mapa principal
                    if (mapaAsientos.containsKey(idAsiento) && mapaAsientos.get(idAsiento).equals("Reservado")) {
                        
                        mapaAsientos.put(idAsiento, "Vendido");// breakpoint
                        
                        asientosCambiadosEnEsteIntento.add(idAsiento);
                    } else {
                        // ¡ERROR! El asiento no estaba reservado como se esperaba.
                        System.out.println("\n!!!!!!!!!!!!!!!!!!!! ERROR DE CONVERSIÓN !!!!!!!!!!!!!!!!!!!!");
                        System.out.println(" Error Crítico: El asiento [" + idAsiento + "] (parte de la reserva ID " + idReserva +") no está en estado 'Reservado'.");
                        System.out.println(" Estado actual en mapa: '" + mapaAsientos.getOrDefault(idAsiento, "NO ENCONTRADO") + "'.");
                        System.out.println(" La conversión no puede completarse.");
                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                        // *** INICIO: Lógica de Reversión ***
                        
                        System.out.println("\n--- Iniciando Reversión Automática ---");
                        if (!asientosCambiadosEnEsteIntento.isEmpty()) {
                            for (String asientoARevertir : asientosCambiadosEnEsteIntento) {
                                mapaAsientos.put(asientoARevertir, "Reservado"); // breakpoint
                                System.out.println("   - Asiento [" + asientoARevertir + "] devuelto a estado 'Reservado'.");
                            }
                            System.out.println("--- Reversión Completada ---");
                        } else {
                            System.out.println("   (No se habían modificado asientos en este intento antes del error).");
                            System.out.println("--- Reversión No Requerida ---");
                        }
                         // *** FIN: Lógica de Reversión ***

                        conversionExitosa = false; // Marcar que la conversión falló
                        break; // Salir del bucle for, no procesar más asientos
                    }
                } // Fin del bucle for

                // 2. Finalizar según el éxito o fracaso del bucle anterior
                if (conversionExitosa) {

                    // a) Cambiar estado de la Transacción
                    tx.setStatus("Vendida");

                    // b) Actualizar Contadores Globales
                    asientosVendidosGlobal += tx.getAsientos().size();
                    ingresosTotalesGlobal += tx.getPrecioTotalCalculado();

                    // c) Mensaje de Éxito
                    System.out.println("\n--- ¡Reserva Convertida a Compra Exitosamente! ---");
                    System.out.println(" Estado de Transacción ID " + tx.getIdTransaccion() + " actualizado a 'Vendida'.");
                    System.out.println(" Asientos marcados como 'Vendido' en el mapa.");
                    System.out.println(tx); // Mostrar detalles finales
                    System.out.println("------------------------------------------------");

                } else {
                    // Si hubo un error y se revirtieron los cambios:
                    System.out.println("\n--- Conversión de Reserva Fallida ---");
                    System.out.println(" La Reserva ID " + idReserva + " NO PUDO ser convertida a compra debido a una inconsistencia.");
                    System.out.println(" El estado de la reserva y los asientos asociados permanecen como 'Reservado'.");
                    System.out.println(" Revise el mapa de asientos y el estado de la reserva.");
                    System.out.println("-------------------------------------");
                }
                 // --- Fin: Proceso de conversión ---

            } else { // Si la transacción no estaba 'Reservada'
                System.out.println("Error: La transacción ID " + idReserva + " no es una reserva activa (Estado: " + tx.getStatus() + ").");
            }
        } else { // Si no se encontró la transacción
            System.out.println("Error: No se encontró ninguna transacción con ID " + idReserva + ".");
        }
    }


    private void modificarVenta() {
        limpiarPantalla();
        System.out.println("--- Modificar / Cancelar ---");
        System.out.println("1. Cancelar una Reserva");
        System.out.println("2. Eliminar una Compra (Devolución)");
        System.out.println("3. Volver al menú");
        int opcion = leerOpcionNumerica(1, 3);

        if (opcion == 3) return; // Volver

        System.out.print("Ingrese el ID de la transacción: ");
        int idTransaccion = leerEntero();
        Optional<Transaccion> transaccionOpt = buscarTransaccionPorId(idTransaccion);

        if (transaccionOpt.isEmpty()) {
            System.out.println("Error: No se encontró la transacción con ID " + idTransaccion + ".");
            return;
        }

        Transaccion tx = transaccionOpt.get();

        if (opcion == 1) { // Cancelar Reserva
            if (!tx.getStatus().equals("Reservada")) {
                System.out.println("Error: La transacción " + idTransaccion + " no está reservada.");
                return;
            }
            // Liberar asientos y eliminar transacción
            for (String idAsiento : tx.getAsientos()) {
                mapaAsientos.put(idAsiento, "Disponible"); // Volver a disponible
            }
            listaTransacciones.remove(tx);// breakpoint
            System.out.println("Reserva ID " + idTransaccion + " cancelada. Asientos liberados.");

        } else if (opcion == 2) { // Eliminar Compra
            if (!tx.getStatus().equals("Vendida")) {
                System.out.println("Error: La transacción " + idTransaccion + " no es una compra finalizada.");
                return;
            }
            // Liberar asientos, revertir globales y eliminar transacción
            for (String idAsiento : tx.getAsientos()) {
                mapaAsientos.put(idAsiento, "Disponible");
            }
            asientosVendidosGlobal -= tx.getAsientos().size();
            ingresosTotalesGlobal -= tx.getPrecioTotalCalculado();
            listaTransacciones.remove(tx);
            System.out.println("Compra ID " + idTransaccion + " eliminada (Devolución procesada). Asientos liberados.");
            System.out.println("Se revirtieron $" + tx.getPrecioTotalCalculado() + " de ingresos y " + tx.getAsientos().size() + " asientos vendidos.");
        }
    }

    // --- Generación de Boletas ---

    private void imprimirBoleta() {
        limpiarPantalla();
        System.out.println("--- Imprimir Boleta ---");
        System.out.print("Ingrese el ID de la transacción (Reserva o Compra): ");
        int idTransaccion = leerEntero();

        Optional<Transaccion> transaccionOpt = buscarTransaccionPorId(idTransaccion);// breakpoint

        if (transaccionOpt.isPresent()) {
            Transaccion tx = transaccionOpt.get();
            System.out.println("\n=========== BOLETA / RESERVA ===========");
            System.out.println(" ID Transacción: " + tx.getIdTransaccion());
            System.out.println(" Estado:         " + tx.getStatus());
            System.out.println(" Cliente:        " + tx.getTipoCliente());
            System.out.println(" Asientos: ");
            for (String idAsiento : tx.getAsientos()) {
                char seccion = idAsiento.charAt(0);
                System.out.println("   - " + idAsiento + " (Sección " + seccion + ", Precio Base: $" + calcularPrecioAsiento(idAsiento) + ")");
            }
            System.out.println("--------------------------------------");
            System.out.println(" Descuento Aplicado: " + tx.getTipoCliente()); 
            System.out.println(" PRECIO TOTAL:      $" + tx.getPrecioTotalCalculado());
            System.out.println("======================================");
            if (tx.getStatus().equals("Reservada")) {
                System.out.println(" (Esta es una reserva, debe ser convertida a compra)");
            }
            System.out.println("\n     ¡Gracias por su preferencia!");
            System.out.println("======================================");

        } else {
            System.out.println("Error: No se encontró ninguna transacción con ID " + idTransaccion + ".");
        }
    }

    // --- Métodos Utilitarios ---

    private Optional<Transaccion> buscarTransaccionPorId(int id) {
        return listaTransacciones.stream()
                .filter(tx -> tx.getIdTransaccion() == id)
                .findFirst();
    }


    private int leerOpcionNumerica(int min, int max) {
        int opcion;
        while (true) {
            System.out.print("Ingrese opción (" + min + "-" + max + "): ");
            try {
                opcion = sc.nextInt();
                sc.nextLine(); // Consumir newline
                if (opcion >= min && opcion <= max) return opcion;
                else System.out.println("Error: Opción fuera de rango.");
            } catch (InputMismatchException e) {
                System.out.println("Error: Ingrese un número.");
                sc.nextLine(); // Limpiar buffer
            }
        }
    }

    private int leerEntero() {
        int numero;
        while (true) {
           // System.out.print("Ingrese un número entero: "); // El prompt se pone antes de llamar
            try {
                numero = sc.nextInt();
                sc.nextLine(); // Consumir newline
                return numero;
            } catch (InputMismatchException e) {
                System.out.println("Error: Ingrese un número entero válido.");
                sc.nextLine(); // Limpiar buffer
            }
        }
    }


    private void pausaParaContinuar() {
        System.out.println("\nPresione Enter para continuar...");
        sc.nextLine();
    }

    public static void limpiarPantalla() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void cerrarScanner() {
        if (sc != null) sc.close();
    }

    private void mostrarResumenSalida() {
        limpiarPantalla();
        System.out.println("======================================");
        System.out.println("     Saliendo del Sistema");
        System.out.println("======================================");
        long reservasActivas = listaTransacciones.stream().filter(tx -> tx.getStatus().equals("Reservada")).count();
        System.out.println("Reservas activas pendientes: " + reservasActivas);
        System.out.println("\nResumen Global Final:");
        System.out.println("  Asientos vendidos hoy: " + asientosVendidosGlobal);
        System.out.println("  Ingresos totales hoy: $" + ingresosTotalesGlobal);
        System.out.println("======================================");
        System.out.println("\n¡Hasta luego!");
    }
}
