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
    }

    public String toString() {
        return "P" + id;
    }
}

class AdministradorRecursos {
    private int totalProcesadores = 16;
    private int totalRAM = 100;
    private ReentrantLock lock = new ReentrantLock();
    Scanner sc = new Scanner(System.in);
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
                Thread.sleep(500);
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

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int w;
        while (true) {
            System.out.println("Cantidad de procesos a ejecutar: ");
            w = sc.nextInt();
            if (w < 2 || w > 20) {
                System.out.println("Inténtelo de nuevo. El número de procesos debe estar entre 2 y 20. ");            
            }
            else{
                break;
            }
        }
        new AdministradorRecursos().generarProcesos(w);
        sc.close();
    }
}
