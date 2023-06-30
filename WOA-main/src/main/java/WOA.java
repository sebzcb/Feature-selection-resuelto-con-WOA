
//import de.vandermeer.asciitable.AsciiTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author : LA4AM12
 * @create : 2023-02-10 16:16:20
 * @description : class implements the whale optimization algorithm
 */
public class WOA {

    private OptFunction optFunction; // función objetivo a optimizar.
    private double lb, ub; //lb valor inferior de busqueda, ub valor superior de busqueda.
    private int population; //numero ballenas
    private int dim; //dimension espacio busqueda
    private int maxIter; //maximo de iteraciones permitidas en este algoritmo
    private double[][] positions; //almacena posiciones de las ballenas
    private boolean minimize; //indica si se quiere maximizar o minimizar la funcion objetivo.
    private double[] convergenceCurve; //almacena los valores de la funcion objetivo en cada iteracion.
    private double[] optimalPos; // es un arreglo que almacena la mejor posición encontrada para la función objetivo.
    private double optimalScore; //representa el mejor puntaje o valor de la función objetivo encontrado.

    public WOA(OptFunction optFunction, int population, double lb, double ub, int dim, int maxIter, boolean minimize) {
        this.optFunction = optFunction;
        this.population = population;
        this.lb = lb;
        this.ub = ub;
        this.dim = dim;
        this.maxIter = maxIter;
        this.positions = new double[population][dim];
        this.convergenceCurve = new double[maxIter];
        this.minimize = minimize;
        this.optimalScore = minimize ? Double.MAX_VALUE : -Double.MAX_VALUE;
        optimalPos = new double[dim];
        initPopulation();
    }

    // ajusta las posiciones de la ballena en caso de que se hayan movido fuera del espacio de búsqueda 
    //permitido y asegura que todas las posiciones estén dentro de los límites [lb, ub].
    private void adjustPositions(int agentIndex) {
        for (int j = 0; j < dim; j++) {
            if (positions[agentIndex][j] < lb) {
                positions[agentIndex][j] = lb;
            }
            if (positions[agentIndex][j] > ub) {
                positions[agentIndex][j] = ub;
            }
        }
    }

    private void initPopulation() {
        Random rand = new Random();
        this.positions = new double[population][dim];
        for (int i = 0; i < population; i++) {
            for (int j = 0; j < dim; j++) {
                //se genera una posición aleatoria inicial para cada ballena en cada dimensión del espacio de búsqueda.
                positions[i][j] = lb + rand.nextDouble() * (ub - lb);
            }
        }
    }
    
    //calcula ballena con mejor aptitud.
    private void calcFitness() {
        for (int i = 0; i < population; i++) {
            //se asegura que esten todos entre los limites [lb,ub]
            adjustPositions(i);

            // calcula la aptitud para cada agente utilizando la función objetivo.
            double fitness = optFunction.calc(positions[i]);

            // Update the leader
            //se actualiza la posición del líder si la aptitud del agente actual es mejor que la aptitud del líder actual
            //minimizar la funcion obj. -> si la aptitud del agente actual es menor a la mejor aptitud ya encontrada entonces esa sera la nueva mejor encontrada.
            //maximizar la funcion obj. -> si la aptitud del agente actual es mejor que la mejor ya encontrada esa sera la mejor encontrada.
            if (minimize && fitness < optimalScore || !minimize && fitness > optimalScore) {
                optimalScore = fitness;
                //se copia la posición del agente actual en el arreglo de posiciones optimales (optimalPos) utilizando la función System.arraycopy.
                System.arraycopy(positions[i], 0, optimalPos, 0, dim);
            }
        }
    }

