import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.PriorityBlockingQueue;

class AdministradorRecursos {
    private int totalProcesadores = 16;
    private int totalRAM = 100;
    private ReentrantLock lock = new ReentrantLock();
    private List<Proceso> procesosTerminados = new ArrayList<>();
    private PriorityBlockingQueue<Proceso> colaPrioridad = new PriorityBlockingQueue<>();

    public void agregarProcesosTerminados(Proceso proceso) {
        procesosTerminados.add(proceso);
    }

    public int getTotalProcesadores() {
        return totalProcesadores;
    }

    public int getTotalRAM() {
        return totalRAM;
    }

    public void generarProcesos(int n) {
        for (int i = 0; i < n; i++) {
            Proceso proceso = new Proceso(this);
            colaPrioridad.add(proceso);
        }
        System.out.printf("[DEBUG] - MainThread : Se crearon %d procesos\n", n);
        ejecutarProcesos();
    }

    public void ejecutarProcesos() {
        while (!colaPrioridad.isEmpty()) {
            Proceso proceso = colaPrioridad.poll();
            new Thread(proceso).start();
            procesosTerminados.add(proceso);
        }
    }

    public boolean asignarRecursos(Proceso proceso) {
        lock.lock();
        try {
            if (totalProcesadores >= proceso.numProcesadores && totalRAM >= proceso.cantidadRAM) {
                totalProcesadores -= proceso.numProcesadores;
                totalRAM -= proceso.cantidadRAM;
                System.out.printf("[DEBUG] - %s         : Proceso en ejecución\n", proceso);
                System.out.printf("[INFO] - %s         : Ocupación Procesadores: %d\n", proceso, totalProcesadores);
                System.out.printf("[INFO] - %s         : Ocupación RAM: %d\n", proceso, totalRAM);
                return true;
            } else {
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    public void liberarRecursos(Proceso proceso) {
        lock.lock();
        try {
            totalProcesadores += proceso.numProcesadores;
            totalRAM += proceso.cantidadRAM;
            System.out.printf("[DEBUG] - %s         : Recursos liberados\n", proceso);
            System.out.printf("[INFO] - %s         : Ocupación Procesadores: %d\n", proceso, totalProcesadores);
            System.out.printf("[INFO] - %s         : Ocupación RAM: %d\n", proceso, totalRAM);
        } finally {
            lock.unlock();
        }
    }

    public void imprimirReporte() {
        System.out.println("------------------------------------------------------------------------");
        System.out.println("                          R E P O R T E");
        System.out.println("------------------------------------------------------------------------");
        System.out.printf("%-6s%-12s%-14s%-8s%-8s%-10s%-9s%-9s%s\n", "Num", "Proceso", "Prioridad", "CPU", "RAM", "tiempo", "%CPU", "%RAM", "Estatus");
        System.out.println("------------------------------------------------------------------------");
        for (int i = 0; i < procesosTerminados.size(); i++) {
            Proceso proceso = procesosTerminados.get(i);
            double porcentajeCPU = (double) proceso.getNumProcesadores() / 16 * 100;
            double porcentajeRAM = (double) proceso.getCantidadRAM() / 100 * 100;
            System.out.printf("%-6d%-12s%-14d%-8d%-8d%-10d%-9.2f%-9.2f%s\n", i + 1, proceso, proceso.getPrioridad(), proceso.getNumProcesadores(),
                    proceso.getCantidadRAM(), proceso.getTiempoEjecucion(), porcentajeCPU, porcentajeRAM, "Atendido");
        }
        System.out.println("------------------------------------------------------------------------");
    }

    public static void main(String[] args) {
        AdministradorRecursos admin = new AdministradorRecursos();
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese el número de procesos a generar (entre 2 y 20): ");
        int numProcesos = scanner.nextInt();

        if (numProcesos < 2 || numProcesos > 20) {
            System.out.println("El número de procesos debe estar entre 2 y 20. Saliendo del programa.");
            return;
        }
        admin.generarProcesos(numProcesos);

        try {
            Thread.sleep(1000*(numProcesos+5)); // Espera para generar la tabla
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Imprimir la tabla
        admin.imprimirReporte();
    }
}