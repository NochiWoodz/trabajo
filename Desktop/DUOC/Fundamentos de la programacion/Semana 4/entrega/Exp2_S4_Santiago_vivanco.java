import java.util.Scanner;

public class Exp2_S4_Santiago_vivanco {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean seguirComprando = true;

        for (; seguirComprando; ) {
            // Variables para el total y la tarifa
            int total = 0;
            int Tarifa , opcion;
            
        // Menu de navegacion
            limpiarPantalla();
            System.out.println("--------------------------------------------------");
            System.out.println("Bienvenido al sistema de venta de entradas del Teatro Moro");
            System.out.println("Seleccione una opción:");
            System.out.println("1. Comprar entrada.");
            System.out.println("2. Ir a pagar.");
            System.out.println("3. Salir.");
            System.out.print("Ingrese su opción: ");
            opcion = sc.nextInt();
            limpiarPantalla();
            System.out.println("------------------------------------");
            System.out.println("          Plano del Teatro:");
            System.out.println("------------------------------------");
            System.out.println("  Escenario   Escenario   Escenario");
            System.out.println("  _________________________________");
            System.out.println("  [A] VIP     [A] VIP      [A] VIP");
            System.out.println("-------------------------------------");
            System.out.println("  [B] Platea Baja   [B] Platea Baja");
            System.out.println("-------------------------------------");
            System.out.println("  [C] Platea Alta   [C] Platea Alta");
            System.out.println("-------------------------------------");
            System.out.println("  [D] Palcos        [D] Palcos");
            System.out.println("-------------------------------------");
            System.out.println("Seleccione el tipo de entrada: ");
            System.out.println("A. VIP ($30.000)");
            System.out.println("B. Platea Baja ($15.000)");
            System.out.println("C. Platea Alta ($18.000)");
            System.out.println("D. Palcos ($13.000)"); 
            char ubicacion;
            do {
                System.out.print("Ingrese la letra de su ubicación: ");
                ubicacion = Character.toUpperCase(sc.next().charAt(0));

        }while (ubicacion != 'A' && ubicacion != 'B' && ubicacion != 'C' && ubicacion != 'D');

            int precioBase = switch (ubicacion) {
                case 'A' -> 30000;
                case 'B' -> 15000;
                case 'C' -> 18000;
                case 'D' -> 13000;
                default -> 0;
            };
        do {  
            limpiarPantalla();
            System.out.println("Seleccione la tarifa: ");
            System.out.println("1. Estudiante 20% de descuento");
            System.out.println("2. Público general 0% de descuento");
            System.out.print("Ingrese su opción: ");
            Tarifa = sc.nextInt();
        }while (seguirComprando && Tarifa != 1 && Tarifa != 2);
        
        // Calculo de total a pagar con if else en formato abreviado{    
        
        if (Tarifa == 1) {
            total = (int) (precioBase * 0.8); // 20% de descuento
        } else if (Tarifa == 2) {
            total = precioBase; // Sin descuento
        } else {
            System.out.println("Opción inválida.");
            continue;
        }
        
        //impresion en pantalla del total a pagar y detalles de la compra
            limpiarPantalla();
            System.out.println("--------------------------------------------------");    
            System.out.println("Tipo de entrada: " + (ubicacion == 'A' ? "VIP" : ubicacion == 'B' ? "Platea Baja" : ubicacion == 'C' ? "Platea Alta" : "Palcos"));
            System.out.println("Tarifa: " + (Tarifa == 1 ? "Estudiante 20% de descuento" : "Público general 0% de descuento"));
            System.out.println("Total a pagar: $" + total);
            System.out.println("Gracias por su compra, disfrute la función.");
        
            System.out.print("¿Desea realizar otra compra? (S/N): ");
            char respuesta = Character.toUpperCase(sc.next().charAt(0));
            if (respuesta != 'S') {
                seguirComprando = false;
            }
        } while (seguirComprando); 
        System.out.println("Muchas gracias por utilizar la aplicación.");
        sc.close();
    }

    //formato para limpiar la pantalla
    public static void limpiarPantalla() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
