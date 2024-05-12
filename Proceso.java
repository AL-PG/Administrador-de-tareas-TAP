import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

class Proceso implements Runnable {
    private static final Random rand = new Random();
    private static int idCounter = 0;
    private final int id;
    int prioridad;
    int numProcesadores;
    int cantidadRAM;
    int tiempoEjecucion;
    AdministradorRecursos admin;

    public Proceso(AdministradorRecursos admin) {
        this.id = idCounter++;
        this.prioridad = rand.nextInt(3);
        this.numProcesadores = 1 + rand.nextInt(8);
        this.cantidadRAM = 10 * (1 + rand.nextInt(5));
        this.tiempoEjecucion = 1 + rand.nextInt(3);
        this.admin = admin;
    }

    @Override
    public void run() {
        admin.asignarRecursos(this);
        try {
            Thread.sleep(tiempoEjecucion * 1000); // Tiempo en milisegundos para pruebas rápidas
            System.out.printf("[DEBUG] - P%d         : Terminé. Liberando Recursos\n", id);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        admin.liberarRecursos(this);
        admin.agregarProcesoTerminado(this);
    }

    public String toString() {
        return "P" + id;
    }

    public int getId() {
        return id;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public int getNumProcesadores() {
        return numProcesadores;
    }

    public int getCantidadRAM() {
        return cantidadRAM;
    }

    public int getTiempoEjecucion() {
        return tiempoEjecucion;
    }

    public AdministradorRecursos getAdmin() {
        return admin;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public void setNumProcesadores(int numProcesadores) {
        this.numProcesadores = numProcesadores;
    }

    public void setCantidadRAM(int cantidadRAM) {
        this.cantidadRAM = cantidadRAM;
    }

    public void setTiempoEjecucion(int tiempoEjecucion) {
        this.tiempoEjecucion = tiempoEjecucion;
    }

    public void setAdmin(AdministradorRecursos admin) {
        this.admin = admin;
    }
}

class AdministradorRecursos {
    private int totalProcesadores = 16;
    private int totalRAM = 100;
    private ReentrantLock lock = new ReentrantLock();
    private List<Proceso> procesosTerminados = new ArrayList<>();

    public int getTotalProcesadores() {
        return totalProcesadores;
    }

    public int getTotalRAM() {
        return totalRAM;
    }

    public void generarProcesos(int n) {
        Thread[] threads = new Thread[n];
        System.out.printf("[DEBUG] - MainThread : Se crearon %d hilos procesos\n", n);
        for (int i = 0; i < n; i++) {
            threads[i] = new Thread(new Proceso(this));
            threads[i].start();
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void asignarRecursos(Proceso proceso) {
        lock.lock();
        try {
            while (!(totalProcesadores >= proceso.numProcesadores && totalRAM >= proceso.cantidadRAM)) {
                System.out.println("[DEBUG] - hiloAdmin  : No hay recursos suficientes para asignar al siguiente proceso");
                lock.unlock();
                Thread.sleep(1000);
                lock.lock();
            }
            totalProcesadores -= proceso.numProcesadores;
            totalRAM -= proceso.cantidadRAM;
            System.out.printf("[DEBUG] - %s         : Proceso en ejecución %s\n", proceso, proceso);
            System.out.printf("[INFO] - %s         : Ocupación Procesadores: %d\n", proceso, totalProcesadores);
            System.out.printf("[INFO] - %s         : Ocupación RAM: %d\n", proceso, totalRAM);
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    public void agregarProcesoTerminado(Proceso proceso) {
        procesosTerminados.add(proceso);
    }

    public void imprimirReporte() {
        System.out.println("------------------------------------------------------------------------");
        System.out.println("                          R E P O R T E");
        System.out.println("------------------------------------------------------------------------");
        System.out.printf("%-6s%-12s%-14s%-8s%-8s%-10s%-9s%-9s%s\n", "Num", "Proceso", "Prioridad", "CPU", "RAM", "tiempo", "%CPU", "%RAM", "Estatus");
        System.out.println("------------------------------------------------------------------------");
        for (int i = 0; i < procesosTerminados.size(); i++) {
            Proceso proceso = procesosTerminados.get(i);
            double porcentajeCPU = (double) proceso.getNumProcesadores() / getTotalProcesadores() * 100;
            double porcentajeRAM = (double) proceso.getCantidadRAM() / getTotalRAM() * 100;
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
        try {
            Thread.sleep(5000); // Esperar 5 segundos para asegurarnos de que todos los procesos hayan terminado
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        admin.generarProcesos(numProcesos);

        try {
            Thread.sleep(3000); // Esperar 5 segundos para asegurarnos de que todos los procesos hayan terminado
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Imprimir reporte
        admin.imprimirReporte();
    }
}