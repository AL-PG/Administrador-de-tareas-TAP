import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Scanner;

class Proceso implements Runnable {
    private static final Random rand = new Random();
    private static int idCounter = 0;
    private final int id;
    int prioridad;
    int numProcesadores;
    int cantidadRAM;
    int tiempoEjecucion;
    String [][] tabla;
    AdministradorRecursos admin;

    public Proceso(AdministradorRecursos admin) {
        this.id = idCounter++;
        this.prioridad = rand.nextInt(3);
        this.numProcesadores = 1 + rand.nextInt(8);
        this.cantidadRAM = 10 * (1 + rand.nextInt(5));
        this.tiempoEjecucion = 1 + rand.nextInt(3);
        this.admin = admin;
        this.tabla = new String[20][8];
        guardardatos();
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

    public void guardardatos(){
        for (int i = 0; i < 20; i++) {
            tabla[i][0] = String.valueOf(id);
            tabla[i][1] = toString();
            tabla[i][2] = String.valueOf(prioridad);
            tabla[i][3] = String.valueOf(numProcesadores);
            tabla[i][4] = String.valueOf(cantidadRAM);
            tabla[i][5] = String.valueOf(tiempoEjecucion);
            tabla[i][6] = "%" + numProcesadores/16;
            tabla[i][7] = "%" + cantidadRAM/100;
        }
    }

    public void dibujarTabla(){

        for (int i = 0; i < tabla.length; i++) {
            for (int j = 0; j < tabla[i].length; j++) {
                System.out.print(tabla[i][j] + " "); 
            }
            System.out.println();
        }
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
    
    public void generarProcesos(int n, Proceso proceso) {
        
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
        proceso.dibujarTabla();
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
        AdministradorRecursos admin = new AdministradorRecursos();
        Proceso proceso = new Proceso(admin);
        Scanner leer = new Scanner(System.in);
        
        while(true){
            System.out.println("Numero de Procesos: ");
            int procesos = leer.nextInt();
            
            if(procesos < 2){
                System.out.println("El numero de procesos debe ser mayor a 2");
            } else if(procesos > 20) {
                System.out.println("El numero debe ser menor a 20");
            } else {
                admin.generarProcesos(procesos,proceso);
                break;
            }
        }
        leer.close();
    }
}
