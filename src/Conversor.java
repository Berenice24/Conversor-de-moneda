// Importación de librerías necesarias para el manejo de JSON y solicitudes HTTP
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Conversor {

    // URL de la API de tasas de cambio (con clave de API)
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/9fda648bdd684468d6bdbd47/latest/USD";

    // Muestra el menú de monedas disponibles
    public static void exibirMenu() {
        System.out.println("Sea bienvenido/a al Conversor de Moneda");
        System.out.println("Monedas disponibles:");
        System.out.println("1 - ARS (Peso argentino)");
        System.out.println("2 - BOB (Boliviano boliviano)");
        System.out.println("3 - BRL (Real brasileño)");
        System.out.println("4 - CLP (Peso chileno)");
        System.out.println("5 - COP (Peso colombiano)");
        System.out.println("6 - USD (Dólar estadounidense)");
        System.out.println("0 - Salir");
    }

    // Solicita al usuario que elija una moneda y valida la entrada
    public static int pedirOpcionMoneda(String mensaje, Scanner scanner) {
        int opcion;
        while (true) {
            exibirMenu();
            System.out.print(mensaje);
            if (scanner.hasNextInt()) {
                opcion = scanner.nextInt();
                scanner.nextLine(); // Limpiar buffer
                if (opcion >= 0 && opcion <= 6) {
                    return opcion;
                } else {
                    System.out.println("Por favor, ingrese un número entre 0 y 6.");
                }
            } else {
                System.out.println("Entrada inválida. Por favor, ingrese un número.");
                scanner.nextLine(); // Limpiar buffer
            }
        }
    }

    // Convierte la opción numérica del usuario en el código de moneda correspondiente
    public static String opcionAMoneda(int opcion) {
        switch (opcion) {
            case 1: return "ARS";
            case 2: return "BOB";
            case 3: return "BRL";
            case 4: return "CLP";
            case 5: return "COP";
            case 6: return "USD";
            default: return null;
        }
    }

    // Solicita al usuario la cantidad de dinero a convertir y valida la entrada
    public static double pedirCantidad(String mensaje, Scanner scanner) {
        double cantidad;
        while (true) {
            System.out.print(mensaje);
            if (scanner.hasNextDouble()) {
                cantidad = scanner.nextDouble();
                scanner.nextLine(); // Limpiar buffer
                if (cantidad > 0) {
                    return cantidad;
                } else {
                    System.out.println("Por favor, ingrese una cantidad mayor a cero.");
                }
            } else {
                System.out.println("Entrada inválida. Por favor, ingrese un número válido.");
                scanner.nextLine(); // Limpiar buffer
            }
        }
    }

    // Realiza la solicitud HTTP a la API de ExchangeRate y devuelve la respuesta en formato JSON
    public static String obtenerDatosAPI() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(API_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // Realiza la conversión de monedas utilizando las tasas de cambio
    public static double convertirMoneda(double cantidad, double tasaOrigen, double tasaDestino) {
        return cantidad * (tasaDestino / tasaOrigen);
    }

    // Método principal del programa
    public static void main(String[] args) {
        try {
            // Obtener los datos JSON desde la API
            String jsonResponse = obtenerDatosAPI();
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

            // Validar que la respuesta fue exitosa
            String estado = jsonObject.has("result") ? jsonObject.get("result").getAsString() : "";
            if (!"success".equalsIgnoreCase(estado)) {
                System.out.println("Error al obtener datos de la API.");
                return;
            }

            // Obtener el objeto de tasas de conversión
            JsonObject conversionRates = jsonObject.getAsJsonObject("conversion_rates");
            Scanner scanner = new Scanner(System.in);

            System.out.println(); // Espacio en blanco

            // Bucle principal para realizar conversiones
            while (true) {
                int opcionOrigen = pedirOpcionMoneda("Elija moneda de origen (0 para salir): ", scanner);
                if (opcionOrigen == 0) {
                    System.out.println("Gracias por usar el conversor. ¡Hasta luego!");
                    break;
                }

                String monedaOrigen = opcionAMoneda(opcionOrigen);
                double cantidad = pedirCantidad("Ingrese la cantidad a convertir: ", scanner);

                int opcionDestino = pedirOpcionMoneda("Elija moneda destino (0 para salir): ", scanner);
                if (opcionDestino == 0) {
                    System.out.println("Gracias por usar el conversor. ¡Hasta luego!");
                    break;
                }

                String monedaDestino = opcionAMoneda(opcionDestino);

                // Obtener tasas de cambio de ambas monedas
                double tasaOrigen = conversionRates.get(monedaOrigen).getAsDouble();
                double tasaDestino = conversionRates.get(monedaDestino).getAsDouble();

                // Realizar conversión y mostrar resultado
                double resultado = convertirMoneda(cantidad, tasaOrigen, tasaDestino);
                System.out.printf("\n%.2f %s equivalen a %.2f %s\n\n", cantidad, monedaOrigen, resultado, monedaDestino);
            }

            scanner.close();

        } catch (Exception e) {
            // Manejo de errores de red o de análisis de datos
            System.out.println("Ocurrió un error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