    /*itera a través de todas las ballenas en la población y utiliza las ecuaciones (2.2) y (2.8) para actualizar su posición en función de su 
        distancia al líder y una posición aleatoria, respectivamente. El algoritmo utiliza valores aleatorios para agregar una exploración aleatoria 
        y evitar quedar atrapado en óptimos locales.*/
    private void updatePosition(double a, double a2) {
        Random rand = new Random();
        
        //recorre todas las ballenas y actualiza sus posiciones segun el lider.
        for (int i = 1; i < population; i++) {
            //valores aleatorios para r1 y r2.
            double r1 = rand.nextDouble();
            double r2 = rand.nextDouble();
            
            //cuando las ballenas estan rodeando a sus presas este algoritmo representa 
            // A y C son valores aleatorios que se utilizan para controlar el movimiento de las ballenas y actualizar su posición en el espacio de búsqueda.
            double A = 2.0 * a * r1 - a;                            // Eq. (2.3) in the paper
            double C = 2.0 * r2;                                    // Eq. (2.4) in the paper
            
            //ecuaciones en espiral de las ballenas
            //b es una constante para definir la forma de la espiral logarítmica
            double b = 1.0;                                          // parameters in Eq. (2.5)
            
            // l is a random number in [−1,1],
            //la ecuación (2.5) se utiliza en ambos casos para agregar un elemento aleatorio a la posición de la ballena. 
            double l = (a2 - 1.0) * rand.nextDouble() + 1.0;        // parameters in Eq. (2.5)
            double p = rand.nextDouble();                           // p in Eq. (2.6)

            for (int j = 0; j < dim; j++) {
                
                //p es un numero entre [0,1] que representa la probabilidad de cazar o actualizar posicion
                if (p < 0.5) {
                    //cuando la magnitud de A es menor que 1, lo que significa que la ballena está cerca de la presa y se mueve hacia el
                    if (Math.abs(A) < 1) {
                        double D_Leader = Math.abs(C * optimalPos[j] - positions[i][j]);  // Eq. (2.1)
                        positions[i][j] = optimalPos[j] - A * D_Leader;      // Eq. (2.2)
                    } else {
                        //significa que la ballena está lejos de la presa, se selecciona una ballena al azar  y se mueve a una posicion aleatoria,
                        int randWhaleIdx = rand.nextInt(population);
                        double[] randomPos = positions[randWhaleIdx];
                        double D_X_rand = Math.abs(C * randomPos[j] - positions[i][j]); // Eq. (2.7)
                        positions[i][j] = randomPos[j] - A * D_X_rand;  // Eq. (2.8)
                    }
                } else {
                    //calcula la nueva posición de la ballena utilizando la ecuación (2.5) del WOA que se utiliza para simular un movimiento hacia el líder
                    double distance2Leader = Math.abs(optimalPos[j] - positions[i][j]); //indica la distancia de la i-ésima ballena a la presa
                    //Eq. (2.5) , b y l son valores aleatorios que controlan la amplitud y la dirección del movimiento de la ballena hacia el líder.
                    positions[i][j] = distance2Leader * Math.exp(b * l) * Math.cos(2.0 * Math.PI * l) + optimalPos[j];
                }
            }
        }
    }
    //hace las iteraciones correspondientes para finalmente obtener la mejor aptitud de

    public double[] execute() {
        // (2) se hacen las iteraciones correspondientes 
        for (int iter = 0; iter < maxIter; iter++) {
            //(3) evalua la aptitud de cada ballena de la poblacion actual
            calcFitness(); 
            /*(4) se guarda el valor de la mejor aptitud encontrada hasta ahora en convergenceCurve en la posicion de la iteracion actual 
            (se actualiza optimalPos en el metodo calcFitness)*/
            convergenceCurve[iter] = optimalScore; 

            //a decrementa linealmente de 2 a 0 por cada iteracion. (ecuacion 2.3)
            double a = 2.0 - (double) iter * (2.0 / maxIter);

            // a2 decrece linealmente de -1 a -2 to calculate t in Eq. (3.12)
            double a2 = -1.0 + (double) iter * (-1.0 / maxIter);
            
            //(3)actualiza posicion de la poblacion de ballenas
            updatePosition(a, a2); 
        }
        calcFitness(); //(4)evalua nuevamente la aptitud de las ballenas de la poblacion actual actualizando optimalPos
        return optimalPos; //retorna el valor con mejor aptitud encontrada. (se actualiza optimalPos en el metodo calcFitness)
    }

    public double[] getConvergenceCurve() {
        return convergenceCurve;
    }

    public double[] getLeaderPos() {
        return optimalPos;
    }

    public double getOptimalScore() {
        return optimalScore;
    }
    public void printOptimal() {
        System.out.println("optimal"); // Cabecera

        for (int i = 0; i < dim; i++) {
            System.out.print("dim" + i + " ");
        }
        System.out.println();

        System.out.printf("%.5f", optimalScore); // Puntaje óptimo
        for (double v : optimalPos) {
            System.out.printf(" %.5f", v); // Valores de posición óptima
        }
        System.out.println();
    }

}

