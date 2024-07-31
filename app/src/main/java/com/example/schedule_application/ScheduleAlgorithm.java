package com.example.schedule_application;

import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScheduleAlgorithm {


    public static Map<String, Map<String, Set<String>>> generateEmployeePreferences(List<com.example.schedule_application.EmployeePreference> employeePreferences) {
        Map<String, Map<String, Set<String>>> employeePreferencesDict = new HashMap<>();
        for (com.example.schedule_application.EmployeePreference empPref : employeePreferences) {
            Map<String, Set<String>> daysMap = new HashMap<>();
            daysMap.put("possible", new HashSet<>(empPref.getPossibleDays()));
            daysMap.put("preferred", new HashSet<>(empPref.getPreferredDays()));
            employeePreferencesDict.put(empPref.getEmail(), daysMap);
        }
        return employeePreferencesDict;
    }

    public static List<String> generateInitialPopulation(int populationSize, int employees, int shiftsPerWeek) {
        List<String> population = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < populationSize; i++) {
            StringBuilder schedule = new StringBuilder();
            for (int j = 0; j < employees * shiftsPerWeek; j++) {
                schedule.append(rand.nextInt(2));
            }
            population.add(schedule.toString());
        }
        return population;
    }

    public static String mutate(String schedule, double mutationRate, int employees, int shiftsPerWeek) {
        Random rand = new Random();
        char[] mutatedSchedule = schedule.toCharArray();
        int i = rand.nextInt(employees * shiftsPerWeek);
        if (rand.nextDouble() < mutationRate) {
            mutatedSchedule[i] = (mutatedSchedule[i] == '0') ? '1' : '0';
        }
        return new String(mutatedSchedule);
    }

    public static List<Integer> tournamentSelection(Map<Integer, Double> resultFitness, double selectivePressure) {
        List<Map.Entry<Integer, Double>> fitnessEntries = new ArrayList<>(resultFitness.entrySet());
        Collections.shuffle(fitnessEntries);

        int[] parentIndices = {0, 1, 2};
        double[] parentFitness = {fitnessEntries.get(0).getValue(), fitnessEntries.get(1).getValue(), fitnessEntries.get(2).getValue()};
        double totalFitness = parentFitness[0] + parentFitness[1] + parentFitness[2];
        double[] probabilities = {selectivePressure * 2 * (parentFitness[0] / totalFitness),
                selectivePressure * 2 * (parentFitness[1] / totalFitness),
                selectivePressure * 2 * (parentFitness[2] / totalFitness)};

        List<Integer> highestIndices = Arrays.asList(0, 1, 2);
        Collections.sort(highestIndices, new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return Double.compare(probabilities[b], probabilities[a]);
            }
        });

        return Arrays.asList(
                fitnessEntries.get(highestIndices.get(0)).getKey(),
                fitnessEntries.get(highestIndices.get(1)).getKey()
        );
    }

    public static String[] crossover(String parent1, String parent2) {
        Random rand = new Random();
        int crossoverPoint = rand.nextInt(parent1.length() - 1) + 1;
        String child1 = parent1.substring(0, crossoverPoint) + parent2.substring(crossoverPoint);
        String child2 = parent2.substring(0, crossoverPoint) + parent1.substring(crossoverPoint);
        return new String[]{child1, child2};
    }

    public static double[] evaluateFitness(String schedule, int employees, int shiftsPerWeek, Map<String, Map<String, Set<String>>> employeePreferences) {
        int[] shiftsPerEmployee = new int[employees];
        for (int i = 0; i < employees; i++) {
            shiftsPerEmployee[i] = 0;
            for (int j = 0; j < shiftsPerWeek; j++) {
                if (schedule.charAt(i * shiftsPerWeek + j) == '1') {
                    shiftsPerEmployee[i]++;
                }
            }
        }

        // Calculate max and min shift counts manually
        int maxShifts = shiftsPerEmployee[0];
        int minShifts = shiftsPerEmployee[0];
        for (int i = 1; i < shiftsPerEmployee.length; i++) {
            if (shiftsPerEmployee[i] > maxShifts) {
                maxShifts = shiftsPerEmployee[i];
            }
            if (shiftsPerEmployee[i] < minShifts) {
                minShifts = shiftsPerEmployee[i];
            }
        }
        int maxShiftDifference = maxShifts - minShifts;
        double fairnessScore = 100.0 / Math.pow(maxShiftDifference + 1, 2);

        int daysCovered = 0;
        for (int day = 0; day < shiftsPerWeek; day++) {
            boolean dayCovered = false;
            for (int emp = 0; emp < employees; emp++) {
                if (schedule.charAt(emp * shiftsPerWeek + day) == '1') {
                    dayCovered = true;
                    break;
                }
            }
            if (dayCovered) daysCovered++;
        }
        double coverageScore = (daysCovered / (double) shiftsPerWeek) * 10;

        double preference = 0;
        double sumPreferredShifts = 0;
        int empIndex = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        for (String email : employeePreferences.keySet()) {
            Set<String> preferredShifts = employeePreferences.get(email).get("preferred");
            for (String day : preferredShifts) {
                try {
                    Date date = dateFormat.parse(day);
                    int dayIndex = date.getDate() - 1;
                    if (schedule.charAt(empIndex * shiftsPerWeek + dayIndex) == '1') {
                        preference++;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                sumPreferredShifts++;
            }
            empIndex++;
        }
        double preferenceScore = (preference / sumPreferredShifts) * 100;

        return new double[]{fairnessScore, coverageScore, preferenceScore};
    }

    public static double fitnessCalculate(double[] score, double fairnessWeight, double coverageWeight, double preferenceWeight) {
        double[] weights = {fairnessWeight, coverageWeight, preferenceWeight};
        Arrays.sort(weights);
        return 1 * weights[0] + 2 * weights[1] + 10 * weights[2];
    }

    public static Map<String, Object> geneticAlgorithm(
            List<EmployeePreference> employeePreferences,
            Set<String> uniqueEmployees,
            double fairness,
            double cover,
            double preference,
            int shiftsPerDay
    ) {
        int employees = uniqueEmployees.size();
        int shiftsPerWeek = 7 * shiftsPerDay;
        int populationSize = 100;
        double mutationRate = 0.05;
        double selectivePressure = 0.8;

        Map<String, Map<String, Set<String>>> employeePreferencesDict = generateEmployeePreferences(employeePreferences);
        List<String> population = generateInitialPopulation(populationSize, employees, shiftsPerWeek);
        int generation = 0;
        String bestSchedule = null;
        double bestFitness = Double.NEGATIVE_INFINITY;
        int countMutations = 0;

        while (generation < 100) {
            Map<Integer, Double> resultFitness = new HashMap<>();
            for (int i = 0; i < population.size(); i++) {
                double[] scores = evaluateFitness(population.get(i), employees, shiftsPerWeek, employeePreferencesDict);
                double fitness = fitnessCalculate(scores, fairness, cover, preference);
                resultFitness.put(i, fitness);
            }

            int bestIndex = -1;
            double bestFitnessValue = Double.NEGATIVE_INFINITY;
            for (Map.Entry<Integer, Double> entry : resultFitness.entrySet()) {
                if (entry.getValue() > bestFitnessValue) {
                    bestFitnessValue = entry.getValue();
                    bestIndex = entry.getKey();
                }
            }
            bestSchedule = population.get(bestIndex);
            bestFitness = bestFitnessValue;

            List<Integer> parents = tournamentSelection(resultFitness, selectivePressure);
            String parent1 = population.get(parents.get(0));
            String parent2 = population.get(parents.get(1));
            String[] children = crossover(parent1, parent2);

            population.set(parents.get(0), children[0]);
            population.set(parents.get(1), children[1]);

            for (int i = 0; i < population.size(); i++) {
                String mutatedSchedule = mutate(population.get(i), mutationRate, employees, shiftsPerWeek);
                if (!mutatedSchedule.equals(population.get(i))) {
                    population.set(i, mutatedSchedule);
                    countMutations++;
                    double[] scores = evaluateFitness(mutatedSchedule, employees, shiftsPerWeek, employeePreferencesDict);
                    double fitness = fitnessCalculate(scores, fairness, cover, preference);
                    resultFitness.put(i, fitness);
                }
            }

            generation++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("population", population);
        result.put("bestSchedule", bestSchedule);
        result.put("generation", generation);
        result.put("bestFitness", bestFitness);
        result.put("countMutations", countMutations);
        return result;
    }
}
