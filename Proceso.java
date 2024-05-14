import java.util.Random;

class Proceso implements Runnable, Comparable<Proceso> {
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
        while (!admin.asignarRecursos(this)) {
            try {
                System.out.printf("[DEBUG] - %s         : Esperando recursos\n", this);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(tiempoEjecucion * 1000); 
            System.out.printf("[DEBUG] - P%d         : Terminé. Liberando Recursos\n", id);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        admin.liberarRecursos(this);
    }

    @Override
    public int compareTo(Proceso o) {
        return Integer.compare(this.prioridad, o.prioridad);
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