//import de.vandermeer.asciitable.AsciiTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author : LA4AM12
 * @create : 2023-02-10 16:16:20
 * @description : class implements the whale optimization algorithm
 */
public class WOA {

    private OptFunction optFunction; // función objetivo a optimizar.
    private double lb, ub; //lb valor inferior de busqueda, ub valor superior de busqueda.
    private int population; //numero ballenas
    private int dim; //dimension espacio busqueda
    private int maxIter; //maximo de iteraciones permitidas en este algoritmo
    private double[][] positions; //almacena posiciones de las ballenas
    private boolean minimize; //indica si se quiere maximizar o minimizar la funcion objetivo.
    private double[] convergenceCurve; //almacena los valores de la funcion objetivo en cada iteracion.
    private double[] optimalPos; // es un arreglo que almacena la mejor posición encontrada para la función objetivo.
    private double optimalScore; //representa el mejor puntaje o valor de la función objetivo encontrado.

    public WOA(OptFunction optFunction, int population, double lb, double ub, int dim, int maxIter, boolean minimize) {
        this.optFunction = optFunction;
        this.population = population;
        this.lb = lb;
        this.ub = ub;
        this.dim = dim;
        this.maxIter = maxIter;
        this.positions = new double[population][dim];
        this.convergenceCurve = new double[maxIter];
        this.minimize = minimize;
        this.optimalScore = minimize ? Double.MAX_VALUE : -Double.MAX_VALUE;
        optimalPos = new double[dim];
        initPopulation();
    }

    // ajusta las posiciones de la ballena en caso de que se hayan movido fuera del espacio de búsqueda 
    //permitido y asegura que todas las posiciones estén dentro de los límites [lb, ub].
    private void adjustPositions(int agentIndex) {
        for (int j = 0; j < dim; j++) {
            if (positions[agentIndex][j] < lb) {
                positions[agentIndex][j] = lb;
            }
            if (positions[agentIndex][j] > ub) {
                positions[agentIndex][j] = ub;
            }
        }
    }

    private void initPopulation() {
        Random rand = new Random();
        this.positions = new double[population][dim];
        for (int i = 0; i < population; i++) {
            for (int j = 0; j < dim; j++) {
                //se genera una posición aleatoria inicial para cada ballena en cada dimensión del espacio de búsqueda.
                positions[i][j] = lb + rand.nextDouble() * (ub - lb);
            }
        }
    }
    
    //calcula ballena con mejor aptitud.
    private void calcFitness() {
        for (int i = 0; i < population; i++) {
            //se asegura que esten todos entre los limites [lb,ub]
            adjustPositions(i);

            // calcula la aptitud para cada agente utilizando la función objetivo.
            double fitness = optFunction.calc(positions[i]);

            // Update the leader
            //se actualiza la posición del líder si la aptitud del agente actual es mejor que la aptitud del líder actual
            //minimizar la funcion obj. -> si la aptitud del agente actual es menor a la mejor aptitud ya encontrada entonces esa sera la nueva mejor encontrada.
            //maximizar la funcion obj. -> si la aptitud del agente actual es mejor que la mejor ya encontrada esa sera la mejor encontrada.
            if (minimize && fitness < optimalScore || !minimize && fitness > optimalScore) {
                optimalScore = fitness;
                //se copia la posición del agente actual en el arreglo de posiciones optimales (optimalPos) utilizando la función System.arraycopy.
                System.arraycopy(positions[i], 0, optimalPos, 0, dim);
            }
        }
    }

