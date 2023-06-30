import org.junit.Test;

import java.util.Arrays;

public class FeatureSelectionTest {
    // Definir los parámetros del problema de selección de características
    private static final int NUM_FEATURES = 10; // Número total de características
    private static final double ALPHA_1 = 0.5; // Valor de alpha1

    @Test
    public void featureSelectionTest() {
        // Definir la función objetivo de selección de características
        OptFunction featureSelectionObjective = params -> {
            // Obtener las características seleccionadas
            boolean[] selectedFeatures = getSelectedFeatures(params);

            // Simular la evaluación del modelo con las características seleccionadas
            double errorRate = evaluateModel(selectedFeatures); // Obtener la tasa de error

            // Calcular la proporción de características seleccionadas
            double proportionSelected = (double) countSelectedFeatures(selectedFeatures) / NUM_FEATURES;

            // Calcular el valor de la función objetivo
            return ALPHA_1 * errorRate * proportionSelected + (1 - ALPHA_1);
        };

        // Configurar y ejecutar el algoritmo WOA
        WOA woa = new WOA(featureSelectionObjective, 30, 0, 1, NUM_FEATURES, 500, true);
        woa.execute();

        System.out.println("Results of feature selection optimization:");
        woa.printOptimal();
    }

    // Función para obtener las características seleccionadas a partir de los parámetros
    private boolean[] getSelectedFeatures(double[] params) {
        boolean[] selectedFeatures = new boolean[NUM_FEATURES];
        for (int i = 0; i < NUM_FEATURES; i++) {
            // Si el valor del parámetro es mayor que 0.5, se selecciona la característica
            selectedFeatures[i] = params[i] > 0.5;
        }
        return selectedFeatures;
    }

    // Función para contar el número de características seleccionadas
    private int countSelectedFeatures(boolean[] selectedFeatures) {
        int count = 0;
        for (boolean feature : selectedFeatures) {
            if (feature) {
                count++;
            }
        }
        return count;
    }

    // Función para simular la evaluación del modelo
    private double evaluateModel(boolean[] selectedFeatures) {
        // Aquí deberías realizar la evaluación real del modelo utilizando las características seleccionadas
        // y obtener la tasa de error resultante
        // En esta implementación de ejemplo, se devuelve un valor aleatorio entre 0 y 1
        return Math.random();
    }
}