    /*itera a través de todas las ballenas en la población y utiliza las ecuaciones (2.2) y (2.8) para actualizar su posición en función de su 
        distancia al líder y una posición aleatoria, respectivamente. El algoritmo utiliza valores aleatorios para agregar una exploración aleatoria 
        y evitar quedar atrapado en óptimos locales.*/
    private void updatePosition(double a, double a2) {
        Random rand = new Random();
        
        //recorre todas las ballenas y actualiza sus posiciones segun el lider.
        for (int i = 1; i < population; i++) {
            //valores aleatorios para r1 y r2.
            double r1 = rand.nextDouble();
            double r2 = rand.nextDouble();
            
            //cuando las ballenas estan rodeando a sus presas este algoritmo representa 
            // A y C son valores aleatorios que se utilizan para controlar el movimiento de las ballenas y actualizar su posición en el espacio de búsqueda.
            double A = 2.0 * a * r1 - a;                            // Eq. (2.3) in the paper
            double C = 2.0 * r2;                                    // Eq. (2.4) in the paper
            
            //ecuaciones en espiral de las ballenas
            //b es una constante para definir la forma de la espiral logarítmica
            double b = 1.0;                                          // parameters in Eq. (2.5)
            
            // l is a random number in [−1,1],
            //la ecuación (2.5) se utiliza en ambos casos para agregar un elemento aleatorio a la posición de la ballena. 
            double l = (a2 - 1.0) * rand.nextDouble() + 1.0;        // parameters in Eq. (2.5)
            double p = rand.nextDouble();                           // p in Eq. (2.6)

            for (int j = 0; j < dim; j++) {
                
                //p es un numero entre [0,1] que representa la probabilidad de cazar o actualizar posicion
                if (p < 0.5) {
                    //cuando la magnitud de A es menor que 1, lo que significa que la ballena está cerca de la presa y se mueve hacia el
                    if (Math.abs(A) < 1) {
                        double D_Leader = Math.abs(C * optimalPos[j] - positions[i][j]);  // Eq. (2.1)
                        positions[i][j] = optimalPos[j] - A * D_Leader;      // Eq. (2.2)
                    } else {
                        //significa que la ballena está lejos de la presa, se selecciona una ballena al azar  y se mueve a una posicion aleatoria,
                        int randWhaleIdx = rand.nextInt(population);
                        double[] randomPos = positions[randWhaleIdx];
                        double D_X_rand = Math.abs(C * randomPos[j] - positions[i][j]); // Eq. (2.7)
                        positions[i][j] = randomPos[j] - A * D_X_rand;  // Eq. (2.8)
                    }
                } else {
                    //calcula la nueva posición de la ballena utilizando la ecuación (2.5) del WOA que se utiliza para simular un movimiento hacia el líder
                    double distance2Leader = Math.abs(optimalPos[j] - positions[i][j]); //indica la distancia de la i-ésima ballena a la presa
                    //Eq. (2.5) , b y l son valores aleatorios que controlan la amplitud y la dirección del movimiento de la ballena hacia el líder.
                    positions[i][j] = distance2Leader * Math.exp(b * l) * Math.cos(2.0 * Math.PI * l) + optimalPos[j];
                }
            }
        }
    }
    //hace las iteraciones correspondientes para finalmente obtener la mejor aptitud de

    public double[] execute() {
        // (2) se hacen las iteraciones correspondientes 
        for (int iter = 0; iter < maxIter; iter++) {
            //(3) evalua la aptitud de cada ballena de la poblacion actual
            calcFitness(); 
            /*(4) se guarda el valor de la mejor aptitud encontrada hasta ahora en convergenceCurve en la posicion de la iteracion actual 
            (se actualiza optimalPos en el metodo calcFitness)*/
            convergenceCurve[iter] = optimalScore; 

            //a decrementa linealmente de 2 a 0 por cada iteracion. (ecuacion 2.3)
            double a = 2.0 - (double) iter * (2.0 / maxIter);

            // a2 decrece linealmente de -1 a -2 to calculate t in Eq. (3.12)
            double a2 = -1.0 + (double) iter * (-1.0 / maxIter);
            
            //(3)actualiza posicion de la poblacion de ballenas
            updatePosition(a, a2); 
        }
        calcFitness(); //(4)evalua nuevamente la aptitud de las ballenas de la poblacion actual actualizando optimalPos
        return optimalPos; //retorna el valor con mejor aptitud encontrada. (se actualiza optimalPos en el metodo calcFitness)
    }

    public double[] getConvergenceCurve() {
        return convergenceCurve;
    }

    public double[] getLeaderPos() {
        return optimalPos;
    }

    public double getOptimalScore() {
        return optimalScore;
    }
    public void printOptimal() {
        System.out.println("optimal"); // Cabecera

        for (int i = 0; i < dim; i++) {
            System.out.print("dim" + i + " ");
        }
        System.out.println();

        System.out.printf("%.5f", optimalScore); // Puntaje óptimo
        for (double v : optimalPos) {
            System.out.printf(" %.5f", v); // Valores de posición óptima
        }
        System.out.println();
    }

}
